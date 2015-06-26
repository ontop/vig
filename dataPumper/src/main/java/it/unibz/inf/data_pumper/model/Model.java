package it.unibz.inf.data_pumper.model;

import it.unibz.inf.data_pumper.basic_datatypes.Schema;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;

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
