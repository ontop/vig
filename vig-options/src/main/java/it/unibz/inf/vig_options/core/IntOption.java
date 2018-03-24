package it.unibz.inf.vig_options.core;

import it.unibz.inf.vig_options.ranges.IntRange;

public class IntOption extends Option {
	protected IntRange range;
	protected int value;
	
	public IntOption(String name, String description, String category, int value, IntRange range) {
		super(name, description, category, "<int>");
		
		this.range = range;
		this.value = value;
		
	}

	/**
	 *  Does it match with <i>--optName=valueInRange ?<\i>
	 */
	@Override
	public boolean parseImpl(String toParse) {
		
		String temp = toParse.trim(); // Eliminate whitespaces
		
		if( temp.startsWith(name+"=") ){
			int tmpValue = Integer.parseInt(temp.substring(name.length() + 1));
			
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

	public int getValue() {
		return value;
	}
}
