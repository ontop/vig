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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Level;

import basicDatatypes.Schema;
import columnTypes.ColumnPumper;
import connection.DBMSConnection;
import core.main.tableGenerator.aggregatedClasses.Distribution;

public class GeneratorDB extends GeneratorColumnBased {

	public GeneratorDB(DBMSConnection dbmsConn) {
		this.dbmsConn = dbmsConn;
		this.distribution = new Distribution(dbmsConn);
		this.random = new Random();
				
		mNumDupsRepetition = new HashMap<String, Integer>();
		maxNumDupsRepetition = 0;

		logger.setLevel(Level.INFO);
	}
	
	@Override
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
		resetChaseSets(schema);
		
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
		
		for( int j = 1; j <= nRows; ++j ){

			/** Keeps track of the DUPLICATE values chosen ---for the current row---
			 *  for columns part of a primary key  **/
			List<String> primaryDuplicateValues = new ArrayList<String>();
			
			for( ColumnPumper column : schema.getColumns() ){
				nRows = pumpColumn(schema, column, stmt, j, nRows, primaryDuplicateValues, uncommittedFresh, 
						mFreshDuplicatesToDuplicatePks, freshDuplicates, tablesToChase);
				if( nRows == Integer.MAX_VALUE ) return new ArrayList<Schema>(); // Stop immediately. Not possible to pump rows (foreign key violations)
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
			if( maxNumDupsRepetition > GeneratorDB.maxRepeatDuplicateWindowReads ){
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

	private void resetChaseSets(Schema schema) {
		for( ColumnPumper c : schema.getColumns() ){
			c.resetChases();
		}		
	}
}