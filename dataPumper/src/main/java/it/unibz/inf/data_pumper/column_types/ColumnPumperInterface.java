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

import java.sql.SQLException;
import java.util.List;

import it.unibz.inf.data_pumper.basic_datatypes.Schema;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;

public interface ColumnPumperInterface<T> {
	
	public String getNextValue();
	
	/** 
	 * Pick values from intervals and put them into the 
	 * domain vector (held in main memory).
	 * 
	 * @param schema
	 * @param db
	 *
	 */
	public void generateValues(Schema schema, DBMSConnection db);
	public void fillFirstIntervalBoundaries(Schema schema, DBMSConnection db) throws SQLException;
	
	public void setDuplicatesRatio(float ratio);
	public float getDuplicateRatio();
	public float getNullRatio();
	public void setNullRatio(float ratio);
	
	/**
	 * In case of a foreign key dependency, having a min value 
	 * equals to the min value of the referred column guarantees
	 * not to break the foreign key dependency
	 * @param <T>
	 * 
	 * @param newMin
	 */
	
	/**
	 * 
	 * @return A view of the intervals
	 */
	public List<Interval<T>> getIntervals();
	public void addInterval(Interval<T> addInterval); 
	
	public void addInterval(String name, long minEncoding, long maxEncoding);
	
	/**
     * Remove the interval with the provided <b>key</b>
     * @param key
     */
    public abstract void removeIntervalOfKey(String key);
		
	public void setNumRowsToInsert(int num);
	public long getNumRowsToInsert();
	
	// N Freshs
	public long getNumFreshsToInsert();
	/**
	 * // The first interval is NOT intersected with any other column
	 * <br>
        for( int i = 1; i < this.getIntervals().size(); ++i ){ 
            result += this.getIntervals().get(i).nFreshsToInsert;
        }        
     */
	public long countFreshsInIntersectedIntervals();
	
	public String getNthInDomain(int i);
	
	public void incrementNumFreshs();
	public void decrementNumFreshs();
	
	public void reset(); // To reset the internal state
};
