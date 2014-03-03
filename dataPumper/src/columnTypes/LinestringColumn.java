package columnTypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import connection.DBMSConnection;
import geometry.Linestring;
import geometry.Point;

public class LinestringColumn extends IncrementableColumn<Linestring>{
	
	private long globalMinX;
	private long globalMaxX;
	
	private long globalMinY;
	private long globalMaxY;
	
	
	
	public LinestringColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		lastInserted = null;
		domain = null;
		domainIndex = 0;
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		
		if( domain != null && domain.size() != 0 ) return; // To avoid duplicate calls
		
		String queryString = "SELECT DISTINCT AsWKT("+getName()+") FROM "+schema.getTableName();
		
		PreparedStatement stmt = db.getPreparedStatement(queryString);
		
		List<Linestring> retrievedPoints = new ArrayList<Linestring>();
		
		try {
			ResultSet rs = stmt.executeQuery();
			
			while( rs.next() ){
				retrievedPoints.add(new Linestring(rs.getString(1)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if( retrievedPoints.size() != 0 ){
			
			Collections.sort(retrievedPoints);
			
			min = retrievedPoints.get(0);
			max = retrievedPoints.get(retrievedPoints.size() -1);
		
			setDomain(retrievedPoints);
		}
		else{
			min = new Linestring("Linestring(0 0,0 0)");
			max = new Linestring("Linestring("+Long.MAX_VALUE+" "+Long.MAX_VALUE+","+Long.MAX_VALUE+" "+Long.MAX_VALUE+")");
			
			lastInserted = min;
			
			domain = retrievedPoints;
		}
	}
		
	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		
		fillDomain(schema, db);
		
		long maxX = Long.MIN_VALUE;
		long maxY = Long.MIN_VALUE;

		long minX = Long.MAX_VALUE;
		long minY = Long.MAX_VALUE;
		
		if( domain == null ) logger.error("Null domain");
		
		if( domain.size() == 0 ){
			globalMaxX = Long.MAX_VALUE;
			globalMinX = Long.MIN_VALUE;
			globalMaxY = Long.MAX_VALUE;
			globalMinY = Long.MIN_VALUE;
			
			return;
		}
		
		for( Linestring ls : domain ){
			for( Point p : ls.toPointsList() ){
				if( p.getX() > maxX ) maxX = p.getX();
				if( p.getX() < minX ) minX = p.getX();
				if( p.getY() > maxY ) maxY = p.getY();
				if( p.getY() < minY ) minY = p.getY();
			}
		}
		globalMaxX = maxX;
		globalMinX = minX;
		globalMaxY = maxY;
		globalMinY = minY;
		
	}

	@Override
	public Linestring increment(Linestring toIncrement) {
		
		List<Point> points = toIncrement.toPointsList();
		
		// Reverse iterator
		for( int i = points.size() - 1; i >= 0; --i ){
			Point p = points.get(i);
			
			if( p.getY() == globalMaxX && p.getY() == globalMaxY ){
				p.setX(globalMinX); p.setY(globalMinY);
			}
			else if( p.getY() == globalMaxY ){
				p.incrementX();
				break;
			}
			else{
				p.incrementY(); break;
			}
		}

		return toIncrement;
	}
	
	@Override
	public Linestring getCurrentMax() {
		if( domain.size() == 0 ){		
			return 
					new Linestring
					("Linestring("+Long.MAX_VALUE +" " + Long.MAX_VALUE +
							","
							+Long.MAX_VALUE + " " + Long.MAX_VALUE + ")");
		}
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}
}
