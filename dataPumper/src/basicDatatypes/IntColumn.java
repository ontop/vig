package basicDatatypes;

import java.util.Collections;
import java.util.List;

public class IntColumn extends Column {
	
	private List<Integer> domain;
	private int domainIndex;
	
	private int maxValue;
	private int minValue;
	
	public IntColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		domain = null;
		this.maxValue = 0;
		this.minValue = 0;
		
		index = 0;
	}
	public void setDomain(List<Integer> domain){
		if( this.domain == null ){
			this.domain = domain;
			Collections.sort(domain);
		}
		
		// For memory reasons, there is an hard-limit about what the
		// rows fetched from the database and that constitute the 
		// 'domain' vector. Hence, the maximum value of the domain
		// might NOT BE the maximum in the column.
		if( domain.get(domain.size() -1) < maxValue )
			domain.set(domain.size() -1, maxValue);
	}
	
	public int getCurrentMax(){
		if( domain == null )
			return Integer.MAX_VALUE;
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}
	
	public void nextMax(){
		++domainIndex;
	}
	
	public boolean hasNextMax(){
		return domain == null ? false : domainIndex < domain.size();
	}
	
	public void setMaxValue(int max){
		maxValue = max;
	}
	
	public int getMaxValue(){
		return maxValue;
	}
	
	public void setMinValue(int min){
		minValue = min;
	}
	
	public int getMinValue(){
		return minValue;
	}
	
	@Override
	/** This method has to be called whenever information held for the column can be released **/
	public void reset(){
		if( domain != null ) domain.clear();
		domainIndex = 0;
	}
}
