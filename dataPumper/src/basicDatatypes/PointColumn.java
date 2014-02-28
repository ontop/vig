package basicDatatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import connection.DBMSConnection;
import utils.wktparsers.PointParserWKT;


/**
 * In NPD, no geographical type is involved in primary keys constraints 
 * @author tir
 *
 */
public class PointColumn extends Column {

	private long minX;
	private long maxX;
	
	private long minY;
	private long maxY;
	
	private List<Long> xDomain;
	private List<Long> yDomain;
	
	private int yIndex;
	private int xIndex;
	
	private String lastInserted;
	
	private PointParserWKT parser;
	
	public PointColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		xDomain = null;
		yDomain = null;
		index = 0;
		lastInserted = null;
		minX = 0;
		maxX = 0;
		minY = 0;
		maxY = 0;
		parser = new PointParserWKT();
		yIndex = 0;
		xIndex = 0;
	}
	
	public void setMaxX(long x){
		maxX = x;
	}
	
	public void setMaxY(long y){
		maxY = y;
	}
	
	public void setMinX(long x){
		minX = x;
	}
	
	public void setMinY(long y){
		minY = y;
	}
	/**
	 * 
	 * @param pointWKT A well-known-text representation of a GIS point
	 */
	public void setLastInserted(String pointWKT){
		this.lastInserted = pointWKT;
	}
	
	public void setDomain(List<String> Points){
		xDomain = new ArrayList<Long>();
		yDomain = new ArrayList<Long>();
		
		for( String pointWKT : Points ){ // WKT stands for "well-known-text"
			parser.parse(pointWKT);
			xDomain.add(parser.getX());
			yDomain.add(parser.getY());
		}
	}

	// TODO Make it more powerful
	@Override
	public String getNextFreshValue() {
		
		if( lastInserted == null ){
			lastInserted = "Point(0 0)";
			return parser.toWKT(0, 0);
		}

		long newX = 0;
		long newY = 0;

		parser.parse(lastInserted);
		long lastX = parser.getX();
		long lastY = parser.getY();
		
		if( xDomain.size() == 0){
			
			if( lastX < maxX ){
				newX = ++lastX;
			}
			else{
				newY = ++lastY;
			}
		}
		else{
			
			while( yIndex < yDomain.size() && ++lastY == yDomain.get(yIndex) ) ++yIndex;
			if( yIndex < yDomain.size() ){
				newX = lastX;
				newY = lastY;
			}
			else{
				
			}
			
		}
		
		lastInserted = parser.toWKT(newX, newY);
		return parser.toWKT(newX, newY);
	}

	@Override
	public void reset() {
		xDomain.clear();
		yDomain.clear();
		
		index = 0;
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		
		String queryString = "SELECT DISTINCT AsWKT("+getName()+") FROM "+schema.getTableName();
		
		PreparedStatement stmt = db.getPreparedStatement(queryString);
	
		List<String> retrievedPoints = new ArrayList<String>();
		
		try {
			ResultSet rs = stmt.executeQuery();
		
			while( rs.next() ){
				retrievedPoints.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		setDomain(retrievedPoints);
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		
		Template template = new Template("SELECT min(?("+getName()+")), max(?("+getName()+")) FROM "+schema.getTableName());
		
		PreparedStatement stmt = null; 
		
		try{
			
			template.setNthPlaceholder(1, "x");
			template.setNthPlaceholder(2, "x");
			
			stmt = db.getPreparedStatement(template);

			ResultSet rs = stmt.executeQuery();

			if( rs.next() ){
				minX = rs.getInt(1);
				maxX = rs.getLong(1);
			}
			
			stmt.close();
			
			template.setNthPlaceholder(1, "y");
			template.setNthPlaceholder(2, "y");

			stmt = db.getPreparedStatement(template);
			
			rs = stmt.executeQuery();

			if( rs.next() ){
				minX = rs.getInt(1);
				maxX = rs.getLong(1);
			}
			
			stmt.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
}
