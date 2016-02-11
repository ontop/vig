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
import it.unibz.inf.data_pumper.connection.DBMSConnection;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BigDecimalColumn extends OrderedDomainColumn<BigDecimal>{
			
	public BigDecimalColumn(String name, MySqlDatatypes type, int index, int datatypeFirstLength, Schema schema) {
		super(name, type, index, schema);
		domain = null;
		this.max = null;
		this.min = null;
	}
	
	public BigDecimalColumn(String name, MySqlDatatypes type, int index, Schema schema) {
		super(name, type, index, schema);
		domain = null;
		this.max = null;
		this.min = null;
	}
	
	@Override
	public void createValues(Schema schema, DBMSConnection db) throws ValueUnsetException {
		
		List<BigDecimal> values = new ArrayList<BigDecimal>();
		
//		for( BigDecimal i = this.getMinValue(); i.compareTo(this.getMaxValue()) < 0; i.add(BigDecimal.ONE) ){
//			values.add(i);
//		}
		
		try {
			for( int i = 0; i < this.getNumRowsToInsert(); ++i ){
				if( i < this.numNullsToInsert ){
					values.add(null);
				}
				else{
				    values.add(min.add(new BigDecimal(this.generator.nextValue(this.numFreshsToInsert))));
				}
			}
		} catch (ValueUnsetException e) {
			e.printStackTrace();
			// TODO Release resources
			System.exit(1);
		}
		
		setDomain(values);
	}
	
	@Override
	public void createNValues(Schema schema, DBMSConnection db, int n) throws ValueUnsetException {

	    List<BigDecimal> values = new ArrayList<BigDecimal>();

	    for( int i = 0; i < n; ++i ){
		if( this.getGeneratedCounter() + i < this.numNullsToInsert ){
		    values.add(null);
		}
		else{
		    values.add(min.add(new BigDecimal(this.generator.nextValue(this.numFreshsToInsert))));
		}
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
		
		ResultSet result = null;
		min = BigDecimal.ZERO;
		max = BigDecimal.ZERO;
		try {
			result = stmt.executeQuery();
			if( result.next() ){
				min = BigDecimal.valueOf(result.getDouble(1));
				max = BigDecimal.valueOf(result.getDouble(2));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		BigDecimal nFreshsBigDecimalTransl = new BigDecimal(this.numFreshsToInsert);
		
		BigDecimal proposedMax = min.add(nFreshsBigDecimalTransl); 
		
		if( proposedMax.compareTo(max) > 0 ){ 
				max = proposedMax;
		}
		
		setMinValue(min);
		setMaxValue(max);
		
		this.setBoundariesSet();
	}
	
	@Override
	public long getMaxEncoding() throws BoundariesUnsetException {
		if(!isBoundariesSet()) throw new BoundariesUnsetException("fillDomainBoundaries() hasn't been called yet");
		return this.max.longValue();
	};

	@Override
	public long getMinEncoding() throws BoundariesUnsetException {
		if(!isBoundariesSet()) throw new BoundariesUnsetException("fillDomainBoundaries() hasn't been called yet");
		return this.min.longValue();
	}

	@Override
	public void updateMinValueByEncoding(long newMin) {
		this.min = new BigDecimal(newMin);		
	}
	
	@Override
	public void updateMaxValueByEncoding(long newMax) {
		this.max = new BigDecimal(newMax);		
	}

}
