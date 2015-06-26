package it.unibz.inf.data_pumper.core.statistics.xml_model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"numShared","involvedCols"})
public class SharingValues{

    private List<ClusterElement> involvedCols;
    
    private long numShared;

    public long getNumShared() {
        return numShared;
    }

    public void setNumShared(long numShared) {
        this.numShared = numShared;
    }

    @XmlElement(name = "involvedCol")
    public List<ClusterElement> getInvolvedCols() {
        return involvedCols;
    }

    public void setInvolvedCols(List<ClusterElement> involvedCols) {
        this.involvedCols = involvedCols;
    }
    
    @Override
    public String toString(){
	StringBuilder resultBuilder = new StringBuilder();
	resultBuilder.append("cols: "+getInvolvedCols() + "\n");
	resultBuilder.append("ratio: "+getNumShared());
	
	return resultBuilder.toString();
    }
}

@XmlType(propOrder = {"tableName", "colName", "dupsRatio"})
class ClusterElement{
    
    private String tableName;
    private String colName;
    private double dupsRatio;
    

    public double getDupsRatio() {
        return dupsRatio;
    }
    public void setDupsRatio(double dupsRatio) {
        this.dupsRatio = dupsRatio;
    }
    @XmlAttribute
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @XmlAttribute
    public String getColName() {
        return colName;
    }
    public void setColName(String colName) {
        this.colName = colName;
    }
    
    
    
    @Override
    public String toString(){
	StringBuilder result = new StringBuilder();
	result.append("tableName: "+getTableName() + "\n");
	result.append("colName:"+getColName() + "\n");
	return result.toString();
    }
}