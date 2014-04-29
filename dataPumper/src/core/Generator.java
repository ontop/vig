package core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import utils.Statistics;
import basicDatatypes.QualifiedName;
import basicDatatypes.Schema;
import columnTypes.ColumnPumper;
import core.TuplesPicker;
import connection.DBMSConnection;

public class Generator{
	
	protected Distribution distribution;
	protected DBMSConnection dbmsConn;
	private Random random;
	
	protected Map<String, Integer> mNumDupsRepetition;
	protected int maxNumDupsRepetition;

	/** Size of the window for duplicates picking **/
	public static int duplicatesWindowSize = 30000;
	public static int maxRepeatDuplicateWindowReads = 5;
	public static int freshDuplicatesSize = 1000;
	private static int maxPrimaryDuplicateValuesBufferSize = 5;
	private boolean pureRandom = false;
	
	/** It picks tuples that need to be repeated **/
	private TuplesPicker tP;
	
	private static Logger logger = Logger.getLogger(Generator.class.getCanonicalName());

	public Generator(DBMSConnection dbmsConn) {
		this.dbmsConn = dbmsConn;
		this.distribution = new Distribution(dbmsConn);
		this.random = new Random();
				
		mNumDupsRepetition = new HashMap<String, Integer>();
		maxNumDupsRepetition = 0;

		logger.setLevel(Level.INFO);
	}
	
