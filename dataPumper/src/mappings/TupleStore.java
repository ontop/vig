package mappings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.csvPlayer.core.CSVPlayer;
import utils.MyHashMapList;

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
	
	public String toString(){
		return allTuples().toString();
	}
}
