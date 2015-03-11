package it.unibz.inf.data_pumper.core.table.statistics;

import it.unibz.inf.data_pumper.basic_datatypes.Schema;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;

public interface TableStatisticsFinder {
	public float findDuplicatesRatio(Schema s, ColumnPumper column);
	public float findNullRatio(Schema s, ColumnPumper column);
}
