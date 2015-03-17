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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IntColumn extends OrderedDomainColumn<Long> {
	
	private int datatypeLengthFirstArgument;
	private int datatypeLengthSecondArgument;
	private boolean boundariesSet;
	
//	private long modulo;
	
	public IntColumn(String name, MySqlDatatypes type, int index, int datatypeLengthFirst, int datatypeLengthSecondArgument, Schema schema) {
		super(name, type, index, schema);
		domain = null;
		this.max = null;
		this.min = null;
		
		this.datatypeLengthFirstArgument = datatypeLengthFirst;
		this.datatypeLengthSecondArgument = datatypeLengthSecondArgument;
				
		fillModulo();
		
		index = 0;
	}
	
	public IntColumn(String name, MySqlDatatypes type, int index, Schema schema) {
		super(name, type, index, schema);
		domain = null;
		this.max = null;
		this.min = null;
		
		this.datatypeLengthFirstArgument = Integer.MAX_VALUE;
		this.datatypeLengthSecondArgument = 0;
		
//		modulo = Long.MAX_VALUE;
		
		index = 0;
	}
	
	private void fillModulo() { 
		
		StringBuilder builder = new StringBuilder();
		
		for( int i = 0; i < (datatypeLengthFirstArgument - datatypeLengthSecondArgument); ++i ){
			builder.append("9");
		}
		
//		modulo = Long.parseLong(builder.toString());
		
	}


	@Override
	public void generateValues(Schema schema, DBMSConnection db) throws BoundariesUnsetException, ValueUnsetException {
		
		if(!boundariesSet) throw new BoundariesUnsetException("fillDomainBoundaries() hasn't been called yet");
		
		List<Long> values = new ArrayList<Long>();
		
		for( int i = 0; i < this.getNumRowsToInsert(); ++i ){
			if( i < this.numNullsToInsert ){
				values.add(null);
			}
			values.add(min + this.generator.nextValue(this.numFreshsToInsert));
		}
		setDomain(values);
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		
		this.initNumDupsNullsFreshs();
		
		Template t = new Template("select ? from "+schema.getTableName()+";");
		PreparedStatement stmt;
		
		t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");
		
		stmt = db.getPreparedStatement(t);
		
		ResultSet result;
		long min = 0;
		long max = 0;
		try {
			result = stmt.executeQuery();
			if( result.next() ){
				min = result.getLong(1);
				max = result.getLong(2);
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		max = min + this.numFreshsToInsert;  
		
		setMinValue(min);
		setMaxValue(max);
		
		this.boundariesSet = true;
	}

	@Override
	public void updateMinValueByEncoding(long newMin) {
		this.min = newMin;
	}
	
	@Override
	public void updateMaxValueByEncoding(long newMax) {
		this.max = newMax;
	}
	
	@Override
	public long getMinEncoding() throws BoundariesUnsetException {
		if(!boundariesSet) throw new BoundariesUnsetException("fillDomainBoundaries() hasn't been called yet");
		return min.longValue();
	}
	
	@Override
	public long getMaxEncoding() throws BoundariesUnsetException {
		if(!boundariesSet) throw new BoundariesUnsetException("fillDomainBoundaries() hasn't been called yet");
		return this.max.longValue();
	}

};
