package it.unibz.inf.data_pumper.column_types;

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

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.basic_datatypes.QualifiedName;
import it.unibz.inf.data_pumper.basic_datatypes.Schema;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class Column {
	private final String name;
	private final MySqlDatatypes type;
	private boolean allDifferent;
	private boolean primary;
	private boolean independent;
	private boolean autoincrement;
	protected Schema schema;
	private List<QualifiedName> referencesTo; // this.name subseteq that.name
	private List<QualifiedName> referencedBy; // that.name subseteq this.name
	
	// Length of the datatype
	protected int datatypeLength;
	
	private int index;	
	
	protected static Logger logger = Logger.getLogger(Column.class.getCanonicalName());
	
	// ---------------------- //
	
	public Column(String name, MySqlDatatypes type, int index, Schema schema){
		this.name = name;
		this.type = type;
		this.primary = false;
		this.independent = false;
		this.allDifferent = false;
		this.autoincrement = false;
		referencesTo = new ArrayList<QualifiedName>();
		referencedBy = new ArrayList<QualifiedName>();
		this.index = index;
		this.datatypeLength = 15; // A default value
		
		this.schema = schema;
		
	}
		
	public Schema getSchema(){
		return schema;
	}
	
	public int getIndex(){
		return index;
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public void setPrimary(){
		primary = true;
	}
	
	public boolean isPrimary(){
		return primary;
	}
	
	public void setAutoIncrement(){
		this.autoincrement = true;
	}
	
	public boolean isAutoIncrement(){
		return autoincrement;
	}
	
	public boolean isIndependent(){
		return independent;
	}
	
	public void setIndependent(){
		independent = true;
	}

	public boolean isAllDifferent() {
		return allDifferent;
	}

	public void setAllDifferent() {
		allDifferent = true;
	}

	public MySqlDatatypes getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
	public List<QualifiedName> referencesTo() {
		return referencesTo;
	}
	
	public List<QualifiedName> referencedBy() {
		return referencedBy;
	}	
	
	public String toString(){
		return name;
	}
	
	@Override
	public boolean equals(Object other) {
	    boolean result = false;
	    if( other != null && this.getClass().equals(other.getClass()) ){
	        Column that = (Column) other;
	        if( this.schema.getTableName().equals(that.schema.getTableName()) ){
	            if( this.getName().equals(that.getName()) ){
	                result = true;
	            }
	        }
	    }
	    return result;
	}
	
	@Override
	public int hashCode() {
	    return (this.getSchema().getTableName() + "." + this.getName()).hashCode();
	}
}
