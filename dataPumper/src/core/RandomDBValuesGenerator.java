package core;

import java.util.Random;

import org.apache.log4j.Logger;

import basicDatatypes.Column;

public class RandomDBValuesGenerator {
	private Random rand;
	private int cnt; 
	
	private static Logger logger = Logger.getLogger(RandomDBValuesGenerator.class.getCanonicalName());
	
	public RandomDBValuesGenerator(){
		rand = new Random();
		cnt = 0;
	}
	
	public String getRandomValue(Column column, int nRows){
		
		String result = null;
		
		switch(column.getType()){
		case INT: {
			Integer resultInt = getRandomInt(column, nRows);
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
	
	public int getRandomInt(Column column, int nRows){
//		Domain<Integer> dom = (Domain<Integer>) schema.getDomain(colName);
//		if( dom.isDbIndependent() ){
//			return dom.getValues().get(rand.nextInt(dom.getValues().size()));
//		}
//		else if( schema.allDifferent(colName) ){
//			return ++cnt; // To be sure they are all different 
//		}
		
//		if( true || column.isAllDifferent() ){
//			logger.debug("isAllDiff");
			int allDiffCnt = column.getLastInserted();
			column.setLastInserted(++allDiffCnt);
			
			return allDiffCnt;
//		}
		
//		if( column.getMaxValue() - column.getMinValue() < nRows ){ // TODO Maybe nRows is a too small value in the comparison
//			// Not enough space to generate all rows
//			// Choose a random number
//			return rand.nextInt(100000000);
//		}
		
		// Normal stuff. Pick a random in the interval
//		int max = (int)column.getMaxValue();
//		int min = (int)column.getMinValue();
		
//		return max == min ? 
//				rand.nextInt() % 100000 : rand.nextInt( max - min ) + min;		
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
	
	public float getRandomFloat(){
		return rand.nextFloat();
	}
}
