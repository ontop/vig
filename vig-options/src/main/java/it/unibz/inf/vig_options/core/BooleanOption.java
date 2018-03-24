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

public class BooleanOption extends Option {

	protected boolean value;
	
	public BooleanOption(String name, String description, String category, boolean value) {
		super(name, description, category, "<bool>");
	
		this.value = value;
	}
	
	/**
	 *  Does it match with optName ? 
	 */
	@Override
	public boolean parseImpl(String toParse) {
		
		String temp = toParse.trim(); // Eliminate whitespaces
		
		if( temp.startsWith(name+"=") ){
			
			String tmpValue = temp.substring(name.length()+1);
			
			if( tmpValue.equalsIgnoreCase("true") ){
				this.value = true; return true;
			}
			else if( tmpValue.equalsIgnoreCase("false") ){
				this.value = false; return true;
			}

			System.err.println("ERROR! Value out of range for the option "+ this.name);
			System.exit(1);
			
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
		builder.append("[true -- false]");
		builder.append(printHugeColSpace("[true -- false]"));
		builder.append("(default: ");
		builder.append(value);
		builder.append(")");
		
		if( verbose ){
			builder.append(printSpace("(default: "+value+")"));
			builder.append(this.description);
		}
		
		return builder.toString();
	}
	
	public boolean getValue(){
		return value;
	}
}
