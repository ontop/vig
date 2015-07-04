package it.unibz.inf.data_pumper.persistence.statistics.xml_model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"name", "dupsRatio", "nullsRatio", "valuesListInjector", "stepInjector"})
@XmlAccessorType (XmlAccessType.FIELD)
public class ColumnModel{

    @XmlAttribute
    private String name;
    private double dupsRatio;
    private double nullsRatio;
    
    private ValuesListInjector valuesListInjector;
    private StepInjector stepInjector;
    
    public StepInjector getStepInj() {
        return stepInjector;
    }

    public void setStepInj(StepInjector stepInj) {
        this.stepInjector = stepInj;
    }

    public ValuesListInjector getDomain() {
        return valuesListInjector;
    }

    public void setDomain(ValuesListInjector domain) {
        this.valuesListInjector = domain;
    }

    public String getName() {
	return name;
    }
    
    public void setName(String name) {
	this.name = name;
    }
    
    public double getDupsRatio() {
	return dupsRatio;
    }
    
    public void setDupsRatio(double dupsRatio) {
	this.dupsRatio = dupsRatio;
    }
    
    public double getNullsRatio() {
	return nullsRatio;
    }
    
    public void setNullsRatio(double nullsRatio) {
	this.nullsRatio = nullsRatio;
    }
    
    public String toString(){
	StringBuilder result = new StringBuilder();
	result.append("name: "+getName() + "\n");
	result.append("nullsRatio:"+getNullsRatio() + "\n");
	result.append("dupsRatio:"+getDupsRatio() + "\n");
	return result.toString();
    }
}

class StepInjector{
    protected String min;
    protected String step;

    public String getMin() {
        return min;
    }
    public void setMin(String min) {
        this.min = min;
    }
    
    public String getStep() {
        return step;
    }
    public void setStep(String step) {
        this.step = step;
    }
};

class ValuesListInjector{
    private List<String> values = null;
    
    public List<String> getValues() {
        return values;
    }
    public void setValues(List<String> values) {
        this.values = values;
    }
};