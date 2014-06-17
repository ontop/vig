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

import basicDatatypes.Schema;
import connection.DBMSConnection;

public interface FreshValuesGenerator {
	
	public String getNextFreshValue();
	public String getNextChased(DBMSConnection db, Schema schema);
	public String getFromReferenced(DBMSConnection db, Schema schema);
	
	public void fillDomain(Schema schema, DBMSConnection db);
	public void fillDomainBoundaries(Schema schema, DBMSConnection db);
	
	public int getCurrentChaseCycle();
	public void incrementCurrentChaseCycle();
	public int getMaximumChaseCycles();
	public void setMaximumChaseCycles(int maximumChaseCycles);
	public abstract boolean hasNextChase();
	public abstract void refillCurChaseSet(DBMSConnection conn, Schema s);
	
	// Duplicates handling
	public abstract void fillDuplicates(DBMSConnection dbmsConn, Schema schema, int insertedRows);
	public abstract String pickNextDupFromDuplicatesToInsert();
	public abstract void beforeFirstDuplicatesToInsert();

	public void reset(); // To reset the internal state

}
