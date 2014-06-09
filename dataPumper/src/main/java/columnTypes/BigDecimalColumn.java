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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import connection.DBMSConnection;

public class BigDecimalColumn extends IncrementableColumn<BigDecimal>{
		
	public BigDecimalColumn(String name, MySqlDatatypes type, int index, int datatypeFirstLength) {
		super(name, type, index);
		domain = null;
		this.max = null;
		this.min = null;
		this.lastFreshInserted = null;
	}
	
	public BigDecimalColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		domain = null;
		this.max = null;
		this.min = null;
		this.lastFreshInserted = null;
	}
	
	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT "+getName()+ " FROM "+schema.getTableName()+ " WHERE "+getName()+" IS NOT NULL");
		
		List<BigDecimal> values = null;
		
		try {
			ResultSet result = stmt.executeQuery();
			
			values = new ArrayList<BigDecimal>();
		
			while( result.next() ){
				values.add(BigDecimal.valueOf(result.getDouble(1)));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setDomain(values);
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		
		if( lastFreshInserted != null ) return; // Boundaries already filled
		
		Template t = new Template("select ? from "+schema.getTableName()+";");
		PreparedStatement stmt;
		
		t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");
		
		stmt = db.getPreparedStatement(t);
		
		ResultSet result;
		try {
			result = stmt.executeQuery();
			if( result.next() ){
				setMinValue(BigDecimal.valueOf(result.getDouble(1)));
				setMaxValue(BigDecimal.valueOf(result.getDouble(2)));
				setLastFreshInserted(BigDecimal.valueOf(result.getDouble(1)));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public BigDecimal increment(BigDecimal toIncrement) {
		
		toIncrement = toIncrement.add(BigDecimal.ONE);
		
		return toIncrement;
	}

	@Override
	public BigDecimal getCurrentMax() {
		if( domain.size() == 0 )
			return BigDecimal.valueOf(Double.MAX_VALUE);
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}
	
	@Override
	public String getNextChased(DBMSConnection db, Schema schema) {
		
		String result = cP.pickChase(db, schema);
		
		if( result == null ) return null;
		
		BigDecimal resultBD = new BigDecimal(result);
		if( resultBD.compareTo(lastFreshInserted) > 0 ){
			lastFreshInserted = resultBD;
		}
		
		return result;
	}

	@Override
	public void proposeLastFreshInserted(String inserted) {
		BigDecimal resultBD = new BigDecimal(inserted);
		if( resultBD.compareTo(lastFreshInserted) > 0 ){
			lastFreshInserted = resultBD;
		}
	}
}
