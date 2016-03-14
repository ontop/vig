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
import it.unibz.inf.data_pumper.columns.intervals.BigDecimalInterval;
import it.unibz.inf.data_pumper.columns.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.tables.MySqlDatatypes;
import it.unibz.inf.data_pumper.tables.Schema;
import it.unibz.inf.data_pumper.utils.Template;

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
    public void createValues(Schema schema, DBMSConnection db) {

	if( !this.firstIntervalSet ) throw new BoundariesUnsetException("fillFirstIntervalBoundaries() hasn't been called yet");

	List<BigDecimal> values = new ArrayList<BigDecimal>();

	for( int i = 0; i < this.getNumRowsToInsert(); ++i ){
	    if( i < this.numNullsToInsert ){
		values.add(null);
	    }
	    else{
		long seqIndex = this.generator.nextValue(this.numFreshsToInsert);
		int intervalIndex = getIntervalIndexFromSeqIndex(seqIndex);
		
		Interval<BigDecimal> interval = this.intervals.get(intervalIndex);
		BigDecimal genFresh = new BigDecimal(seqIndex);
		values.add(interval.getMinValue().add(genFresh));
	    }
	}
	setDomain(values);
    }
    
    @Override
    public void createNValues(Schema schema, DBMSConnection db, long n) {

	if( !this.firstIntervalSet ) throw new BoundariesUnsetException("fillFirstIntervalBoundaries() hasn't been called yet");

	List<BigDecimal> values = new ArrayList<BigDecimal>();

	for( int i = 0; i < n; ++i ){
	    if( this.getGeneratedCounter() + i < this.numNullsToInsert ){
		values.add(null);
	    }
	    else{
		long seqIndex = this.generator.nextValue(this.numFreshsToInsert);
		int intervalIndex = getIntervalIndexFromSeqIndex(seqIndex);
		
		Interval<BigDecimal> interval = this.intervals.get(intervalIndex);
		BigDecimal genFresh = new BigDecimal(seqIndex);
		values.add(interval.getMinValue().add(genFresh));
	    }
	}
	setDomain(values);
    }

    @Override
    public void fillFirstIntervalBoundaries(Schema schema, DBMSConnection db) {

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
	    throw new RuntimeException(e);
	}

	BigDecimal nFreshsBigDecimalTransl = new BigDecimal(this.numFreshsToInsert);
	
	// Guarantee positive encodings
	if( min.compareTo(BigDecimal.ZERO) < 0 ){
	    min = BigDecimal.ZERO;
	}
	
	BigDecimal proposedMax = min.add(nFreshsBigDecimalTransl); 

	if( proposedMax.compareTo(max) > 0 ){ 
	    max = proposedMax;
	}

	// Create the single initial interval
	List<ColumnPumper<BigDecimal>> involvedCols = new LinkedList<ColumnPumper<BigDecimal>>();
	involvedCols.add(this);
	Interval<BigDecimal> initialInterval = new BigDecimalInterval(this.getQualifiedName().toString(), this.getType(), this.numFreshsToInsert, involvedCols);

	initialInterval.updateMinEncodingAndValue(min.longValue());
	initialInterval.updateMaxEncodingAndValue(max.longValue());

	this.intervals.add(initialInterval);

	this.firstIntervalSet = true;
    }

    @Override
    public void addInterval(String name, long minEncoding, long maxEncoding) {
	Interval<BigDecimal> toAdd = new BigDecimalInterval(name, getType(), minEncoding, maxEncoding);
	toAdd.updateMinEncodingAndValue(minEncoding);
	toAdd.updateMaxEncodingAndValue(maxEncoding);
	this.addInterval(toAdd);
    }
}
