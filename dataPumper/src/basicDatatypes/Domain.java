package basicDatatypes;

import java.util.List;

public class Domain<T>{
	public T min;
	public T max;
	
	private List<T> values = null;
	
	public Domain(T min, T max){
		this.min = min;
		this.max = max;
	}
	/**
	 * TODO When am I gonna fill this <i>values</i> vector??
	 * @return
	 */
	public boolean isDbIndependent(){
		return values != null;
	}
	
	/** 
	 * In case the domain is database-independent, this vector holds
	 * all the values of the domain (TODO What if they are a lot)
	 **/
	public Domain(List<T> values){
		this.values = values;
	}
	
	public List<T> getValues(){
		return values;
	}
	
	public String toString(){
		return "(Min: "+min+", Max: "+max+")";
	}
};