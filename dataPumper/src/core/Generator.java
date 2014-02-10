package core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import basicDatatypes.*;

public class Generator {
	private Connection conn;
	private RandomDBValuesGenerator random;
	
	public Generator(Connection conn){
		this.conn = conn;
		random = new RandomDBValuesGenerator();
	}
	
	/**
	 * @author tir
	 * @param tableName
	 * @param domainIndependentCols : May be null, and it specifies what columns HAVE TO be considered as domainIndependent.
	 * 
	 * Pump the table, column by column. Each element will be randomly chosen 
	 * according to the distribution of the database. Primary keys, instead, will
	 * be newly generated
	 * 
	 * Domain independent columns will be generated by taking values from the domain 
	 * (that is retrieved by a projection on the database column
	 *  -- therefore I assume that the db is NON-EMPTY [as in FactPages])
	 * 
	 * Domain independend columns can also be inferred, by looking at the projection and comparing it 
	 * against the total number of tuples in the table <b>tableName</b>
	 */
	public void pumpTable(String tableName, int nRows, Schema schema, boolean inferDomainIndependent){		
		
		
		String templateInsert = createInsertTemplate(schema);
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement(templateInsert);
			
			for( int j = 0; j < nRows; ++j ){
				
				// By understanding the types, one can decide what to generate
				int i = 0;
				for( String colName : schema.getColNames() ){
					switch(schema.getType(colName)){
					case INT: {
						Domain<Integer> dom = (Domain<Integer>) schema.getDomain(colName);
						int debug = random.getRandomInt(dom);
						stmt.setInt(++i, random.getRandomInt(dom));
					}
					case CHAR:
						break;
					case DATETIME:
						break;
					case LINESTRING:
						break;
					case LONGTEXT:
						break;
					case MULTILINESTRING:
						break;
					case MULTIPOLYGON:
						break;
					case POINT:
						break;
					case POLYGON:
						break;
					case TEXT:
						break;
					case VARCHAR : {
						Domain<String> dom = (Domain<String>) schema.getDomain(colName);
						stmt.setString(++i, random.getRandomString(dom));
						break;
					}

					default:
						break;
					}
				}
				stmt.addBatch();
			} // End of REPEAT nRows
			stmt.executeBatch();			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param s
	 * @return
	 * 
	 * Types in FactPages db
	 * int, datetime, varchar, decimal, char, text, longtext, point, linestring, polygon, multipolygon, multilinestring
	 */
	public void fillDomainBoundaries(Schema s){
		
		Map<String, Domain<?>> domains = new HashMap<String, Domain<?>>();

		try {
			Template t = new Template("select ? from "+s.getTableName()+" group by ?;");
			PreparedStatement stmt;
						
			for( String colName : s.getColNames() ){
				
				switch(s.getType(colName)){
				case INT : {
					
					// TODO One has to understand whether the domain is dependent to the db size or not.
					
					t.setNthPlaceholder(1, "min("+colName+"), max("+colName+")");
					t.setNthPlaceholder(2, colName);
					
					stmt = conn.prepareStatement(t.getFilled());
					ResultSet result = stmt.executeQuery();
										
					while( result.next() ){
						domains.put(colName, new Domain<Integer>(result.getInt(1), result.getInt(2)));
					}
					break;
				}
				case CHAR : {
					break;
				}
				case VARCHAR : {
					break;
				}
				case TEXT : {
					break;
				}
				case LONGTEXT : {
					break;
				}
				case DATETIME : {
					// Not sure whether etc.
					break;
				}
				case POINT : {
					break;
				}
				case LINESTRING : {
					break;
				}
				case MULTILINESTRING : {
					break;
				}
				case POLYGON : {
					break;
				}
				case MULTIPOLYGON : {
					break;
				}
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		s.setDomains(domains);
	}
	/**
	 * 
	 * @author tir
	 * @param s: Schema of the table for which an insert query has to be created
	 * @return A template insert query with a suitable number of place-holders
	 */
	public String createInsertTemplate(Schema s){
		StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("INSERT into "+s.getTableName()+" (");
		int i = 0;
		
		for( String colName : s.getColNames() ){
			insertQuery.append(colName);
			if( ++i < s.getNumColumns() )
				insertQuery.append(", ");
		}
		insertQuery.append(") VALUES (");
		
		for( i = 0; i < s.getNumColumns(); ++i ){
			insertQuery.append("?");
			if( i < s.getNumColumns() - 1 )
				insertQuery.append(", ");
		}
		insertQuery.append(");");
		
		return insertQuery.toString();
	}
	
	public Schema getTableSchema(String tableName){
		Schema schema = new Schema();
		
		try{
			PreparedStatement stmt;
			stmt = conn.prepareStatement("DESCRIBE "+tableName);
			ResultSet result = stmt.executeQuery();
			
			// Field - Type - Null - Default - Extra
			while(result.next()){
				schema.addField(result.getString(1), result.getString(2));
			}
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		schema.setTableName(tableName);
		return schema;
	}
};