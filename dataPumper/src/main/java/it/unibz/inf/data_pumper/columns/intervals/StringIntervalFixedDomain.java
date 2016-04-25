package it.unibz.inf.data_pumper.columns.intervals;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.tables.MySqlDatatypes;
import it.unibz.inf.data_pumper.utils.Template;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class StringIntervalFixedDomain extends StringInterval {

    private List<String> fixedDomainValues;
    
    public StringIntervalFixedDomain(String key, MySqlDatatypes type,
	    long nValues, 
	    List<ColumnPumper<String>> involvedCols) throws SQLException {
	super(key, type, nValues, involvedCols);

	fixedDomainValues = new ArrayList<String>();
	
	create();
    }
    
    private void create() throws SQLException{
	String tableName = this.getInvolvedColumnPumpers().iterator().next().getSchema().getTableName();
	String colName = this.getInvolvedColumnPumpers().iterator().next().getName();
	
	Template t = new Template("select DISTINCT "+colName+" from "+tableName+" WHERE "+colName+" IS NOT NULL;");
        PreparedStatement stmt;

        stmt = DBMSConnection.getInstance().getPreparedStatement(t);

        ResultSet result;

        result = stmt.executeQuery();
        while( result.next() ){
            fixedDomainValues.add(result.getString(1));
        }
        stmt.close();
    }
    
    @Override
    public long getMaxEncoding() {
        return this.maxEncoding;
    }
    
    @Override
    public String encode(long value){
	return fixedDomainValues.get((int) value);
    }

    @Override
    public Interval<String> getCopyInstance() {
        throw new RuntimeException("Cannot copy fixed-domain");
    }
    
    @Override
    public String lowerBoundValue(){
	StringBuilder builder = new StringBuilder();
	return builder.toString();
    }

    @Override
    public void updateMaxEncodingAndValue(long newMax) {
	this.maxEncoding = newMax;
    }

    @Override
    public long getMinEncoding() {
	return 0;
    }

    @Override
    protected String upperBoundValue() {
	return fixedDomainValues.get( fixedDomainValues.size() - 1 );
    }

    @Override
    public void synchronizeMinMaxNFreshs() {
	// TODO Auto-generated method stub
	
    }   
};
