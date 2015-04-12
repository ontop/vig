package it.unibz.inf.data_pumper.core.table.statistics;

import java.sql.SQLException;
import java.util.List;

import it.unibz.inf.data_pumper.basic_datatypes.Schema;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.connection.exceptions.InstanceNullException;

public interface TableStatisticsFinder {
	public float findDuplicatesRatio(Schema s, ColumnPumper column);
	public float findNullRatio(Schema s, ColumnPumper column);
	public float findSharedRatio(ColumnPumper col, ColumnPumper referenced) throws SQLException, InstanceNullException, ValueUnsetException;
	public float findSharedRatio(List<ColumnPumper> cols) throws SQLException, InstanceNullException, ValueUnsetException;
}
