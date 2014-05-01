package core;

import java.util.List;

import mappings.MappingAnalyzer;

import org.apache.log4j.Logger;

import columnTypes.ColumnPumper;
import utils.TrivialQueue;
import basicDatatypes.Schema;
import configuration.Conf;
import connection.DBMSConnection;

// Speed trick 
// Connection c = DriverManager.getConnection("jdbc:mysql://host:3306/db?useServerPrepStmts=false&rewriteBatchedStatements=true", "username", "password");
// TODO Try
// Tried. Very Well.
public class DatabasePumper {
	
	private DBMSConnection dbOriginal;
	private DBMSConnection dbToPump;
	private boolean pureRandom = false;
	
	private static Logger logger = Logger.getLogger(DatabasePumper.class.getCanonicalName());	
	
	public DatabasePumper(DBMSConnection dbOriginal, DBMSConnection dbToPump){
		this.dbOriginal = dbOriginal;
		this.dbToPump = dbToPump;
	}
	
	public void pumpDatabase(float percentage, String fromTable){
		long startTime = System.currentTimeMillis();
		
		dbToPump.setForeignCheckOff();
		dbToPump.setUniqueCheckOff();
		
		Generator gen = new Generator(dbToPump);
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
	public void pumpDatabase(float percentage){
		
		long startTime = System.currentTimeMillis();
		
		dbToPump.setForeignCheckOff();
		dbToPump.setUniqueCheckOff();
		
		Generator gen = new Generator(dbToPump);
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
		MappingAnalyzer.setInstance(dbOriginal, Conf.mappingsFile());
		MappingAnalyzer mA = MappingAnalyzer.getInstance();
		
//		mA.initTuples();
		
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