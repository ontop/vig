package it.unibz.inf.data_pumper.persistence.statistics.xml_model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement // It specifies who is the root element
@XmlAccessorType (XmlAccessType.FIELD) // It specifies that access to field happen directly and NOT through getters and setter
@XmlType(propOrder = {"scaleFactor","tables","sharing"})
public class DatabaseModel {

    private double scaleFactor;
    private TableModelsContainer tables;
    private SharingValuesContainer sharing;

    public TableModelsContainer getTables() {
	return tables;
    }
    
    public void setTables(TableModelsContainer tables) {
	this.tables = tables;
    }
    
    public SharingValuesContainer getSharing() {
	return sharing;
    }
    
    public void setSharing(SharingValuesContainer sharing) {
	this.sharing = sharing;
    }
    
    @Override
    public String toString(){
	StringBuilder resultBuilder = new StringBuilder();
	resultBuilder.append("tables: "+tables.toString() + "\n");
	resultBuilder.append("sharing: "+sharing.toString() + "\n");
	
	return resultBuilder.toString();
    }

    public double getScaleFactor() {
	return scaleFactor;
    }
    
    public void setScaleFactor(double scaleFactor) {
	this.scaleFactor = scaleFactor;
    }
}