package basicDatatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntColumn extends Column {
	
	private List<Integer> domain = null;
	private int index;
	
	public IntColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
	
		domain = new ArrayList<Integer>();
		index = 0;
	}
	public void setDomain(List<Integer> domain){
		if( this.domain == null ){
			this.domain = domain;
			Collections.sort(domain);
		}
	}
	
	public int getCurrentMax(){
		return domain.get(index);
	}
	
	public void nextMax(){
		++index;
	}
}
