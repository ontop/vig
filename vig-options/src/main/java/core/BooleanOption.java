package core;

public class BooleanOption extends Option {

	protected boolean value;
	
	protected BooleanOption(String name, String description, String category,
			String typeName, boolean value) {
		super(name, description, category, typeName);
	
		this.value = value;
	}
	
	/**
	 *  Does it match with <i>optName<\i> ? 
	 */
	@Override
	public boolean parse(String toParse) {
		
		String temp = toParse.trim(); // Eliminate whitespaces
		
		if( temp.startsWith(name+"=") ){
			
			String tmpValue = temp.substring(name.length()+1);
			
			if( tmpValue.equalsIgnoreCase("on") ){
				this.value = true; return true;
			}
			else if( tmpValue.equalsIgnoreCase("off") ){
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
