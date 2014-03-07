package columnTypes;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.QualifiedName;
import basicDatatypes.Schema;

public abstract class Column implements FreshValuesGenerator {
	private final String name;
	private final MySqlDatatypes type;
	private boolean allDifferent;
	private boolean primary;
	protected boolean geometric;
	private boolean independent;
	private boolean autoincrement;
	private Schema schema;
	private List<QualifiedName> referencesTo; // this.name subseteq that.name
	private List<QualifiedName> referencedBy; // that.name subseteq this.name
	
	// Length of the datatype
	protected int datatypeLength;
	
	private final int index;	
	
	// Pumping related properties (as they change during the execution of pumpTable)
	
	private int maximumChaseCycles; // The maximum number of times fresh elements should be created for this column 
	// --- Each fresh element triggers a chase if some other column depends on this column
	private int currentChaseCycle;  // Number of times that this column triggered a chase during pumping
	private float duplicatesRatio;
	
	protected static Logger logger = Logger.getLogger(Column.class.getCanonicalName());
	
	// ---------------------- //
	
	public Column(String name, MySqlDatatypes type, int index){
		this.name = name;
		this.type = type;
		this.primary = false;
		this.independent = false;
		this.allDifferent = false;
		this.autoincrement = false;
		referencesTo = new ArrayList<QualifiedName>();
		referencedBy = new ArrayList<QualifiedName>();
		this.maximumChaseCycles = Integer.MAX_VALUE;
		this.currentChaseCycle = 0;
		this.duplicatesRatio = 0;
		this.index = index;
		this.geometric = false;
		this.datatypeLength = 15; // A default value
		
	}
	
	public Column(Schema schema, String name, MySqlDatatypes type, int index){
		this.name = name;
		this.type = type;
		this.primary = false;
		this.independent = false;
		this.allDifferent = false;
		this.autoincrement = false;
		referencesTo = new ArrayList<QualifiedName>();
		referencedBy = new ArrayList<QualifiedName>();
		this.maximumChaseCycles = Integer.MAX_VALUE;
		this.currentChaseCycle = 0;
		this.duplicatesRatio = 0;
		this.index = index;
		this.geometric = false;
		this.datatypeLength = 15; // A default value
		this.schema = schema;
	}
	
	public Schema getSchema(){
		return schema;
	}
	
	public boolean isGeometric(){
		return geometric;
	}
	
	public int getIndex(){
		return index;
	}
	
	public void setDuplicateRatio(float ratio){
		duplicatesRatio = ratio;
	}
	
	public float getDuplicateRatio(){
		return duplicatesRatio;
	}
	
	public void setPrimary(){
		primary = true;
	}
	
	public boolean isPrimary(){
		return primary;
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

	public int getCurrentChaseCycle(){
		return currentChaseCycle;
	}
	
	public void incrementCurrentChaseCycle(){
		++currentChaseCycle;
	}

	public int getMaximumChaseCycles() {
		return maximumChaseCycles;
	}

	public void setMaximumChaseCycles(int maximumChaseCycles) {
		this.maximumChaseCycles = maximumChaseCycles;
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
		return name;
	}

	public int getDuplicatesDistribution() {
		 // TODO
		return 0;
	}
};