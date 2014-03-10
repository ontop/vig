package core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import basicDatatypes.Template;
import columnTypes.Column;
import connection.DBMSConnection;

public class Generator4 extends Generator{
	
	protected Distribution distribution;
	protected DBMSConnection dbmsConn;
	private Random random;
	
	protected Map<String, ResultSet> referencedValues; 
	protected Map<String, ResultSet> duplicateValues;     // Pointers to ResultSets storing duplicate values for each column
	protected Map<String, Integer> mNumDupsRepetition;
	protected int maxNumDupsRepetition;

	public static int duplicatesWindowSize = 80000;
	public static int maxRepeatDuplicateWindowReads = 5;
	public static int freshDuplicatesSize = 20;
	
	private static Logger logger = Logger.getLogger(Generator4.class.getCanonicalName());

	public Generator4(DBMSConnection dbmsConn) {
		this.dbmsConn = dbmsConn;
		this.distribution = new Distribution(dbmsConn);
		this.random = new Random();
		
		duplicateValues = new HashMap<String, ResultSet>();
		
		referencedValues = new HashMap<String, ResultSet>();
		mNumDupsRepetition = new HashMap<String, Integer>();
		maxNumDupsRepetition = 0;

		logger.setLevel(Level.INFO);
	}
	
	public List<Schema> pumpTable(int nRows, Schema schema){
		
		logger.setLevel(Level.INFO);
		initDuplicateValues(schema, 0);
		initDuplicateRatios(schema);
		
		PreparedStatement stmt = null;
		String templateInsert = dbmsConn.createInsertTemplate(schema);
		
		List<Schema> tablesToChase = new LinkedList<Schema>();
		referencedValues.clear();
		initNumDupsRepetitionCounters();
		
		Map<String, List<String>> mFreshDuplicatesToDuplicatePks = new HashMap<String, List<String>>();
		Queue<String> freshDuplicates = new LinkedList<String>();
		
		Map<String, List<String>> uncommittedFresh = new HashMap<String, List<String>>(); // Keeps track of uncommitted fresh values
		for( Column c : schema.getColumns() ){
			if( c.isPrimary() && c.referencedBy().size() > 0 ){
				uncommittedFresh.put(c.getName(), new ArrayList<String>());
			}
		}
		increaseChaseCycles(schema);
		try {
			stmt = dbmsConn.getPreparedStatement(templateInsert);
			logger.debug(templateInsert);
			
			// Disable auto-commit
			dbmsConn.setAutoCommit(false);

			// Idea: I can say that nRows = number of things that need to be chased, when the maximum
			// cycle is reached. To test this out			
			for( int j = 1; j <= nRows; ++j ){
				
				List<String> primaryDuplicateValues = new ArrayList<String>();
				for( Column column : schema.getColumns() ){
					
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
						// to tale the necessary number of elements (non-duplicate with this column) from the referenced column(s)
						toInsert = pickFromReferenced(schema, column, referencedValues);
						
						if( toInsert == null ){
							stmt.close();
							return new ArrayList<Schema>();
						}
						
						dbmsConn.setter(stmt, column.getIndex(), column.getType(), toInsert);
					}
					else{ // Add a random value						
						Statistics.addInt(schema.getTableName()+"."+column.getName()+" fresh values", 1);
						String generatedRandom = putFreshRandom(column, stmt, uncommittedFresh);
						updateFreshDuplicates(schema, column, generatedRandom, primaryDuplicateValues, freshDuplicates, mFreshDuplicatesToDuplicatePks);
						updateTablesToChase(column, tablesToChase);
					}
				}
				
				stmt.addBatch();
				if( (j % 300000 == 0) ){ // Let's put a limit to the dimension of the stmt 
					
					stmt.executeBatch();	
					dbmsConn.commit();
					
					initUncommittedFresh(schema, uncommittedFresh);					
				}
				if( maxNumDupsRepetition > Generator4.maxRepeatDuplicateWindowReads ){
					logger.info("Advancing the set of candidate duplicates");
					
					stmt.executeBatch();	
					dbmsConn.commit();
					
					initUncommittedFresh(schema, uncommittedFresh);
					initDuplicateValues(schema, j);
					initNumDupsRepetitionCounters();
					mFreshDuplicatesToDuplicatePks.clear();
					freshDuplicates.clear();
					System.gc();
				}
			} 
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

	private String putFreshRandom(Column column, PreparedStatement stmt, Map<String, List<String>> uncommittedFresh) {

		String generatedRandom = column.getNextFreshValue();
								
		if( generatedRandom == null ) logger.error("NULL fresh"); 
		logger.debug("Adding Fresh");
		dbmsConn.setter(stmt, column.getIndex(), column.getType(), generatedRandom);
		
		if( column.isPrimary() && column.referencedBy().size() > 0){ // Chased values might be duplicate
			addToUncommittedFresh(uncommittedFresh, column, generatedRandom);
		}
		
		return generatedRandom;
	}

	private void updateFreshDuplicates(Schema schema, Column column,
			String generatedRandom, List<String> primaryDuplicateValues,
			Queue<String> freshDuplicates,
			Map<String, List<String>> mFreshDuplicatesToDuplicatePks) {
		
		
		// Let's do this
		if( column.isPrimary() && (primaryDuplicateValues.size() == schema.getPks().size() - 1 ) ){
			if( freshDuplicates.size() < Generator4.freshDuplicatesSize ){
				mFreshDuplicatesToDuplicatePks.put(generatedRandom, primaryDuplicateValues);
				
				Statistics.addInt(schema.getTableName()+"."+column.getName()+"___adds_to_mFreshDuplicatesToDuplicatePks", 1);
			}
		}
		
		if( schema.getPks().size() > 0 && column.getIndex() == schema.getPks().get(schema.getPks().size()-1).getIndex() ){
			if( freshDuplicates.size() < Generator4.freshDuplicatesSize ){
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
			Map<String, List<String>> uncommittedFresh, Column column,
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
	
	/**
	 * This method, for the moment, assumes that it is possible
	 * to reference AT MOST 1 TABLE.
	 * NOT VERY EFFICIENT. If slow, then refactor as the others
	 * @param schema
	 * @param column
	 * @return
	 */
	protected String pickFromReferenced(Schema schema, Column column, Map<String, ResultSet> referencedValues) {
		
		String result = null;
		
		if( !referencedValues.containsKey(column.getName()) ){
			
			// SELECT referencedColumn FROM referencedTable WHERE referencedColumn NOT IN (select thisColumn from thisTable)
			Template templ = new Template("SELECT DISTINCT ? FROM ? WHERE ? NOT IN (SELECT ? FROM ?)");
			
			if( !column.referencesTo().isEmpty() ){
				QualifiedName refQN = column.referencesTo().get(0);
				templ.setNthPlaceholder(1, refQN.getColName());
				templ.setNthPlaceholder(2, refQN.getTableName());
				templ.setNthPlaceholder(3, refQN.getColName());
				templ.setNthPlaceholder(4, column.getName());
				templ.setNthPlaceholder(5, schema.getTableName());
			}
			else{
				logger.error("Cannot access a referenced field");
			}
			
			PreparedStatement stmt = dbmsConn.getPreparedStatement(templ);
			try {
				referencedValues.put(column.getName(), stmt.executeQuery());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		ResultSet rs = referencedValues.get(column.getName());
		
		try {
			if( !rs.next() ){
				logger.debug("Not possible to add a non-duplicate value. No row will be added");
//				throw new SQLException();
			}else
				result = rs.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	protected void resetState(Schema schema) {
		resetDuplicateValues();
		resetColumns(schema);
		System.gc();
	}
	
	protected void resetColumns(Schema schema) {
		for( Column c : schema.getColumns() )
			c.reset();
	}
	
	protected void updateTablesToChase(Column column, List<Schema> tablesToChase) {
		// New values inserted imply new column to chase
		for( QualifiedName qN : column.referencesTo() ){
			if( !tablesToChase.contains(dbmsConn.getSchema(qN.getTableName())) ){
				tablesToChase.add(dbmsConn.getSchema(qN.getTableName()));
			}
		}
	}
	
	protected void initDuplicateValues(Schema schema, int insertedRows) {
		resetDuplicateValues();
		
		for( Column c : schema.getColumns() ){
			ResultSet rs = fillDuplicates(c, schema.getTableName(), insertedRows);
			try {
				if( duplicateValues.containsKey(c.getName()) )
					duplicateValues.get(c.getName()).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			duplicateValues.put(c.getName(), rs);
		}
		System.gc();
	}
	
	protected void initDuplicateRatios(Schema schema){
		for( Column c : schema.getColumns() ){
			c.setDuplicateRatio(findDuplicateRatio(schema, c));
		}
	}
	
	protected void resetDuplicateValues(){
		for( String key : duplicateValues.keySet() ){
			try {
				duplicateValues.get(key).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		duplicateValues.clear();
	}
	
	protected ResultSet fillDuplicates(Column column, String tableName, int insertedRows) {
		
		ResultSet result = null;
		int startIndex = insertedRows - Generator4.duplicatesWindowSize > 0 ? insertedRows - Generator4.duplicatesWindowSize : 0;
		
		String queryString = null;
		
		if( column.isGeometric() ){
			queryString = "SELECT AsWKT(" + column.getName() + ") FROM " + tableName + " "
					+ " WHERE AsWKT(" + column.getName() + ") IS NOT NULL LIMIT " + startIndex + ", " + Generator4.duplicatesWindowSize;
		}
		else{
			queryString = "SELECT "+column.getName()+ " FROM "+tableName+" WHERE "+column.getName()+" IS NOT NULL LIMIT "+ startIndex +", "+Generator4.duplicatesWindowSize;
		}
		try{
			PreparedStatement stmt = dbmsConn.getPreparedStatement(queryString);
			result = stmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public float findDuplicateRatio(Schema s, Column column){
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
		return ratio > 0.9 ? 1 : ratio < 0.1 ? 0 : ratio;
	}	
	
	protected String pickNextDupFromOldValues(Schema schema, Column column, boolean force) {
		
		ResultSet duplicatesToInsert = duplicateValues.get(column.getName());
		if(duplicatesToInsert == null){
			logger.error("duplicateValues was not correctly initialized");
			return null;
		}
		String result = null;
		
		try {
			
			boolean hasNext = duplicatesToInsert.next();
			
			if( !hasNext && force ){
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
				duplicatesToInsert.beforeFirst();
				if( !duplicatesToInsert.next() ){
					logger.error(column.toString() + ": No duplicate element can be forced");
				}
			}
			else if( !hasNext && !force ){
				return null;
			}
			String retrieved = duplicatesToInsert.getString(1);
			if( retrieved == null ) logger.error(schema.getTableName() + "." + column.getName() +": Retrieved is null");
			result = duplicatesToInsert.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	protected void initNumDupsRepetitionCounters(){
		maxNumDupsRepetition = 0;
		mNumDupsRepetition.clear();
	}
	
	protected void putDuplicate(Schema schema, Column column, List<String> primaryDuplicateValues, 
			Map<String, List<String>> mFreshDuplicatesToDuplicatePks,
			Queue<String> freshDuplicates, PreparedStatement stmt,
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
			while( toAdd == null && !freshDuplicates.isEmpty() ){
				String suitableDup = freshDuplicates.poll();
				if( !mFreshDuplicatesToDuplicatePks.containsKey(suitableDup) )
					toAdd = suitableDup;
				else{
					if( !mFreshDuplicatesToDuplicatePks.get(suitableDup).equals(primaryDuplicateValues) )
						toAdd = suitableDup;
					mFreshDuplicatesToDuplicatePks.remove(suitableDup);
				}
			}
			if( toAdd != null ){
				toInsert = toAdd;
				Statistics.addInt("Number_Of_Successful_Dup_Pick_From_Fresh_Values_For_Last_Element_Of_Pk", 1);
				
				dbmsConn.setter(stmt, column.getIndex(), column.getType(), toInsert);
			}
			else{ // Cannot find an element among fresh, try with random
				
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
			
			String nextDuplicate = pickNextDupFromOldValues(schema, column, true);
			
			if( nextDuplicate == null ) logger.error("NULL duplicate"); 
			toInsert = nextDuplicate;
			
			dbmsConn.setter(stmt, column.getIndex(), column.getType(), nextDuplicate); // Ensures to put all chased elements, in a uniform way w.r.t. other columns
			
			if( column.isPrimary() ){
				primaryDuplicateValues.add(nextDuplicate);
			}
		}
	}
	private void increaseChaseCycles(Schema schema) {
		for( Column column : schema.getColumns() ){
			column.incrementCurrentChaseCycle();
		}
	}
}
