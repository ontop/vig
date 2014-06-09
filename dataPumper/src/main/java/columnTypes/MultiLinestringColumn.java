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
import geometry.MultiLinestring;
import geometry.Point;
import geometry.Polygon;

public class MultiLinestringColumn extends IncrementableColumn<MultiLinestring>{

	private BigDecimal globalMinX;
	private BigDecimal globalMaxX;
	
	private BigDecimal globalMinY;
	private BigDecimal globalMaxY;
	
	public MultiLinestringColumn(String name, MySqlDatatypes type, int index) {
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
		// NOT USED.
		domain = new ArrayList<MultiLinestring>(); // Although I might want to keep this null
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		// I need to find out gobalMaxXs etc. Then
		// I will produce rectangular areas accordingly
		
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT AsWKT("+getName()+") FROM "+schema.getTableName() +" "
				+ " WHERE AsWKT("+ getName() +") IS NOT NULL LIMIT 100000");
		
		List<Polygon> retrievedPolygons = new ArrayList<Polygon>();
		
		try {
			ResultSet rs = stmt.executeQuery();
		
			while( rs.next() ){
				String retrieved = rs.getString(1);
				if( retrieved != null )
					retrievedPolygons.add(new Polygon(rs.getString(1)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if( retrievedPolygons.size() != 0 ){
			
			globalMinY = BigDecimal.valueOf(Double.MAX_VALUE);
			globalMinX = BigDecimal.valueOf(Double.MAX_VALUE);
			globalMaxY = BigDecimal.valueOf(Double.MIN_VALUE);
			globalMaxX = BigDecimal.valueOf(Double.MIN_VALUE);
			
			for( Polygon polygon : retrievedPolygons ){
				for( Linestring linestring : polygon.toLinestringList() ){
					for( Point p : linestring.toPointsList() ){
						if( globalMaxX.compareTo(p.getX()) == -1 ) globalMaxX = p.getX();
						if( globalMaxY.compareTo(p.getY()) == -1 ) globalMaxY = p.getY();
						if( globalMinX.compareTo(p.getX()) == 1 ) globalMinX = p.getX();
						if( globalMinY.compareTo(p.getY()) == 1 ) globalMinY = p.getY();
					}
				}
			}
			
			min = MultiLinestring.getInstanceFromRectangle(globalMinX, globalMinY, globalMinX.add(BigDecimal.ONE), globalMinY.add(BigDecimal.ONE));
			max = MultiLinestring.getInstanceFromRectangle(globalMinX, globalMinY, globalMaxX, globalMaxY);
		}
		else{
			min = new MultiLinestring("Multilinestring((0 0,1 0,1 1,0 1,0 0))");
			max = new MultiLinestring("Multilinestring((0 0,"+Double.MAX_VALUE+" 0,"+Double.MAX_VALUE+" "+Double.MAX_VALUE+",0 "+Double.MAX_VALUE+",0 0))");
			
			
			globalMaxX = BigDecimal.valueOf(Double.MAX_VALUE);
			globalMaxY = BigDecimal.valueOf(Double.MAX_VALUE);
			globalMinX = BigDecimal.valueOf(Double.MIN_VALUE);
			globalMinY = BigDecimal.valueOf(Double.MIN_VALUE);
		}
		setLastFreshInserted(min);		
	}

	@Override
	public MultiLinestring increment(MultiLinestring toIncrement) {
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
	
			if( p2.getX().compareTo(globalMaxX) == -1 ){
				p2.incrementX(); p3.incrementX(); 
				break;
			}
			if( p3.getY().compareTo(globalMaxY) == -1 ){
				p3.incrementY(); p4.incrementY();
				break;
			}
		}
		return toIncrement;
	}

	@Override
	public MultiLinestring getCurrentMax() {
		return max;
	}

	@Override
	public String getNextChased(DBMSConnection db, Schema schema) {
		return cP.pickChase(db, schema);
	}

	@Override
	public void proposeLastFreshInserted(String inserted) {
		// TODO Auto-generated method stub
		
	}

}
