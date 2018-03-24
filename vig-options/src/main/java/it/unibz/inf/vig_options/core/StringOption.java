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

public class StringOption extends Option {

	protected String value;
	
	public StringOption(String name, String description, String category, String value) {
		super(name, description, category, "<string>");
		
		this.value = value;
	}

	@Override
	public boolean parseImpl(String toParse) {
		String temp = toParse.trim(); // Eliminate whitespaces
		
		if( temp.startsWith(name+"=") ){
			this.value = temp.substring(name.length()+1);			
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
		builder.append(printHugeColSpace(""));
		builder.append("(default: ");
		builder.append(value == null ? "NULL" : value);
		builder.append(")");
		
		if( verbose ){
			builder.append(printSpace("(default: "+ (value == null ? "NULL" : value) + ")"));
			builder.append(this.description);
		}
		
		return builder.toString();
	}
	
	public String getValue(){
		return value;
	}

}
