package core;

import java.util.Random;

import basicDatatypes.Domain;

public class RandomDBValuesGenerator {
	private Random rand;
	private int cnt;
	
	public RandomDBValuesGenerator(){
		rand = new Random();
		cnt = 0;
	}
	
	public int getRandomInt(Domain<Integer> dom){
		if( dom.isDbIndependent() ){
			return dom.getValues().get(rand.nextInt(dom.getValues().size()));
		}
		// TODO Wrong, as it works only for natural numbers
		return dom.max == 
				dom.min ? rand.nextInt() : rand.nextInt( (dom.max - dom.min) + 1 ) + dom.min;
	}
	
	public String getRandomString(Domain<String> dom){
		if( dom != null){
			if( dom.isDbIndependent() ){
				return dom.getValues().get(rand.nextInt(dom.getValues().size()));
			}
		}
		return "randomString"+(++cnt);
	}
}
