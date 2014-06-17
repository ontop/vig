package basicDatatypes;

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
	private String tableName;
	private String colName;
	
	public QualifiedName(){}
	
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
		
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	
	public String toString(){
		return tableName + "." + colName;
	}
}
