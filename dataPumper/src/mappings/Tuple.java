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
			TupleTemplate tt = new TupleTemplateImpl(ttString, mTableName_Columns, this, tupleTemplates.size());
			tupleTemplates.add(tt);
		}
	}
	
	public String getFunctName(){
		return functName;
	}
	
	public int getId(){
		return id;
	}
	
	public List<TupleTemplate> getTupleTemplates(){
		return Collections.unmodifiableList(this.tupleTemplates);
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("Tuple id: " + id + "\n");
		builder.append("TupleTemplates: " + tupleTemplates.toString());
		
		return builder.toString();
	}
};


