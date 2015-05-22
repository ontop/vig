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

import java.util.LinkedList;
import java.util.List;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.basic_datatypes.QualifiedName;
import it.unibz.inf.data_pumper.basic_datatypes.Schema;
import it.unibz.inf.data_pumper.column_types.aggregate_types.ColumnsCluster;
import it.unibz.inf.data_pumper.column_types.aggregate_types.ColumnsClusterImpl;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.connection.exceptions.InstanceNullException;
import it.unibz.inf.data_pumper.core.main.DebugException;
import it.unibz.inf.data_pumper.core.table.statistics.exception.TooManyValuesException;

public abstract class ColumnPumper<T> extends Column implements ColumnPumperInterface<T>{
		
	private float duplicateRatio;
	private float nullRatio;
	private long numRowsToInsert;
	protected long numFreshsToInsert;
	protected long numDupsToInsert;
	protected long numNullsToInsert;
	
	private boolean duplicateRatioSet;
	private boolean nullRatioSet;
	private boolean numRowsToInsertSet;
	
	protected boolean numDupsNullRowsSet;
	
	protected CyclicGroupGenerator generator;
	
	protected ColumnsCluster<T> cluster;
	
	// For general purposes
	public boolean visited;
	
	public ColumnPumper(String name, MySqlDatatypes type, int index, Schema schema){ // index: index of the column
	    super(name, type, index, schema);
	    this.duplicateRatio = 0;
		this.nullRatio = 0;
		this.numRowsToInsert = 0;
		this.numFreshsToInsert = 0;
		this.numDupsToInsert = 0;

		duplicateRatioSet = false;
		nullRatioSet = false;
		numRowsToInsertSet = false;
		this.numDupsNullRowsSet = false;
		visited = false;
	}

//	@Override
	/**
	 * Get the closure under referredBy and refersTo
	 * @return
	 * @throws InstanceNullException 
	 * @throws BoundariesUnsetException 
	 * @throws DebugException 
	 * @throws ValueUnsetException 
	 */
	public ColumnsCluster<T> getCluster() throws InstanceNullException, ValueUnsetException, DebugException, BoundariesUnsetException{
	    return new ColumnsClusterImpl<T>(this);
	}
	
	@Override
	public void setDuplicatesRatio(float ratio) {
		this.duplicateRatio = ratio;
		this.duplicateRatioSet = true;
	}

	@Override
	public float getDuplicateRatio() throws ValueUnsetException {
		if( ! duplicateRatioSet ) throw new ValueUnsetException();
		return this.duplicateRatio;
	}

	@Override
	public float getNullRatio() throws ValueUnsetException {
		if( ! nullRatioSet ) throw new ValueUnsetException();
		return this.nullRatio;
	}

	@Override
	public void setNullRatio(float ratio) {
		this.nullRatioSet = true;
		this.nullRatio = ratio;
	}
	
	@Override
	public void setNumRowsToInsert(int num) throws TooManyValuesException{
		this.numRowsToInsertSet = true;
		this.numRowsToInsert = num;
	}
	
	@Override
	public long getNumRowsToInsert() throws ValueUnsetException{
		if( ! numRowsToInsertSet ) throw new ValueUnsetException();		
		return this.numRowsToInsert;
	}
	
	protected void initNumDupsNullsFreshs() throws ValueUnsetException{

	    if(this.numDupsNullRowsSet == true) return; // Values set already

	    this.numDupsToInsert = (long) (this.getNumRowsToInsert() * this.getDuplicateRatio());
	    this.numNullsToInsert = (long) (this.getNumRowsToInsert() * this.getNullRatio());
	    this.numFreshsToInsert = this.getNumRowsToInsert() - this.numDupsToInsert - this.numNullsToInsert;			
	    this.numDupsNullRowsSet = true;
	    this.generator = new CyclicGroupGenerator(numFreshsToInsert);
	}

	
	@Override
	public long getNumFreshsToInsert() throws ValueUnsetException{
		if( !this.numDupsNullRowsSet ) throw new ValueUnsetException();
		return numFreshsToInsert;
	}
	
	@Override
	public void incrementNumFreshs() throws DebugException, BoundariesUnsetException {
		++this.numFreshsToInsert;
		Interval<T> interval = this.getIntervals().get(0);
		interval.setNFreshsToInsert(interval.getNFreshsToInsert() + 1);
		interval.updateMaxEncodingAndValue(interval.getMaxEncoding() + 1);
	}
	
	@Override
	public void decrementNumFreshs() throws DebugException, BoundariesUnsetException {
		--this.numFreshsToInsert;
		Interval<T> interval = this.getIntervals().get(0);
		interval.setNFreshsToInsert(interval.getNFreshsToInsert() - 1);
		interval.updateMaxEncodingAndValue(interval.getMaxEncoding() - 1);
	}

	public List<ColumnPumper<T>> getRefersToClosure() throws InstanceNullException {
	    
	    List<ColumnPumper<T>> result = new LinkedList<>();
	    for( QualifiedName qN : this.referencesTo() ){
		@SuppressWarnings("unchecked")
		ColumnPumper<T> cP = (ColumnPumper<T>)DBMSConnection.getInstance().getSchema(qN.getTableName()).getColumn(qN.getColName());
		
		if( !result.contains(cP) ) result.add(cP);
		
		for( ColumnPumper<T> inClosure : cP.getRefersToClosure() ){
		    if( !result.contains(inClosure) ){
			result.add(inClosure);
		    }
		}
	    }
	    return result;
	}
	
	public List<ColumnPumper<T>> getReferredByClosure() throws InstanceNullException {
	    
	    List<ColumnPumper<T>> result = new LinkedList<>();
	    for( QualifiedName qN : this.referencedBy() ){
		@SuppressWarnings("unchecked")
		ColumnPumper<T> cP = (ColumnPumper<T>)DBMSConnection.getInstance().getSchema(qN.getTableName()).getColumn(qN.getColName());
		
		if( !result.contains(cP) ) result.add(cP);
		
		for( ColumnPumper<T> inClosure : cP.getReferredByClosure() ){
		    if( !result.contains(inClosure) ){
			result.add(inClosure);
		    }
		}
	    }
	    return result;
	}
};