package core;

import java.util.Random;

import org.apache.log4j.Logger;

import basicDatatypes.CharColumn;
import basicDatatypes.Column;
import basicDatatypes.DateTimeColumn;
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
		case CHAR: {
			char resultChar = ((CharColumn)column).getChar();
			result = ""+resultChar;
			break;
		}
		case DATETIME:
			result = getRandomDatetime((DateTimeColumn)column);
			break;
		case LINESTRING:
			break;
		case LONGTEXT:
			result = getRandomString(column);
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
			result = getRandomString(column);
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
	
	private String getRandomDatetime(DateTimeColumn column) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRandomInt(IntColumn column, int nRows){
		
		int allDiffCnt = column.getLastInserted();
		
		while( ++allDiffCnt >= column.getCurrentMax() && column.hasNextMax() ) column.nextMax();
		
		column.setLastInserted(allDiffCnt);
		
		return allDiffCnt;
	}
	
	public String getRandomString(Column column){
		return "randomString"+(++cnt);
	}
	
	public int getRandomInt(int max){
		return rand.nextInt(max);
	}
	
	public float getRandomFloat(){
		return rand.nextFloat();
	}
}
