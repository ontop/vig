package basicDatatypes;

/**
 * 
 * @author tir
 *
 * Constraints: 
 * 
 * 1) The template string CANNOT start with a placeholder '?'
 * 2) There cannot be two consecutive placeholders --this would not make sense, anyway
 */
public class Template{
	private String[] splits;
	private String[] fillers;
	private String template;
	
	public Template(String templateString){
		template = templateString;
		parseTemplate();
	}
	
	public void setNthPlaceholder(int n, String filler) {
		fillers[n-1] = filler;
	}
	
	private void parseTemplate(){
		splits = template.split("\\?");
		int cnt = 0;
		for( int i = 0; i < template.length(); i++ ){
			if( template.charAt(i) == '?' ) cnt++;
		}
		fillers = new String[cnt];
	}
	
	public String getFilled(){
		StringBuilder temp = new StringBuilder();
		for( int i = 0; i < splits.length; i++ ){
			temp.append(splits[i]);
			if( i < fillers.length ) temp.append(fillers[i]);
		}
		return temp.toString(); 
	}
	
	public String toString(){
		return template;
	}
};