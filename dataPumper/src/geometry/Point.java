package geometry;

public class Point implements Comparable<Point>{

	private long x;
	private long y;
	
	public Point(String pointWKT){
		int indexX = pointWKT.indexOf("(") +1;
		int indexBlank = pointWKT.indexOf(" ");
		int indexY = indexBlank +1;
		
		x = Long.parseLong(pointWKT.substring(indexX, indexBlank));
		y = Long.parseLong(pointWKT.substring(indexY, pointWKT.indexOf(")")));
	};
	
	public Point(long x, long y){
		this.x = x;
		this.y = y;
	}
	
	public String toWKT(long x, long y){
		return "Point(" + x + " " + y + ")";
	}
	
	public long getX(){
		return x;
	}
	
	public void incrementX(){
		++x;
	}
	
	public void incrementY(){
		++y;
	}
	
	public void setX(long newX){
		x = newX;
	}
	
	public void setY(long newY){
		y = newY;
	}
	
	public long getY(){
		return y;
	}
	
	/**
	 * Lexicographic order
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(Point o) {
		// -1 0 1
		
		if( x < o.x ) return -1;
		if( x == o.x && y < o.y ) return -1;
		if( x == o.x && y == o.y ) return 0;
		return 1;
		
	}
	@Override public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof Point) {
			Point that = (Point) other;
			result = (this.getX() == that.getX() && this.getY() == that.getY());
		}
		return result;
	}
    @Override public int hashCode() {
        return toString().hashCode();
    }
	@Override
	public String toString(){
		return toWKT(this.x, this.y);
	}
};
