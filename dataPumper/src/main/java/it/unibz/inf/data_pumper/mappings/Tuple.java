package it.unibz.inf.data_pumper.mappings;

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

//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import utils.MyHashMapList;
//
///** 
// * It keeps the information concerning a <b>Function<b> head of 
// * @author tir
// *
// */
//public class Tuple {
//	private final String functName;
//	
//	private final int id;
//	
//	private List<TupleTemplate> tupleTemplates;
//	/**
//	 * Sets up all the sub-structures
//	 * @param id
//	 */
//	Tuple(int id, String functName, MyHashMapList<String, String> mTableName_ColumnsGLOBAL, MyHashMapList<String, String> mTupleTemplate_Tables){ // It can be created only in this package
//		
//		tupleTemplates = new ArrayList<TupleTemplate>();		
//		this.functName = functName;
//		this.id = id;
//		
//		
//		for( String ttString : mTupleTemplate_Tables.keyset() ){
//			MyHashMapList<String, String> mTableName_Columns = new MyHashMapList<String, String>();
//			for( String tableName : mTupleTemplate_Tables.get(ttString) ){
//				mTableName_Columns.putAll(tableName, mTableName_ColumnsGLOBAL.get(tableName));
//			}
//			TupleTemplate tt = new TupleTemplateImpl(ttString, mTableName_Columns, this, tupleTemplates.size());
//			tupleTemplates.add(tt);
//		}
//	}
//	
//	Tuple(int id, String functName, List<TupleTemplate> tupleTemplates){
//		this.id = id;
//		this.functName = functName;
//		this.tupleTemplates = tupleTemplates;
//	}
//	
//	public String getFunctName(){
//		return functName;
//	}
//	
//	public int getId(){
//		return id;
//	}
//	
//	public List<TupleTemplate> getTupleTemplates(){
//		return Collections.unmodifiableList(this.tupleTemplates);
//	}
//	
//	public String toString(){
//		StringBuilder builder = new StringBuilder();
//		
//		builder.append("Tuple id: " + id + "\n");
//		builder.append("TupleTemplates: " + tupleTemplates.toString());
//		
//		return builder.toString();
//	}
//};
//
//
