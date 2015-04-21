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
import it.unibz.inf.data_pumper.column_types.intervals.BigDecimalInterval;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BigDecimalColumn extends MultiIntervalColumn<BigDecimal>{
		
    public BigDecimalColumn(String name, MySqlDatatypes type, int index, Schema schema) {
	    super(name, type, index, schema);		
	    this.intervals = new ArrayList<Interval<BigDecimal>>();
	}
	
	@Override
	public void generateValues(Schema schema, DBMSConnection db) throws BoundariesUnsetException, ValueUnsetException {
		
		if( !this.firstIntervalSet ) throw new BoundariesUnsetException("fillFirstIntervalBoundaries() hasn't been called yet");

		int intervalIndex = 0;
		
		List<BigDecimal> values = new ArrayList<BigDecimal>();
		int insertedInInterval = 0;
		int numDupsInsertedInInterval = 0;
		
		for( int i = 0; i < this.getNumRowsToInsert(); ++i ){
		    if( i < this.numNullsToInsert ){
		        values.add(null);
		    }
		    else{
		        Interval<BigDecimal> interval = this.intervals.get(intervalIndex);
		        BigDecimal genFresh = new BigDecimal(this.generator.nextValue(this.numFreshsToInsert));
		        values.add(interval.getMinValue().add(genFresh));

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
		
		Template t = new Template("select ? from "+schema.getTableName()+";");
		PreparedStatement stmt;
		
		t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");
		
		stmt = db.getPreparedStatement(t);
		
		ResultSet result = null;
		BigDecimal min = BigDecimal.ZERO;
		BigDecimal max = BigDecimal.ZERO;
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
		
		// Create the single initial interval
		List<ColumnPumper<BigDecimal>> involvedCols = new LinkedList<ColumnPumper<BigDecimal>>();
		involvedCols.add(this);
        Interval<BigDecimal> initialInterval = new BigDecimalInterval(this.getQualifiedName(), this.getType(), this.numFreshsToInsert, involvedCols);
		
		initialInterval.setMinValue(min);
		initialInterval.setMaxValue(max);
		
		this.intervals.add(initialInterval);
		
		this.firstIntervalSet = true;
	}
}
