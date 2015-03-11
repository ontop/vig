package core.table.statistics;

import connection.DBMSConnection;
import core.main.table.statistics.aggrclasses.Distribution;
import basicDatatypes.Schema;
import columnTypes.ColumnPumper;

public class TableStatisticsFinderImpl implements TableStatisticsFinder{

	private Distribution distribution;
	
	public TableStatisticsFinderImpl(DBMSConnection dbmsConn) {
		this.distribution = new Distribution(dbmsConn);
	}
	
	@Override
	public float findDuplicatesRatio(Schema s, ColumnPumper column){
		float ratio = 0; // Ratio of the duplicates

		// First of all, I need to understand the distribution of duplicates. Window analysis!
		ratio = distribution.naiveStrategy(column.getName(), s.getTableName());
		
		return ratio;
	}	

	@Override
	public float findNullRatio(Schema s, ColumnPumper column){
		float ratio = 0;
		ratio = distribution.nullRatioNaive(column.getName(), s.getTableName());
		return ratio;
	}

}
