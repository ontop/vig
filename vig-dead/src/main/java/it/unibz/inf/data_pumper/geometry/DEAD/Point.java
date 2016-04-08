package it.unibz.inf.data_pumper.geometry.DEAD;

/*
 * #%L
 * dataPumper
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.math.BigDecimal;

public class Point implements Comparable<Point>{

	private BigDecimal x;
	private BigDecimal y;
	
	public Point(String pointWKT){
		int indexX = pointWKT.indexOf("(") +1;
		int indexBlank = pointWKT.indexOf(" ");
		int indexY = indexBlank +1;
		
		x = BigDecimal.valueOf(Double.parseDouble(pointWKT.substring(indexX, indexBlank)));
		y = BigDecimal.valueOf(Double.parseDouble(pointWKT.substring(indexY, pointWKT.indexOf(")"))));
	};
	
	public Point(BigDecimal x, BigDecimal y){
		this.x = x;
		this.y = y;
	}
	
	public String toWKT(BigDecimal x, BigDecimal y){
		return "Point(" + x + " " + y + ")";
	}
	
	public String toWKTLowerCase(BigDecimal x, BigDecimal y){
		return "Point(" + getXLowerCase() + " " + getYLowerCase() + ")";
	}
	
	public BigDecimal getX(){
		return x;
	}
	
	public void incrementX(){
		x = this.x.add(BigDecimal.ONE);
	}
	
	public void incrementY(){
		y = y.add(BigDecimal.ONE);
	}
	
	public void setX(BigDecimal newX){
		x = newX;
	}
	
	public void setY(BigDecimal newY){
		y = newY;
	}
	
	public BigDecimal getY(){
		return y;
	}
	
	public String getXLowerCase(){
		String toPrint = x.toString();
		toPrint = toPrint.replace("E","e");
		return toPrint;
	}
	
	public String getYLowerCase(){
		String toPrint = y.toString();
		toPrint = toPrint.replace("E", "e");
		return toPrint;
	}
	
	/**
	 * Lexicographic order
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(Point o) {
		// -1 0 1
		
		if( x.compareTo(o.x) == -1 ) return -1;
		if( x.compareTo(o.x) == 0 && y.compareTo(o.y) == -1 ) return -1;
		if( x.compareTo(o.x) == 0 && y.compareTo(o.y) == 0 ) return 0;
		return 1;
		
	}
	@Override public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof Point) {
			Point that = (Point) other;
			result = (this.getX().compareTo(that.getX()) == 0 && this.getY().compareTo(that.getY()) == 0);
		}
		return result;
	}
    @Override public int hashCode() {
        return toString().hashCode();
    }
	@Override
	public String toString(){
		return toWKTLowerCase(this.x, this.y);
	}
};
