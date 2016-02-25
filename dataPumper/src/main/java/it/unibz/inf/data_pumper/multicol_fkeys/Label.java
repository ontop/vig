package it.unibz.inf.data_pumper.multicol_fkeys;

public class Label {
    private final String code;
    
    public Label(String code){
	this.code = code;
    }
    
    @Override
    public String toString(){
	return this.code;
    }
}
