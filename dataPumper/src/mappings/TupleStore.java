package mappings;

import java.util.ArrayList;
import java.util.Map;

import utils.MyHashMapList;

/**
 * 
 * @author tir
 * @note This is a <b>singleton</b> class.
 */
public class TupleStore {
	
	private static TupleStore store = null;
	
	private ArrayList<Tuple> tuples;
	private Map<Integer, Tuple> mIdTuple;
	private MyHashMapList<String, Tuple> mTableName_Tuples;
	
	private TupleStore(){};
	
	public TupleStore(MyHashMapList<String, String> tuplesHash) {
		
	}

	public static TupleStore getInstance(){
		if( store == null ){
			store = new TupleStore();
		}
		return store;
	}
	
	public static TupleStore getInstance(MyHashMapList<String, String> tuplesHash){
		if( store == null ){
			store = new TupleStore(tuplesHash);
		}
		return store;
	}
}
