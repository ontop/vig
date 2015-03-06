package core.tableGenerator;

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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import utils.Statistics;
import basicDatatypes.QualifiedName;
import basicDatatypes.Schema;
import columnTypes.ColumnPumper;
import connection.DBMSConnection;
import core.main.tableGenerator.aggregatedClasses.Distribution;

public abstract class GeneratorColumnBased extends Generator {

	protected Distribution distribution;
	protected DBMSConnection dbmsConn;
	protected Random random;
	
	protected Map<String, Integer> mNumDupsRepetition;
	protected int maxNumDupsRepetition;

	/** Size of the window for duplicates picking **/
	public static int duplicatesWindowSize = 30000;
	public static int maxRepeatDuplicateWindowReads = 5;
	public static int freshDuplicatesSize = 1000;
	protected static int maxPrimaryDuplicateValuesBufferSize = 5;
	protected boolean pureRandom = false;
	
	protected static Logger logger = Logger.getLogger(GeneratorDB.class.getCanonicalName());
	
	protected int pumpColumn(Schema schema, ColumnPumper column,
			PreparedStatement stmt, int j,
			int nRows,
			List<String> primaryDuplicateValues,
			Map<String, List<String>> uncommittedFresh,
			Map<String, List<List<String>>> mFreshDuplicatesToDuplicatePks,
			List<String> freshDuplicates, List<Schema> tablesToChase) {
		
		String toInsert = null;
		float dupOrNullToss = random.nextFloat();
		
		boolean stopChase = (column.referencesTo().size() > 0) && column.getMaximumChaseCycles() < column.getCurrentChaseCycle();
		if( stopChase && (column.getDuplicateRatio() > 0 || !column.isPrimary()) ){
			column.setDuplicateRatio(1); // DO NOT generate fresh values. Fresh values trigger new chase steps.
		}	
		if( j == nRows && (toInsert = column.getNextChased(dbmsConn, schema)) != null && 
				(!column.isPrimary() || !uncommittedFresh.get(column.getName()).contains(toInsert)) ){ // If you have inserted all the required rows but there's
			                                                                                           // still some chased value
			dbmsConn.setter(stmt, column.getIndex(), column.getType(), toInsert); 
			addToUncommittedFresh(uncommittedFresh, column, toInsert);
			if( column.hasNextChase() )	++nRows; // I haven't finished yet to insert chased values.
		}
		else if( column.getDuplicateRatio() > dupOrNullToss){
			putDuplicate(schema, column, primaryDuplicateValues, 
					mFreshDuplicatesToDuplicatePks, freshDuplicates, stmt, uncommittedFresh, tablesToChase);	
		}
		else if( column.getNullRatio() > (dupOrNullToss - column.getDuplicateRatio()) ){
			// TODO Remove this expensive check
			List<String> names = new ArrayList<String>();
			for( int i = 0; i < schema.getPk().size(); ++i ){
				names.add(schema.getPk().get(i).getName());
			}
			if( names.contains(column.getName()) ){ 
				try{
					throw new Exception("Trying to insert a null in a primary key field");
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			putNull(schema, column, stmt);
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
				return Integer.MAX_VALUE;
			}
			dbmsConn.setter(stmt, column.getIndex(), column.getType(), toInsert);
		}
		else{ // Add a random value						
			Statistics.addInt(schema.getTableName()+"."+column.getName()+" fresh values", 1);
			String generatedRandom = putFreshRandom(column, stmt, uncommittedFresh);
			updateFreshDuplicates(schema, column, generatedRandom, primaryDuplicateValues, freshDuplicates, mFreshDuplicatesToDuplicatePks);
			updateTablesToChase(column, tablesToChase);
		}
		return nRows;
	}

	protected String putFreshRandom(ColumnPumper column, PreparedStatement stmt, Map<String, List<String>> uncommittedFresh) {

		String generatedRandom = column.getNextFreshValue();
								
		if( generatedRandom == null ) logger.error("NULL fresh"); 
//		logger.debug("Adding Fresh");
		dbmsConn.setter(stmt, column.getIndex(), column.getType(), generatedRandom);
		
		if( column.isPrimary() && column.referencedBy().size() > 0){ // Chased values might be duplicate
			addToUncommittedFresh(uncommittedFresh, column, generatedRandom);
		}
		
		return generatedRandom;
	}

	protected void updateFreshDuplicates(Schema schema, ColumnPumper column,
			String generatedRandom, List<String> primaryDuplicateValues,
			List<String> freshDuplicates,
			Map<String, List<List<String>>> mFreshDuplicatesToDuplicatePks) {
		
		
		// If (c_1,..,c_n, column) is a primary key AND
		// from_dup(c_i), where 1 <= i <= n, THEN keep track
		// of column -> (c_1, ..., c_n)
		if( column.isPrimary() && (primaryDuplicateValues.size() == schema.getPk().size() - 1 ) ){
			if( freshDuplicates.size() < GeneratorDB.freshDuplicatesSize ){
				List<List<String>> listOfPrimaryDuplicateValues = new ArrayList<List<String>>();
				listOfPrimaryDuplicateValues.add(primaryDuplicateValues);
				mFreshDuplicatesToDuplicatePks.put(generatedRandom, listOfPrimaryDuplicateValues);
				
				Statistics.addInt(schema.getTableName()+"."+column.getName()+"___adds_to_mFreshDuplicatesToDuplicatePks", 1);
			}
		}
		
		if( schema.getPk().size() > 0 && column.getIndex() == schema.getPk().get(schema.getPk().size()-1).getIndex() ){
			if( freshDuplicates.size() < GeneratorDB.freshDuplicatesSize ){
				freshDuplicates.add(generatedRandom);
				
				Statistics.addInt(schema.getTableName()+"."+column.getName()+"___adds_to_freshDuplicates", 1);
			}
		}
		
	}

	protected void initUncommittedFresh(Schema schema,
			Map<String, List<String>> uncommittedFresh) {
		
		for( String key : uncommittedFresh.keySet() ){
			if( !uncommittedFresh.get(key).isEmpty() ){
				uncommittedFresh.get(key).clear();
				schema.getColumn(key).refillCurChaseSet(dbmsConn, schema);
			}
		}
		
	}

	protected void addToUncommittedFresh(
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
	
	protected float findNullRatio(Schema s, ColumnPumper column){
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
	
	protected float findDuplicateRatio(Schema s, ColumnPumper column){
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

	protected String pickNextDupFromOldValues(Schema schema, ColumnPumper column) {
		
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
		
//		logger.debug("Put a duplicate into "+schema.getTableName() + "." + column.getName());
		
		// If, in all columns but one of the primary key I've put duplicates, 
		// pay attention to how you pick the last column. You might generate
		// a duplicate row if you do not do it correctly
		if( (primaryDuplicateValues.size() == schema.getPk().size() - 1) && column.isPrimary() ){
			
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
						if( mFreshDuplicatesToDuplicatePks.get(suitableDup).size() > GeneratorDB.maxPrimaryDuplicateValuesBufferSize ){
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
//			logger.debug("Adding a duplicate for "+ (new QualifiedName(schema.getTableName(), column.getName())).toString());
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
	protected void increaseChaseCycles(Schema schema) {
		for( ColumnPumper column : schema.getColumns() ){
			column.incrementCurrentChaseCycle();
		}
	}
	
	public void setPureRandomGeneration() {
		pureRandom = true;
	}
	
}
