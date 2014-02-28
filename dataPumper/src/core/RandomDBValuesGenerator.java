package core;

import java.sql.Timestamp;
import java.util.Random;

import org.apache.log4j.Logger;

import basicDatatypes.Column;
import basicDatatypes.DateTimeColumn;
import basicDatatypes.IntColumn;
@Deprecated
public class RandomDBValuesGenerator {
//	private Random rand;
//	
//	private static Logger logger = Logger.getLogger(RandomDBValuesGenerator.class.getCanonicalName());
//	
//	public RandomDBValuesGenerator(){
//		rand = new Random();
//		cnt = 0;
//	}
//	
//	public String getRandomValue(Column column, int nRows){
//		
//		String result = null;
//		
//		return column.getNextFreshValue();
		
//		switch(column.getType()){
//		case INT: {
//			Integer resultInt = getRandomInt((IntColumn)column, nRows);
//			result = resultInt.toString();
//			break;
//		}
//		case CHAR: {
//			result = getRandomString(column);
//			break;
//		}
//		case DATETIME:
//			result = getRandomDatetime((DateTimeColumn)column).toString();
//			break;
//		case LINESTRING:
//			break;
//		case LONGTEXT:
//			result = getRandomString(column);
//			break;
//		case MULTILINESTRING:
//			break;
//		case MULTIPOLYGON:
//			break;
//		case POINT:
//			break;
//		case POLYGON:
//			break;
//		case TEXT:
//			result = getRandomString(column);
//			break;
//		case VARCHAR : {
//			result = getRandomString(column);
//			break;
//		}
//		
//		default:
//			break;
//		}
//		logger.debug("Generated value: "+result);
		
//		return result;
//	}
//	
//	private Timestamp getRandomDatetime(DateTimeColumn column) {
//		long toInsert = column.getLastInserted().getTime();
//		
//		while( ++toInsert >= column.getCurrentMax() && column.hasNextMax() ) column.nextMax();
//		
//		Timestamp toReturn = new Timestamp(toInsert);
//		column.setLastInserted(toReturn);
//		
//		return toReturn;
//	}
//
//	public int getRandomInt(IntColumn column, int nRows){
//		
//		int allDiffCnt = column.getLastInserted();
//		
//		while( ++allDiffCnt >= column.getCurrentMax() && column.hasNextMax() ) column.nextMax();
//		
//		column.setLastInserted(allDiffCnt);
//		
//		return allDiffCnt;
//	}
//	
//	public int getRandomInt(int max){
//		return rand.nextInt(max);
//	}
//	
//	public float getRandomFloat(){
//		return rand.nextFloat();
//	}
}