	public List<Schema> pumpTable(int nRows, Schema schema){
		
		PreparedStatement stmt = null;
		List<Schema> tablesToChase = new LinkedList<Schema>(); // Return value
		
		/** mapping (vn -> v_1, ..., v_n-1) where (v1, ..., vn) is a pk **/
		Map<String, List<List<String>>> mFreshDuplicatesToDuplicatePks = new HashMap<String, List<List<String>>>(); 
		                                                                                                  
		List<String> freshDuplicates = new LinkedList<String>(); // Freshly generated strings from which duplicates can be chosen
		Map<String, List<String>> uncommittedFresh = new HashMap<String, List<String>>(); // Keeps track of uncommitted fresh values
		
		initNullRatios(schema);
		initDuplicateValues(schema, 0);
		initDuplicateRatios(schema);		
		initNumDupsRepetitionCounters();
		increaseChaseCycles(schema);
		
		for( ColumnPumper c : schema.getColumns() ){
			if( c.isPrimary() && c.referencedBy().size() > 0 ){
				uncommittedFresh.put(c.getName(), new ArrayList<String>());
			}
		}
		// templateInsert to be called AFTER the ratios initialization
		// because of the reordering of the columns
		String templateInsert = dbmsConn.createInsertTemplate(schema);
		
		
		stmt = dbmsConn.getPreparedStatement(templateInsert);
		logger.debug(templateInsert);

		// Disable auto-commit
		dbmsConn.setAutoCommit(false);

		// Init the tuplesPicker
		tP.init(schema);
		
		for( int j = 1; j <= nRows; ++j ){

			/** Keeps track of the DUPLICATE values chosen ---for the current row---
			 *  for columns part of a primary key  **/
			List<String> primaryDuplicateValues = new ArrayList<String>();

			for( ColumnPumper column : schema.getColumns() ){
				boolean terminate = pumpColumn(schema, column, stmt, j, nRows, primaryDuplicateValues, uncommittedFresh, 
						mFreshDuplicatesToDuplicatePks, freshDuplicates, tablesToChase);
				if( terminate )	return new ArrayList<Schema>(); // Stop immediately. Not possible to pump rows (foreign key violations)
			}
			try{
				stmt.addBatch();
			}catch(SQLException e){
				e.printStackTrace();
			}
			if( (j % 350000 == 0) ){ // Let's put a limit to the dimension of the stmt 
				try{
					stmt.executeBatch();	
					dbmsConn.commit();
				}catch(SQLException e){
					e.printStackTrace();
				}
				initUncommittedFresh(schema, uncommittedFresh);					
			}
			if( maxNumDupsRepetition > Generator.maxRepeatDuplicateWindowReads ){
				logger.info("Advancing the set of candidate duplicates");

				try{
					stmt.executeBatch();	
					dbmsConn.commit();
				}catch(SQLException e){
					e.printStackTrace();
				}

				initUncommittedFresh(schema, uncommittedFresh);
				initDuplicateValues(schema, j);
				initNumDupsRepetitionCounters();
				mFreshDuplicatesToDuplicatePks.clear();
				freshDuplicates.clear();
				System.gc();
			}
		}
		try{
			stmt.executeBatch();	
			dbmsConn.commit();
			stmt.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		dbmsConn.setAutoCommit(true);	
		resetState(schema); // Frees memory
		logger.info("Table '"+ schema.getTableName() + "' pumped with " + nRows +" rows.");
		
		return tablesToChase; 
	}
	
	private boolean pumpColumn(Schema schema, ColumnPumper column,
			PreparedStatement stmt, int nRows,
			int j,
			List<String> primaryDuplicateValues,
			Map<String, List<String>> uncommittedFresh,
			Map<String, List<List<String>>> mFreshDuplicatesToDuplicatePks,
			List<String> freshDuplicates, List<Schema> tablesToChase) {
		
		
		String toInsert = null;

		boolean stopChase = (column.referencesTo().size() > 0) && column.getMaximumChaseCycles() < column.getCurrentChaseCycle();
		if( stopChase && (column.getDuplicateRatio() > 0 || !column.isPrimary()) ){
			column.setDuplicateRatio(1); // DO NOT generate fresh values. Fresh values trigger new chase steps.
		}	
		if( j == nRows && (toInsert = column.getNextChased(dbmsConn, schema)) != null && 
				(!column.isPrimary() || !uncommittedFresh.get(column.getName()).contains(toInsert)) ){
			dbmsConn.setter(stmt, column.getIndex(), column.getType(), toInsert); 
			addToUncommittedFresh(uncommittedFresh, column, toInsert);
			if( column.hasNextChase() )	++nRows; // I haven't finished yet to insert chased values.
		}
		else if( column.getDuplicateRatio() > random.nextFloat() ){
			if (column.getDuplicateRatio() + column.getNullRatio() > random.nextFloat()) {
				putNull(schema, column, stmt);
			} else
			putDuplicate(schema, column, primaryDuplicateValues, 
					mFreshDuplicatesToDuplicatePks, freshDuplicates, stmt, uncommittedFresh, tablesToChase);	
		}
		else if( ( 0.8 > random.nextFloat() ) && 
				(toInsert = column.getNextChased(dbmsConn, schema) ) != null && 
				(!column.isPrimary() || !uncommittedFresh.get(column.getName()).contains(toInsert)) 
				){
			dbmsConn.setter(stmt, column.getIndex(), column.getType(), toInsert); 
			addToUncommittedFresh(uncommittedFresh, column, toInsert);
		}
		else if( stopChase ){
			// We cannot take a chase value, neither we can pick a duplicate. The only way out is 
			// to take the necessary number of elements (non-duplicate with this column) from the referenced column(s)
			toInsert = column.getFromReferenced(dbmsConn, schema);
			
			if( toInsert == null ){
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return true;
			}
			dbmsConn.setter(stmt, column.getIndex(), column.getType(), toInsert);
		}
		else{ // Add a random value						
			Statistics.addInt(schema.getTableName()+"."+column.getName()+" fresh values", 1);
			String generatedRandom = putFreshRandom(column, stmt, uncommittedFresh);
			updateFreshDuplicates(schema, column, generatedRandom, primaryDuplicateValues, freshDuplicates, mFreshDuplicatesToDuplicatePks);
			updateTablesToChase(column, tablesToChase);
		}
		return false;
	}

	private String putFreshRandom(ColumnPumper column, PreparedStatement stmt, Map<String, List<String>> uncommittedFresh) {

		String generatedRandom = column.getNextFreshValue();
								
		if( generatedRandom == null ) logger.error("NULL fresh"); 
		logger.debug("Adding Fresh");
		dbmsConn.setter(stmt, column.getIndex(), column.getType(), generatedRandom);
		
		if( column.isPrimary() && column.referencedBy().size() > 0){ // Chased values might be duplicate
			addToUncommittedFresh(uncommittedFresh, column, generatedRandom);
		}
		
		return generatedRandom;
	}

	private void updateFreshDuplicates(Schema schema, ColumnPumper column,
			String generatedRandom, List<String> primaryDuplicateValues,
			List<String> freshDuplicates,
			Map<String, List<List<String>>> mFreshDuplicatesToDuplicatePks) {
		
		
		// If (c_1,..,c_n, column) is a primary key AND
		// from_dup(c_i), where 1 <= i <= n, THEN keep track
		// of column -> (c_1, ..., c_n)
		if( column.isPrimary() && (primaryDuplicateValues.size() == schema.getPks().size() - 1 ) ){
			if( freshDuplicates.size() < Generator.freshDuplicatesSize ){
				List<List<String>> listOfPrimaryDuplicateValues = new ArrayList<List<String>>();
				listOfPrimaryDuplicateValues.add(primaryDuplicateValues);
				mFreshDuplicatesToDuplicatePks.put(generatedRandom, listOfPrimaryDuplicateValues);
				
				Statistics.addInt(schema.getTableName()+"."+column.getName()+"___adds_to_mFreshDuplicatesToDuplicatePks", 1);
			}
		}
		
		if( schema.getPks().size() > 0 && column.getIndex() == schema.getPks().get(schema.getPks().size()-1).getIndex() ){
			if( freshDuplicates.size() < Generator.freshDuplicatesSize ){
				freshDuplicates.add(generatedRandom);
				
				Statistics.addInt(schema.getTableName()+"."+column.getName()+"___adds_to_freshDuplicates", 1);
			}
		}
		
	}

	private void initUncommittedFresh(Schema schema,
			Map<String, List<String>> uncommittedFresh) {
		
		for( String key : uncommittedFresh.keySet() ){
			if( !uncommittedFresh.get(key).isEmpty() ){
				uncommittedFresh.get(key).clear();
				schema.getColumn(key).refillCurChaseSet(dbmsConn, schema);
			}
		}
		
	}

	private void addToUncommittedFresh(
			Map<String, List<String>> uncommittedFresh, ColumnPumper column,
			String generatedRandom) {
		
		if( uncommittedFresh.containsKey(column.getName()) ){
			if( generatedRandom.endsWith(".0") || generatedRandom.endsWith(".00") ) //TODO Nasty
				generatedRandom = generatedRandom.substring(0, generatedRandom.lastIndexOf('.'));
			uncommittedFresh.get(column.getName()).add(generatedRandom);
		}
		else{
			if( generatedRandom.endsWith(".0") || generatedRandom.endsWith(".00") )
				generatedRandom = generatedRandom.substring(0, generatedRandom.lastIndexOf('.'));
			uncommittedFresh.put(column.getName(), new ArrayList<String>());
			uncommittedFresh.get(column.getName()).add(generatedRandom);
		}
	}
	
	protected void resetState(Schema schema) {
		resetDuplicateValues(schema);
		resetColumns(schema);
		System.gc();
	}
	
	protected void resetColumns(Schema schema) {
		for( ColumnPumper c : schema.getColumns() )
			c.reset();
	}
	
	protected void updateTablesToChase(ColumnPumper column, List<Schema> tablesToChase) {
		// New values inserted imply new column to chase
		for( QualifiedName qN : column.referencesTo() ){
			if( !tablesToChase.contains(dbmsConn.getSchema(qN.getTableName())) ){
				tablesToChase.add(dbmsConn.getSchema(qN.getTableName()));
			}
		}
	}
	
	protected void initDuplicateValues(Schema schema, int insertedRows) {
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDuplicates(dbmsConn, schema, insertedRows);
		}
		System.gc();
	}
	
