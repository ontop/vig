package core;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class Option {
	
	/** The name of the application
	 * 
	 */
	public static String appName = "vig";
	public static boolean verbose = false;
	
	
	protected static List<Option> options = null; // All options
	
	protected String name;
	protected String description;
	protected String category;
	protected String typeName;
	
	private static final int columnSpace = 20;
	private static final int hugeColumnSpace = 35;
	
	protected static List<Option> getOptionsList(){
		if( options == null )
			options = new ArrayList<Option>();
		return Option.options;
	}
	
	protected static String getUsageString(){
		return "USAGE: java -jar "+appName+".jar [OPTIONS]";
	}
	
	protected static String getHelpPrefixString(){
		
		return "--help";
	}
	
	protected static String getVerboseHelpPrefixString(){
		return "--help-verbose";
	}
	
	protected Option(String name, String description, 
			String category, String typeName){
		this.name = name;
		this.description = description;
		this.category = category;
		this.typeName = typeName;
		getOptionsList().add(this);
	}
	
	public abstract boolean parse(String toParse);
	public abstract String help(boolean verbose);
	
	public static void parseOptions(String[] args){
		
		for( String curArg : args ){
			
			if( curArg.equals(getHelpPrefixString()) ){
				printUsageAndExit();
			}	
			else if( curArg.equals(getVerboseHelpPrefixString()) ){
				verbose = true;
				printUsageAndExit();
			}
			
			boolean parsed = false;
			for( Option opt : getOptionsList() ){
				if( opt.parse(curArg) ) parsed = true;
			}
			if( !parsed ){
				System.err.println("Unknown option \""+curArg+"\". Type --help or --help-verbose for a list of options");
				System.exit(1);
			}
		}
	}
	
	/**
	 * Print the usage string, plus the description of each option
	 */
	protected static void printUsageAndExit(){
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(getUsageString() + "\n\n");
		
		// Split category-based
		// Trick: Keep it sorted by categories and types
		sortOptionsAccordingToCategoriesAndTypes();
		String lastCategory = "null";
		String lastType = "null";
		for( Option opt : Option.options ){
			if( !lastCategory.equals(opt.category) ){
				builder.append(opt.category + " OPTIONS" + "\n\n");
				lastCategory = opt.category;
			}
			else if( !lastType.equals(opt.typeName) ){
				builder.append("\n");
			}
			builder.append(opt.help(verbose) + "\n");
		}
		System.err.println(builder.toString());
		System.exit(0);
	}
	
	
	private static void sortOptionsAccordingToCategoriesAndTypes(){
		Collections.sort(Option.options, new Comparator<Option>() {
			@Override
			public int compare(Option o1, Option o2) { 
				if( o1.category.compareTo(o2.category) == 0 ){
					return o1.typeName.compareTo(o2.typeName);
				}
				return o1.category.compareTo(o2.category);
			}
		});
	}

	protected String printSpace(String field){
		
		StringBuilder builder = new StringBuilder();
		for( int i = 0; i < columnSpace - field.length(); ++i ) builder.append(" ");
		return builder.toString();
	}
	
	protected String printHugeColSpace(String field){
		StringBuilder builder = new StringBuilder();
		for( int i = 0; i < hugeColumnSpace - field.length(); ++i ) builder.append(" ");
		return builder.toString();
	}
}
