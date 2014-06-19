package ranges;

public class DoubleRange {
	public double begin;
	public double end;
	
	public boolean beginInclusive;
	public boolean endInclusive;
	
	public DoubleRange(double begin, double end, boolean beginInclusive, boolean endInclusive){
		this.begin = begin;
		this.end = end;
		this.beginInclusive = beginInclusive;
		this.endInclusive = endInclusive;
	}
}