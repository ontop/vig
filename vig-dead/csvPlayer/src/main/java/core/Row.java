package core;

import java.util.ArrayList;
import java.util.List;

public class Row {
	private List<String> values;
	
	public Row(List<String> values){
		this.values = values;
	}
	
	/**
	 * Column indexes start from one.
	 * @param index
	 * @return
	 */
	public List<String> getProjection(int... index){
		List<String> result = new ArrayList<String>();
		for( int i : index ){
			result.add(values.get(i));
		}
		return result;
	}
	
	/**
	 * Column indexes start from one.
	 * @param index
	 * @return
	 */
	public String getProjection(int index){
		return values.get(index-1);
	}
}
