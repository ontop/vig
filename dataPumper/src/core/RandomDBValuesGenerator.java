package core;

import java.util.Random;

import basicDatatypes.Schema;

public class RandomDBValuesGenerator {
	private Random rand;
	private int cnt;
	
	public RandomDBValuesGenerator(){
		rand = new Random();
		cnt = 0;
	}
	
	public int getRandomInt(Schema schema, String colName){
//		Domain<Integer> dom = (Domain<Integer>) schema.getDomain(colName);
//		if( dom.isDbIndependent() ){
//			return dom.getValues().get(rand.nextInt(dom.getValues().size()));
//		}
//		else if( schema.allDifferent(colName) ){
//			return ++cnt; // To be sure they are all different 
//		}
//		int max = dom.max.intValue();
//		int min = dom.min.intValue();
//		
//		return max == min ? 
//				rand.nextInt() % 100000 : rand.nextInt( max - min ) + min;
		
		return rand.nextInt(10000000);
	}
	
	public String getRandomString(Schema schema, String colName){
//		Domain<String> dom = (Domain<String>) schema.getDomain(colName);
//		if( dom != null){
//			if( dom.isDbIndependent() ){
//				return dom.getValues().get(rand.nextInt(dom.getValues().size()));
//			}
//		}
		//TODO This will return all different things, that maybe is unwanted
		return "randomString"+(++cnt);
	}
	
	public int getRandomInt(int max){
		return rand.nextInt(max);
	}
}
