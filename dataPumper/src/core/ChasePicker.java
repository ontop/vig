package core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import connection.DBMSConnection;
import basicDatatypes.QualifiedName;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import columnTypes.ColumnPumper;

public class ChasePicker {

	private ColumnPumper column;
	
	private int maximumChaseCycles; // The maximum number of times fresh elements should be created for this column 
	// --- Each fresh element triggers a chase if some other column depends on this column
	private int currentChaseCycle;  // Number of times that this column triggered a chase during pumping
	
	private int chaseFrom; // The column from which one has to chase
	public ResultSet toChase;
	
	protected static Logger logger = Logger.getLogger(ChasePicker.class.getCanonicalName());
	
	public ChasePicker(ColumnPumper column){
		this.column = column;
	}
	
	public boolean hasNextChaseSet(){
		if( column.referencedBy() == null ) return false;
		if( chaseFrom + 1 < column.referencedBy().size() ){ return true; }
		return false;
	}
	
	public boolean nextChaseSet(){
		if( column.referencedBy() == null ) return false;
		if( chaseFrom + 1 < column.referencedBy().size() ){ ++chaseFrom; return true; }
		return false;
	}
	
	public boolean toChase(){
		return (column.referencedBy() != null) && chaseFrom < column.referencedBy().size(); 
	}

	
	public void refillCurChaseSet(DBMSConnection db, Schema s){
		if( toChase != null ){
			try {
				toChase.close();
				toChase = fillChaseValues(db, s);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String pickChase(DBMSConnection db, Schema s){
		if( toChase != null ){
			try {
				if( toChase.next() ){
					return toChase.getString(1);
				}
				else{
					if( nextChaseSet() ){
						toChase.close();
						toChase = fillChaseValues(db, s);
						return pickChase(db, s);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if( toChase() ){
			toChase = fillChaseValues(db, s);
			return pickChase(db, s);
		}
		return null;
	}
		
	private ResultSet fillChaseValues(DBMSConnection dbmsConn, Schema schema) {
		// SELECT referredByCol FROM referredByTable WHERE referredByCol NOT IN (SELECT column.name() FROM schema.name()); 
		// TODO Distinguish between geometric and non-geometric		
		
		Template query = null;
		ResultSet rs = null;
		
		if( column.isGeometric() ){
			query = new Template("SELECT DISTINCT AsText(?) FROM ? WHERE AsText(?) IS NOT NULL AND "
					+ "AsText(?) NOT IN (SELECT AsText(?) FROM ?)");
		}
		else{
			query = new Template("SELECT DISTINCT ? FROM ? WHERE ? IS NOT NULL AND ? NOT IN (SELECT ? FROM ?)");
		}
		QualifiedName referencedBy = column.referencedBy().get(chaseFrom);
		
		// Fill the query
		query.setNthPlaceholder(1,referencedBy.getColName());
		query.setNthPlaceholder(2, referencedBy.getTableName());
		query.setNthPlaceholder(3, referencedBy.getColName());
		query.setNthPlaceholder(4, referencedBy.getColName());
		query.setNthPlaceholder(5, column.getName());
		query.setNthPlaceholder(6, schema.getTableName());
		
		try {
			PreparedStatement stmt = dbmsConn.getPreparedStatement(query);
			rs = stmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	public boolean hasNextChase(){
		
		boolean returnVal = false;
		
		if( toChase != null ){
			try {
				returnVal = !(toChase.isLast() || toChase.isAfterLast());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if( hasNextChaseSet() ){
			returnVal = true;
		}
		
		return returnVal;
	}
	
	public void reset(){
		chaseFrom = 0;
		if( toChase != null ){
			try {
				toChase.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		toChase = null;
	}
};
