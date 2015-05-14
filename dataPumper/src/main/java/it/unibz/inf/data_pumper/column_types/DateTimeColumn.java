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
import it.unibz.inf.data_pumper.column_types.intervals.DatetimeInterval;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.main.DebugException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class DateTimeColumn extends MultiIntervalColumn<Timestamp>{
	
	public DateTimeColumn(String name, MySqlDatatypes type, int index, Schema schema) {
		super(name, type, index, schema);
		this.intervals = new ArrayList<Interval<Timestamp>>();
	}

	@Override
	public void generateValues(Schema schema, DBMSConnection db) throws BoundariesUnsetException, ValueUnsetException, DebugException{

	    if(!firstIntervalSet) throw new BoundariesUnsetException("fillFirstIntervalBoundaries() hasn't been called yet");


	    List<Timestamp> values = new ArrayList<Timestamp>();

	    // 86400 Seconds in one day

	    for( int i = 0; i < this.getNumRowsToInsert(); ++i ){
		if( i < this.numNullsToInsert ){
		    values.add(null);
		}
		else{
		    long seqIndex = this.generator.nextValue(this.numFreshsToInsert);
		    int intervalIndex = getIntervalIndexFromSeqIndex(seqIndex);
		    Interval<Timestamp> interval = this.intervals.get(intervalIndex);
		    Calendar c = Calendar.getInstance();
		    c.setTime(interval.getMinValue());

		    long nextValue = this.map(seqIndex) * DatetimeInterval.MILLISECONDS_PER_DAY + c.getTimeInMillis();
		    values.add(new Timestamp(nextValue));
		}
		setDomain(values);
	    }
	}

	@Override
	public void fillFirstIntervalBoundaries(Schema schema, DBMSConnection db) throws ValueUnsetException, SQLException, DebugException {		

	    this.initNumDupsNullsFreshs();

	    Template t = new Template("select ? from "+schema.getTableName()+";");
	    PreparedStatement stmt;

	    t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");

	    stmt = db.getPreparedStatement(t);
	    ResultSet result = stmt.executeQuery();
	    
	    Timestamp min = new Timestamp(0);
	    Timestamp max = new Timestamp(Long.MAX_VALUE);

	    if( result.next() && (result.getTimestamp(1) != null) ){

	        if( result.getTimestamp(1).compareTo(result.getTimestamp(2)) == 0 && result.getTimestamp(1).compareTo(new Timestamp(Long.MAX_VALUE)) == 0 ){ // 9999,11,31 = min = max
	            // Do nothing
	        }
	        else{
	            min = result.getTimestamp(1);
	            max = result.getTimestamp(2);
	        }
	    }
	    stmt.close();

	    Calendar c = Calendar.getInstance();
	    c.setTimeInMillis(min.getTime());

	    Calendar upperBound = Calendar.getInstance();
	    upperBound.set(9999,11,31);

	    Calendar curMax = Calendar.getInstance();
	    curMax.setTimeInMillis(max.getTime());

	    boolean upperBoundTouched = false;
	    for( int i = 1; i <= this.numFreshsToInsert; ++i ){

		if( c.compareTo(upperBound) > -1 ){
		    this.numFreshsToInsert = i;
		    upperBoundTouched = true;
		    break;
		}
	        c.add(Calendar.DATE, 1);
	    }
	    
	    if( upperBoundTouched ){
		max = new Timestamp(Long.MAX_VALUE);
	    }
	    else{
		max = new Timestamp( ( (min.getTime() / DatetimeInterval.MILLISECONDS_PER_DAY) + numFreshsToInsert ) * DatetimeInterval.MILLISECONDS_PER_DAY );
	    }
//	    max = new Timestamp(c.getTimeInMillis());

	    // Create the single initial interval
	    List<ColumnPumper<Timestamp>> involvedCols = new LinkedList<ColumnPumper<Timestamp>>();
	    involvedCols.add(this);
	    Interval<Timestamp> initialInterval = new DatetimeInterval(this.getQualifiedName().toString(), this.getType(), this.numFreshsToInsert, involvedCols);
	    
	    initialInterval.updateMinEncodingAndValue(min.getTime() / DatetimeInterval.MILLISECONDS_PER_DAY);
	    initialInterval.updateMaxEncodingAndValue(max.getTime() / DatetimeInterval.MILLISECONDS_PER_DAY);
	    
	    this.intervals.add(initialInterval);	    
	    
	    this.firstIntervalSet = true;
	}
};