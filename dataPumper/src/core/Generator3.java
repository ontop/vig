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

import org.apache.log4j.Level;

import columnTypes.Column;
import utils.Statistics;
import connection.DBMSConnection;
import basicDatatypes.*;

//import objectexplorer.ObjectGraphMeasurer;

public class Generator3 extends Generator {
	
	protected Map<String, Integer> mNumChases;
	protected Map<String, ResultSet> referencedValues; 
	protected Map<String, Integer> mNumDupsRepetition;
	protected int maxNumDupsRepetition;

	public static int duplicatesWindowSize = 80000;
	public static int maxRepeatDuplicateWindowReads = 5;
	public static int freshDuplicatesSize = 20;
	
	public Generator3(DBMSConnection dbmsConn) {
		super(dbmsConn);
		
		 mNumChases = new HashMap<String, Integer>(); // It holds the number of chased elements for each column
		 referencedValues = new HashMap<String, ResultSet>();
		 mNumDupsRepetition = new HashMap<String, Integer>();
		 maxNumDupsRepetition = 0;
	}
		
	
	public List<Schema> pumpTable(int nRows, Schema schema){
		
		logger.setLevel(Level.INFO);
		
		nRows = initChaseValues(nRows, schema);
		initDuplicateValues(schema, 0);
		
		initDuplicateRatios(schema);
		// Now, the current dup ratios are calculated
		// according to nRows. However, due to the implementation
		// the real number favorable cases is nRows - numChases (look at the first if condition)
		// Thus, an update on the ratios is needed.
		updateDupRatios(nRows, schema);
		
		PreparedStatement stmt = null;
		String templateInsert = dbmsConn.createInsertTemplate(schema);
		
		List<Schema> tablesToChase = new LinkedList<Schema>();
		referencedValues.clear();
		initNumDupsRepetitionCounters();
		
		Map<String, List<String>> mFreshDuplicatesToDuplicatePks = new HashMap<String, List<String>>();
		Queue<String> freshDuplicates = new LinkedList<String>();
		
		dbmsConn.initColumns(schema);
		
		try {
			stmt = dbmsConn.getPreparedStatement(templateInsert);
			logger.debug(templateInsert);
			
			// Disable auto-commit
			dbmsConn.setAutoCommit(false);
			
			// Idea: I can say that nRows = number of things that need to be chased, when the maximum
			// cycle is reached. To test this out
			for( int j = 1; j <= nRows; ++j ){
				
				int columnIndex = 0;
				List<String> primaryDuplicateValues = new ArrayList<String>();
				for( Column column : schema.getColumns() ){
										
					boolean stopChase = (column.referencesTo().size() > 0) && column.getMaximumChaseCycles() < column.getCurrentChaseCycle();
					
					if( mNumChases.containsKey(column.getName()) && canAdd(nRows, j, mNumChases.get(column.getName())) )  {
						dbmsConn.setter(stmt, ++columnIndex, column.getType(), pickNextChased(schema, column)); // Ensures to put all chased elements, in a uniform way w.r.t. other columns
						
					}
					else if( column.getDuplicateRatio() > random.nextFloat() ){
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
								Statistics.addInt("Number_Of_Successful_Dup_Pick_From_Fresh_Values_For_Last_Element_Of_Pk", 1);
								dbmsConn.setter(stmt, ++columnIndex, column.getType(), toAdd);
							}
							else{ // Cannot find an element among fresh, try with random
								Statistics.addInt(schema.getTableName()+"."+column.getName()+"_forced_fresh_values", 1);
								Statistics.addInt(schema.getTableName()+"."+column.getName()+" fresh values", 1);
								
								String generatedRandom = column.getNextFreshValue();
								if( generatedRandom == null ) logger.error("NULL!");
								dbmsConn.setter(stmt, ++columnIndex, column.getType(), generatedRandom);
								if( freshDuplicates.size() < Generator3.freshDuplicatesSize ){
									mFreshDuplicatesToDuplicatePks.put(generatedRandom, primaryDuplicateValues);
									freshDuplicates.add(generatedRandom);
								}
								
								updateTablesToChase(column, tablesToChase);
							}							
							long end = System.currentTimeMillis();
							
							Statistics.addTime("Time_spent_picking_a_problematic_duplicate_for_a_primary_key", end - start);
						}else{
							logger.debug("Adding a duplicate for "+ (new QualifiedName(schema.getTableName(), column.getName())).toString());
							Statistics.addInt(schema.getTableName()+"."+column.getName()+" Adding a duplicate from initial database values", 1);
							
							String nextDuplicate = pickNextDupFromOldValues(schema, column, true);
							
							if( nextDuplicate == null ) logger.error("NULL duplicate"); 
							
							
							dbmsConn.setter(stmt, ++columnIndex, column.getType(), nextDuplicate); // Ensures to put all chased elements, in a uniform way w.r.t. other columns
							
							if( column.isPrimary() ){
								primaryDuplicateValues.add(nextDuplicate);
							}
						}
					}
					else if( stopChase ){
						// We cannot take a chase value, neither we can pick a duplicate. The only way out is 
						// to tale the necessary number of elements (non-duplicate with this column) from the referenced column(s)
						dbmsConn.setter(stmt, ++columnIndex, column.getType(), pickFromReferenced(schema, column, referencedValues));
					}
					else{ // Add a random value
						Statistics.addInt(schema.getTableName()+"."+column.getName()+" fresh values", 1);
						
						String generatedRandom = column.getNextFreshValue();
						
						if( generatedRandom == null ) logger.error("NULL fresh"); 
						logger.debug("Adding Fresh");
						dbmsConn.setter(stmt, ++columnIndex, column.getType(), generatedRandom);
						
						// Let's do this
						if( column.isPrimary() && (primaryDuplicateValues.size() == schema.getPks().size() - 1 ) ){
							if( freshDuplicates.size() < Generator3.freshDuplicatesSize ){
								mFreshDuplicatesToDuplicatePks.put(generatedRandom, primaryDuplicateValues);
								
								Statistics.addInt(schema.getTableName()+"."+column.getName()+"___adds_to_mFreshDuplicatesToDuplicatePks", 1);
							}
						}
						
						if( schema.getPks().size() > 0 && column.getIndex() == schema.getPks().get(schema.getPks().size()-1).getIndex() ){
							if( freshDuplicates.size() < Generator3.freshDuplicatesSize ){
								freshDuplicates.add(generatedRandom);
								
								Statistics.addInt(schema.getTableName()+"."+column.getName()+"___adds_to_freshDuplicates", 1);
							}
						}
						updateTablesToChase(column, tablesToChase);
					}
				}
//				System.err.println(stmt.toString().substring("com.mysql.jdbc.JDBC4PreparedStatement@c4dc1e4: ".length()));
				stmt.addBatch();
				if( (j % 1000000 == 0) ){ // Let's put a limit to the dimension of the stmt 
					stmt.executeBatch();	
					dbmsConn.commit();
//					logger.info(ObjectGraphMeasurer.measure(stmt));
				}
				if( maxNumDupsRepetition > Generator3.maxRepeatDuplicateWindowReads ){
					logger.info("Advancing the set of candidate duplicates");
					stmt.executeBatch();	
					dbmsConn.commit();
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
		
	/**
	 * 
	 * nR * oldRatio = (nR - numChases) * newRatio
	 * => implies
	 * newRatio = (nR * oldRatio) / (nR - numChases)
	 * 
	 * @param nRows
	 * @param schema
	 */
	protected void updateDupRatios(int nRows, Schema schema) {
		
		for( Column c : schema.getColumns() ){
			int numChases = mNumChases.containsKey(c.getName()) ? mNumChases.get(c.getName()) : 0;
			float oldRatio = c.getDuplicateRatio();
			
			float newRatio = (nRows * oldRatio) / (nRows - numChases );
			
			logger.info(schema.getTableName() +"."+ c.getName() +" updated dup ratio: " + newRatio);
			c.setDuplicateRatio(newRatio);
		}
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
//			if(c.getDuplicateRatio() > 0.95) then c.setIndependent(); TODO Mindaugas
		}
	}

	protected int initChaseValues(int nRows, Schema schema){
		for( String key : chasedValues.keySet() ){
			for( ResultSet rs : chasedValues.get(key) ){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		chasedValues.clear();
		mNumChases.clear();
		
		// Fill chased
		for( Column column : schema.getColumns() ){
			chasedValues.put(column.getName(), fillChase(column, schema.getTableName(), mNumChases));
			column.incrementCurrentChaseCycle();
		}
		
		if( nRows == 0 ){ // This is a pure-chase phase. And it is also THE ONLY place where I need to chase
			// I need to generate (at least) as many rows as the maximum among the chases
			int max = 0;
			for( String key: mNumChases.keySet() ){
				if( max < mNumChases.get(key) )
					max = mNumChases.get(key);
			}
			nRows = max; // Set the number of rows that need to be inserted.
		}
		
		return nRows;
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
	
	/**
	 * 
	 * @param column
	 * @param tableName
	 * @return A result set containing the number of duplicates---taken from the original set--- that need to be inserted
	 */
	protected ResultSet fillDuplicates(Column column, String tableName, int insertedRows) {
		
		ResultSet result = null;
		int startIndex = insertedRows - Generator3.duplicatesWindowSize > 0 ? insertedRows - Generator3.duplicatesWindowSize : 0;
		
		String queryString = null;
		
		if( column.isGeometric() ){
			queryString = "SELECT AsWKT(" + column.getName() + ") FROM " + tableName + " "
					+ " WHERE AsWKT(" + column.getName() + ") IS NOT NULL LIMIT " + startIndex + ", " + Generator3.duplicatesWindowSize;
		}
		else{
			queryString = "SELECT "+column.getName()+ " FROM "+tableName+" WHERE "+column.getName()+" IS NOT NULL LIMIT "+ startIndex +", "+Generator3.duplicatesWindowSize;
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
		return ratio;
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
}
