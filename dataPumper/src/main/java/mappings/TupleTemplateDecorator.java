package mappings;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * It enriches the Tuple class with 
 * features like the duplicate ratio
 * @author tir
 *
 */
public class TupleTemplateDecorator extends TupleTemplate{
	private Map<String, Float> mTableName_inDupR;
	private float dupR;
	private int numToInsert;
	
	private final TupleTemplate decorated;
	
	TupleTemplateDecorator(TupleTemplate tt){
		decorated = tt;
		mTableName_inDupR = new HashMap<String, Float>();
		numToInsert = 0;
	}
	
	/**
	 * Associates the duplicate ratio <b>dupRatio</b> to the table <b>tableName<b>
	 * for <b>this</b> tuple
	 * @param tableName
	 * @param dupRatio
	 */
	public void setInDupR(String tableName, float dupRatio){
		mTableName_inDupR.put(tableName, dupRatio);
	}
	
	/**
	 * 
	 * @param tableName
	 * @return The duplicate ratio of <b>this</b> tuple template relative to
	 * table <b>tableName</b>
	 */
	public float getInDupR(String tableName){
		return mTableName_inDupR.get(tableName);
	}
	
	/**
	 * 
	 * @return The duplicate ratio for the whole relation w.r.t. <b>this</b> template
	 */
	public float getDupR() {
		return dupR;
	}

	public void addToInsert(int increment) {
		numToInsert += increment;
		TupleStoreFactory.getInstance().getTupleStoreInstance().bufferizeNToInsert(this, numToInsert);
	}

	public int leftToInsert() {
		return numToInsert;
	}
	
	public void decreaseToInsert(){
		--numToInsert;
		TupleStoreFactory.getInstance().getTupleStoreInstance().bufferizeNToInsert(this, numToInsert);
	}

	public void setDupR(float dupR) {
		this.dupR = dupR;
	}

	@Override
	public String getTemplatesString() {
		return decorated.getTemplatesString();
	}

	@Override
	public Set<String> getReferredTables() {
		return decorated.getReferredTables();
	}

	@Override
	public List<String> getColumnsInTable(String tableName) {
		return decorated.getColumnsInTable(tableName);
	}

	@Override
	public String toString() {
		return decorated.toString();
	}

	@Override
	public int belongsToTuple() {
		return decorated.belongsToTuple();
	}

	@Override
	public int getID() {
		return decorated.getID();
	}

	@Override
	public boolean equals(Object other) {
		return decorated.equals(other);
	}

	@Override
	public int hashCode() {
		return decorated.hashCode();
	}


}
