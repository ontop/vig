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
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.table.statistics.exception.TooManyValuesException;

public interface ColumnPumperInterface {
	
	public String getNextValue();
	
	/** 
	 * Pick values from intervals and put them into the 
	 * domain vector (held in main memory).
	 * 
	 * @param schema
	 * @param db
	 * @throws BoundariesUnsetException
	 * @throws ValueUnsetException
	 *
	 */
	public void generateValues(Schema schema, DBMSConnection db) throws BoundariesUnsetException, ValueUnsetException;
	public void fillFirstIntervalBoundaries(Schema schema, DBMSConnection db) throws ValueUnsetException, SQLException;
	
	public void setDuplicatesRatio(float ratio);
	public float getDuplicateRatio() throws ValueUnsetException;
	public float getNullRatio() throws ValueUnsetException;
	public void setNullRatio(float ratio);
	
	/**
	 * In case of a foreign key dependency, having a min value 
	 * equals to the min value of the referred column guarantees
	 * not to break the foreign key dependency
	 * @param <T>
	 * 
	 * @param newMin
	 */
//	public void updateMinValueByEncoding(long newMin);
//	public void updateMaxValueByEncoding(long newMax);
//	public long getMaxEncoding() throws BoundariesUnsetException;
//	public long getMinEncoding() throws BoundariesUnsetException;
	
	// TODO Do I really want to guarantee free access to the intervals?
	// Pro: modular
	// Contro: I might corrupt the intervals 
	public <T> List<Interval<T>> getIntervals();
		
	public void setNumRowsToInsert(int num) throws TooManyValuesException;
	public long getNumRowsToInsert() throws ValueUnsetException;
	
	long getNumFreshsToInsert() throws ValueUnsetException;
	
	
	public String getNthInDomain(int i);
	
	public void incrementNumFreshs();
	public void decrementNumFreshs();
	
	public void reset(); // To reset the internal state
//	int getNumFreshsToInsert() throws ValueUnsetException;
//	void setNumFreshsToInsert(int numFreshsToInsert);
//	int getNumDupsToInsert() throws ValueUnsetException;
//	void setNumDupsToInsert(int numDupsToInsert);


}
