package connection;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.PreparedStatement;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKBReader;

public class GeometryAttempt {
	private static String jdbcConnector = "jdbc:mysql";
	private static String databaseUrl = "10.7.20.39:3306/npd";
	private static String username = "fish";
	private static String password = "fish";
	
	
	public static void main(String[] args) {
		
		DBMSConnection dbmc = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		Connection conn = dbmc.getConnection();
		 
		Statement statement;
		try {
			statement = conn.createStatement();
//			String query = "SELECT AsText(wlbPointGeometryWKT) FROM npd.wlbPoint";
//			
//			ResultSet resultSet;
//			
//			resultSet = statement.executeQuery(query);
//			
//			while(resultSet.next()) {
//				
//				//MySQL geometries are returned in JDBC as binary streams.  The
//				//stream will be null if the record has no geometry.
//				//InputStream inputStream = resultSet.getBinaryStream("wlbPointGeometryWKT");
//			Geometry geometry = getGeometryFromInputStream(inputStream);
//				System.out.println(resultSet.getString(1));
//				
//				// do something with geometry...
//			}	
			
			String query1 = "SELECT AsWKT(prlAreaGeometryWKT) FROM npd.prlArea";
			ResultSet resultSet1;
			resultSet1 = statement.executeQuery(query1);
			
			while(resultSet1.next()) {
				
				//MySQL geometries are returned in JDBC as binary streams.  The
				//stream will be null if the record has no geometry.
//				InputStream inputStream = resultSet1.getBinaryStream("prlAreaGeometryWKT");
//				Geometry geometry = getGeometryFromInputStream(inputStream);
				
				System.out.println(resultSet1.next());
				
				// do something with geometry...
			}	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
//		Here’s the code that does the actual InputStream to Geometry conversion:
		private static Geometry getGeometryFromInputStream(InputStream inputStream) throws Exception {
		
		Geometry dbGeometry = null;
		
		if (inputStream != null) {
			
			//convert the stream to a byte[] array
			//so it can be passed to the WKBReader
			byte[] buffer = new byte[255];
			
			int bytesRead = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}
			
			byte[] geometryAsBytes = baos.toByteArray();
			
			if (geometryAsBytes.length < 5) {
				throw new Exception("Invalid geometry inputStream - less than five bytes");
			}
			
			//first four bytes of the geometry are the SRID,
			//followed by the actual WKB.  Determine the SRID
			//here
			byte[] sridBytes = new byte[4];
			System.arraycopy(geometryAsBytes, 0, sridBytes, 0, 4);
			boolean bigEndian = (geometryAsBytes[4] == 0x00);
			
			int srid = 0;
			if (bigEndian) {
				for (int i = 0; i < sridBytes.length; i++) {
					srid = (srid << 8) + (sridBytes[i] & 0xff);
				}
			} else {
				for (int i = 0; i < sridBytes.length; i++) {
					srid += (sridBytes[i] & 0xff) << (8 * i);
				}
			}
			
			//use the JTS WKBReader for WKB parsing
			WKBReader wkbReader = new WKBReader();
			
			//copy the byte array, removing the first four
			//SRID bytes
			byte[] wkb = new byte[geometryAsBytes.length - 4];
			System.arraycopy(geometryAsBytes, 4, wkb, 0, wkb.length);
			dbGeometry = wkbReader.read(wkb);
			dbGeometry.setSRID(srid);
		}
		
		return dbGeometry;
	}
};

