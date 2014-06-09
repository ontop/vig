package columnTypes;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import connection.DBMSConnection;
import geometry.Linestring;
import geometry.MultiPolygon;
import geometry.Point;
import geometry.Polygon;

public class MultiPolygonColumn extends IncrementableColumn<MultiPolygon>{
	private BigDecimal globalMinX;
	private BigDecimal globalMaxX;
	
	private BigDecimal globalMinY;
	private BigDecimal globalMaxY;
	
	public MultiPolygonColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		lastFreshInserted = null;
		domain = null;
		domainIndex = 0;

		geometric = true;
		globalMinX = BigDecimal.ZERO;
		globalMaxX = BigDecimal.valueOf(Double.MAX_VALUE);
		globalMinY = BigDecimal.ZERO;
		globalMaxY = BigDecimal.valueOf(Double.MAX_VALUE);
	}

	/**
	 * I do not make use of <b>domain</b> for multilinestrings and polygons
	 */
	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		// NOT USED
		domain = new ArrayList<MultiPolygon>(); // Although I might want to keep this null
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		// I need to find out gobalMaxXs etc. Then
		// I will produce rectangular areas accordingly
		
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT AsWKT("+getName()+") FROM "+schema.getTableName() +" "
				+ "WHERE AsWKT("+getName()+") IS NOT NULL LIMIT 100000");
		
		List<MultiPolygon> retrievedMultiPolygons = new ArrayList<MultiPolygon>();
		
		try {
			ResultSet rs = stmt.executeQuery();
		
			while( rs.next() ){
				String retrieved = rs.getString(1);
				if( retrieved != null ){ // Looks like I need this
					retrievedMultiPolygons.add(new MultiPolygon(rs.getString(1)));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if( retrievedMultiPolygons.size() != 0 ){
			
			globalMinY = BigDecimal.valueOf(Double.MAX_VALUE);
			globalMinX = BigDecimal.valueOf(Double.MAX_VALUE);
			globalMaxY = BigDecimal.valueOf(Double.MIN_VALUE);
			globalMaxX = BigDecimal.valueOf(Double.MIN_VALUE);
			
			// TODO Fill globals
			for( MultiPolygon multipolygon : retrievedMultiPolygons ){
				for( Polygon polygon : multipolygon.toPolygonsList() ){
					for( Linestring l : polygon.toLinestringList() ){
						for( Point p : l.toPointsList() ){							
							if( globalMaxX.compareTo(p.getX()) == -1 ) globalMaxX = p.getX();
							if( globalMaxY.compareTo(p.getY()) == -1 ) globalMaxY = p.getY();
							if( globalMinX.compareTo(p.getX()) == 1 ) globalMinX = p.getX();
							if( globalMinY.compareTo(p.getY()) == 1 ) globalMinY = p.getY();
						}
					}
				}
			}
			
			min = MultiPolygon.getInstanceFromRectangle(globalMinX, globalMinY, globalMinX.add(BigDecimal.ONE), globalMinY.add(BigDecimal.ONE));
			max = MultiPolygon.getInstanceFromRectangle(globalMinX, globalMinY, globalMaxX, globalMaxY);
		}
		else{
			min = new MultiPolygon("Multipolygon(((0 0,1 0,1 1,0 1,0 0)))");
			max = new MultiPolygon("Multipolygon(((0 0,"+Double.MAX_VALUE+" 0,"+Double.MAX_VALUE+" "+Double.MAX_VALUE+",0 "+Double.MAX_VALUE+",0 0)))");
			
			
			globalMaxX = BigDecimal.valueOf(Double.MAX_VALUE);
			globalMaxY = BigDecimal.valueOf(Double.MAX_VALUE);
			globalMinX = BigDecimal.valueOf(Double.MIN_VALUE);
			globalMinY = BigDecimal.valueOf(Double.MIN_VALUE);
		}
		
		setLastFreshInserted(min);
	}

	/**
	 * TODO Now I consider only rectangles. Maybe in future ...
	 */
	@Override
	public MultiPolygon increment(MultiPolygon toIncrement) {
		// Generate recantangles
		// 
		//  p2       p3
		//
		//  p1       p4
		//
		
		List<Polygon> polygons = toIncrement.toPolygonsList();
		
		// Reverse iterator
		for( int i = polygons.size() - 1; i >= 0; --i ){
			Polygon p = polygons.get(i);
			
			List<Linestring> linestrings = p.toLinestringList();
			
			for( int j = linestrings.size() - 1; j >= 0; --j ){
				
				Linestring l = linestrings.get(j);
				
				List<Point> points = l.toPointsList();
				
				Point p2 = points.get(1);
				Point p3 = points.get(2);
				Point p4 = points.get(3);
				
				if( p2.getX().compareTo(globalMaxX) == -1 ){
					p2.incrementX(); p3.incrementX(); 
					break;
				}
				if( p3.getY().compareTo(globalMaxY) == -1 ){
					p3.incrementY(); p4.incrementY();
					break;
				}
			}
		}
		return toIncrement;
	}
	
	@Override
	public MultiPolygon getCurrentMax() {
		return max;
	}

	@Override
	public String getNextChased(DBMSConnection db, Schema schema) {
		String chased = cP.pickChase(db, schema);
		
		
		// Now, one should check if it is a (rectangle > than last inserted) and if it is, do
		// setLastFreshInserted(new MultiPolygon(chased));
		
		return chased;
	}

	@Override
	public void proposeLastFreshInserted(String inserted) {
		// TODO Auto-generated method stub
		
	}

}