	protected void initNullRatios(Schema schema){
		if( pureRandom ){
			for( ColumnPumper c : schema.getColumns() ){
				c.setNullRatio((float) 0.0);
			}
			return;
		}
		for( ColumnPumper c : schema.getColumns() ){
			c.setNullRatio(findNullRatio(schema, c));
		}
		//schema.sortColumnsAccordingToDupRatios();
	}
	
	public float findNullRatio(Schema s, ColumnPumper column){
		float ratio = 0;
		ratio = distribution.nullRatioNaive(column.getName(), s.getTableName());
		return ratio;
	}
	protected void initDuplicateRatios(Schema schema){
		if( pureRandom ){
			for( ColumnPumper c : schema.getColumns() ){
				c.setDuplicateRatio((float) 0.0);
			}
			return;
		}
		for( ColumnPumper c : schema.getColumns() ){
			c.setDuplicateRatio(findDuplicateRatio(schema, c));
		}
		schema.sortColumnsAccordingToDupRatios();
	}
	
	protected void resetDuplicateValues(Schema schema){
		for( ColumnPumper c : schema.getColumns()){
			c.reset();
		}
	}
	
	public float findDuplicateRatio(Schema s, ColumnPumper column){
		float ratio = 0; // Ratio of the duplicates
		// If generating fresh values will lead to a chase, and the maximum number of chases is reached
		if( (column.referencesTo().size() > 0) && column.getMaximumChaseCycles() < column.getCurrentChaseCycle() ){
			// It has NOT to produce fresh values
			// However, if the field is a allDifferent() then I cannot close the chase with a duplicate
			if( column.isAllDifferent() ){
				return 0; // Either no duplicates or no row at all
			}
			// Else, it is ok to close the cycle with a duplicate
			ratio = 1;
		}
		else{
			// First of all, I need to understand the distribution of duplicates. Window analysis!
			ratio = distribution.naiveStrategy(column.getName(), s.getTableName());
			Statistics.setFloat(s.getTableName()+"."+column.getName()+" dups ratio", ratio);
		}
		return ratio > 0.95 ? 1 : ratio < 0.05 ? 0 : ratio;
	}	
	
