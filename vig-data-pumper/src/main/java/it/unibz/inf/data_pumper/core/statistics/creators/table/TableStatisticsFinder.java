package it.unibz.inf.data_pumper.core.statistics.creators.table;

import java.sql.SQLException;
import java.util.List;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.tables.Schema;

public interface TableStatisticsFinder {
	public float findDuplicatesRatio(Schema s, ColumnPumper<? extends Object> column);
	public float findNullRatio(Schema s, ColumnPumper<? extends Object> column);
	public <T> float findSharedRatio(ColumnPumper<T> col, ColumnPumper<T> referenced) throws SQLException;
	public <T> float findSharedRatio(List<ColumnPumper<T>> cols) throws SQLException;
}
