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
			
			// Extract the info regarding the tables and the columns
			for( String csvProj : tuplesHash.get(functName) ){
				List<String> temp = CSVPlayer.parseRow(csvProj, " ");
				String tableName = temp.get(0);
				List<String> columns = temp.subList(1, temp.size());
				mTable_Columns.putAll(tableName, columns);
			}
			Tuple newT = new Tuple(++tupleCnt, functName, mTable_Columns);
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
}
