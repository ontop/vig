package core;

import java.util.Random;

import org.apache.log4j.Logger;

import basicDatatypes.Column;
import basicDatatypes.IntColumn;

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
			Integer resultInt = getRandomInt((IntColumn)column, nRows);
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
	
	public int getRandomInt(IntColumn column, int nRows){
		
		int allDiffCnt = column.getLastInserted();
		
		while( ++allDiffCnt < column.getCurrentMax() ) column.nextMax();
		
		return allDiffCnt;
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
