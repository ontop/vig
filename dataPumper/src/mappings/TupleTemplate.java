package mappings;

import java.util.List;
import java.util.Set;

public abstract class TupleTemplate {
	public abstract String getTemplatesString();
	
	public abstract Set<String>getReferredTables();
	
	public abstract List<String> getColumnsInTable(String tableName);
	
	public abstract String toString();
}
