package it.unibz.inf.data_pumper.columns;

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

import it.unibz.inf.data_pumper.columns.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.columns.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.columns.intervals.Interval;
import it.unibz.inf.data_pumper.columns_cluster.ColumnsCluster;
import it.unibz.inf.data_pumper.columns_cluster.ColumnsClusterImpl;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.tables.MySqlDatatypes;
import it.unibz.inf.data_pumper.tables.Schema;
import it.unibz.inf.vig_mappings_analyzer.core.utils.QualifiedName;

public abstract class ColumnPumper<T> extends Column implements ColumnPumperInterface<T>{

    private boolean fixed;
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

    private long generatedCounter = 0;

    // For general purposes
    public boolean visited;
    private double scaleFactor;

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
	this.fixed = false;
    }

    //	@Override
    /**
     * Get the closure under referredBy and refersTo
     * @return
     */
    public ColumnsCluster<T> getCluster() {
	return new ColumnsClusterImpl<T>(this);
    }

    @Override
    public void setDuplicatesRatio(float ratio) {
	this.duplicateRatio = ratio;
	this.duplicateRatioSet = true;
    }

    @Override
    public float getDuplicateRatio() {
	if( ! duplicateRatioSet ) throw new ValueUnsetException();
	float result = this.duplicateRatio;
	return result;
    }

    @Override
    public float getNullRatio() {
	if( ! nullRatioSet ) throw new ValueUnsetException();
	return this.nullRatio;
    }

    @Override
    public void setNullRatio(float ratio) {
	this.nullRatioSet = true;
	this.nullRatio = ratio;
    }

    @Override
    public void setNumRowsToInsert(int num) {
	this.numRowsToInsertSet = true;
	this.numRowsToInsert = num;
    }

    @Override
    public long getNumRowsToInsert() {
	if( ! numRowsToInsertSet ) throw new ValueUnsetException();		
	return this.numRowsToInsert;
    }

    protected void initNumDupsNullsFreshs() {

	this.numDupsToInsert = (long) (this.getNumRowsToInsert() * this.getDuplicateRatio());
	this.numNullsToInsert = (long) (this.getNumRowsToInsert() * this.getNullRatio());

	if( this.isFixed() ) {
	    this.setDuplicatesRatio(1);
	    this.numFreshsToInsert = this.getNumRowsToInsert() - this.numDupsToInsert - this.numNullsToInsert;
	    this.numFreshsToInsert = (long) (this.numFreshsToInsert / this.scaleFactor);
	    this.numDupsToInsert = this.getNumRowsToInsert() - (this.numFreshsToInsert + this.numNullsToInsert);
	}
	else{
	    this.numFreshsToInsert = this.getNumRowsToInsert() - this.numDupsToInsert - this.numNullsToInsert;			
	}

	//	    checkIfTooManyDups();

	this.numDupsNullRowsSet = true;
	this.generator = new CyclicGroupGenerator(numFreshsToInsert);
    }


    //	private void checkIfTooManyDups(){
    //	    	    
    //	    // If the duplicates ratio has been alterated to 1
    //	    if( this.dupsCorrectionFactorApplied() ){
    //	    
    //		// Check
    //		if( this.numDupsToInsert + this.numNullsToInsert >= this.getNumRowsToInsert() ){
    //		    this.numDupsToInsert = this.getNumRowsToInsert() - this.numNullsToInsert - 1;
    //		    
    //		    assert this.numDupsToInsert > 0 : "Assertion Failed: No duplicate can be subtracted for column " + this.toString(); 
    //		} 
    //	    }
    //	}

    @Override
    public long getNumFreshsToInsert() {
	if( !this.numDupsNullRowsSet ) throw new ValueUnsetException();
	return numFreshsToInsert;
    }

    @Override
    public void incrementNumFreshs() {
	++this.numFreshsToInsert;
	Interval<T> interval = this.getIntervals().get(0);
	interval.setNFreshsToInsert(interval.getNFreshsToInsert() + 1);
	interval.updateMaxEncodingAndValue(interval.getMaxEncoding() + 1);
    }

    @Override
    public void decrementNumFreshs() {
	--this.numFreshsToInsert;
	Interval<T> interval = this.getIntervals().get(0);
	interval.setNFreshsToInsert(interval.getNFreshsToInsert() - 1);
	interval.updateMaxEncodingAndValue(interval.getMaxEncoding() - 1);
    }

    public abstract void createValues(Schema schema, DBMSConnection db);
    public abstract void createNValues(Schema schema, DBMSConnection db, long n);


    @Override
    public void generateValues(Schema schema, DBMSConnection db)
	    throws BoundariesUnsetException, ValueUnsetException{

	createValues(schema, db);

	this.generatedCounter = this.numRowsToInsert;
    }

    @Override
    public boolean generateNValues(Schema schema, DBMSConnection db, long n)
	    throws BoundariesUnsetException, ValueUnsetException{

	long nOld = n;

	n = this.numRowsToInsert - this.generatedCounter > n ? n : this.numRowsToInsert - this.generatedCounter;

	createNValues(schema, db, n);

	this.generatedCounter += n;

	return n != nOld; // true: All the values have been generated
    }

    /**
     * 
     * @return The number of values generated so far for <b>this</b> column.
     */
    protected long getGeneratedCounter(){
	return this.generatedCounter;
    }

    @Deprecated
    public List<ColumnPumper<T>> getRefersToClosure() {

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

    @Deprecated
    public List<ColumnPumper<T>> getReferredByClosure() {

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

    public void setScaleFactor(double percentage) {
	this.scaleFactor = percentage;
    }

    public boolean isFixed(){
	return this.fixed;
    }

    public void setFixed(){
	this.fixed = true;
    }
};