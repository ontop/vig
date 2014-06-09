package core.tableGenerator;


import java.util.List;

import basicDatatypes.Schema;

public abstract class Generator {
	
	public abstract List<Schema> pumpTable(int nRows, Schema schema);

	public abstract void setPureRandomGeneration();
	
//	public abstract float findNullRatio(Schema s, ColumnPumper column);
//	
//	public abstract float findDuplicateRatio(Schema s, ColumnPumper column);



}
