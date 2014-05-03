package columnTypes;

import mappings.TuplesPicker;
import connection.DBMSConnection;
import core.ChasePicker;
import core.DuplicatesPicker;
import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;

public abstract class ColumnPumper extends Column implements FreshValuesGenerator{
	
	protected ChasePicker cP;
	private DuplicatesPicker dP;
	
	private TuplesPicker tP;
	
	public ColumnPumper(String name, MySqlDatatypes type, int index){
		super(name, type, index);
		cP = new ChasePicker(this);
		dP = new DuplicatesPicker(this);
	}
	
	public void attachTuplesPicker(TuplesPicker tP){
		this.tP = tP;
	}
	
	public void setDuplicateRatio(float ratio){
		dP.setDuplicateRatio(ratio);
	}
	
	public float getDuplicateRatio(){
		return dP.getDuplicateRatio();
	}
	
	public float getNullRatio(){
		return dP.getNullRatio();
	}
	
	public void setNullRatio(float ratio){
		dP.setNullRatio(ratio);
	}
	
	@Override
	public String pickNextDupFromDuplicatesToInsert(){
		return dP.pickNextDupFromDuplicatesToInsert();
	}
	@Override
	public void beforeFirstDuplicatesToInsert(){
		dP.beforeFirstDuplicatesToInsert();
	}
	
	@Override
	public void fillDuplicates(DBMSConnection dbmsConn, Schema schema, int insertedRows){
		dP.fillDuplicates(dbmsConn, schema.getTableName(), insertedRows);
	}
	
	@Override
	public void refillCurChaseSet(DBMSConnection dbConn, Schema s){
		cP.refillCurChaseSet(dbConn, s);
	}
	@Override
	public boolean hasNextChase(){
		return cP.hasNextChase();
	}
	
	@Override
	public int getCurrentChaseCycle(){
		return cP.getCurrentChaseCycle();
	}
	
	@Override
	public void incrementCurrentChaseCycle(){
		cP.incrementChaseCycle();
	}
	
	@Override
	public int getMaximumChaseCycles(){
		return cP.getMaximumChaseCycles();
	}
	
	@Override
	public void setMaximumChaseCycles(int maximumChaseCycles){
		cP.setMaximumChaseCycles(maximumChaseCycles);
	}
	
	@Override
	public String getFromReferenced(DBMSConnection db, Schema schema){
		return cP.getFromReferenced(db, schema);
	}

	@Override
	public void reset(){
		cP.reset();
		dP.reset();
		tP = null; // Detach tP.
		System.gc();
	}
	
	/**
	 * TODO
	 * @return
	 */
	public boolean partOfTuple() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public boolean takeValue() {
		// TODO Auto-generated method stub
		return false;
	}
};