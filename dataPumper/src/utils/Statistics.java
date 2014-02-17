package utils;

import java.util.HashMap;
import java.util.Map;

public class Statistics {
	private static Map<String, Integer> mIntegerStats = new HashMap<String, Integer>();
	private static Map<String, Float> mFloatStats = new HashMap<String, Float>();
	
	public static void addInt(String key, int increment){
		if( mIntegerStats.containsKey(key) ){
			int temp = mIntegerStats.get(key);
			temp += increment;
			mIntegerStats.put(key, temp);
		}
		else
			mIntegerStats.put(key, increment);
	}
	
	public static void addFloat(String key, float increment){
		if( mFloatStats.containsKey(key) ){
			float temp = mFloatStats.get(key);
			temp += increment;
			mFloatStats.put(key, temp);
		}
		else
			mFloatStats.put(key, increment);
	}
	
	public static float getFloatStat(String key){
		return mFloatStats.get(key);
	}
	
	public static int getIntStat(String key){
		return mIntegerStats.get(key);
	}
	
	public String printStats(){
		
		StringBuilder result = new StringBuilder();
		
		for( String key : mIntegerStats.keySet() ){
			result.append("[");
			result.append(key);
			result.append("] = ");
			result.append(mIntegerStats.get(key));
		}
		for( String key : mFloatStats.keySet() ){
			result.append("[");
			result.append(key);
			result.append("] = ");
			result.append(mFloatStats.get(key));
		}
		
		return result.toString();
	}
}
