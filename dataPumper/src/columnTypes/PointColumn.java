package columnTypes;

import geometry.Point;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import connection.DBMSConnection;


/**
 * In NPD, no geographical type is involved in primary keys constraints 
 * @author tir
 *
 */
public class PointColumn extends IncrementableColumn<Point> {
	
	private long globalMinX;
	private long globalMaxX;
	
	private long globalMinY;
	private long globalMaxY;

				
	public PointColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		lastInserted = null;
		domain = null;
		domainIndex = 0;
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		
		String queryString = "SELECT DISTINCT AsWKT("+getName()+") FROM "+schema.getTableName();
		
		PreparedStatement stmt = db.getPreparedStatement(queryString);
	
		List<Point> retrievedPoints = new ArrayList<Point>();
		
		retrievedPoints.add(new Point(globalMinX, globalMinY));
		
		try {
			ResultSet rs = stmt.executeQuery();
		
			while( rs.next() ){
				retrievedPoints.add(new Point(rs.getString(1)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if( retrievedPoints.size() == 0 ) retrievedPoints.clear(); 
		else{
			retrievedPoints.add(new Point(globalMaxX, globalMaxY));
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
				globalMinX = rs.getLong(1);
				globalMaxX = rs.getLong(1);
			}
			
			stmt.close();
			
			template.setNthPlaceholder(1, "y");
			template.setNthPlaceholder(2, "y");

			stmt = db.getPreparedStatement(template);
			
			rs = stmt.executeQuery();

			if( rs.next() ){
				globalMinX = rs.getInt(1);
				globalMaxX = rs.getLong(1);
			}
			
			stmt.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		
		max = new Point(globalMaxX, globalMaxY);
		min = new Point(globalMinX, globalMinY);
		
		setLastInserted(min);
	}

	@Override
	public Point increment(Point toIncrement) {
		// Lexicographical increment
		
		if( toIncrement.getY() < globalMaxY ){ toIncrement.incrementY();}
		else{
			toIncrement.incrementX(); 
			toIncrement.setY(globalMinY);
		}
		return toIncrement;
	}

	@Override
	public Point getCurrentMax() {
		if( domain.size() == 0 )
			return new Point(Long.MAX_VALUE, Long.MAX_VALUE);
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}
}
