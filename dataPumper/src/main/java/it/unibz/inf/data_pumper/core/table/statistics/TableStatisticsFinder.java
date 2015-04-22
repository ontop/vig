package it.unibz.inf.data_pumper.core.table.statistics;

import java.sql.SQLException;
import java.util.List;

import it.unibz.inf.data_pumper.basic_datatypes.Schema;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.connection.exceptions.InstanceNullException;
import it.unibz.inf.data_pumper.core.main.DEBUGEXCEPTION;

public interface TableStatisticsFinder {
	public float findDuplicatesRatio(Schema s, ColumnPumper<? extends Object> column);
	public float findNullRatio(Schema s, ColumnPumper<? extends Object> column);
	public <T> float findSharedRatio(ColumnPumper<T> col, ColumnPumper<T> referenced) throws SQLException, InstanceNullException, ValueUnsetException;
	public <T> float findSharedRatio(List<ColumnPumper<T>> cols) throws SQLException, InstanceNullException, ValueUnsetException, DEBUGEXCEPTION;
}
