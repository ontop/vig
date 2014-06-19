package core;

public class StringOption extends Option {

	private String value;
	
	public StringOption(String name, String description, String category, String value) {
		super(name, description, category, "<string>");
		
		this.value = value;
	}

	@Override
	public boolean parse(String toParse) {
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
