package columnTypes;

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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import connection.DBMSConnection;

public class DateTimeColumn extends IncrementableColumn<Timestamp>{
	
	public DateTimeColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		
		lastFreshInserted = null;
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT "+getName()+ " FROM "+schema.getTableName()+ " WHERE "+getName()+" IS NOT NULL");
		
		List<Timestamp> values = null;
		
		try {
			ResultSet result = stmt.executeQuery();
			
			values = new ArrayList<Timestamp>();
		
			while( result.next() ){
				values.add(result.getTimestamp(1));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setDomain(values);
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		
		if( getName().equals("bsns_arr_area_operator") ){
			logger.error("START DEBUG");
		}
		
		if( lastFreshInserted != null ) return; // Boundaries already filled
		
		Template t = new Template("select ? from "+schema.getTableName()+";");
		PreparedStatement stmt;
		
		t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");
		
		stmt = db.getPreparedStatement(t);
		try{
			ResultSet result = stmt.executeQuery();
			
			if( result.next() && (result.getTimestamp(1) != null) ){
				
				if( result.getTimestamp(1).compareTo(result.getTimestamp(2)) == 0 && result.getTimestamp(1).compareTo(new Timestamp(Long.MAX_VALUE)) == 0 ){ // It looks crazy but it happens
					setMinValue(new Timestamp(0));
					setMaxValue(new Timestamp(Long.MAX_VALUE));
					setLastFreshInserted(new Timestamp(0));
				}
				else{
					setMinValue(result.getTimestamp(1));
					setMaxValue(result.getTimestamp(2));
					setLastFreshInserted(result.getTimestamp(1));
				}
			}
			else{
				setMinValue(new Timestamp(0));
				setMaxValue(new Timestamp(Long.MAX_VALUE));
				setLastFreshInserted(new Timestamp(0));
			}
			stmt.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}

	@Override
	public Timestamp increment(Timestamp toIncrement) {		
		
		Calendar c = Calendar.getInstance();
		
		c.set(9999,11,31);
		Timestamp upperBound = new Timestamp(c.getTimeInMillis());
		
		c.setTime(toIncrement);
		Timestamp temp = new Timestamp(c.getTimeInMillis());
		if (temp.compareTo(upperBound) > -1 ){
			logger.debug("Insufficient number of days. Fresh value generation can no longer be guaranteed"
					+ "for column "+this.getName());
			return min;  // Altro giro di giostra
		}
		
		c.add(Calendar.DATE, 1);
		
		return new Timestamp(c.getTimeInMillis());
		
	}

	@Override
	public Timestamp getCurrentMax() {
		if( domain.size() == 0 )
			return new Timestamp(Long.MAX_VALUE);
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}

	@Override
	public String getNextChased(DBMSConnection db, Schema schema) {
		
		String result = cP.pickChase(db, schema);
		
		if( result == null ) return null;

		Timestamp chased = new Timestamp(Long.parseLong(result));
		
		if( chased.compareTo(lastFreshInserted) > 0 )
			lastFreshInserted = chased;
		
		return result;
	}

	@Override
	public void proposeLastFreshInserted(String inserted) {
		Timestamp chased = new Timestamp(Long.parseLong(inserted));
		
		if( chased.compareTo(lastFreshInserted) > 0 )
			lastFreshInserted = chased;
	}
};