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
import it.unibz.inf.data_pumper.basic_datatypes.Schema;

import java.util.List;

public abstract class OrderedDomainColumn<T extends Comparable<? super T>> extends ColumnPumper {
	
	private static final String NULL = "\\N";

	protected List<T> domain;
	private int domainIndex;
	protected T max;
	protected T min;
	
	public OrderedDomainColumn(String name, MySqlDatatypes type, int index, Schema schema) {
		super(name, type, index, schema);
		domain = null;
		domainIndex = 0;
	}
	
	@Override
	/** This method has to be called whenever information held for the column can be released **/
	public void reset(){
		if( domain != null ) domain.clear();
		domainIndex = 0;
		System.gc();
	}
	
	@Override
	public String getNextValue(){
		String result = domain.get(domainIndex++).toString();
		return result;
	}
		
	public void setMaxValue(T max){
		this.max = max;
	}
	
	public T getMaxValue(){
		return max;
	}
	
	public void setMinValue(T min){
		this.min = min;
	}
	
	public T getMinValue(){
		return min;
	}
	
	public void setDomain(List<T> newDomain){
		if( domain == null ){
			domain = newDomain;
//			if( domain.size() != 0 )
//				Collections.shuffle(domain);
		}
	}
	
	public String getNthInDomain(int n){
		String result = domain.get(n) == null ? NULL : domain.get(n).toString() ;
		if( result == null ){
			return NULL;
		}
		if( this.getType().equals(MySqlDatatypes.BIGINT) ){
			String value1 = result.substring(0, result.indexOf("."));
			result = value1;
		}
		return result;
	}
}

