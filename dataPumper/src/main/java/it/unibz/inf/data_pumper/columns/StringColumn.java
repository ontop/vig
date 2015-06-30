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


import it.unibz.inf.data_pumper.columns.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.columns.intervals.Interval;
import it.unibz.inf.data_pumper.columns.intervals.StringInterval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.main.DebugException;
import it.unibz.inf.data_pumper.tables.MySqlDatatypes;
import it.unibz.inf.data_pumper.tables.Schema;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StringColumn extends MultiIntervalColumn<String> {

    // Constants
    private static final int MAX_LENGTH = 20;

    // Characters out of which Strings will be formed
    String characters = StringInterval.characters;

    public StringColumn(String name, MySqlDatatypes type, int index, int datatypeLength, Schema schema){
	super(name, type, index, schema);

	this.datatypeLength = datatypeLength;

	if( this.datatypeLength > MAX_LENGTH ) this.datatypeLength = MAX_LENGTH;

	//		rndIndexes = new ArrayList<Integer>(datatypeLength);
	//
	//		for( int i = 0; i < datatypeLength; ++i )
	//			rndIndexes.add(0); // Initial String: 00000000...

	this.numFreshsToInsert = 0;

	this.intervals = new ArrayList<Interval<String>>();
    }

    public StringColumn(String name, MySqlDatatypes type, int index, Schema schema) {
	super(name, type, index, schema);

	this.datatypeLength = MAX_LENGTH;
	//		rndIndexes = new ArrayList<Integer>(datatypeLength);
	//
	//		for( int i = 0; i < datatypeLength; ++i )
	//			rndIndexes.add(0); // Initial String: 00000000...

	this.numFreshsToInsert = 0;

	this.intervals = new ArrayList<Interval<String>>();
    }

    @Override
    public void generateValues(Schema schema, DBMSConnection db) {

	if( !this.firstIntervalSet ) throw new BoundariesUnsetException("fillFirstIntervalBoundaries() hasn't been called yet");

	int intervalIndex = 0;

	// Debug
	if( this.schema.getTableName().equals("wellbore_development_all") && this.getName().equals("wlbNamePart3") && datatypeLength > 1 ){
	    if( this.numFreshsToInsert >= this.characters.length()){
		throw new DebugException("Problem with StringColumn");
	    }
	}

	List<String> values = new ArrayList<String>();
	
	for( int i = 0; i < this.getNumRowsToInsert(); ++i ){
	    if( i < this.numNullsToInsert ){
		values.add(null);
	    }
	    else{
		
		long seqIndex = this.generator.nextValue(this.numFreshsToInsert);
		intervalIndex = getIntervalIndexFromSeqIndex(seqIndex);
		
		StringInterval interval = (StringInterval) this.intervals.get(intervalIndex);

		String trail = interval.encode(interval.getMinEncoding() + this.map(seqIndex));

		StringBuilder zeroes = new StringBuilder();
		for( int j = 0; j < interval.lowerBoundValue().length() - trail.length(); ++j ){
		    zeroes.append("0");
		}
		String valueToAdd = zeroes.toString() + trail;
		values.add(valueToAdd);
	    }
	}				
	setDomain(values);
    }

    @Override
    public void fillFirstIntervalBoundaries(Schema schema, DBMSConnection db) throws SQLException {

	this.initNumDupsNullsFreshs();

	//		this.getIntervals().get(0).minEncoding = 0; // TODO See this part
	//		String lowerBouldValue = lowerBoundValue();
	//		
	//		String trail = encode(this.numFreshsToInsert);
	//		StringBuilder zeroes = new StringBuilder();
	//		
	//		if( lowerBouldValue.length() > trail.length() ){
	//			for( int j = 0; j < lowerBouldValue.length() - trail.length(); ++j ){
	//				zeroes.append("0");
	//			}
	//			this.max = zeroes.toString() + trail;
	//		}
	//		else{
	//			this.max = upperBoundValue();
	//			this.numFreshsToInsert = 1;
	//			for( int i = 0; i < this.max.length(); ++i ){
	//				this.numFreshsToInsert *= characters.length() -1;
	//			}
	//		}

	// Create the single initial interval
	List<ColumnPumper<String>> involvedCols = new LinkedList<ColumnPumper<String>>();
	involvedCols.add(this);
	Interval<String> initialInterval = getIntervalInstance(this.getQualifiedName().toString(), involvedCols);

	initialInterval.updateMinEncodingAndValue(0);
	initialInterval.updateMaxEncodingAndValue(this.numFreshsToInsert);

	this.intervals.add(initialInterval);

	this.firstIntervalSet = true;
    }

    protected Interval<String> getIntervalInstance(
	    String qualifiedName, List<ColumnPumper<String>> involvedCols){
	Interval<String> interval = new StringInterval(qualifiedName, this.getType(), this.numFreshsToInsert, this.datatypeLength, involvedCols);
	return interval;
    }

    @Override
    public void addInterval(String name, long minEncoding, long maxEncoding) {
	 Interval<String> toAdd = new StringInterval(name, getType(), minEncoding, maxEncoding, this.datatypeLength);
	 toAdd.updateMaxEncodingAndValue(maxEncoding);
	 toAdd.updateMinEncodingAndValue(minEncoding);
	 this.addInterval(toAdd);
    }
    
    //	private String lowerBoundValue(){
    //		StringBuilder builder = new StringBuilder();
    //		
    //		for( int i = 0; i < datatypeLength; ++i ){
    //			builder.append(characters.charAt(0)); // Minimum
    //		}
    //		
    //		return builder.toString();
    //	}
    //	
    //	private String upperBoundValue(){
    //		StringBuilder builder = new StringBuilder();
    //		
    //		for( int i = 0; i < (datatypeLength > MAX_LENGTH ? MAX_LENGTH : datatypeLength); ++i ){
    //			builder.append(characters.charAt(characters.length()-1)); // Maximum
    //		}
    //		
    //		return builder.toString();
    //	}
};

//private String increment(String toIncrement) {
//
//StringBuilder builder = new StringBuilder(toIncrement);
//
//for( int i = toIncrement.length() -1; i >= 0; --i ){
//	
//	if( toIncrement.substring(i, i+1).compareTo(upperBoundValue().substring(0, 1)) < 0 ){
//		builder.replace(i, i+1, characters.charAt(characters.indexOf(toIncrement.charAt(i)) + 1)+"");
//		return builder.toString();
//	}
//	int j = i;
//	
//	while( j >= 0 && toIncrement.charAt(j) == upperBoundValue().charAt(0) ){
//		builder.replace(j, j+1, lowerBoundValue().substring(0, 1));
//		--j;
//	}
//	if( j >= 0 ){
//		builder.replace(j, j+1, characters.charAt(characters.indexOf(toIncrement.charAt(j)) + 1)+"");
//		return builder.toString();
//	}
//} // Available symbols are finished. Put a duplicate.
//logger.debug("NOT POSSIBLE TO ADD A FRESH VALUE. RE-GENERATING");
//
//return null;
//}
