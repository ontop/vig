package columnTypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import geometry.Linestring;
import geometry.Point;
import geometry.Polygon;
import connection.DBMSConnection;

public class PolygonColumn extends IncrementableColumn<Polygon>{

	private double globalMinX;
	private double globalMaxX;
	
	private double globalMinY;
	private double globalMaxY;
	
	public PolygonColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		lastInserted = null;
		domain = null;
		domainIndex = 0;

		globalMinX = 0;
		globalMaxX = Double.MAX_VALUE;
		globalMinY = 0;
		globalMaxY = Double.MAX_VALUE;
	}

	/**
	 * I do not make use of <b>domain</b> for polygons
	 */
	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		domain = new ArrayList<Polygon>(); // Although I might want to keep this null
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		// I need to find out gobalMaxXs etc. Then
		// I will produce rectangular areas accordingly
		
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT AsWKT("+getName()+") FROM "+schema.getTableName() +" LIMIT 100000");
		
		List<Polygon> retrievedPolygons = new ArrayList<Polygon>();
		
		try {
			ResultSet rs = stmt.executeQuery();
		
			while( rs.next() ){
				retrievedPolygons.add(new Polygon(rs.getString(1)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if( retrievedPolygons.size() != 0 ){
			
			globalMinY = globalMinX = Double.MAX_VALUE;
			globalMaxY = globalMaxX = Double.MIN_VALUE;
			
			// TODO Fill globals
			for( Polygon polygon : retrievedPolygons ){
				for( Linestring linestring : polygon.toLinestringList() ){
					for( Point p : linestring.toPointsList() ){
						if( globalMaxX < p.getX() ) globalMaxX = p.getX();
						if( globalMaxY < p.getY() ) globalMaxY = p.getY();
						if( globalMinX > p.getX() ) globalMinX = p.getX();
						if( globalMinY > p.getY() ) globalMinY = p.getY();
					}
				}
			}
			
			min = Polygon.getInstanceFromRectangle(globalMinX, globalMinY, globalMinX + 1, globalMinY + 1);
			max = Polygon.getInstanceFromRectangle(globalMinX, globalMinY, globalMaxX, globalMaxY);
		}
		else{
			min = new Polygon("Polygon((0 0,1 0,1 1,0 1,0 0))");
			max = new Polygon("Polygon((0 0,"+Double.MAX_VALUE+" 0,"+Double.MAX_VALUE+" "+Double.MAX_VALUE+",0 "+Double.MAX_VALUE+",0 0))");
			
			lastInserted = min;
			
			globalMaxX = globalMaxY = Double.MAX_VALUE;
			globalMinX = globalMinY = Double.MIN_VALUE;
		}
		
	}

	@Override
	public Polygon increment(Polygon toIncrement) {
		// Generate recantangles
		// 
		//  p2       p3
		//
		//  p1       p4
		//
		
		List<Linestring> linestrings = toIncrement.toLinestringList();
		
		// Reverse iterator
		for( int i = linestrings.size() - 1; i >= 0; --i ){
			Linestring l = linestrings.get(i);
			
			List<Point> points = l.toPointsList();
			
			Point p2 = points.get(1);
			Point p3 = points.get(2);
			Point p4 = points.get(3);
	
			if( p2.getX() < globalMaxX ){
				p2.incrementX(); p3.incrementX(); 
				break;
			}
			if( p3.getY() < globalMaxY ){
				p3.incrementY(); p4.incrementY();
				break;
			}
		}
		return toIncrement;
	}

	@Override
	public Polygon getCurrentMax() {
		return max;
	}
	

}
