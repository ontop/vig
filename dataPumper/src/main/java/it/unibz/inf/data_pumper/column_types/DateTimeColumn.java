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
import it.unibz.inf.data_pumper.column_types.exceptions.DateOutOfBoundariesException;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.connection.DBMSConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DateTimeColumn extends OrderedDomainColumn<Timestamp>{
	
	private boolean boundariesSet = false;
	private final int MILLISECONDS_PER_DAY=86400000;
	
	public DateTimeColumn(String name, MySqlDatatypes type, int index, Schema schema) {
		super(name, type, index, schema);
		domain = null;
		this.max = null;
		this.min = null;
		this.numFreshsToInsert = 0;
	}

	@Override
	public void generateValues(Schema schema, DBMSConnection db) throws BoundariesUnsetException, ValueUnsetException{
		
		if(!boundariesSet) throw new BoundariesUnsetException("fillDomainBoundaries() hasn't been called yet");
		
		
		List<Timestamp> values = new ArrayList<Timestamp>();
		
		Calendar c = Calendar.getInstance();
		c.setTime(min);
		
		// 86400 Seconds in one day
		
		for( int i = 0; i < this.getNumRowsToInsert(); ++i ){
			
			if( i < this.numNullsToInsert ){
				values.add(null);
			}
			
			long nextValue = this.generator.nextValue(this.numFreshsToInsert) * this.MILLISECONDS_PER_DAY + c.getTimeInMillis();
			values.add(new Timestamp(nextValue));
		}
		setDomain(values);
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) throws ValueUnsetException{		
		
		this.initNumDupsNullsFreshs();
		
		Template t = new Template("select ? from "+schema.getTableName()+";");
		PreparedStatement stmt;
		
		t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");
		
		stmt = db.getPreparedStatement(t);
		try{
			ResultSet result = stmt.executeQuery();
			min = new Timestamp(0);
			max = new Timestamp(Long.MAX_VALUE);
			
			if( result.next() && (result.getTimestamp(1) != null) ){
				
				if( result.getTimestamp(1).compareTo(result.getTimestamp(2)) == 0 && result.getTimestamp(1).compareTo(new Timestamp(Long.MAX_VALUE)) == 0 ){ // It looks crazy but it happens
					// Do nothing
				}
				else{
					min = result.getTimestamp(1);
					max = result.getTimestamp(2);
				}
			}
			stmt.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
				
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(min.getTime());
		
		Calendar upperBound = Calendar.getInstance();
		upperBound.set(9999,11,31);
		
		Calendar curMax = Calendar.getInstance();
		curMax.setTimeInMillis(max.getTime());
				
		for( int i = 1; i <= this.numFreshsToInsert; ++i ){
			
			if( c.compareTo(upperBound) > -1 ){
				this.numFreshsToInsert = i;
				break;
			}
			c.add(Calendar.DATE, 1);
		}

		max = new Timestamp(c.getTimeInMillis());
		
		setMinValue(min);
		setMaxValue(max);
		
		this.boundariesSet = true;
	}

	@Override
	public void updateMinValueByEncoding(long newMin) {
		min = new Timestamp(newMin * this.MILLISECONDS_PER_DAY);
	}
	
	@Override
	public void updateMaxValueByEncoding(long newMax) {
		
		Calendar upperBound = Calendar.getInstance();
		upperBound.set(9999,11,31);
		
		if( upperBound.getTimeInMillis() > newMax * this.MILLISECONDS_PER_DAY ){		
			max = new Timestamp(newMax * this.MILLISECONDS_PER_DAY);
		}
		else{
			try{
				throw new DateOutOfBoundariesException();
			}catch(DateOutOfBoundariesException e){
				logger.error("The Date field cannot hold this many rows");
				System.exit(1);
			}
		}
	}
	
	@Override
	public long getMinEncoding() throws BoundariesUnsetException {
		long encoding = (long) (this.min.getTime() / this.MILLISECONDS_PER_DAY);
		return encoding;
	}

	@Override
	public long getMaxEncoding() throws BoundariesUnsetException {
		long encoding = (long) (this.max.getTime() / this.MILLISECONDS_PER_DAY);
		return encoding;
	}
};