	protected void putNull(Schema schema, ColumnPumper column, PreparedStatement stmt){
		String toAdd = null;
		dbmsConn.setter(stmt, column.getIndex(), column.getType(), toAdd);
	}

	private String pickNextDupFromOldValues(Schema schema, ColumnPumper column) {
		
		String result = column.pickNextDupFromDuplicatesToInsert();
		
		if( result == null ){
			if( mNumDupsRepetition.containsKey(column.getName()) ){
				if( maxNumDupsRepetition < (mNumDupsRepetition.get(column.getName()) + 1) ){
					maxNumDupsRepetition = mNumDupsRepetition.get(column.getName()) + 1;
				}
				mNumDupsRepetition.put(column.getName(), mNumDupsRepetition.get(column.getName()) + 1);
			}
			else{
				mNumDupsRepetition.put(column.getName(), 1);
				if( maxNumDupsRepetition < 1 ) ++maxNumDupsRepetition;
			}
			column.beforeFirstDuplicatesToInsert();
			if( (result = column.pickNextDupFromDuplicatesToInsert()) == null ){
				logger.error(column.toString() + ": No duplicate element can be forced");
			}
		}	
		return result;
	}

	protected void initNumDupsRepetitionCounters(){
		maxNumDupsRepetition = 0;
		mNumDupsRepetition.clear();
	}
	
