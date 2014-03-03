package columnTypes;

import geometry.Point;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import connection.DBMSConnection;

public abstract class GeometryColumn<T extends Comparable<? super T>> extends IncrementableColumn<T>{

	public long globalMinX;
	public long globalMaxX;
	
	private long globalMinY;
	private long globalMaxY;
	
	public GeometryColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
	}
	
}
