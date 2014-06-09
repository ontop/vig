package mappings;

import java.util.List;
import java.util.Set;

public abstract class TupleTemplate {
	
	public abstract int getID();
	
	public abstract String getTemplatesString();
	
	public abstract Set<String>getReferredTables();
	
	public abstract List<String> getColumnsInTable(String tableName);
	
	public abstract int belongsToTuple();
	
	public abstract String toString();

	public abstract boolean equals(Object other);

	public abstract int hashCode();
}
