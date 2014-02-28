package utils.wktparsers;

public class PointParserWKT{
	
	private long x;
	private long y;
	
	public void parse(String pointWKT){
		
		int indexX = pointWKT.indexOf("(") +1;
		int indexBlank = pointWKT.indexOf(" ");
		int indexY = indexBlank +1;
		
		x = Long.parseLong(pointWKT.substring(indexX, indexBlank));
		y = Long.parseLong(pointWKT.substring(indexY, pointWKT.indexOf(")")));
	}
	
	public String toWKT(long x, long y){
		return "Point(" + x + " " + y + ")";
	}
	
	public long getX(){
		return x;
	}
	
	public long getY(){
		return y;
	}
}