package core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import utils.Pair;
import utils.Statistics;
import connection.DBMSConnection;
import basicDatatypes.*;

public class Generator3 extends Generator {
	
	private Map<String, Integer> mNumChases;
//	private Map<String, ResultSet> mDuplicates; // map storing the resultsets from where duplicates have to be taken
	private Map<String, ResultSet> referencedValues; 
	private Map<String, Integer> mNumDupsRepetition;
	private int maxNumDupsRepetition;

	public Generator3(DBMSConnection dbmsConn) {
		super(dbmsConn);
		
		 mNumChases = new HashMap<String, Integer>(); // It holds the number of chased elements for each column
//		 mDuplicates = new HashMap<String, ResultSet>();
		 referencedValues = new HashMap<String, ResultSet>();
		 mNumDupsRepetition = new HashMap<String, Integer>();
		 maxNumDupsRepetition = 0;
	}
		
	
	public List<Schema> pumpTable(int nRows, Schema schema){
		
		nRows = initChaseValues(nRows, schema);
		initDuplicateValuesAndRatios(schema);
		
		PreparedStatement stmt = null;
		String templateInsert = dbmsConn.createInsertTemplate(schema);
		
		List<Schema> tablesToChase = new LinkedList<Schema>();
		referencedValues.clear();
		initNumDupsRepetitionCounters();
		
		Map<String, List<String>> fromDuplicatesPks = new HashMap<String, List<String>>();
		
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
					else if( column.getDuplicateRatio() > random.getRandomFloat() ){
						logger.debug("Put a duplicate");
						
						// If, in all columns but one of the primary key I've put duplicates, 
						// pay attention to how you pick the last column. You might generate
						// a duplicate row if you do not do it correctly
						if( (primaryDuplicateValues.size() == schema.getPks().size() - 1) && column.isPrimary() ){
							// TODO This part is VERY EXPENSIVE. 
							//      Do not 
							// Force commit, because we need to check the content of the database
							
							Statistics.addInt("Number_Of_Duplicate_Expensive_Avoidance_For_Primary_Keys", 1);
							long start = System.currentTimeMillis();
							
//							stmt.executeBatch();
//							dbmsConn.commit();
							
							// Force either
							// Check if the key constructed so far is already in the database
							Pair<Boolean, String> isDistinct_usedQuery = checkIfDistinctPk(schema, primaryDuplicateValues);
							
							boolean isDistinct = isDistinct_usedQuery.first;
							if( !isDistinct ){
								// There is the need to either
								// 1) Pick a duplicate in this value that leads to a fresh pk tuple
								// 2) Generate a fresh value
								
								// Trying to pick a duplicate in this value that leads to a fresk pk tuple
								String nextDuplicate = null;
								List<String> nonValidValues = null;
								if( fromDuplicatesPks.containsKey(primaryDuplicateValues.toString()) ) 
									nonValidValues = fromDuplicatesPks.get(primaryDuplicateValues.toString());
								else nonValidValues = new ArrayList<String>();
								
								nextDuplicate = pickDuplicateForPk(schema, column, isDistinct_usedQuery.second);
									
								if( nextDuplicate != null && !nonValidValues.contains(nextDuplicate) ){
									dbmsConn.setter(stmt, ++columnIndex, column.getType(), nextDuplicate);
									if( !fromDuplicatesPks.containsKey(primaryDuplicateValues.toString()) ){
										fromDuplicatesPks.put(primaryDuplicateValues.toString(), nonValidValues);
									}
									fromDuplicatesPks.get(primaryDuplicateValues.toString()).add(nextDuplicate);
								}
								else{
									// Generate random fresh
									
									Statistics.addInt(schema.getTableName()+"."+column.getName()+" fresh values", 1);
									
									String generatedRandom = random.getRandomValue(column, nRows);
									dbmsConn.setter(stmt, ++columnIndex, column.getType(), generatedRandom);
									updateTablesToChase(column, tablesToChase);
								}
							}
							else{ 
								// Check if it is in some tuple not yet commited
								
								logger.debug("Adding a duplicate from initial database values");
								Statistics.addInt(schema.getTableName()+"."+column.getName()+" Adding a duplicate from initial database", 1);
								
								String nextDuplicate = null;
								List<String> nonValidValues = null;
								if( fromDuplicatesPks.containsKey(primaryDuplicateValues.toString()) ) 
									nonValidValues = fromDuplicatesPks.get(primaryDuplicateValues.toString());
								else nonValidValues = new ArrayList<String>();
								int dupRepetition = mNumDupsRepetition.containsKey(column) ? mNumDupsRepetition.get(column) : 0;
								do{
									nextDuplicate = pickNextDupFromOldValues(schema, column, true);
								}
								while( (mNumDupsRepetition.get(column) == dupRepetition) && nonValidValues.contains(nextDuplicate) );
								
								if( mNumDupsRepetition.get(column) != dupRepetition ){
									// Generate random fresh
									
									Statistics.addInt(schema.getTableName()+"."+column.getName()+" fresh values", 1);
									
									String generatedRandom = random.getRandomValue(column, nRows);
									dbmsConn.setter(stmt, ++columnIndex, column.getType(), generatedRandom);
									updateTablesToChase(column, tablesToChase);
								}
								else{
									
									dbmsConn.setter(stmt, ++columnIndex, column.getType(), nextDuplicate); // Ensures to put all chased elements, in a uniform way w.r.t. other columns
									
									if( !fromDuplicatesPks.containsKey(primaryDuplicateValues.toString()) ){
										fromDuplicatesPks.put(primaryDuplicateValues.toString(), nonValidValues);
									}
									fromDuplicatesPks.get(primaryDuplicateValues.toString()).add(nextDuplicate);
								}
							}
							
							long end = System.currentTimeMillis();
							
							Statistics.addTime("Time_spent_picking_a_problematic_duplicate_for_a_primary_key", end - start);
						}else{
							logger.debug("Adding a duplicate from initial database values");
							Statistics.addInt(schema.getTableName()+"."+column.getName()+" Adding a duplicate from initial database values", 1);
							
							String nextDuplicate = pickNextDupFromOldValues(schema, column, true);
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
						
						String generatedRandom = random.getRandomValue(column, nRows);
						dbmsConn.setter(stmt, ++columnIndex, column.getType(), generatedRandom);
						updateTablesToChase(column, tablesToChase);
					}
				}
				stmt.addBatch();
				if( (j % 1000000 == 0) ){ // Let's put a limit to the dimension of the stmt 
					stmt.executeBatch();	
					dbmsConn.commit();
				}
				if( maxNumDupsRepetition > 3 ){
					logger.info("Enlarging the set of candidate duplicates");
					stmt.executeBatch();	
					dbmsConn.commit();
					initDuplicateValuesAndRatios(schema);
					initNumDupsRepetitionCounters();
					fromDuplicatesPks.clear();
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
		return tablesToChase; 
	}
	
		
	private void updateTablesToChase(Column column, List<Schema> tablesToChase) {
		// New values inserted imply new column to chase
		for( QualifiedName qN : column.referencesTo() ){
			if( !tablesToChase.contains(dbmsConn.getSchema(qN.getTableName())) ){
				tablesToChase.add(dbmsConn.getSchema(qN.getTableName()));
			}
		}
	}

	private void initDuplicateValuesAndRatios(Schema schema) {
		for( String key : duplicateValues.keySet() ){
			try {
				duplicateValues.get(key).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		duplicateValues.clear();
		
		for( Column c : schema.getColumns() ){
			ResultSet rs = fillDuplicates(c, schema.getTableName());
			try {
				if( duplicateValues.containsKey(c.getName()) )
					duplicateValues.get(c.getName()).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			duplicateValues.put(c.getName(), rs);
			c.setDuplicateRatio(findDuplicateRatio(schema, c));
		}
		System.gc();
	}

	private int initChaseValues(int nRows, Schema schema){
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
	
	/**
	 * 
	 * @param column
	 * @param tableName
	 * @return A result set containing the number of duplicates---taken from the original set--- that need to be inserted
	 */
	protected ResultSet fillDuplicates(Column column, String tableName) {
		
		ResultSet result = null;
				
 		String queryString = "SELECT "+column.getName()+ " FROM "+tableName;
		
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
				if( !duplicatesToInsert.next() )
					logger.error("No duplicate element can be forced");
			}
			else if( !hasNext && !force ){
				return null;
			}
			result = duplicatesToInsert.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private void initNumDupsRepetitionCounters(){
		maxNumDupsRepetition = 0;
		mNumDupsRepetition.clear();
	}
}
