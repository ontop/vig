package geometry;

/**
 * Polygon constraints:
 * - Start point == End point
 * - In case of multiple (closed) linestrings, each linestring are contained 
 *   one into another
 * @author tir
 *
 */
public class Polygon extends MultiLinestring{
		
	public Polygon(String polygonWKT){
		super(polygonWKT);
	}
	
	/**
	 * Rectangle constructor
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 */
	public static Polygon getInstanceFromRectangle(long minX, long minY, long maxX, long maxY){
		StringBuilder rectangleWKT = new StringBuilder();
		
		rectangleWKT.append("Polygon((" + minX + " " + minY);
		rectangleWKT.append(",");
		rectangleWKT.append(maxX + " " + minY);
		rectangleWKT.append(",");
		rectangleWKT.append(maxX + " " + maxY);
		rectangleWKT.append(",");
		rectangleWKT.append(minX + " " + minY);
		rectangleWKT.append("))");
		
		return new Polygon(rectangleWKT.toString());
	}
	
	private String toWKT(){
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("Polygon(");
		
		for( Linestring l : linestrings ){
			builder.append(l.toPointList());
			builder.append(",");
		}
		builder.deleteCharAt(builder.lastIndexOf(","));
		builder.append(")");
		
		return builder.toString();
	}
	
	public String toPointList(){
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("(");
		
		for( Linestring l : linestrings ){
			builder.append(l.toPointList());
			builder.append(",");
		}
		builder.deleteCharAt(builder.lastIndexOf(","));
		builder.append(")");
		
		return builder.toString();
	}
	
	@Override
	public String toString(){
		return toWKT();
	}
}
