package mappings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.MyHashMapList;

/** 
 * It keeps the information concerning a <b>Function<b> head of 
 * @author tir
 *
 */
public class Tuple {
	private final String functName;
	
	private final int id;
	private Map<String, Float> mTableName_inDupR;
	private float dupR;
	
	private List<TupleTemplate> tupleTemplates;
	/**
	 * Sets up all the sub-structures
	 * @param id
	 */
	Tuple(int id, String functName, MyHashMapList<String, String> mTableName_ColumnsGLOBAL, MyHashMapList<String, String> mTupleTemplate_Tables){ // It can be created only in this package
		
		tupleTemplates = new ArrayList<TupleTemplate>();		
		this.functName = functName;
		this.id = id;
		
		for( String ttString : mTupleTemplate_Tables.keyset() ){
			MyHashMapList<String, String> mTableName_Columns = new MyHashMapList<String, String>();
			for( String tableName : mTupleTemplate_Tables.get(ttString) ){
				mTableName_Columns.putAll(tableName, mTableName_ColumnsGLOBAL.get(tableName));
			}
			TupleTemplate tt = new TupleTemplate(ttString, mTableName_Columns);
			tupleTemplates.add(tt);
		}
	}
	
	public String getFunctName(){
		return functName;
	}
	
	public int getId(){
		return id;
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
	
	/**
	 * 
	 * @param tableName
	 * @return The duplicate ratio of <b>this</b> tuple relative to
	 * table <b>tableName</b>
	 */
	public float getInDupR(String tableName){
		return mTableName_inDupR.get(tableName);
	}
	
	/**
	 * 
	 * @return The duplicate ratio for the whole relation <b>this</b>
	 */
	public float getDupR() {
		return dupR;
	}

	public void setDupR(float dupR) {
		this.dupR = dupR;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("Tuple id: " + id + "\n");
		builder.append("TupleTemplates: " + tupleTemplates.toString());
		
		return builder.toString();
	}
};

class TupleTemplate{
	private final String templatesString;
	
	private final MyHashMapList<String, String> mTableName_Columns;
	private Map<String, Float> mTableName_inDupR;
	private float dupR;
	
	/**
	 * Sets up all the sub-structures
	 * @param id
	 */
	TupleTemplate(String templatesString, MyHashMapList<String, String> mTableName_Columns){ // It can be created only in this package
		this.templatesString = templatesString;
		this.mTableName_Columns = mTableName_Columns;
	}
	
	public String getTemplatesString(){
		return templatesString;
	}
	
	public Set<String>getReferredTables(){
		return mTableName_Columns.keyset();
	}
	
	public List<String> getColumnsInTable(String tableName){
		return Collections.unmodifiableList(mTableName_Columns.get(tableName));
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
	
	/**
	 * 
	 * @param tableName
	 * @return The duplicate ratio of <b>this</b> tuple template relative to
	 * table <b>tableName</b>
	 */
	public float getInDupR(String tableName){
		return mTableName_inDupR.get(tableName);
	}
	
	/**
	 * 
	 * @return The duplicate ratio for the whole relation w.r.t. <b>this</b> template
	 */
	public float getDupR() {
		return dupR;
	}

	public void setDupR(float dupR) {
		this.dupR = dupR;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("Template: " + templatesString + "\n");
		builder.append("Tables: " + mTableName_Columns.keyset().toString() + "\n");
		
		return builder.toString();
	}
};
