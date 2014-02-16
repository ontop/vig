package core;

import java.util.LinkedList;
import java.util.List;

import basicDatatypes.Schema;

/**
 * An ordering might not be possible (e.g., self foreign keys or cycles of fk dependencies). 
 * In those cases, the schemas of the "problematic" tables will be filled with the necessary
 * information to deal with the problem (i.e., the solution is deferred to other methods).
 * 
 * @author tir
 *
 */
public class SchemaOrderer {
//	private List<Schema> schemas = null;
//	private Generator gen;
//	private String dbName;
//	
//	public SchemaOrderer(Generator gen, String dbName){
//		this.gen = gen;
//		this.dbName = dbName;
//	}
//	
//	public List<Schema> getOrderedSchemas(){
//		if( schemas != null ) return schemas;
//		
//		schemas = new LinkedList<Schema>();
//		
//		for( String tableName : gen.getAllTableNamesOf(dbName) ){
//			List<String> visited = new LinkedList<String>(); // For cycle detection and report
//			Boolean cycleDetected = new Boolean(false);      
//			addSchema(tableName, visited, cycleDetected); 
//			
//			if(cycleDetected){
//				tagProblematicSchemas();
//			}
//		}
//		
//		return schemas;
//	}
//	
//	private void addSchema(String tableName, List<String> visited, Boolean cycleDetected){
//		if(visited.contains(tableName)){
//			cycleDetected = true;
//			return; 
//		}
//		Schema curSchema = gen.getTableSchema(tableName);
//		List<String> fks = curSchema.getFks();
//		
//		for( String fk : fks ){
//			String refTableName = curSchema.getReferencedTable(fk);
//			if( !schemas.contains(gen.getTableSchema(refTableName)) ) addSchema(refTableName, visited, cycleDetected);
//		}
//		if( !schemas.contains(curSchema) ) schemas.add(curSchema);
//	}
//	
//	private void tagProblematicSchemas(){
//		// Tag and Insert
//	}
}
