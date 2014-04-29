package mappings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.MyHashMapList;

/** 
 * It keeps the information concerning a <b>Function<b> head of 
 * @author tir
 *
 */
public class Tuple {
	private String functionName;
	
	private MyHashMapList<String, String> mTableName_Columns;
	private final int id;
	private Map<String, Float> mTableName_inDupR;
	private float dupR;
	
	public Tuple(int id){
		this.id = id;
	}

	/**
	 * Associates the duplicate ratio <b>dupRatio</b> to the table <b>tableName<b>
	 * for <b>this</b> tuple
	 * @param tableName
	 * @param dupRatio
	 */
	public void setInDupR(String tableName, float dupRatio){
		mTableName_inDupR.put(tableName, dupRatio);
	}
	
	public float getInDupR(String tableName){
		return mTableName_inDupR.get(tableName);
	}
	
	public float getDupR() {
		return dupR;
	}

	public void setDupR(float dupR) {
		this.dupR = dupR;
	}
	
	
	
	
}
