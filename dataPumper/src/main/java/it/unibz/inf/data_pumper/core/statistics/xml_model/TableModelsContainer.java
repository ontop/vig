package it.unibz.inf.data_pumper.core.statistics.xml_model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class TableModelsContainer{
    
    private List<TableModel> tablesList;
    
    @XmlElement(name = "table")
    public List<TableModel> getTablesList() {
	return tablesList;
    }
    
    public void setTablesList(List<TableModel> tables) {
	this.tablesList = tables;
    }
    
    @Override
    public String toString(){
	return getTablesList().toString();
    }
};