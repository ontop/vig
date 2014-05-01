package columnTypes;

import java.math.BigDecimal;
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
	
	private BigDecimal globalMinX;
	private BigDecimal globalMaxX;
	
	private BigDecimal globalMinY;
	private BigDecimal globalMaxY;
	
	public LinestringColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		
		geometric = true;
		lastFreshInserted = null;
		domain = null;
		domainIndex = 0;
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		
		if( domain != null && domain.size() != 0 ) return; // To avoid duplicate calls
		
		String queryString = "SELECT DISTINCT AsWKT("+getName()+") FROM "+schema.getTableName()+ " WHERE "+getName()+" IS NOT NULL";
		
		PreparedStatement stmt = db.getPreparedStatement(queryString);
		
		List<Linestring> retrievedPoints = new ArrayList<Linestring>();
		
		try {
			ResultSet rs = stmt.executeQuery();
			
			while( rs.next() ){
				String retrieved = rs.getString(1);
				if( retrieved != null && retrieved.startsWith("LINESTRING")){
					retrievedPoints.add(new Linestring(rs.getString(1)));
				}
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
			max = new Linestring("Linestring("+Double.MAX_VALUE+" "+Double.MAX_VALUE+","+Double.MAX_VALUE+" "+Double.MAX_VALUE+")");
			
			setDomain(retrievedPoints);
		}
		
		lastFreshInserted = new Linestring(min.toString());
	}
		
	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		
		if( domain == null ) fillDomain(schema, db);
		
		BigDecimal maxX = BigDecimal.valueOf(Double.MIN_VALUE);
		BigDecimal maxY = BigDecimal.valueOf(Double.MIN_VALUE);

		BigDecimal minX = BigDecimal.valueOf(Double.MAX_VALUE);
		BigDecimal minY = BigDecimal.valueOf(Double.MAX_VALUE);
		
		if( domain == null ) logger.error("Null domain");
		
		if( domain.size() == 0 ){
			globalMaxX = BigDecimal.valueOf(Double.MAX_VALUE);
			globalMinX = BigDecimal.valueOf(Double.MIN_VALUE);
			globalMaxY = BigDecimal.valueOf(Double.MAX_VALUE);
			globalMinY = BigDecimal.valueOf(Double.MIN_VALUE);
			
			return;
		}
		
		for( Linestring ls : domain ){
			for( Point p : ls.toPointsList() ){
				if( p.getX().compareTo(maxX) == 1) maxX = p.getX();
				if( p.getX().compareTo(minX) == -1 ) minX = p.getX();
				if( p.getY().compareTo(maxY) == 1 ) maxY = p.getY();
				if( p.getY().compareTo(minY) == -1 ) minY = p.getY();
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
			
			if( p.getY().compareTo(globalMaxX) == 0 && p.getY().compareTo(globalMaxY) == 0 ){
				p.setX(globalMinX); p.setY(globalMinY);
			}
			else if( p.getY().compareTo(globalMaxY) == 0 ){
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
					("Linestring("+Double.MAX_VALUE +" " + Double.MAX_VALUE +
							","
							+Double.MAX_VALUE + " " + Double.MAX_VALUE + ")");
		}
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}

	@Override
	public String getNextChased(DBMSConnection db, Schema schema) {
		return cP.pickChase(db, schema);
	}
}
