package it.unibz.inf.data_pumper.core.main;

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

import it.unibz.inf.data_pumper.basic_datatypes.QualifiedName;
import it.unibz.inf.data_pumper.basic_datatypes.Schema;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.connection.exceptions.InstanceNullException;
import it.unibz.inf.data_pumper.core.exception.ProblematicCycleForPrimaryKeyException;
import it.unibz.inf.data_pumper.core.table.statistics.TableStatisticsFinder;
import it.unibz.inf.data_pumper.core.table.statistics.TableStatisticsFinderImpl;
import it.unibz.inf.data_pumper.core.table.statistics.exception.TooManyValuesException;
import it.unibz.inf.data_pumper.persistence.LogToFile;
import it.unibz.inf.data_pumper.utils.UtilsMath;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

public class DatabasePumperDB extends DatabasePumper {

    protected DBMSConnection dbOriginal;

    protected static Logger logger = Logger.getLogger(DatabasePumperDB.class.getCanonicalName());	

    protected final TableStatisticsFinder tStatsFinder;

    protected final LogToFile persistence;

    protected static double scaleFactor;

    public DatabasePumperDB(){
	try {
	    this.dbOriginal = DBMSConnection.getInstance();
	} catch (InstanceNullException e) {
	    e.printStackTrace();
	    this.persistence.closeFile();
	    System.exit(1);
	}
	this.tStatsFinder = new TableStatisticsFinderImpl(dbOriginal);
	this.persistence = LogToFile.getInstance();

    }
    /**
     * 
     * @param dbOriginal
     * @param db
     * @param nRows
     */
    public void pumpDatabase(double scaleFactor){


	long startTime = System.currentTimeMillis();

	List<Schema> schemas = new LinkedList<Schema>();
	List<ColumnPumper<? extends Object>> listColumns = new ArrayList<ColumnPumper<? extends Object>>();
	initListAllColumns(listColumns, scaleFactor);

	try {
	    establishColumnBounds(listColumns);
	    updateBoundariesWRTForeignKeys(listColumns);
	    checkIntervalsAssertions(listColumns);
	} catch (ValueUnsetException | InstanceNullException | BoundariesUnsetException | DebugException | SQLException e) {
	    e.printStackTrace();
	    DatabasePumper.closeEverything();
	    System.exit(1);
	}

	for( String tableName : dbOriginal.getAllTableNames() ){
	    Schema schema = dbOriginal.getSchema(tableName);
	    schemas.add(schema);
	    try {
		checkPrimaryKeys(schema); 
	    } catch (ValueUnsetException | DebugException | BoundariesUnsetException e) {
		e.printStackTrace();
		DatabasePumper.closeEverything();
		System.exit(1);
	    }
	}

	for( Schema schema : schemas ){

	    int nRows = dbOriginal.getNRows(schema.getTableName());

	    nRows = (int) (nRows * scaleFactor);
	    logger.info("Pump "+schema.getTableName()+" of "+nRows+" rows, please.");

	    fillDomainsForSchema(schema, dbOriginal);			
	    printDomain(schema);

	    schema.reset();
	}
	long endTime = System.currentTimeMillis();

	logger.info("Database pumped in " + (endTime - startTime) + " msec.");
    }
    
    /** This method simply verifies that the boundaries of each interval X
     *  are consistent w.r.t. the number of freshs to insert in X
     * @throws DebugException 
     * @throws BoundariesUnsetException 
     */
    private void checkIntervalsAssertions(List<ColumnPumper<? extends Object>> listColumns) throws BoundariesUnsetException, DebugException {
	for( ColumnPumper<? extends Object> cP : listColumns ){
	    for( Interval<?> interval : cP.getIntervals() ){
		if( interval.getNFreshsToInsert() != interval.getMaxEncoding() - interval.getMinEncoding() )
		    throw new DebugException("Inconsistent Interval Detected! : Interval " + interval.toString());
	    }
	}
    }


    /**
     * for each primary key (col1,col2,...,coln) of table schema, 
     * check whether lcm(col1.nFreshs, ..., coln.nFreshs) > nFreshsToInsert
     * 
     * @param schema
     * @throws ValueUnsetException 
     * @throws BoundariesUnsetException 
     * @throws DebugException 
     */
    private void checkPrimaryKeys(Schema schema) throws ValueUnsetException, DebugException, BoundariesUnsetException {
	class LocalUtils{
	    long[] limitValues = {10,100,1000,10000,100000,1000000,10000000,1000000000}; // TODO Something nicer

	    boolean isLimit(long n){
		boolean result = false;
		for( int i = 0; i < limitValues.length; ++i ){
		    if( n == limitValues[i] ){
			result = true;
		    }
		}
		return result;
	    }
	}

	List<ColumnPumper<? extends Object>> pk = schema.getPk();
	List<Number> freshs = new ArrayList<Number>();

	LocalUtils lu = new LocalUtils();

	for( ColumnPumper<? extends Object> cP : pk ){
	    freshs.add(cP.getNumFreshsToInsert());
	}

	long lcm = UtilsMath.lcm(freshs);

	boolean violation = false;
	for( ColumnPumper<? extends Object> cP : pk ){
	    long nValuesToInsert = cP.getNumRowsToInsert();
	    if( nValuesToInsert > lcm ){
		violation = true;
		break;
	    }
	}
	if( violation ){ // We broke out
	    boolean noneEmpty = true;
	    for( ColumnPumper<? extends Object> cP : pk ){
		if( cP.referencesTo().isEmpty() ){
		    if( !lu.isLimit(cP.getNumFreshsToInsert()) ){ 
			noneEmpty = false;
			cP.incrementNumFreshs();
			checkPrimaryKeys(schema);
			break;
		    }
		}
	    }
	    if( noneEmpty ){
		noneEmpty = true;
		for( ColumnPumper<? extends Object> cP : pk ){
		    if( cP.referencedBy().isEmpty() ){
			noneEmpty = false;
			cP.decrementNumFreshs();
			checkPrimaryKeys(schema);
			break;
		    }
		}
		if( noneEmpty ){
		    try{
			throw new ProblematicCycleForPrimaryKeyException();
		    }catch(ProblematicCycleForPrimaryKeyException e){
			e.printStackTrace();
			DatabasePumper.closeEverything();
			System.exit(1);
		    }
		}
	    }
	}
    }

