package it.unibz.inf.vig_options.core;

/************************************************************************************************
Copyright (c) 2008-2010, Niklas Sorensson

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
**************************************************************************************************/

import it.unibz.inf.vig_options.ranges.DoubleRange;

public class DoubleOption extends Option {

	protected DoubleRange range;
	protected double value;
	
	public DoubleOption(String name, String description, String category, double value, DoubleRange range) {
		super(name, description, category, "<double>");
		
		this.range = range;
		this.value = value;
		
	}

	/**
	 *  Does it match with <i>--optName=valueInRange ?<\i>
	 */
	@Override
	public boolean parse(String toParse) {
		
		String temp = toParse.trim(); // Eliminate whitespaces
		
		if( temp.startsWith(name+"=") ){
			double tmpValue = Double.parseDouble(temp.substring(name.length() + 1));
			
			if( tmpValue > range.begin && tmpValue < range.end ){
				this.value = tmpValue;
			}
			else if( (tmpValue == range.begin && range.beginInclusive) || (tmpValue == range.end && range.endInclusive) ){
				this.value = tmpValue;
			}
			else{
				System.err.println("ERROR! Value out of range for the option "+ this.name);
				System.exit(1);
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public String help(boolean verbose) {
		
		StringBuilder builder = new StringBuilder();		
		
		builder.append(this.name);
		builder.append(printSpace(this.name));
		builder.append(this.typeName);
		builder.append(printSpace(this.typeName));
		builder.append(range.beginInclusive ? "[" : "(");
		builder.append(this.range.begin);
		builder.append(" -- ");
		builder.append(this.range.end);
		builder.append(range.endInclusive ? "]" : ")");
		builder.append(printHugeColSpace("["+this.range.begin+" -- "+this.range.end+"]"));
		builder.append("(default: ");
		builder.append(value);
		builder.append(")");
		
		if( verbose ){
			builder.append(printSpace("(default: " + value + ")"));
			builder.append(this.description);
		}
		
		return builder.toString();
	}

	public double getValue() {
		return value;
	}
};