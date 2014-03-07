package core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Level;

import utils.Statistics;
import basicDatatypes.QualifiedName;
import basicDatatypes.Schema;
import columnTypes.Column;
import connection.DBMSConnection;

public class Generator4 extends Generator3 {

	public Generator4(DBMSConnection dbmsConn) {
		super(dbmsConn);
	}
	
	public List<Schema> pumpTable(int nRows, Schema schema){
		
		logger.setLevel(Level.INFO);
		
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
		
		try {
			stmt = dbmsConn.getPreparedStatement(templateInsert);
			logger.debug(templateInsert);
			
			// Disable auto-commit
			dbmsConn.setAutoCommit(false);
			
			// Idea: I can say that nRows = number of things that need to be chased, when the maximum
			// cycle is reached. To test this out			
			for( int j = 1; j <= nRows; ++j ){
				
				boolean chaseInserted = false;
				
				int columnIndex = 0;
				List<String> primaryDuplicateValues = new ArrayList<String>();
				for( Column column : schema.getColumns() ){
					
					String toInsert = null;

					boolean stopChase = (column.referencesTo().size() > 0) && column.getMaximumChaseCycles() < column.getCurrentChaseCycle();
					
					if( j == nRows && (toInsert = column.getNextChased(dbmsConn, schema)) != null  ){
						dbmsConn.setter(stmt, ++columnIndex, column.getType(), toInsert); 
						++nRows; // I haven't finished yet to insert chased values.
						chaseInserted = true;
					}
					else if( ( 0.8 > random.nextFloat() ) && 
							(toInsert = column.getNextChased(dbmsConn, schema) ) != null ){
						dbmsConn.setter(stmt, ++columnIndex, column.getType(), toInsert); 
						chaseInserted = true;
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
				stmt.addBatch();
				if( (j % 1000000 == 0) ){ // Let's put a limit to the dimension of the stmt 
					stmt.executeBatch();	
					dbmsConn.commit();
				}
				if( chaseInserted ){ //TODO Efficiency?
					stmt.executeBatch();
					dbmsConn.commit();
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
}
