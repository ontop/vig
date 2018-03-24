package it.unibz.inf.data_pumper.core.statistics.model;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.tables.Schema;

public interface Model {
    float getDuplicatesRatioForColumn(ColumnPumper<?> cP);
    float getNullsRatioForColumn(ColumnPumper<?> cP);
    long getNumFreshsToInsertInColumn(ColumnPumper<?> cP);
    long getNumRowsToInsertInTable(Schema s); 
    
    void setDuplicatesRatioForColumn(ColumnPumper<?> cP);
    void setNullsRatioForColumn(ColumnPumper<?> cP);
    void setNumFreshsToInsertInColumn(ColumnPumper<?> cP);
    void setNumRowsToInsertInTable(Schema s); 
}
