package it.unibz.inf.vig_options.ranges;

import java.util.ArrayList;
import java.util.List;

import it.unibz.inf.vig_options.ranges.exceptions.NotRangeStringException;

public class StringRange {
	
	private final List<String> range;
	
	public StringRange(List<String> range){
		this.range = range;
	}
	
	public StringRange(String rangeString){
		range = toList(rangeString);
	}
	
	public boolean isInRange(String s){
		return range.contains(s);
	}
	
	/**
	 * 
	 * @param rangeString String of the form [el_1,el_2,el_3,...,el_n]
	 */
	private List<String> toList(String rangeString) {
		
		List<String> result = new ArrayList<String>();
		
		if( !rangeString.startsWith("[") || !rangeString.endsWith("]") ){
			try{
				throw new NotRangeStringException();
			}catch(NotRangeStringException e){
				e.printStackTrace();
			}
		}
		String list = rangeString.substring(1, rangeString.length()-1);
		String[] splits = list.split(",");
		
		for( String s : splits ){
			result.add(s);
		}
		
		return result;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		boolean first = true;
		for( String s : range ){
			if( first ){
				builder.append(s);
				first = false;
			}
			else{
				builder.append("," + s);
			}
		}
		return builder.toString();
	}
}
