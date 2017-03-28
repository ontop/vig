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

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.columns.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.columns.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.columns.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.statistics.creators.table.TableStatisticsFinder;
import it.unibz.inf.data_pumper.core.statistics.creators.table.TableStatisticsFinderImpl;
import it.unibz.inf.data_pumper.persistence.LogToFile;
import it.unibz.inf.data_pumper.tables.Schema;
import it.unibz.inf.data_pumper.utils.UtilsMath;
import it.unibz.inf.vig_mappings_analyzer.core.utils.QualifiedName;

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

    private static long LINES_BUF_SIZE=10000; // Keep in RAM at most 10000 values for a table
    
    public DatabasePumperDB(){
	this.dbOriginal = DBMSConnection.getInstance();
	this.tStatsFinder = new TableStatisticsFinderImpl(dbOriginal);
	this.persistence = LogToFile.getInstance();
	this.restrictToTables = new ArrayList<>();
	this.restrictoToColumns = new ArrayList<>();
    }
    /**
     * 
     * @param dbOriginal
     * @param db
     * @param nRows
     */
    public void pumpDatabase(double scaleFactor){

	long startTime = System.currentTimeMillis();

	// Initialization
	List<Schema> schemas = new LinkedList<Schema>();
	List<ColumnPumper<? extends Object>> listColumns = new ArrayList<ColumnPumper<? extends Object>>();
	initListAllColumns(listColumns, scaleFactor);

	try {
	    establishColumnBounds(listColumns);
	    updateBoundariesWRTForeignKeys(listColumns);
	    checkIntervalsAssertions(listColumns); // FIXME Try with size 5, there might be a bug
	} catch (SQLException e) {
	    e.printStackTrace();
	    DatabasePumper.closeEverything();
	    throw new RuntimeException("Exception while pumping the database"); 
	}

	// Check
	for( String tableName : dbOriginal.getAllTableNames() ){
	    Schema schema = dbOriginal.getSchema(tableName);
	    schemas.add(schema);
	    checkPrimaryKeys(schema); 
	}

	// Generation
	generate(schemas, scaleFactor);
	
	long endTime = System.currentTimeMillis();

	logger.info("Database pumped in " + (endTime - startTime) + " msec.");
    }
    
    private void generateForColumn( ColumnPumper<? extends Object> cP, double scaleFactor ){
	int nRows = dbOriginal.getNRows(cP.getSchema().getTableName());

	nRows = (int) (nRows * scaleFactor);
	logger.info("Pump column "+cP.getQualifiedName().toString()+" of "+nRows+" values, please.");

	try {
	    persistence.openFile( cP.getQualifiedName().toString()+ ".csv" );

	    while( !fillDomainUpToNForColumn(cP, dbOriginal, LINES_BUF_SIZE) ){
		printColumnDomain(cP, LINES_BUF_SIZE);
		cP.resetDomain(); // Release memory 
	    }
	    // Print the last values
	    printColumnDomain(cP, nRows % LINES_BUF_SIZE);
	    cP.resetDomain(); // Release Memory
	} catch (IOException e) {
	    e.printStackTrace();
	    dbOriginal.close();
	    persistence.closeFile();
	    System.exit(1);
	}
	persistence.closeFile();
    }
    
    private void generateForTable(Schema schema, double scaleFactor){
	int nRows = dbOriginal.getNRows(schema.getTableName());

	nRows = (int) (nRows * scaleFactor);
	
	logger.info("Pump "+schema.getTableName()+" of "+nRows+" rows, please.");

	try {
	    persistence.openFile(schema.getTableName() + ".csv");

	    while( !fillDomainsUpToNForSchema(schema, dbOriginal, LINES_BUF_SIZE) ){
		printDomain(schema, LINES_BUF_SIZE);
		schema.resetColumnsDomains(); // Release memory 
	    }
	    // Print the last values
	    printDomain(schema, nRows % LINES_BUF_SIZE);
	    schema.resetColumnsDomains();
	} catch (IOException e) {
	    e.printStackTrace();
	    dbOriginal.close();
	    persistence.closeFile();
	    System.exit(1);
	}
	persistence.closeFile();
    }

    private void generate( List<Schema> schemas, double scaleFactor ) {
	
	class LocalUtils{
	    List<Schema> schemas;
	    
	    LocalUtils(List<Schema> schemas){
		this.schemas = schemas;
	    }
	    
	    Schema getSchemaFromName( QualifiedName name ){
		Schema result = null;
		for( Schema s : schemas ){
		    if( s.getTableName().equals( name.getTableName() ) ){
			result = s;
			break;
		    }
		}
		return result;
	    }
	    
	    ColumnPumper<? extends Object> getColumnPumperFromName( QualifiedName name ){
		ColumnPumper<? extends Object> result = null;
		Schema s = getSchemaFromName( name );
		result = s.getColumn( name.getColName() );
		
		return result;
	    }
	};
	
	LocalUtils utils = new LocalUtils(schemas);
	
	if( restrictToTables.size() == 0 && restrictoToColumns.size() == 0 ){
	    for( Schema schema : schemas ){
		generateForTable(schema, scaleFactor);
	    }
	}
	else{
	    if( restrictToTables.size() != 0 ){
		for( QualifiedName q : restrictToTables ){
		    generateForTable( utils.getSchemaFromName(q), scaleFactor );
		}
	    }
	    if( restrictoToColumns.size() != 0 ){
		for( QualifiedName q : restrictoToColumns ){
		    generateForColumn( utils.getColumnPumperFromName(q), scaleFactor );
		}
	    }
	}
    }
    /** This method simply verifies that the boundaries of each interval X
     *  are consistent w.r.t. the number of freshs to insert in X
     */
    private void checkIntervalsAssertions(List<ColumnPumper<? extends Object>> listColumns) {
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
     */
    private void checkPrimaryKeys(Schema schema) {
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

	if( freshs.isEmpty() ){
	    // No Pk
	    return;
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
		    throw new ProblematicCycleForPrimaryKeyException();
		}
	    }
	}
    }

    protected void updateBoundariesWRTForeignKeys(List<ColumnPumper<? extends Object>> listColumns) {
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
	} catch (IOException e) {
	    e.printStackTrace();
	    dbOriginal.close();
	    persistence.closeFile();
	    System.exit(1);
	}
    }
    
    private void printColumnDomain( ColumnPumper<? extends Object> col, long nToPrint ){

	for( int i = 0; i < nToPrint; ++i ){
	    String value = col.getNthInDomain(i);
	    persistence.appendLine(value);
	}
    }
    
    private void printDomain(Schema schema, long nToPrint) {

	List<ColumnPumper<? extends Object>> cols = schema.getColumns();
	StringBuilder line = new StringBuilder();

	for( int i = 0; i < nToPrint; ++i ){
	    line.delete(0, line.length());
	    for( int j = 0; j < cols.size(); ++j ){
		if( j != 0 ) line.append("`");

		ColumnPumper<? extends Object> col = cols.get(j);
		line.append(col.getNthInDomain(i));
	    }

	    String value = line.toString();
	    persistence.appendLine(value);
	}	
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
		System.err.println("initAllColumns: Col: " + c);
		listColumns.add(c);
		float dupsRatio = tStatsFinder.findDuplicatesRatio(s, c);
		c.setDuplicatesRatio(dupsRatio);

		float nullRatio = tStatsFinder.findNullRatio(s, c);
		c.setNullRatio(nullRatio);
		
		c.setScaleFactor(percentage);
		
		int nRows = dbOriginal.getNRows(s.getTableName());
		nRows = (int) (nRows * percentage);
		c.setNumRowsToInsert(nRows);
	    }
	}	
    }

    protected <T> void establishColumnBounds(List<ColumnPumper<? extends Object>> listColumns) throws SQLException{
	for( ColumnPumper<? extends Object> cP : listColumns ){
	    cP.fillFirstIntervalBoundaries(cP.getSchema(), dbOriginal);
	}
    }

    private void fillDomainsForSchema(Schema schema, DBMSConnection originalDb){
	for( ColumnPumper<? extends Object> column : schema.getColumns() ){
	    column.generateValues(schema, originalDb);
	}
    }

    public static double getScaleFactor(){
	return scaleFactor;
    }

    private boolean fillDomainUpToNForColumn( ColumnPumper<? extends Object> cP, DBMSConnection originalDb, long n ){
	boolean filled = false;
	
	try {
	    filled = cP.generateNValues(cP.getSchema(), originalDb, n);
	} catch (BoundariesUnsetException | ValueUnsetException e) {
	    e.printStackTrace();
	    DatabasePumper.closeEverything();
	    System.exit(1);
	}
	return filled;
    }
    
    private boolean fillDomainsUpToNForSchema(Schema schema, DBMSConnection originalDb, long n){
	
	boolean filled = false;
	
	for( ColumnPumper<? extends Object> column : schema.getColumns() ){
	    try {
		filled = column.generateNValues(schema, originalDb, n);
	    } catch (BoundariesUnsetException | ValueUnsetException e) {
		e.printStackTrace();
		DatabasePumper.closeEverything();
		System.exit(1);
	    }
	}
	return filled;
    }
};