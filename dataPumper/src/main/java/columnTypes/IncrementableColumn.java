package columnTypes;

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

import java.util.Collections;
import java.util.List;
import basicDatatypes.MySqlDatatypes;

public abstract class IncrementableColumn<T extends Comparable<? super T>> extends ColumnPumper {

	protected List<T> domain;
	protected int domainIndex;
	protected T lastFreshInserted;
	protected T max;
	protected T min;
	
	public IncrementableColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		domain = null;
		domainIndex = 0;
	}
	
	
	public abstract T increment(T toIncrement);
	public abstract T getCurrentMax();
	
	@Override
	/** This method has to be called whenever information held for the column can be released **/
	public void reset(){
		super.reset();
		if( domain != null ) domain.clear();
		domainIndex = 0;
	}
	
	@Override
	public String getNextFreshValue(){
		
		T toInsert = this.getLastFreshInserted();
		
		if( toInsert == null ) logger.error(this.toString() +" toInsert is NULL");
		
		do{
			toInsert = increment(toInsert);
			
			while( toInsert.compareTo(this.getCurrentMax()) == 1 && this.hasNextMax() )
				this.nextMax();
		}
		while(toInsert.compareTo(this.getCurrentMax()) == 0);
		
		this.setLastFreshInserted(toInsert);
		
		return toInsert.toString();
	}

	public void setLastFreshInserted(T toInsert){
		lastFreshInserted = toInsert;
	}

	public T getLastFreshInserted(){
		return lastFreshInserted;
	}
	
	
	public boolean hasNextMax(){
		return domainIndex < domain.size();
	}
	
	public void nextMax(){
		++domainIndex;
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
			if( domain.size() != 0 )
				Collections.sort(domain);
		}
	}
}

