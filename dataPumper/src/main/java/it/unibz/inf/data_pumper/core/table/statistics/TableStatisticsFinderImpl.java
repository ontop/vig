package it.unibz.inf.data_pumper.core.table.statistics;

import it.unibz.inf.data_pumper.basic_datatypes.Schema;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.main.table.statistics.aggrclasses.Distribution;

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
