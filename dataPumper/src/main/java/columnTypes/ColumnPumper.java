package columnTypes;

/*
 * #%L
 * dataPumper
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;

import connection.DBMSConnection;
import core.main.tableGenerator.aggregatedClasses.ChasePicker;
import core.main.tableGenerator.aggregatedClasses.DuplicatesPicker;
import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;

public abstract class ColumnPumper extends Column implements FreshValuesGenerator{
	
	protected ChasePicker cP;
	private DuplicatesPicker dP;
	protected boolean ignore;
	
	public ColumnPumper(String name, MySqlDatatypes type, int index){
		super(name, type, index);
		cP = new ChasePicker(this);
		dP = new DuplicatesPicker(this);
		ignore = false;
	}
	
	public abstract void proposeLastFreshInserted(String inserted);
	
	public boolean ignore(){
		return ignore;
	}
	
	/**
	 * Do NOT fill this column
	 */
	public void setIgnore(){
		ignore = true;
	}
	/**
	 * Filling of this column is enabled again
	 */
	public void unsetIgnore(){
		ignore = false;
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
	
	public void resetChases(){
		cP.reset();
	}

	@Override
	public void reset(){
		cP.reset();
		dP.reset();
		System.gc();
	}

	public String getNextChased(DBMSConnection dbmsConn, Schema schema,
			List<String> uncommittedFreshs) {
		String result = cP.pickChase(dbmsConn, schema, uncommittedFreshs);
		return result;
	}
};