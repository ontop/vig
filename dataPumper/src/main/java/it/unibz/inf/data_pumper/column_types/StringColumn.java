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
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.column_types.intervals.StringInterval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.main.DEBUGEXCEPTION;

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
	public void generateValues(Schema schema, DBMSConnection db) throws BoundariesUnsetException, ValueUnsetException {

	    if( !this.firstIntervalSet ) throw new BoundariesUnsetException("fillFirstIntervalBoundaries() hasn't been called yet");

	    int intervalIndex = 0;

	    // Debug
	    if( this.schema.getTableName().equals("wellbore_development_all") && this.getName().equals("wlbNamePart3") && datatypeLength > 1 ){
	        if( this.numFreshsToInsert >= this.characters.length()){
	            try{
	                throw new DEBUGEXCEPTION();
	            }
	            catch(DEBUGEXCEPTION e){
	                e.printStackTrace();
	                System.exit(1);
	            }
	        }
	    }

	    List<String> values = new ArrayList<String>();
	    int insertedInInterval = 0;
	    int numDupsInsertedInInterval = 0;

	    for( int i = 0; i < this.getNumRowsToInsert(); ++i ){
	        if( i < this.numNullsToInsert ){
	            values.add(null);
	        }
	        else{
	            Interval<String> interval = this.intervals.get(intervalIndex);

	            String trail = StringInterval.encode(interval.getMinEncoding() + this.generator.nextValue(this.numFreshsToInsert));

	            StringBuilder zeroes = new StringBuilder();
	            for( int j = 0; j < StringInterval.encode(interval.getMinEncoding()).length() - trail.length(); ++j ){
	                zeroes.append("0");
	            }
	            String valueToAdd = zeroes.toString() + trail;
	            values.add(valueToAdd);

	            ++insertedInInterval;
	            
	            if( insertedInInterval >= interval.nFreshsToInsert && (intervalIndex < intervals.size() - 1) ){
                    if( numDupsInsertedInInterval++ == numDupsForInterval(intervalIndex) ){
                        insertedInInterval = 0;
                        ++intervalIndex;
                        numDupsInsertedInInterval = 0;
                    }
                }
	        }
	    }				
	    setDomain(values);
	}

	@Override
	public void fillFirstIntervalBoundaries(Schema schema, DBMSConnection db) throws ValueUnsetException, SQLException {
		
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
        Interval<String> initialInterval = new StringInterval(this.getQualifiedName(), this.getType(), this.numFreshsToInsert, this.datatypeLength, involvedCols);
        
        initialInterval.setMinValue(lowerBoundValue());
        initialInterval.setMaxValue(upperBoundValue());
        
        this.intervals.add(initialInterval);
		
		this.firstIntervalSet = true;
	}
	
	private String lowerBoundValue(){
		StringBuilder builder = new StringBuilder();
		
		for( int i = 0; i < datatypeLength; ++i ){
			builder.append(characters.charAt(0)); // Minimum
		}
		
		return builder.toString();
	}
	
	private String upperBoundValue(){
		StringBuilder builder = new StringBuilder();
		
		for( int i = 0; i < (datatypeLength > MAX_LENGTH ? MAX_LENGTH : datatypeLength); ++i ){
			builder.append(characters.charAt(characters.length()-1)); // Maximum
		}
		
		return builder.toString();
	}
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
