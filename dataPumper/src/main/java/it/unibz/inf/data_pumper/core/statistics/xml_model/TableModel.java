package it.unibz.inf.data_pumper.core.statistics.xml_model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"name", "size", "scaleFormula", "columns"})
@XmlAccessorType (XmlAccessType.FIELD)
public class TableModel{
    private int size;
    
    // pow(n,2), mul(n,3)
    private ScaleFormulaModel scaleFormula;
    
    @XmlAttribute
    private String name;
    
    @XmlElement(name = "column")
    private List<ColumnModel> columns;

    public String getName() {
	return name;
    }
    public void setName(String name) {
	this.name = name;
    }
    
    public int getSize() {
	return size;
    }
    public void setSize(int size) {
	this.size = size;
    }
    public List<ColumnModel> getColumns() {
        return columns;
    }
    public void setColumns(List<ColumnModel> columns) {
        this.columns = columns;
    } 
    
    @Override
    public String toString(){
	StringBuilder result = new StringBuilder();
	result.append("name: "+name + "\n");
	result.append("size: "+size + "\n");
	result.append("columns: ");
	result.append(this.getColumns() + "\n");
	return result.toString();
    }
    public ScaleFormulaModel getScaleFormula() {
	return scaleFormula;
    }
    public void setScaleFormula(ScaleFormulaModel scaleFormula) {
	this.scaleFormula = scaleFormula;
    }
}