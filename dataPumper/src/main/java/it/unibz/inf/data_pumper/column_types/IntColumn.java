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
import it.unibz.inf.data_pumper.basic_datatypes.Template;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.column_types.intervals.IntInterval;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.main.DebugException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class IntColumn extends MultiIntervalColumn<Long> {


    public IntColumn(String name, MySqlDatatypes type, int index, int datatypeLengthFirstArgument, int datatypeLengthSecondArgument, Schema schema) {
        super(name, type, index, schema);

        this.intervals = new ArrayList<Interval<Long>>();
    }

    public IntColumn(String name, MySqlDatatypes type, int index, Schema schema) {
        super(name, type, index, schema);

        this.intervals = new ArrayList<Interval<Long>>();
    }
    
    @Override
    public void generateValues(Schema schema, DBMSConnection db) throws BoundariesUnsetException, ValueUnsetException, DebugException {

        if(!firstIntervalSet) throw new BoundariesUnsetException("fillFirstIntervalBoundaries() hasn't been called yet");
        
        List<Long> values = new ArrayList<Long>();
        
        for( int i = 0; i < this.getNumRowsToInsert(); ++i ){
            if( i < this.numNullsToInsert ){
                values.add(null);
            }			
            else{
        	long seqIndex = this.generator.nextValue(this.numFreshsToInsert);
        	int intervalIndex = getIntervalIndexFromSeqIndex(seqIndex);
		
		IntInterval interval = (IntInterval) this.intervals.get(intervalIndex);
        	
		long toAdd = interval.getMinEncoding() + this.map(seqIndex);
		
        	values.add(toAdd);        
            }
        }
        setDomain(values);
    }

//    @Override
//    public void generateValues(Schema schema, DBMSConnection db) throws BoundariesUnsetException, ValueUnsetException {
//
//        if(!firstIntervalSet) throw new BoundariesUnsetException("fillFirstIntervalBoundaries() hasn't been called yet");
//        
//        int intervalIndex = 0;
//
//        List<Long> values = new ArrayList<Long>();
//        int insertedInInterval = 0;
//              
//        for( int i = 0; i < this.getNumRowsToInsert(); ++i ){
//            if( i < this.numNullsToInsert ){
//                values.add(null);
//            }			
//            else{
//                Interval<Long> interval = intervals.get(intervalIndex);
//                values.add(interval.getMinValue() + this.generator.nextValue(interval.getNFreshsToInsert()));
//
//                ++insertedInInterval;
//
//                if( insertedInInterval >= interval.getNFreshsToInsert()  ){
//                    insertedInInterval = 0;
//                    intervalIndex = ( intervalIndex + 1 ) %  intervals.size();
//                }
//            }
//        }
//        setDomain(values);
//    }
    

    @Override
    public void fillFirstIntervalBoundaries(Schema schema, DBMSConnection db) throws ValueUnsetException, SQLException, DebugException {

        this.initNumDupsNullsFreshs();

        Template t = new Template("select ? from "+schema.getTableName()+";");
        PreparedStatement stmt;

        t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");

        stmt = db.getPreparedStatement(t);

        ResultSet result;
        long min = 0;
        long max = 0;

        result = stmt.executeQuery();
        if( result.next() ){
            min = result.getLong(1);
            max = result.getLong(2);
        }
        stmt.close();

        max = min + this.numFreshsToInsert;  
                
        
        // Create the single initial interval
        List<ColumnPumper<Long>> involvedCols = new LinkedList<ColumnPumper<Long>>();
        involvedCols.add(this);
        Interval<Long> interval = 
                new IntInterval(
                        this.getQualifiedName().toString(), this.getType(), 
                        this.numFreshsToInsert, involvedCols);

        interval.updateMinEncodingAndValue(min);
        interval.updateMaxEncodingAndValue(max);

        this.intervals.add(interval);

        this.firstIntervalSet = true;
    }
};
