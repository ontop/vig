package basicDatatypes;

import java.util.ArrayList;
import java.util.List;

public class Tuple {
	private List<String> fields;
	private List<String> indexes;
	private int consumedItems;
	
	public Tuple(){
		fields = new ArrayList<String>();
		indexes = new ArrayList<String>();
	}
	
	public boolean consumed(){
		if( consumedItems == fields.size() ){ consumedItems = 0; return true; }
		return false;
	}
	
	public void addFieldToTuple(String columnName){
		fields.add(null);
		indexes.add(columnName);
	}
	
	public String consumeField(String fieldName){
		int i = indexes.indexOf(fieldName);
		return fields.get(i);
	}
}