    protected void updateBoundariesWRTForeignKeys(List<ColumnPumper<? extends Object>> listColumns) throws InstanceNullException, BoundariesUnsetException, DebugException, ValueUnsetException {
	Queue<ColumnPumper<? extends Object>> toUpdateBoundaries = new LinkedList<ColumnPumper<? extends Object>>();
	toUpdateBoundaries.addAll(listColumns);

	while( !toUpdateBoundaries.isEmpty() ){
	    ColumnPumper<? extends Object> first = toUpdateBoundaries.remove();
	    
	    if( first.getIntervals().size() != 1 ) continue;
	    
	    long firstMinEncoding = first.getIntervals().get(0).getMinEncoding();
	    for( QualifiedName referredName : first.referencesTo() ){
		ColumnPumper<? extends Object> referred = DBMSConnection.getInstance().getSchema(referredName.getTableName()).getColumn(referredName.getColName());
		long refMinEncoding = referred.getIntervals().get(0).getMinEncoding();
		if( firstMinEncoding > refMinEncoding ){
		    Interval<? extends Object> interval = first.getIntervals().get(0);
		    interval.updateMinEncodingAndValue(refMinEncoding);
		    interval.updateMaxEncodingAndValue(refMinEncoding + interval.getNFreshsToInsert());
		    // Update the boundaries for all the kids
		    for( QualifiedName kidName : first.referencedBy() ){
			ColumnPumper<? extends Object> kid = (ColumnPumper<? extends Object>) DBMSConnection.getInstance().getSchema(kidName.getTableName()).getColumn(kidName.getColName());
			toUpdateBoundaries.add(kid);
		    }
		}
	    }
	}
    }
    private void printDomain(Schema schema) {

	List<ColumnPumper<? extends Object>> cols = schema.getColumns();

	StringBuilder line = new StringBuilder();
	try {
	    persistence.openFile(schema.getTableName() + ".csv");
	    for( int i = 0; i < cols.get(0).getNumRowsToInsert(); ++i ){
		line.delete(0, line.length());
		for( int j = 0; j < cols.size(); ++j ){
		    if( j != 0 ) line.append("`");

		    ColumnPumper<? extends Object> col = cols.get(j);
		    line.append(col.getNthInDomain(i));
		}

		String value = line.toString();

		persistence.appendLine(value);
	    }
	} catch (ValueUnsetException | IOException e) {
	    e.printStackTrace();
	    dbOriginal.close();
	    persistence.closeFile();
	    System.exit(1);
	}
	persistence.closeFile();

    }
    /**
     * 
     * This method puts in listColumns all the columns and initializes, for each of them, 
     * the duplicates ratio and the number of values that need to be inserted. 
     * 
     * Finally, it starts establishing the column bounds
     * 
     * @param listColumns The output
     * @param percentage The increment ratio
     */
    private void initListAllColumns(List<ColumnPumper<? extends Object>> listColumns, double percentage) {
	for( String tableName : dbOriginal.getAllTableNames()){
	    Schema s = dbOriginal.getSchema(tableName);
	    for( ColumnPumper<? extends Object> c : s.getColumns() ){
		listColumns.add(c);
		float dupsRatio = tStatsFinder.findDuplicatesRatio(s, c);
		c.setDuplicatesRatio(dupsRatio);

		float nullRatio = tStatsFinder.findNullRatio(s, c);
		c.setNullRatio(nullRatio);

		int nRows = dbOriginal.getNRows(s.getTableName());
		nRows = (int) (nRows * percentage);
		try {
		    c.setNumRowsToInsert(nRows);
		} catch (TooManyValuesException e) {
		    e.printStackTrace();
		    dbOriginal.close();
		    System.exit(1);
		}
	    }
	}	
    }

    protected <T> void establishColumnBounds(List<ColumnPumper<? extends Object>> listColumns) throws ValueUnsetException, DebugException, InstanceNullException, SQLException{
	for( ColumnPumper<? extends Object> cP : listColumns ){
	    cP.fillFirstIntervalBoundaries(cP.getSchema(), dbOriginal);
	}
    }

    private void fillDomainsForSchema(Schema schema, DBMSConnection originalDb){
	for( ColumnPumper<? extends Object> column : schema.getColumns() ){
	    try {
		column.generateValues(schema, originalDb);
	    } catch (BoundariesUnsetException | ValueUnsetException | DebugException e) {
		e.printStackTrace();
		DatabasePumper.closeEverything();
		System.exit(1);
	    }
	}
    }

    public static double getScaleFactor(){
	return scaleFactor;
    }
};