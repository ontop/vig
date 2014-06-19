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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import mappings.Tuple;
import mappings.TupleStore;
import mappings.TupleStoreFactory;
import mappings.TupleTemplate;
import mappings.TupleTemplateDecorator;
import mappings.TuplesPicker;

import org.apache.log4j.Level;

import basicDatatypes.Schema;
import columnTypes.ColumnPumper;
import connection.DBMSConnection;
import core.main.tableGenerator.aggregatedClasses.Distribution;

public class GeneratorOBDA extends GeneratorColumnBased {

	public GeneratorOBDA(DBMSConnection dbmsConn) {
		this.dbmsConn = dbmsConn;
		this.distribution = new Distribution(dbmsConn);
		this.random = new Random();
				
		mNumDupsRepetition = new HashMap<String, Integer>();
		maxNumDupsRepetition = 0;

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
		
		// Something about the tuples ...
		TupleTemplateDecorator candidate = searchForCandidate(schema.getTableName());
		
		if( schema.isFilled() ) candidate = null;
		
		// Count how many dups need to be inserted
		if(candidate != null)
			cntNumDupTuplesToInsert(nRows, this.dbmsConn, schema.getTableName(), candidate);
		
		// templateInsert to be called AFTER the ratios initialization
		// because of the reordering of the columns
		String templateInsert = dbmsConn.createInsertTemplate(schema);
		
		stmt = dbmsConn.getPreparedStatement(templateInsert);
		logger.debug(templateInsert);

		// Disable auto-commit
		dbmsConn.setAutoCommit(false);
		boolean tuplesFlushed = false;
		for( int j = 1; j <= nRows; ++j ){

			/** Keeps track of the DUPLICATE values chosen ---for the current row---
			 *  for columns part of a primary key  **/
			List<String> primaryDuplicateValues = new ArrayList<String>();
			
			if( candidate != null ){
				
				if( duplicatesTuplesToPick(dbmsConn, schema.getTableName(), nRows, candidate) ){
					Map<String, String> m_ColName_Value = tryToPickATuple(dbmsConn, schema.getTableName(), candidate);
					if( m_ColName_Value != null ){ 
						decreaseNumTuplesToInsert(candidate);
						attachTuple(m_ColName_Value, stmt, schema);
					}	
					else{
						logger.debug("Flushing Tuples");
						if(!tuplesFlushed){
							try{
								tuplesFlushed = true;
								stmt.executeBatch();	
								dbmsConn.commit();
							}catch(SQLException e){
								e.printStackTrace();
							}
							initUncommittedFresh(schema, uncommittedFresh);	
						}
					}
				}
			}
			
			for( ColumnPumper column : schema.getColumns() ){
				if( column.ignore() ){ 
					column.unsetIgnore(); 
					continue; 
				} // If the TuplesPicker already decided a value
				  // for the column
				
				boolean terminate = pumpColumn(schema, column, stmt, j, nRows, primaryDuplicateValues, uncommittedFresh, 
						mFreshDuplicatesToDuplicatePks, freshDuplicates, tablesToChase);
				if( terminate ){
					for( ColumnPumper cP : schema.getColumns() ){
						cP.unsetIgnore();
					}
					return new ArrayList<Schema>(); // Stop immediately. Not possible to pump rows (foreign key violations)
				}
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
			if( maxNumDupsRepetition > GeneratorColumnBased.maxRepeatDuplicateWindowReads ){
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
		resetTuplesPicker();
		logger.info("Table '"+ schema.getTableName() + "' pumped with " + nRows +" rows.");
		
		return tablesToChase; 
	}
	
	private void decreaseNumTuplesToInsert(TupleTemplateDecorator candidate) {
		candidate.decreaseToInsert();
	}

	private void cntNumDupTuplesToInsert(int nRows, DBMSConnection dbmsConn,
			String tableName, TupleTemplateDecorator candidate) {
		
		candidate.addToInsert(((int)Math.ceil(nRows * candidate.getDupR())));		
	}

	private boolean duplicatesTuplesToPick(DBMSConnection dbmsConn,
			String tableName, int nRows, TupleTemplateDecorator candidate) {
		
		if( allOtherTablesUnfilled(candidate.getReferredTables()) ){
			return false;
		}
		
		if( candidate.leftToInsert() > 0 ){
			return true;
		}
		else return false;
	}

	/**
	 * TOO SLOW.
	 * @param m_ColName_Value
	 * @param schema
	 * @return
	 */
	@Deprecated 
	private boolean isDupTuple(Map<String, String> m_ColName_Value, Schema schema) {
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("SELECT COUNT(*) FROM " + schema.getTableName() + " WHERE ");
		boolean first = true;
		for( String colName : m_ColName_Value.keySet() ){
			
			if( first == true ){
				first = false;
				if( m_ColName_Value.get(colName) == null ){
					builder.append(colName + "=" + "null" );
				}
				else{
					builder.append(colName + "=" + "'" +m_ColName_Value.get(colName)+"'" );
				}
			}
			else{
				builder.append(" AND " );
				if( m_ColName_Value.get(colName) == null ){
					builder.append(colName + "=" + "null" );
				}
				else{
					builder.append(colName + "=" + "'" + m_ColName_Value.get(colName) +"'" );
				}
			}
		}
		
		logger.debug("THE DUPLICATES-AVOID QUERY IS: " + builder.toString());
		
		PreparedStatement stmt = dbmsConn.getPreparedStatement(builder.toString());
		int count = 0;
		try {
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()){
				count = rs.getInt(1);
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return count > 0;
	}

	private void resetTuplesPicker() {
		TupleStoreFactory.getInstance().getTuplesPickerInstance().reset();
	}

	/**
	 * If the call of <b>tryToPickATuple</b> successfully retrieved a tuple, <b>attachTuple</b>
	 * puts it in the PreparedStatement <b>stmt</b> and informs all the involved columns
	 * of the <b>schema</b> about this fact
	 * 
	 * @param m_ColName_Value
	 * @param stmt
	 * @param schema
	 */
	private void attachTuple(Map<String, String> m_ColName_Value,
			PreparedStatement stmt, Schema schema) {
		
		
		
		if( m_ColName_Value == null ) return;
		
		try{
			
			for( String colName : m_ColName_Value.keySet() ){
				ColumnPumper cP = schema.getColumn(colName);
				int index = cP.getIndex();
				
				// TODO TEST Update last inserted
				cP.proposeLastFreshInserted(m_ColName_Value.get(colName));
				
				// Fill stmt
				stmt.setString(index, m_ColName_Value.get(colName));
				
				// Inform involved columns
				cP.setIgnore();
				
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	private Map<String, String> tryToPickATuple(DBMSConnection dbConn, String tableName, TupleTemplateDecorator candidate) {
		
		List<String> tuple = null;
		
		TuplesPicker tP = TupleStoreFactory.getInstance().getTuplesPickerInstance();
		tuple = tP.pickTuple(dbConn, tableName, candidate);
		
		if( tuple == null ) return null;
		
		if( tuple.size() == 0 ){
			logger.debug("Strange Fact");
			try{
				throw new Exception();
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		Map<String, String> result = new HashMap<String, String>();
		
		for( int i = 0; i < tuple.size(); ++i ){
			result.put(candidate.getColumnsInTable(tableName).get(i), tuple.get(i));
		}
		
		return result;
	}

	private boolean allOtherTablesUnfilled(Set<String> referredTables) {
		
		for( String tableName : referredTables ){
			if( dbmsConn.getSchema(tableName).isFilled() ) return false;
		}
		return true;
	}

	/**
	 * 
	 * @return A <b>TupleTemplate</b> spreading over several tables AND 
	 *         whose columns set subsumes <b>this.primary_key</b>
	 */
	private TupleTemplateDecorator searchForCandidate(String tableName) {
		
		TupleStore ts = TupleStoreFactory.getInstance().getTupleStoreInstance();
		
		// Find, among the tuples related to this table,
		// those whose templates fall in more than one table 
		// TODO This is only temporary, the complete algorithm would require different
		List<Tuple> referringTuples = ts.getAllTuplesOfTable(tableName);
		if( referringTuples == null ) return null;
		
		int max = 0;
		TupleTemplate maxTemplate = null;
		for( Tuple t : referringTuples ){
			for( TupleTemplate tt : t.getTupleTemplates() ){
				if(!tt.getReferredTables().contains(tableName)) continue;
				if(tt.getReferredTables().size() < 2) continue; // TODO Well, if we want to extend in one future
				
				// Check against Geometrics
				// TODO Fully support geometrics
				boolean geometricFound = false;
				for( String columnName : tt.getColumnsInTable(tableName) ){
					ColumnPumper cP = this.dbmsConn.getSchema(tableName).getColumn(columnName);
					if( cP.isGeometric() )
						geometricFound = true;
				}
				
				if( geometricFound ) continue;
				
				// Check if the columns subsume the pk
				List<ColumnPumper> pk = this.dbmsConn.getSchema(tableName).getPk();
				Set<String> names = new HashSet<String>();
				for( ColumnPumper cP : pk ){
					if( !names.contains(cP.getName())) names.add( cP.getName() );
				}
				if( tt.getColumnsInTable(tableName).containsAll(names) ){					
					if( max < tt.getReferredTables().size() ){
						max = tt.getReferredTables().size(); maxTemplate = tt; 
					}  
				}else{
					// If NO pk is touched at all
					boolean forall = true;
					for( String col : tt.getColumnsInTable(tableName) ){
						if( names.contains(col) ){
							forall = false;
							break;
						}
					}
					if(forall){
						System.err.println("FORALL!! NO PK TOUCHED");
						if( max < tt.getReferredTables().size() ){
							max = tt.getReferredTables().size(); maxTemplate = tt; 
						} 
					}
					else{
						System.err.println("NOT!!! TOUCH SOME!!!!!");
					}
				}
			}
		}
		
		return maxTemplate == null ? null : ts.decorateTupleTemplate(maxTemplate);
	}
};
