package geometry;

public class Point implements Comparable<Point>{

	private double x;
	private double y;
	
	public Point(String pointWKT){
		int indexX = pointWKT.indexOf("(") +1;
		int indexBlank = pointWKT.indexOf(" ");
		int indexY = indexBlank +1;
		
		x = Double.parseDouble(pointWKT.substring(indexX, indexBlank));
		y = Double.parseDouble(pointWKT.substring(indexY, pointWKT.indexOf(")")));
	};
	
	public Point(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public String toWKT(double x, double y){
		return "Point(" + x + " " + y + ")";
	}
	
	public double getX(){
		return x;
	}
	
	public void incrementX(){
		++x;
	}
	
	public void incrementY(){
		++y;
	}
	
	public void setX(double newX){
		x = newX;
	}
	
	public void setY(double newY){
		y = newY;
	}
	
	public double getY(){
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
