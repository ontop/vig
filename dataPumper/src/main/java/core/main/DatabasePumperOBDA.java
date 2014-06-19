package core.main;

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

import mappings.TupleStoreFactory;
import columnTypes.ColumnPumper;
import utils.TrivialQueue;
import basicDatatypes.Schema;
import configuration.Conf;
import connection.DBMSConnection;
import core.tableGenerator.Generator;
import core.tableGenerator.GeneratorOBDA;

import java.util.List;

import org.apache.log4j.Logger;

// Speed trick 
// Connection c = DriverManager.getConnection("jdbc:mysql://host:3306/db?useServerPrepStmts=false&rewriteBatchedStatements=true", "username", "password");
// TODO Try
// Tried. Very Well.
public class DatabasePumperOBDA extends DatabasePumper {
	
	private DBMSConnection dbOriginal;
	private DBMSConnection dbToPump;
	
	private static Logger logger = Logger.getLogger(DatabasePumperOBDA.class.getCanonicalName());	
	
	public DatabasePumperOBDA(DBMSConnection dbOriginal, DBMSConnection dbToPump){
		this.dbOriginal = dbOriginal;
		this.dbToPump = dbToPump;
	}
	
	public void pumpDatabase(double percentage, String fromTable){
		long startTime = System.currentTimeMillis();
		
		dbToPump.setForeignCheckOff();
		dbToPump.setUniqueCheckOff();
		
		Generator gen = new GeneratorOBDA(dbToPump);
		if( pureRandom ) gen.setPureRandomGeneration();
		
		TrivialQueue<Schema> schemas = new TrivialQueue<Schema>();
		
		for( String tableName : dbToPump.getAllTableNames() ){
			Schema s = dbToPump.getSchema(tableName);
			for( ColumnPumper c : s.getColumns() ){
				if( !c.referencesTo().isEmpty() ){
					c.setMaximumChaseCycles(2); // Default for npd
				}
			}
		}
		
		// Init the queue
		boolean reached = false;
		for( String tableName : dbToPump.getAllTableNames()){
			if( !tableName.equals(fromTable) && !reached ) continue;
			reached = true;
			schemas.enqueue(dbToPump.getSchema(tableName));
		}
				
		// Breadth first strategy
		// TODO I need a limit, for the moment I put an hard one.
		int cnt = 0;
		while(schemas.hasNext()){
			Schema schema = schemas.dequeue();
			
			fillDomain(schema, dbOriginal);
			
			List<Schema> toChase = null;
			if(schema.isFilled()){ // 
				toChase = gen.pumpTable(1, schema);
			}
			else{
				int nRows = dbOriginal.getNRows(schema.getTableName());
				nRows = (int) (nRows * percentage);
				logger.info("Pump "+schema.getTableName()+" of "+nRows+" rows, please.");
				
				toChase = gen.pumpTable(nRows, schema);
				schema.setFilled();
			}
			for( Schema s : toChase ){
				if(!schemas.contains(s)){
					
					if(++cnt % 1 == 0) logger.debug("Ciclo "+cnt);
					schemas.enqueue(s);
					
				}
			}
		}
		long endTime = System.currentTimeMillis();
		
		logger.info("Database pumped in " + (endTime - startTime) + " msec.");
	}

	/**
	 * 
	 * @param dbOriginal
	 * @param db
	 * @param nRows
	 */
	public void pumpDatabase(double percentage){
		
		long startTime = System.currentTimeMillis();
		
		dbToPump.setForeignCheckOff();
		dbToPump.setUniqueCheckOff();
		
		GeneratorOBDA gen = new GeneratorOBDA(dbToPump);
		if( pureRandom ) gen.setPureRandomGeneration();
		
		TrivialQueue<Schema> schemas = new TrivialQueue<Schema>();
		
		for( String tableName : dbToPump.getAllTableNames() ){
			Schema s = dbToPump.getSchema(tableName);
			for( ColumnPumper c : s.getColumns() ){
				if( !c.referencesTo().isEmpty() ){
					c.setMaximumChaseCycles(2); // Default for npd
				}
			}
		}
		
		// Init the queue
		for( String tableName : dbToPump.getAllTableNames()){
			schemas.enqueue(dbToPump.getSchema(tableName));
		}
		
		// Analyze the tuples
		TupleStoreFactory.setInstance(dbOriginal, Conf.mappingsFile());
		TupleStoreFactory mA = TupleStoreFactory.getInstance();
		
		// Breadth first strategy
		// TODO I need a limit, for the moment I put an hard one.
		int cnt = 0;
		while(schemas.hasNext()){
			Schema schema = schemas.dequeue();
			
			fillDomain(schema, dbOriginal);
			fillOriginalTableSize(schema, dbOriginal);
			
			List<Schema> toChase = null;
			if(schema.isFilled()){ // 
				toChase = gen.pumpTable(1, schema);
			}
			else{
				int nRows = dbOriginal.getNRows(schema.getTableName());
				nRows = (int) (nRows * percentage);
				logger.info("Pump "+schema.getTableName()+" of "+nRows+" rows, please.");
				
				toChase = gen.pumpTable(nRows, schema);
				schema.setFilled();
			}
			for( Schema s : toChase ){
				if(!schemas.contains(s)){
					
					if(++cnt % 1 == 0) logger.debug("Ciclo "+cnt);
					schemas.enqueue(s);
					
				}
			}
		}
		long endTime = System.currentTimeMillis();
		
		logger.info("Database pumped in " + (endTime - startTime) + " msec.");
	}

	private void fillOriginalTableSize(Schema schema, DBMSConnection dbOriginal) {
		if( schema.getOriginalSize() == 0 ) schema.setOriginalSize(dbOriginal.getNRows(schema.getTableName()));
	}

	private void fillDomain(Schema schema, DBMSConnection originalDb) {
		for( ColumnPumper column : schema.getColumns() ){
			column.fillDomain(schema, originalDb);
			column.fillDomainBoundaries(schema, originalDb);
		}
	}

	public void setPureRandomGeneration() {
		pureRandom = true;
	}
};