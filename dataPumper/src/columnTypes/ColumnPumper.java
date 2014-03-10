package columnTypes;

import connection.DBMSConnection;
import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;

public abstract class ColumnPumper extends Column implements FreshValuesGenerator{
	
	// Pumping related properties (as they change during the execution of pumpTable)
	
	private int maximumChaseCycles; // The maximum number of times fresh elements should be created for this column 
	// --- Each fresh element triggers a chase if some other column depends on this column
	private int currentChaseCycle;  // Number of times that this column triggered a chase during pumping
	private float duplicatesRatio;
	
	private boolean chaseSetSkipOccurred; // True if it occurred a change of the set from where chased values were being taken.
	
	
	public ColumnPumper(String name, MySqlDatatypes type, int index){
		super(name, type, index);
	
		this.maximumChaseCycles = Integer.MAX_VALUE;
		this.currentChaseCycle = 0;
		this.duplicatesRatio = 0;
	}
	
	public abstract boolean hasNextChase();
	
	public abstract void refillCurChaseSet(DBMSConnection conn, Schema s);
	
	public void setDuplicateRatio(float ratio){
		duplicatesRatio = ratio;
	}
	
	public float getDuplicateRatio(){
		return duplicatesRatio;
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
	
	public void setChaseSetSkipOccurred() {
		chaseSetSkipOccurred = true;
	}
	
	public boolean chaseSetSkipOccurred(){
		return chaseSetSkipOccurred;
	}
	
	public void unsetChaseSetSkipOccurred(){
		chaseSetSkipOccurred = false;
	}
};