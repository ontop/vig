package columnTypes;

import java.util.Collections;
import java.util.List;

import basicDatatypes.MySqlDatatypes;

public abstract class IncrementableColumn<T extends Comparable<? super T>> extends Column {

	protected List<T> domain;
	protected int domainIndex;
	protected T lastInserted;
	protected T max;
	protected T min;
	
	public IncrementableColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		domain = null;
		domainIndex = 0;
	}
	
	
	public abstract T increment(T toIncrement);
	public abstract T getCurrentMax();

	
	@Override
	/** This method has to be called whenever information held for the column can be released **/
	public void reset(){
		if( domain != null ) domain.clear();
		domainIndex = 0;
	}
	
	@Override
	public String getNextFreshValue(){
		
		T toInsert = this.getLastInserted();
		
		if( toInsert == null ) logger.error(this.toString() +" toInsert is NULL");
		
		do{
			toInsert = increment(toInsert);
			
			while( toInsert.compareTo(this.getCurrentMax()) == 1 && this.hasNextMax() )
				this.nextMax();
		}
		while(toInsert.compareTo(this.getCurrentMax()) == 0);
		
//		while( increment(toInsert).compareTo(this.getCurrentMax()) > -1 && this.hasNextMax() ) this.nextMax();
		
		this.setLastInserted(toInsert);
		
		return toInsert.toString();
	}

	public void setLastInserted(T toInsert){
		lastInserted = toInsert;
	}

	public T getLastInserted(){
		return lastInserted;
	}
	
	
	public boolean hasNextMax(){
		return domainIndex < domain.size();
	}
	
	public void nextMax(){
		++domainIndex;
	}
	
	public void setMaxValue(T max){
		this.max = max;
	}
	
	public T getMaxValue(){
		return max;
	}
	
	public void setMinValue(T min){
		this.min = min;
	}
	
	public T getMinValue(){
		return min;
	}
	
	public void setDomain(List<T> newDomain){
		if( domain == null ){
			domain = newDomain;
			if( domain.size() != 0 )
				Collections.sort(domain);
		}
		
		// For memory reasons, there is an hard-limit about what the
		// rows fetched from the database and that constitute the 
		// 'domain' vector. Hence, the maximum value of the domain
		// might NOT BE the maximum in the column.
		if(domain.size() == 0) return;
		if( domain.get(domain.size() -1).compareTo(max) == -1 )
			domain.set(domain.size() -1, max);
	}
	
}

