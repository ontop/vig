package it.unibz.inf.data_pumper.column_types;

public class CyclicGroupGenerator {
	
	final long G; // Generator for the multiplicative group modulo P
	final long P; // Prime number > N
	
	long seed;
	
	public CyclicGroupGenerator(long numValues) {
		
		if( numValues < 10 ){
			this.G = 2;
			this.P = 11;
		}else if( numValues < 100 ){
			this.G = 7;
			this.P = 101;
		}else if( numValues < 1000 ){
			this.G = 26;
			this.P = 1009;
		}
		else if( numValues < 10000 ){
			this.G = 59;
			this.P = 10007;
		}
		else if( numValues < 100000 ){
			this.G = 242;
			this.P = 100003;
		}
		else if( numValues < 1000000 ){
			this.G = 568;
			this.P = 1000003;
		}
		else if( numValues < 10000000 ){
			this.G = 1792;
			this.P = 10000019;
		}
		else if( numValues < 100000000 ){
			this.G = 5649;
			this.P = 100000007;
		}
		else if( numValues < 1000000000 ){
			this.G = 16807;
			this.P = 2147483647;
		}
		else{
			this.P = 0;
			this.G = 0;
		}

		seed = G;
	}

	public long nextValue(long n){
		seed = (G * seed) % P;
		while( seed > n ) seed = (G * seed) % P; // Discard all > N
		return seed;
	}
}


