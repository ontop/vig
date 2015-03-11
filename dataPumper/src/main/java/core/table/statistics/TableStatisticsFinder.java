package core.table.statistics;

import basicDatatypes.Schema;
import columnTypes.ColumnPumper;

public interface TableStatisticsFinder {
	public float findDuplicatesRatio(Schema s, ColumnPumper column);
	public float findNullRatio(Schema s, ColumnPumper column);
}
