package core;

import java.util.List;

import org.apache.log4j.Logger;

import columnTypes.ColumnPumper;
import utils.TrivialQueue;
import basicDatatypes.Schema;
import connection.DBMSConnection;

// Speed trick 
// Connection c = DriverManager.getConnection("jdbc:mysql://host:3306/db?useServerPrepStmts=false&rewriteBatchedStatements=true", "username", "password");
// TODO Try
// Tried. Very Well.
public class Main {
	
	private Generator gen;
	
	private static Logger logger = Logger.getLogger(Main.class.getCanonicalName());
		
	public static void main(String[] args){
		
//		DBMSConnection dbmsConn = new DBMSConnection("jdbc:mysql","10.7.20.39:3306/pumperTest", "test", "ontop2014");
		
		
	}
	
	/**
	 * 
	 * @param originalDb
	 * @param db
	 * @param nRows
	 */
	public void pumpDatabase(DBMSConnection originalDb, DBMSConnection db, float percentage){
		Generator gen = new Generator(db);
		
		TrivialQueue<Schema> schemas = new TrivialQueue<Schema>();
		
		// Init the queue
		for( String tableName : db.getAllTableNames()){
			schemas.enqueue(db.getSchema(tableName));
		}
		
		// Breadth first strategy
		// TODO I need a limit, for the moment I put an hard one.
		int cnt = 0;
		while(schemas.hasNext()){
			Schema schema = schemas.dequeue();
			
			fillDomain(schema, originalDb);
			
			List<Schema> toChase = null;
			if(schema.isFilled()){ // 
				toChase = gen.pumpTable(1, schema);
			}
			else{
				int nRows = originalDb.getNRows(schema.getTableName());
				nRows = (int) (nRows * percentage);
				
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
	}
	
	/**
	 * 
	 * @param originalDb
	 * @param db
	 * @param nRows
	 */
	public void pumpDatabase(DBMSConnection originalDb, DBMSConnection db, int nRows){
		Generator gen = new Generator(db);
		
		TrivialQueue<Schema> schemas = new TrivialQueue<Schema>();
		
		// Init the queue
		for( String tableName : db.getAllTableNames()){
			schemas.enqueue(db.getSchema(tableName));
		}
		
		// Breadth first strategy
		// TODO I need a limit, for the moment I put an hard one.
		int cnt = 0;
		while(schemas.hasNext()){
			Schema schema = schemas.dequeue();
			
			fillDomain(schema, originalDb);
			
			List<Schema> toChase = null;
			if(schema.isFilled()){ // 
				toChase = gen.pumpTable(1, schema);
			}
			else{
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
	}

	private void fillDomain(Schema schema, DBMSConnection originalDb) {
		for( ColumnPumper column : schema.getColumns() ){
			column.fillDomain(schema, originalDb);
			column.fillDomainBoundaries(schema, originalDb);
		}
	}
};