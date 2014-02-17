package core;

import java.util.Random;

import basicDatatypes.Column;
import basicDatatypes.Schema;

public class RandomDBValuesGenerator {
	private Random rand;
	private int cnt;
	
	public RandomDBValuesGenerator(){
		rand = new Random();
		cnt = 0;
	}
	
	public String getRandomValue(Column column){
		
		String result = null;
		
		switch(column.getType()){
		case INT: {
			Integer resultInt = getRandomInt(column);
			result = resultInt.toString();
			break;
		}
		case CHAR:
			break;
		case DATETIME:
			break;
		case LINESTRING:
			break;
		case LONGTEXT:
			break;
		case MULTILINESTRING:
			break;
		case MULTIPOLYGON:
			break;
		case POINT:
			break;
		case POLYGON:
			break;
		case TEXT:
			break;
		case VARCHAR : {
			result = getRandomString(column);
			break;
		}
		
		default:
			break;
		}
		
		return result;
	}
	
	public int getRandomInt(Column column){
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
	
	public String getRandomString(Column column){
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
