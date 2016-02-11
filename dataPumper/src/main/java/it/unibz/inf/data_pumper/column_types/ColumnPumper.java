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
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.table.statistics.exception.TooManyValuesException;

public abstract class ColumnPumper extends Column implements ColumnPumperInterface{
		
	private float duplicateRatio;
	private float nullRatio;
	private int numRowsToInsert;
	protected int numFreshsToInsert;
	protected int numDupsToInsert;
	protected int numNullsToInsert;
	
	private boolean duplicateRatioSet;
	private boolean nullRatioSet;
	private boolean numRowsToInsertSet;
	
	private boolean numDupsNullRowsSet;
	
	protected CyclicGroupGenerator generator;
	
	private long generatedCounter = 0;
	
	private boolean boundariesSet = false;
	
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
//		boolean numFreshsToInsertSet = false;
//		boolean numDupsToInsertSet = false;

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
	public int getNumRowsToInsert() throws ValueUnsetException{
		if( ! numRowsToInsertSet ) throw new ValueUnsetException();		
		return this.numRowsToInsert;
	}
	
	protected void initNumDupsNullsFreshs(){
		
		if(this.numDupsNullRowsSet == true) return; // Values set already
		
		try{
			this.numDupsToInsert = (int) (this.getNumRowsToInsert() * this.getDuplicateRatio());
			this.numNullsToInsert = (int) (this.getNumRowsToInsert() * this.getNullRatio());
			this.numFreshsToInsert = this.getNumRowsToInsert() - this.numDupsToInsert - this.numNullsToInsert;
			
//			if( this.numFreshsToInsert == -1 ){
//				System.err.println("FIXME");
//			}
			
		}catch(ValueUnsetException e){
			e.printStackTrace();			
			// TODO: Release all resources
			System.exit(1);
		}
		this.numDupsNullRowsSet = true;
		this.generator = new CyclicGroupGenerator(numFreshsToInsert);
	}

	
	@Override
	public int getNumFreshsToInsert() throws ValueUnsetException{
		if( !this.numDupsNullRowsSet ) throw new ValueUnsetException();
		return numFreshsToInsert;
	}
	
	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if( other != null && this.getClass().equals(other.getClass()) ){
			ColumnPumper that = (ColumnPumper) other;
			if( this.getSchema().getTableName().equals(that.getSchema().getTableName()) ){
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
	
	@Override
	public void incrementNumFreshs() {
		++this.numFreshsToInsert;
	}
	
	@Override
	public void decrementNumFreshs() {
		--this.numFreshsToInsert;
	}

	@Override
	public void generateValues(Schema schema, DBMSConnection db)
		throws BoundariesUnsetException, ValueUnsetException{
	    
	    if(!boundariesSet) throw new BoundariesUnsetException("fillDomainBoundaries() hasn't been called yet");
	    
	    createValues(schema, db);
	    
	    this.generatedCounter = this.numRowsToInsert;
	}

	@Override
	public void generateNValues(Schema schema, DBMSConnection db, int n)
		throws BoundariesUnsetException, ValueUnsetException{
	    
	    if(!boundariesSet) throw new BoundariesUnsetException("fillDomainBoundaries() hasn't been called yet");
	    
	    createNValues(schema, db, n);
	    
	    this.generatedCounter += n;
	}
	
	protected abstract void createValues(Schema schema, DBMSConnection db) throws ValueUnsetException;
	
	protected abstract void createNValues(Schema schema, DBMSConnection db, int n) throws ValueUnsetException;
	
	
	protected void setBoundariesSet(){
	    this.boundariesSet = true;
	}
	
	protected boolean isBoundariesSet(){
	    return this.boundariesSet;
	}
	
	protected long getGeneratedCounter(){
	    return this.generatedCounter;
	}
	
	//	
//	@Override
//	public void setNumFreshsToInsert(int numFreshsToInsert) {
//		this.numFreshsToInsertSet = true;
//		this.numFreshsToInsert = numFreshsToInsert;
//	}
//
//	@Override
//	public int getNumDupsToInsert() throws ValueUnsetException {
//		if( ! numDupsToInsertSet ) throw new ValueUnsetException();
//		return numDupsToInsert;
//	}
//
//	@Override
//	public void setNumDupsToInsert(int numDupsToInsert) {
//		this.numDupsToInsertSet = true;
//		this.numDupsToInsert = numDupsToInsert;
//	}


};