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
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class MultiIntervalColumn<T> extends ColumnPumper<T> {
	
	private static final String NULL = "\\N";

	protected List<T> domain;
	private int domainIndex;
	
	protected List<Interval<T>> intervals;
	protected boolean firstIntervalSet;
	
	public MultiIntervalColumn(String name, MySqlDatatypes type, int index, Schema schema) {
		super(name, type, index, schema);
		this.domain = null;
		this.domainIndex = 0;
		this.firstIntervalSet = false;
	}
	
	@Override
	public void addInterval(Interval<T> addInterval){
	    this.intervals.add(addInterval);
	}
	
    @Override
    public List<Interval<T>> getIntervals() {
        return Collections.unmodifiableList(this.intervals);
    }
	
    @Override
    public void removeIntervalOfKey(String key){
        for( Iterator<Interval<T>> it = intervals.iterator(); it.hasNext(); ){
            Interval<T> curInt = it.next();
            if( curInt.getKey().equals(key) ){
                it.remove();
                break;
            }
        }
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
		
    /**
     * 
     * @param intervalIndex
     * @return distribute duplicates proportionally to the size of each interval
     */
    protected long numDupsForInterval( int intervalIndex ) {
        
        long totFreshsToInsert = this.numFreshsToInsert;
        long totDupsToInsert = this.numDupsToInsert;
        
        long intervalFreshs = this.intervals.get(intervalIndex).nFreshsToInsert;
        
        float ratioFreshsInInterval = intervalFreshs / totFreshsToInsert;
        
        long numDupsToInsertInInterval = (long) (totDupsToInsert * ratioFreshsInInterval);
        
        return numDupsToInsertInInterval;
    }
	
//	public void setMaxValue(T max){
//		this.max = max;
//	}
//	
//	public T getMaxValue(){
//		return max;
//	}
//	
//	public void setMinValue(T min){
//		this.min = min;
//	}
//	
//	public T getMinValue(){
//		return min;
//	}
	
	public String getQualifiedName(){
	    String result = this.getSchema().getTableName() + "." + this.getName();
	    return result;
	}
	
	public void setDomain(List<T> newDomain){
		if( domain == null ){
			domain = newDomain;
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
	
	@Override
    public long countFreshsInIntervals() throws ValueUnsetException {
        if( !this.numDupsNullRowsSet ) throw new ValueUnsetException();
        
        long result = 0;
        
        for( Interval<?> i : this.getIntervals() ){
            result += i.nFreshsToInsert;
        }
        
        return result;
    }
}