	protected void putDuplicate(Schema schema, ColumnPumper column, List<String> primaryDuplicateValues, 
			Map<String, List<List<String>>> mFreshDuplicatesToDuplicatePks,
			List<String> freshDuplicates, PreparedStatement stmt,
			Map<String, List<String>> uncommittedFresh,
			List<Schema> tablesToChase){
		
		String toInsert = null;
		
		logger.debug("Put a duplicate into "+schema.getTableName() + "." + column.getName());
		
		// If, in all columns but one of the primary key I've put duplicates, 
		// pay attention to how you pick the last column. You might generate
		// a duplicate row if you do not do it correctly
		if( (primaryDuplicateValues.size() == schema.getPks().size() - 1) && column.isPrimary() ){
			
			long start = System.currentTimeMillis();
			// Search among uncommitted fresh values
			String toAdd = null;
			int i = 0;
			while( toAdd == null && i < freshDuplicates.size() ){
				String suitableDup = freshDuplicates.get(i);
				if( !mFreshDuplicatesToDuplicatePks.containsKey(suitableDup) ){
					toAdd = suitableDup;
					List<List<String>> listOfPrimaryDuplicateValues = new ArrayList<List<String>>();
					listOfPrimaryDuplicateValues.add(primaryDuplicateValues);
					mFreshDuplicatesToDuplicatePks.put(toAdd, listOfPrimaryDuplicateValues);
				}
				else{
					// Now we do an expensive foreach. Still, less expensive than going into the database
					boolean duplicatePKey = false;
					for( List<String> primaryDuplicateValuesOldTuple : mFreshDuplicatesToDuplicatePks.get(suitableDup) ){ 						
						if( primaryDuplicateValuesOldTuple.equals(primaryDuplicateValues) ){
							duplicatePKey = true;
							break;
						}
					}
					if(!duplicatePKey){
						toAdd = suitableDup;
						mFreshDuplicatesToDuplicatePks.get(suitableDup).add(primaryDuplicateValues);
						
						// Size check
						if( mFreshDuplicatesToDuplicatePks.get(suitableDup).size() > Generator.maxPrimaryDuplicateValuesBufferSize ){
							freshDuplicates.remove(i);
							mFreshDuplicatesToDuplicatePks.remove(suitableDup);
//							System.gc(); // TODO How expensive is this?
						}
					}
				}
				++i;
			}
			if( toAdd != null ){
				toInsert = toAdd;
				Statistics.addInt("Number_Of_Successful_Dup_Pick_From_Fresh_Values_For_Last_Element_Of_Pk", 1);
				
				dbmsConn.setter(stmt, column.getIndex(), column.getType(), toInsert);
			}
			else{ 
				
				// TODO Expensive Strategy
				
				// Cannot find an element among fresh, try with random
				
				Statistics.addInt(schema.getTableName()+"."+column.getName()+" forced fresh values", 1);
				Statistics.addInt(schema.getTableName()+"."+column.getName()+" fresh values", 1);
				
				toInsert = putFreshRandom(column, stmt, uncommittedFresh);
				
				updateFreshDuplicates(schema, column, toInsert, primaryDuplicateValues, freshDuplicates, mFreshDuplicatesToDuplicatePks);
				updateTablesToChase(column, tablesToChase);
			}							
			long end = System.currentTimeMillis();
			
			Statistics.addTime("Time_spent_picking_a_problematic_duplicate_for_a_primary_key", end - start);
		}else{
			logger.debug("Adding a duplicate for "+ (new QualifiedName(schema.getTableName(), column.getName())).toString());
			Statistics.addInt(schema.getTableName()+"."+column.getName()+" Adding a duplicate from initial database values", 1);
			
			String nextDuplicate = pickNextDupFromOldValues(schema, column);
			
			if( nextDuplicate == null ) logger.error("NULL duplicate"); 
			toInsert = nextDuplicate;
			
			dbmsConn.setter(stmt, column.getIndex(), column.getType(), nextDuplicate); // Ensures to put all chased elements, in a uniform way w.r.t. other columns
			
			if( column.isPrimary() ){
				primaryDuplicateValues.add(nextDuplicate);
			}
		}
	}
	private void increaseChaseCycles(Schema schema) {
		for( ColumnPumper column : schema.getColumns() ){
			column.incrementCurrentChaseCycle();
		}
	}

	public void setPureRandomGeneration() {
		pureRandom = true;
	}
}