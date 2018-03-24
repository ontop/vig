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
import it.unibz.inf.data_pumper.columns.intervals.DatetimeInterval;
import it.unibz.inf.data_pumper.columns.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.tables.MySqlDatatypes;
import it.unibz.inf.data_pumper.tables.Schema;
import it.unibz.inf.data_pumper.utils.Template;

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
	public void createValues(Schema schema, DBMSConnection db) {

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
	public void createNValues(Schema schema, DBMSConnection db, long n) {

	    if(!firstIntervalSet) throw new BoundariesUnsetException("fillFirstIntervalBoundaries() hasn't been called yet");

	    List<Timestamp> values = new ArrayList<Timestamp>();

	    // 86400 Seconds in one day

	    for( int i = 0; i < n; ++i ){
		if( this.getGeneratedCounter() + i < this.numNullsToInsert ){
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
	public void fillFirstIntervalBoundaries(Schema schema, DBMSConnection db) throws SQLException {	
	    
	    this.initNumDupsNullsFreshs();

	    Template t = new Template("select ? from "+schema.getTableName()+";");
	    PreparedStatement stmt;

	    t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");

	    stmt = db.getPreparedStatement(t);
	    ResultSet result = stmt.executeQuery();
	    
	    Timestamp min = new Timestamp(0);
	    Timestamp max = new Timestamp(Long.MAX_VALUE);

	    if( result.next() ){
		// Avoid zero-dates 
		String stringDate = result.getString(1);
		if( stringDate != null && stringDate.equals("0000-00-00") ){
		    
		}
		else{
		    if( (result.getTimestamp(1) != null) ){
			min = result.getTimestamp(1);
			max = result.getTimestamp(2);
		    }
		}
	    }
	    
	    stmt.close();
	    
	    Calendar upperBound = Calendar.getInstance();
	    upperBound.set(9999,11,31);
	    DatetimeInterval.normalizeCalendar(upperBound);
	    
	    long minTime = min.getTime();
	    long maxTime = upperBound.getTimeInMillis();
	    
	    if( minTime < 0 ){
		minTime = 0;
	    }
	    
	    if( maxTime - minTime < (this.numFreshsToInsert * DatetimeInterval.MILLISECONDS_PER_DAY) ){
		minTime = maxTime - this.numFreshsToInsert * DatetimeInterval.MILLISECONDS_PER_DAY;
		
		if( minTime < 0 ){
		    minTime = 0;
		}
	    }
	    
	    maxTime = minTime + numFreshsToInsert * DatetimeInterval.MILLISECONDS_PER_DAY;
	    
	    // Create the single initial interval
	    List<ColumnPumper<Timestamp>> involvedCols = new LinkedList<ColumnPumper<Timestamp>>();
	    involvedCols.add(this);
	    Interval<Timestamp> initialInterval = new DatetimeInterval(this.getQualifiedName().toString(), this.getType(), this.numFreshsToInsert, involvedCols);
	    
	    initialInterval.updateMinEncodingAndValue(minTime / DatetimeInterval.MILLISECONDS_PER_DAY);
	    initialInterval.updateMaxEncodingAndValue(maxTime / DatetimeInterval.MILLISECONDS_PER_DAY);
	    
	    initialInterval.synchronizeMinMaxNFreshs();
	    
	    this.intervals.add(initialInterval);	    
	    
	    this.firstIntervalSet = true;
	}

	@Override
	public void addInterval(String name, long minEncoding, long maxEncoding) {
	    Interval<Timestamp> toAdd = new DatetimeInterval(name, getType(), minEncoding, maxEncoding);
	    toAdd.updateMaxEncodingAndValue(maxEncoding);
	    toAdd.updateMinEncodingAndValue(minEncoding);
	    this.addInterval(toAdd);
	}
};