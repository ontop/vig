package mappings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import basicDatatypes.Template;
import connection.DBMSConnection;

/**
 * Singleton class
 * @author tir
 *
 */
public class TuplesPicker {
	
	private static TuplesPicker picker = null;
	List<ResultSet> pickFrom;
	int pickIndex;
	private TupleTemplate lastTT;
	private List<String> tableNames;
	private static final String renamePostfix = "_1";
	
	private TuplesPicker(){
		pickFrom = new ArrayList<ResultSet>();
		pickIndex = 0;
		lastTT = null;
		tableNames = null;
	};
	
	static TuplesPicker getInstance(){
		if( picker == null ){
			picker = new TuplesPicker();
		}
		return picker;
	}
	
	public List<String> pickTuple(DBMSConnection dbToPump, String curTable, TupleTemplate tt){
		
		if( this.needsInit(tt) ){
			this.init(tt);
		}
		
		setValidPickIndex(dbToPump, curTable, tt);
		
		// Now, according to the tuple cardinality, pick results from the resultSet
		ResultSet rs = pickFrom.get(pickIndex);
		assert rs != null;
		
		List<String> tuple = new ArrayList<String>();
		
		try {
			if( rs.next() ){
				for( int i = 1; i <= tt.getColumnsInTable(curTable).size(); ++i ){
					tuple.add(rs.getString(i));
				}
			}
			else{
				assert false; // The setValidPickIndex HAS TO ensure the existence of rs.next()
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// TODO Also, I needed to make the union of the domains, but I forgot
		//      Because, otherwise, this tuple might be duplicate
		return tuple;
	}
	
	private void init(TupleTemplate tt) {
		lastTT = tt;
		tableNames = new ArrayList<String>(tt.getReferredTables());
		pickFrom = new ArrayList<ResultSet>();
		for( int i = 0; i < tableNames.size(); ++ i ) pickFrom.add(null);
	}
	
	/**
	 * To be called each time I end filling a schema
	 */
	public void reset(){
		this.pickFrom.clear();
		pickIndex = 0;
		lastTT = null;
		tableNames = null;
	}

	private boolean needsInit(TupleTemplate tt) {
		return !tt.equals(lastTT);
	}
	
	/**
	 * It finds a suitable <b>pickIndex</b> to pick values
	 * from the <b>pickFrom</b> resultSet vector
	 * @param dbToPump
	 * @param curTable
	 * @param tt
	 */
	private void setValidPickIndex(DBMSConnection dbToPump, String curTable, TupleTemplate tt) {
		
		try{
			if( tableNames.get(pickIndex).equals(curTable) ) {
				pickIndex = pickIndex + 1 % tableNames.size();
			}	
			if( pickFrom.get(pickIndex) == null ){ 
				assert !tableNames.get(pickIndex).equals(curTable);
				createNewResultSet(dbToPump, curTable, tt);
			}
			int avoidInfLoop = tableNames.size();
			do{
				pickIndex = pickIndex + 1 % tableNames.size();
				if( ++avoidInfLoop > tableNames.size() ) break;
			}
			while( pickFrom.get(pickIndex).isAfterLast() );
			
			assert avoidInfLoop <= tableNames.size(); // No more available dups to insert ??!
		
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	private void createNewResultSet(DBMSConnection dbToPump, String curTable, TupleTemplate tt) {
		
		String referredTable = tableNames.get(pickIndex+1);
		
		PreparedStatement stmt = 
				dbToPump.getPreparedStatement(createTakeTuplesQueryString(curTable, referredTable, tt));
		
		ResultSet rs = null;
		
		try {
			rs = stmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pickFrom.set(pickIndex, rs);
	}
	/**
	 *   <i>table_a SetMinus table_b</i><br/>
		 SELECT a.x, a.y
		 <br />FROM table_a a LEFT JOIN table_b b
		 ON a.x = b.x AND a.y = b.y
		 WHERE b.x IS NULL;
		 <br /><br />
		 <i>referredTable SetMinus curTable</i><br/>
		 SELECT refRn.x, refRn.y
		 <br />FROM referredTable refRn LEFT JOIN curTable curRn
		 ON refRn.x = curRn.x AND refRn.y = curRn.y
		 WHERE curRn.x IS NULL;
	 * @param curTable
	 * @param referredTable
	 * @param tt
	 */
	private String createTakeTuplesQueryString(String curTable,
			String referredTable, TupleTemplate tt) {
		
		Template templ = new Template("SELECT ? FROM ? ? LEFT JOIN ? ? ON ? WHERE ? IS NULL");
		
		templ.setNthPlaceholder(1, projectionList(referredTable, tt));
		templ.setNthPlaceholder(2, referredTable);
		templ.setNthPlaceholder(3, rename(referredTable));
		templ.setNthPlaceholder(4, curTable);
		templ.setNthPlaceholder(5, rename(curTable));
		templ.setNthPlaceholder(6, onCondition(curTable, referredTable, tt));
		templ.setNthPlaceholder(7, lastInOnCondition(curTable, tt));
		
		return templ.getFilled();
	}

	private String rename(String tableName){
		return tableName + renamePostfix;
	}
	
	/**
	 * WHERE curRn.x IS NULL;
	 * @param curTable
	 * @param tt
	 * @return
	 */
	private String lastInOnCondition(String curTable, TupleTemplate tt) {
		
		String lastCol = tt.getColumnsInTable(curTable).get(tt.getColumnsInTable(curTable).size()-1);
		
		return rename(curTable) + "." + lastCol;
	}

	/**
	 * ON refRn.x = curRn.x AND refRn.y = curRn.y
	 * @param curTable
	 * @param referredTable
	 * @param tt
	 * @return
	 */
	private String onCondition(String curTable, String referredTable,
			TupleTemplate tt) {
		
		List<String> colsCur = tt.getColumnsInTable(curTable);
		List<String> colsRef = tt.getColumnsInTable(referredTable);
		
		assert colsCur.size() == colsRef.size();
		
		StringBuilder builder = new StringBuilder();
		
		for( int i = 0; i < colsCur.size(); ++i ){
			if( !(i == 0) ){
				builder.append(" AND ");
			}
			builder.append(colsRef.get(i) + "." + rename(referredTable));
			builder.append("=");
			builder.append(colsCur.get(i) + "." + rename(curTable));
		}
		
		return builder.toString();
	}

	/**
	 * SELECT refRn.x, curRn.y
	 * @param curTable
	 * @param referredTable
	 * @param tt
	 * @return
	 */
	private String projectionList(String referredTable,
			TupleTemplate tt) {
		
		List<String> cols = tt.getColumnsInTable(referredTable);
		
		StringBuilder builder = new StringBuilder();
		
		for( String col : cols ){
			if( !(builder.length() == 0) ){
				builder.append(", ");
			}
			builder.append(referredTable + "." + col);
		}
		
		return builder.toString();
	}
	
};