package mappings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import basicDatatypes.Template;
import connection.DBMSConnection;
import main.java.csvPlayer.core.CSVPlayer;
import utils.MyHashMapList;
import utils.Pair;

/**
 * 
 * @author tir
 * @note This is a <b>singleton</b> class.
 */
public class TupleStore {
	
	private static TupleStore store = null;
	private static int tupleCnt = 0;
	
	private ArrayList<Tuple> tuples;
	private Map<Integer, Tuple> mId_Tuple; // id -> tuple
	private MyHashMapList<String, Tuple> mTableName_Tuples; // tableName -> tuple_1, tuple_2, ..., tuple_n
		
	private TupleStore(MyHashMapList<String, String> tuplesHash) {
		tuples = new ArrayList<Tuple>();
		mId_Tuple = new HashMap<Integer, Tuple>();
		mTableName_Tuples = new MyHashMapList<String, Tuple>();
		
		for( String functName : tuplesHash.keyset() ){
			MyHashMapList<String, String> mTable_Columns = new MyHashMapList<String, String>();
			MyHashMapList<String, String> mTupleTemplate_Tables = new MyHashMapList<String, String>();
			
			// Extract the info regarding the tables and the columns
			for( String csvProj : tuplesHash.get(functName) ){
				List<String> temp = CSVPlayer.parseRow(csvProj, " ");
				String tableName = temp.get(0);
				String tupleTemplate = temp.get(1) + temp.get(2);
				List<String> columns = temp.subList(3, temp.size());
				mTable_Columns.putAll(tableName, columns);
				mTupleTemplate_Tables.put(tupleTemplate, tableName);
			}
			Tuple newT = new Tuple(++tupleCnt, functName, mTable_Columns, mTupleTemplate_Tables);
			tuples.add(newT);
			mId_Tuple.put(tupleCnt, newT);
			for( String tableName : mTable_Columns.keyset() ){
				mTableName_Tuples.put(tableName, newT);
			}
		}
	}
	
	public List<Tuple> getAllTuplesOfTable(String tableName){ 
		if(mTableName_Tuples.containsKey(tableName))
			return Collections.unmodifiableList(mTableName_Tuples.get(tableName));
		else return null;
	}
	
	static TupleStore getInstance(){
		return store;
	}
	
	static TupleStore getInstance(MyHashMapList<String, String> tuplesHash){
		if( store == null ){
			store = new TupleStore(tuplesHash);
		}
		return store;
	}
	
	public List<Tuple> allTuples(){
		return Collections.unmodifiableList(tuples);
	}
	
	/**
	 * The duplicate ratios for each template are evaluated w.r.t. the UNION
	 * of the tuples of the tables covered by that template
	 * @param tt
	 * @return
	 * 
	 * TODO
	 */
	public TupleTemplateDecorator decorateTupleTemplate(TupleTemplate tt){
		// (select wlbName, wlbCoreNumber from wellbore_core) union all (select wlbName, wlbCoreNumber from wellbore_core);
		// (select wlbName, wlbCoreNumber from wellbore_core) union (select wlbName, wlbCoreNumber from wellbore_core);
		
		
		TupleTemplateDecorator ttD = new TupleTemplateDecorator(tt);
		DuplicateRatiosFinder dF = new DuplicateRatiosFinder();
		
		dF.fillDupRatio(ttD);
		
		return ttD;
	}
	
	public String toString(){
		return allTuples().toString();
	}
}

class DuplicateRatiosFinder{
	
	private float findDuplicateRatio(TupleTemplateDecorator ttD){
		
		DBMSConnection dbOriginal = TupleStoreFactory.getInstance().getDBMSConnection();
		
		// Find duplicates ratios
		int nTables = ttD.getReferredTables().size();
		
		Pair<String, String> templDiff_templUnion = getTemplateStrings(nTables); 
		
		Template tDiff = new Template(templDiff_templUnion.first);
		Template tUnion = new Template(templDiff_templUnion.second);
		
		fillTemplate(tDiff, ttD);
		fillTemplate(tUnion, ttD);
		
		PreparedStatement stmtDiff = dbOriginal.getPreparedStatement(tDiff);
		int numDiff = countNumResults(stmtDiff);
		
		PreparedStatement stmtUnion = dbOriginal.getPreparedStatement(tUnion);
		int numUnion = countNumResults(stmtUnion);
		
		float dupRatio = (float)(numDiff - numUnion) / (float)numDiff;
		
		closeStatements(stmtDiff, stmtUnion);
	
		return dupRatio; 
	}
	
	private void closeStatements(PreparedStatement stmtDiff,
			PreparedStatement stmtUnion) {
		try {
			stmtDiff.close();
			stmtUnion.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private int countNumResults(PreparedStatement stmt) {
		
		int result = 0;
		
		try {
			ResultSet rs = stmt.executeQuery();
			result = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void fillTemplate(Template template, TupleTemplate tt) {
		
		
		Set<String> referredTables = tt.getReferredTables();
		
		int i = 1;
		for( String tableName : referredTables ){
			StringBuilder builder = new StringBuilder();	
			builder.append("SELECT ");
			builder.append(projList(tableName, tt));
			builder.append(" FROM " + tableName);
			
			template.setNthPlaceholder(i, builder.toString());
			++i;
		}
	}

	private Object projList(String tableName, TupleTemplate tt) {
		
		StringBuilder builder = new StringBuilder();
		List<String> colNames = tt.getColumnsInTable(tableName);
		
		for( int i = 0; i < colNames.size(); ++i ){
			String colName = colNames.get(i);
			builder.append(colName);
			if( i < colNames.size() - 1 ) builder.append(", ");
		}
		return builder.toString();
	}

	private Pair<String, String> getTemplateStrings(int nTables) {
		StringBuilder builderDiff = new StringBuilder();
		StringBuilder builderSet = new StringBuilder();
		builderDiff.append("SELECT COUNT(*) FROM (");
		builderSet.append("SELECT COUNT(*) FROM (");
		for( int i = 0; i < nTables; ++i ){
			builderDiff.append("(?)");
			builderSet.append("(?)");
			if( i < nTables - 1 ){
				builderDiff.append(" union all ");
				builderSet.append(" union ");
			}
		}
		builderDiff.append(")");
		builderSet.append(")");
		return new Pair<String, String>(builderDiff.toString(), builderSet.toString());
	}
	
	void fillDupRatio(TupleTemplateDecorator ttD) {
		
	}
};