package basicDatatypes;

import java.util.ArrayList;
import java.util.List;

public class Column{
	private final String name;
	private final MySqlDatatypes type;
	private boolean allDifferent;
	private boolean independent;
	private boolean autoincrement;
	//	private Domain<T> domain;
	private int maximumChasedElements;
	private List<QualifiedName> referencesTo; // this.name subseteq that.name
	private List<QualifiedName> referencedBy; // that.name subseteq this.name
	
	private double maxValue;
	private double minValue;
	private double lastInserted; // In case of allDifferent
	
	public Column(String name, MySqlDatatypes type){
		this.name = name;
		this.type = type;
		this.independent = false;
		this.allDifferent = false;
		this.autoincrement = false;
		this.maxValue = 0;
		this.minValue = 0;
		this.lastInserted = 0;
		referencesTo = new ArrayList<QualifiedName>();
		referencedBy = new ArrayList<QualifiedName>();
	}
	
	public void setLastInserted(double lastInserted){
		this.lastInserted = lastInserted;
	}
	
	public double getLastInserted(){
		return lastInserted;
	}
	
	public void setMaxValue(double max){
		maxValue = max;
	}
	
	public double getMaxValue(){
		return maxValue;
	}
	
	public void setMinValue(double min){
		minValue = min;
	}
	
	public double getMinValue(){
		return minValue;
	}
	
	public void setAutoIncrement(){
		this.autoincrement = true;
	}
	
	public boolean isAutoIncrement(){
		return autoincrement;
	}
	
	public boolean isIndependent(){
		return independent;
	}
	
	public void setIndependent(){
		independent = true;
	}

	public boolean isAllDifferent() {
		return allDifferent;
	}

	public void setAllDifferent() {
		allDifferent = true;
	}

//	public Domain<T> getDomain() {
//		return domain;
//	}
//
//	public void setDomain(Domain<T> domain) {
//		this.domain = domain;
//	}

	public int getNumberChasedElements() {
		return maximumChasedElements;
	}

	public void setNumberChasedElements(int maximumChasedElements) {
		this.maximumChasedElements = maximumChasedElements;
	}

	public MySqlDatatypes getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
	public List<QualifiedName> referencesTo() {
		return referencesTo;
	}
	
	public List<QualifiedName> referencedBy() {
		return referencedBy;
	}	
	
	public String toString(){
		return "TODO"; //TODO
	}

	public int getDuplicatesDistribution() {
		// TODO Auto-generated method stub
		return 0;
	}
};