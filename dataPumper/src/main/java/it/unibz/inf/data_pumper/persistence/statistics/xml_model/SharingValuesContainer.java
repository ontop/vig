package it.unibz.inf.data_pumper.persistence.statistics.xml_model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class SharingValuesContainer{
    
    private List<SharingValues> sharingList;
    
    @XmlElement(name = "sharingValues")
    public List<SharingValues> getSharingList() {
 	return sharingList;
     }

     public void setSharingList(List<SharingValues> sharing) {
 	this.sharingList = sharing;
     }
     
     @Override
     public String toString(){
	 return getSharingList().toString();
     }
}
