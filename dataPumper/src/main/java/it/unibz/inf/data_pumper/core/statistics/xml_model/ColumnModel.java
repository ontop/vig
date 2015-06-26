package it.unibz.inf.data_pumper.core.statistics.xml_model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"name", "dupsRatio", "nullsRatio", "domain"})
@XmlAccessorType (XmlAccessType.FIELD)
public class ColumnModel{

    @XmlAttribute
    private String name;
    private double dupsRatio;
    private double nullsRatio;
    
    private FixedDomain domain;
    
    public FixedDomain getDomain() {
        return domain;
    }

    public void setDomain(FixedDomain domain) {
        this.domain = domain;
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

class FixedDomain{
    private Long min = null;
    private Long max = null;
    private List<String> values = null;
    
    public Long getMin() {
        return min;
    }
    public void setMin(Long min) {
        this.min = min;
    }
    public Long getMax() {
        return max;
    }
    public void setMax(Long i) {
        this.max = i;
    }
    public List<String> getValues() {
        return values;
    }
    public void setValues(List<String> values) {
        this.values = values;
    }
}