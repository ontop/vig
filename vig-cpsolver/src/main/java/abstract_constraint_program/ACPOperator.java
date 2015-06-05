package abstract_constraint_program;

public enum ACPOperator {

    EQUALS("="),
    GEQ(">="),
    LEQ("<=");
        
    private String text;
    
    ACPOperator(String text){
	this.text = text;
    }
    
    public String getText(){
	return this.text;
    }
}
