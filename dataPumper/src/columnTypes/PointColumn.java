package columnTypes;

import geometry.Point;

import java.math.BigDecimal;
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
 * 
 * In NPD, no polygon is primary key or referenced by a fk. Thus, 
 * the "domain" attribute is not used.
 * 
 * @author tir
 *
 */
public class PointColumn extends IncrementableColumn<Point> {
	
	private BigDecimal globalMinX;
	private BigDecimal globalMaxX;
	
	private BigDecimal globalMinY;
	private BigDecimal globalMaxY;

				
	public PointColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		lastFreshInserted = null;
		domain = null;
		domainIndex = 0;
		geometric = true;
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		
		if( max == null ) fillDomainBoundaries(schema, db);
		
		String queryString = "SELECT DISTINCT AsWKT("+getName()+") FROM "+schema.getTableName() +" "
				+ " WHERE AsWKT("+ getName() +") IS NOT NULL LIMIT 100000";
		
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
		
		if( max != null ) return; // Already filled
		
		Template template = new Template("SELECT min(?("+getName()+")), max(?("+getName()+")) FROM "+schema.getTableName());
		
		PreparedStatement stmt = null; 
		
		try{
			
			template.setNthPlaceholder(1, "x");
			template.setNthPlaceholder(2, "x");
			
			stmt = db.getPreparedStatement(template);

			ResultSet rs = stmt.executeQuery();

			if( rs.next() ){
				globalMinX = BigDecimal.valueOf(rs.getDouble(1));
				globalMaxX = BigDecimal.valueOf(rs.getDouble(2));
			}
			
			stmt.close();
			
			template.setNthPlaceholder(1, "y");
			template.setNthPlaceholder(2, "y");

			stmt = db.getPreparedStatement(template);
			
			rs = stmt.executeQuery();

			if( rs.next() ){
				globalMinY = BigDecimal.valueOf(rs.getDouble(1));
				globalMaxY = BigDecimal.valueOf(rs.getDouble(2));
			}
			
			stmt.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		
		max = new Point(globalMaxX, globalMaxY);
		min = new Point(globalMinX, globalMinY);
		
		setLastFreshInserted(min);
	}

	@Override
	public Point increment(Point toIncrement) {
		// Lexicographical increment
		
		if( toIncrement.getY().compareTo(globalMaxY) == -1 ){ toIncrement.incrementY();}
		else{
			toIncrement.incrementX(); 
			toIncrement.setY(globalMinY);
		}
		return toIncrement;
	}

	@Override
	public Point getCurrentMax() {
		if( domain.size() == 0 )
			return new Point(BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal.valueOf(Double.MAX_VALUE));
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}

	@Override
	public String getNextChased(DBMSConnection db, Schema schema) {
		return cP.pickChase(db, schema);
	}
}
