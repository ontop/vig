package it.unibz.inf.data_pumper.multicol_fkeys;

public final class Label {
    private final String code;
    
    private Label(String code){
	this.code = code;
    }
    
    static public Label makeLabel(String code){
	return new Label(code);
    }
    
    @Override
    public String toString(){
	return this.code;
    }
}
