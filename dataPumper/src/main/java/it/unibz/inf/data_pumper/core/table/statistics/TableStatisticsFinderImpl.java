package it.unibz.inf.data_pumper.core.table.statistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.unibz.inf.data_pumper.basic_datatypes.QualifiedName;
import it.unibz.inf.data_pumper.basic_datatypes.Schema;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.connection.exceptions.InstanceNullException;
import it.unibz.inf.data_pumper.core.main.DEBUGEXCEPTION;
import it.unibz.inf.data_pumper.core.main.table.statistics.aggrclasses.Distribution;

public class TableStatisticsFinderImpl implements TableStatisticsFinder{

	private Distribution distribution;
	
	public TableStatisticsFinderImpl(DBMSConnection dbmsConn) {
		this.distribution = new Distribution(dbmsConn);
	}
	
	@Override
	public float findDuplicatesRatio(Schema s, ColumnPumper<? extends Object> column){
		float ratio = 0; // Ratio of the duplicates

		// First of all, I need to understand the distribution of duplicates. Window analysis!
		ratio = distribution.dupsRatioNaive(column.getName(), s.getTableName());
		
		return ratio;
	}	

	@Override
	public float findNullRatio(Schema s, ColumnPumper<? extends Object> column){
		float ratio = 0;
		ratio = distribution.nullRatioNaive(column.getName(), s.getTableName());
		return ratio;
	}

	@Override
	public <T> float findSharedRatio(ColumnPumper<T> col,
			ColumnPumper<T> referenced) throws SQLException, InstanceNullException, ValueUnsetException {
		
		float sharedRatio = 0;
		
		String colName = col.getName(); String colTableName = col.getSchema().getTableName();
		String refName = referenced.getName(); String refTableName = referenced.getSchema().getTableName();
		
		int sharedDistinctRowsOriginal = 
				distribution.sharedDistinctRows(colName, colTableName, 
						refName, refTableName);
		
		int colNRowsOriginal = DBMSConnection.getInstance().getNRows(col.getSchema().getTableName());
		int nDupsOriginal = (int) (colNRowsOriginal * col.getDuplicateRatio());
		int nNullsOriginal = (int) (colNRowsOriginal * col.getNullRatio());
		int nDistinctOriginal = colNRowsOriginal - nDupsOriginal - nNullsOriginal;
		
		if( sharedDistinctRowsOriginal != 0 ){
			sharedRatio = ((float) sharedDistinctRowsOriginal) / ((float) nDistinctOriginal);
		}
		
		return sharedRatio;
	}

	/**
	 * cols.size() == 1 ? 1
	 * @throws DEBUGEXCEPTION 
	 */
    @Override
    public <T> float findSharedRatio(
            List<ColumnPumper<T>> cols)
            throws SQLException,
            InstanceNullException,
            ValueUnsetException, DEBUGEXCEPTION {
                
        if( ! (cols.size() > 1) ){
            throw new DEBUGEXCEPTION("Violated assertion cols.size() > 1");
        }
        
        float sharedRatio = 0;
        
        List<QualifiedName> columnsNames = new ArrayList<QualifiedName>();
        for( ColumnPumper<T> c : cols ){
            columnsNames.add(c.getQualifiedName());
        }
        
        int sharedDistinctRowsOriginal = 
                distribution.sharedDistinctRows(columnsNames);
        
        if( sharedDistinctRowsOriginal > 0 ){
            ColumnPumper<T> beingInsertedCol = cols.get(0);
            
            int colNRowsOriginal = DBMSConnection.getInstance().getNRows(beingInsertedCol.getSchema().getTableName());
            int nDupsOriginal = (int) (colNRowsOriginal * beingInsertedCol.getDuplicateRatio());
            int nNullsOriginal = (int) (colNRowsOriginal * beingInsertedCol.getNullRatio());
            int nDistinctOriginal = colNRowsOriginal - nDupsOriginal - nNullsOriginal; 
            
            sharedRatio = ((float) sharedDistinctRowsOriginal) / ((float) nDistinctOriginal);
        }
        
        return sharedRatio;
        
    }
}
