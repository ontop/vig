package geometry;

import java.util.ArrayList;
import java.util.List;

public class Linestring implements Comparable<Linestring>{

	// LINESTRING(3 4,10 50,20 25)
	
	private List<Point> points;

	public Linestring(String linestringWKT){
		points = new ArrayList<Point>();
		
		parse(linestringWKT);
		
	}
	
	private void parse(String linestringWKT){
		int indexFirstPoint = linestringWKT.indexOf("(") + 1;
		int indexClosingParenthesis = linestringWKT.indexOf(")");
		
		String stringPoints = linestringWKT.substring(indexFirstPoint, indexClosingParenthesis);
		
		String[] splits = stringPoints.split(",");
		
		for( String split : splits ){ // How nice that the iterator works also for arrays
			points.add(new Point("Point("+split+")"));
		}
	}
	
	public List<Point> toPointsList(){
		return points;
	}
	
	@Override
	public int compareTo(Linestring toCompare) {
		// This ordering relies on a MIN_X, MIN_Y, MAX_Y and MAX_Y
		// (1 1) < (1 2) < (1 1, 1 1) < (1 1, 1 9) < (1 2, 1 1) ecc.
		
		
		if( points.size() < toCompare.points.size() )
			return -1;
		if( points.size() == toCompare.points.size() ){
			
			for( int i = 0; i < points.size(); ++i ){
				if( points.get(i).compareTo(toCompare.points.get(i)) == -1 ){
					return -1;
				}
				else if( points.get(i).compareTo(toCompare.points.get(i)) == 1 ){
					return 1;
				}
			}
			return 0;
		}
		return 1;
	}
	
	public String toPointList(){
		StringBuilder result = new StringBuilder();
		result.append("(");
		for( Point p : points ){
			result.append(p.getXLowerCase() + " " + p.getYLowerCase());
			result.append(",");
		}
		result.deleteCharAt(result.lastIndexOf(","));
		result.append(")");
		
		return result.toString();
	}
	
	/**
	 * To WKT
	 */
	@Override
	public String toString(){
		return "Linestring" + toPointList();
	}
}
