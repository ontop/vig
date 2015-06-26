package it.unibz.inf.data_pumper.core.statistics.xml_model;

public class ScaleFormulaModel {
    
    private OperatorType opType;
    private double scalar;
    
    public OperatorType getOpType() {
	return opType;
    }
    public void setOpType(OperatorType opType) {
	this.opType = opType;
    }
    public double getScalar() {
	return scalar;
    }
    public void setScalar(double d) {
	this.scalar = d;
    }
    
    public String toString(){
	
	StringBuilder result = new StringBuilder();
	
	switch(opType){
	case MUL:
	    result.append("scaleFactor * "+scalar);
	    break;
	case POW:
	    result.append("scaleFactor^"+scalar);
	}
	return result.toString();
    }
}

enum OperatorType{
    POW,
    MUL
}