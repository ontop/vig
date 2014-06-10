package mappings;

/*
 * #%L
 * dataPumper
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import utils.MyHashMapList;
import utils.Pair;
import basicDatatypes.Template;
import connection.DBMSConnection;
import core.CSVPlayer;

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
	private Map<Pair<Integer, Integer>, Float> mBufferized_DupRatios;
	private Map<Pair<Integer, Integer>, Integer> mBufferized_nToInsert;
	
	private static Logger logger = Logger.getLogger(TupleStore.class.getCanonicalName());
	
	private TupleStore(MyHashMapList<String, String> tuplesHash) {
		tuples = new ArrayList<Tuple>();
		mId_Tuple = new HashMap<Integer, Tuple>();
		mTableName_Tuples = new MyHashMapList<String, Tuple>();
		mBufferized_DupRatios = new HashMap<Pair<Integer,Integer>, Float>();
		mBufferized_nToInsert = new HashMap<Pair<Integer,Integer>, Integer>();
		
		for( String functName : tuplesHash.keyset() ){
			MyHashMapList<String, String> mTable_Columns = new MyHashMapList<String, String>();
			MyHashMapList<String, String> mTupleTemplate_Tables = new MyHashMapList<String, String>();
			
			// Extract the info regarding the tables and the columns
			for( String csvProj : tuplesHash.get(functName) ){
				List<String> temp = CSVPlayer.parseRow(csvProj, " ");
				String tableName = temp.get(0);
								
				String tupleTemplate = temp.get(1) + temp.get(2);
				
				List<String> columns = temp.subList(3, temp.size());
				
				List<Integer> toRemove = new ArrayList<Integer>();
								
				// Duplicates check
				for( int i = 0; i < columns.size(); ++i ){
					for( int j = i+1; j < columns.size(); ++j ){
						if( columns.get(i).equals(columns.get(j)) ){
							toRemove.add(j);
						}
					}
				}
				
				for( Integer index : toRemove ){
					columns.remove(index);
				}
				
				if(!mTable_Columns.containsKey(tableName)){
					mTable_Columns.putAll(tableName, columns); // TODO At the moment I do not recognize different columns giving 
					                                           //      birth to the same template
				}
				if(mTupleTemplate_Tables.containsKey(tupleTemplate)){
					if( !mTupleTemplate_Tables.get(tupleTemplate).equals(tableName) )
						mTupleTemplate_Tables.put(tupleTemplate, tableName);
				}
				else{
					mTupleTemplate_Tables.put(tupleTemplate, tableName);
				}
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

	public Tuple getTupleOfID(int id){
		return mId_Tuple.get(id);
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
		
		TupleTemplateDecorator ttD = new TupleTemplateDecorator(tt);
		
		if( isDupRatioBufferized(ttD) ){
			ttD.setDupR(getBufferizedDupRatio(ttD));
			ttD.addToInsert(getBufferizedNToInsert(ttD));
		}
		else{
			DuplicateRatiosFinder dF = new DuplicateRatiosFinder();
			dF.fillDupRatio(ttD);
			bufferizeDupRatio(ttD);
		}
		
		return ttD;
	}
	
	private void bufferizeDupRatio(TupleTemplateDecorator ttD) {
		int tupleID = ttD.belongsToTuple();
		int ttID = ttD.getID();
		
		Pair<Integer, Integer> key = new Pair<Integer, Integer>(tupleID, ttID);
		
		mBufferized_DupRatios.put(key, ttD.getDupR());
	}
	
	public void bufferizeNToInsert(TupleTemplateDecorator ttD, int nToInsert){
		int tupleID = ttD.belongsToTuple();
		int ttID = ttD.getID();
		
		Pair<Integer,Integer> key = new Pair<Integer, Integer>(tupleID, ttID);
		
		mBufferized_nToInsert.put(key, nToInsert);
	}
	
	private int getBufferizedNToInsert(TupleTemplateDecorator ttD){
		int tupleID = ttD.belongsToTuple();
		int ttID = ttD.getID();
		
		Pair<Integer,Integer> key = new Pair<Integer, Integer>(tupleID, ttID);
		
		return mBufferized_nToInsert.get(key);
	}

	private float getBufferizedDupRatio(TupleTemplateDecorator ttD){
		int tupleID = ttD.belongsToTuple();
		int ttID = ttD.getID();
		
		Pair<Integer,Integer> key = new Pair<Integer, Integer>(tupleID, ttID);
		
		return mBufferized_DupRatios.get(key);
	}
	
	private boolean isDupRatioBufferized(TupleTemplateDecorator ttD){
		
		int tupleID = ttD.belongsToTuple();
		int ttID = ttD.getID();
		
		Pair<Integer,Integer> key = new Pair<Integer, Integer>(tupleID, ttID);
		
		if( mBufferized_DupRatios.containsKey(key) ){
			return true;
		}
		return false;
	}
	
	
	public String toString(){
		return allTuples().toString();
	}
}

class DuplicateRatiosFinder{
	
	private static Logger logger = Logger.getLogger(DuplicateRatiosFinder.class.getCanonicalName());
	
	private float findDuplicateRatio(TupleTemplateDecorator ttD){
		// (select wlbName, wlbCoreNumber from wellbore_core) union all (select wlbName, wlbCoreNumber from wellbore_core);
		// (select wlbName, wlbCoreNumber from wellbore_core) union (select wlbName, wlbCoreNumber from wellbore_core);
		
		if( ttD.getReferredTables().size() < 2 ) return 0; // TODO Anyways, I have a bug giving duplicate columns in the projection
		                                                    //      evaluated in fillTemplate(), for table licence_task (funct npdv:name).
		                                                    //      DEBUG
		
		DBMSConnection dbOriginal = TupleStoreFactory.getInstance().getDBMSConnection();
		
		// Find duplicates ratios
		int nTables = ttD.getReferredTables().size();
		
		Pair<String, String> templDiff_templUnion = getTemplateStrings(nTables); 
		
		Template tDiff = new Template(templDiff_templUnion.first);
		Template tUnion = new Template(templDiff_templUnion.second);
		
		fillTemplate(tDiff, ttD);
		fillTemplate(tUnion, ttD);
		
		logger.debug("tDiff: "+tDiff.getFilled());
		logger.debug("tUnion: "+tUnion.getFilled());
		
		PreparedStatement stmtDiff = dbOriginal.getPreparedStatement(tDiff);
		int numDiff = countNumResults(stmtDiff);
		
		PreparedStatement stmtUnion = dbOriginal.getPreparedStatement(tUnion);
		int numUnion = countNumResults(stmtUnion);
		
		float dupRatio = (float)(numDiff - numUnion) / (float)numDiff;
		
		closeStatements(stmtDiff, stmtUnion);
		
		logger.debug("THE DUP RATIO IS" + dupRatio);
		
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
			if( rs.next() ){
				result = rs.getInt(1);
			}
			else throw new SQLException("The COUNT did not went smoothly");
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

	private String projList(String tableName, TupleTemplate tt) {
		
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
		builderDiff.append(") alias");
		builderSet.append(") alias");
				
		return new Pair<String, String>(builderDiff.toString(), builderSet.toString());
	}
	
	void fillDupRatio(TupleTemplateDecorator ttD) {
		
		float dupRatio = findDuplicateRatio(ttD);
		ttD.setDupR(dupRatio);
	}
};