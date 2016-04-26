package it.unibz.inf.vig_mappings_analyzer.core.utils;

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

public class QualifiedName {
	private final String tableName;
	private final String colName;
		
	public QualifiedName(String tableName, String colName){
		this.tableName = tableName;
		this.colName = colName;
	}
	
	public QualifiedName(String csvName){
		// "\\s+"
		String[] splits = csvName.split(" ");
		tableName = splits[0];
		colName = splits[1];
	}
	
	public static QualifiedName makeFromDotSeparated(String dotSeparated){
	    String[] splits = dotSeparated.split("\\.");
	    
	    assert splits.length == 2 : "Not dotSeparated, it was: "+dotSeparated;
	    
	    return new QualifiedName(splits[0], splits[1]);
	}
	
	public String getTableName() {
		return tableName;
	}

	public String getColName() {
		return colName;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if( obj instanceof QualifiedName ){
			QualifiedName other = (QualifiedName) obj;
			result = (this.tableName.equals(other.tableName)) && (this.colName.equals(other.colName));
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return (this.tableName + "." + this.colName).hashCode();
	}
		
	@Override
	public String toString(){
		return tableName + "." + colName;
	}
}
