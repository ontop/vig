package it.unibz.inf.data_pumper.mappings;

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

import it.unibz.inf.data_pumper.basic_datatypes.Template;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.connection.DBMSConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Singleton class
 * @author tir
 *
 */
@Deprecated
public class TuplesPicker {
	
	private static TuplesPicker picker = null;
	List<ResultSet> pickFrom;
	int pickIndex;
	private TupleTemplate lastTT;
	private List<String> tableNames;
	private static final String renamePostfix = "_1";
	
	private Map<String, Integer> missedInserts; 
	
	
	private static Logger logger = Logger.getLogger(TuplesPicker.class.getCanonicalName());
	
	private TuplesPicker(){
		pickFrom = new ArrayList<ResultSet>();
		pickIndex = 0;
		lastTT = null;
		tableNames = null;
		missedInserts = new HashMap<String, Integer>();
	};
	
	static TuplesPicker getInstance(){
		if( picker == null ){
			picker = new TuplesPicker();
		}
		return picker;
	}
	
	public List<String> pickTuple(DBMSConnection dbToPump, String curTable, TupleTemplate tt){
		
		if( this.needsInit(tt) ){
			
			this.init(tt);
		}
		
		setValidPickIndex(dbToPump, curTable, tt);
		
		if( !(pickIndex < pickFrom.size()) ) return null;
		
		// Now, according to the tuple cardinality, pick results from the resultSet
		ResultSet rs = pickFrom.get(pickIndex);
		assert rs != null;
		
		List<String> tuple = null;
		
		try {
			if( rs.next() ){
				tuple = new ArrayList<String>();
				for( int i = 1; i <= tt.getColumnsInTable(curTable).size(); ++i ){
					if( rs.getString(i) == null ) return null;
					tuple.add(rs.getString(i));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// TODO Also, I needed to make the union of the domains, but I forgot
		//      Because, otherwise, this tuple might be duplicate
		return tuple;
	}

	private void init(TupleTemplate tt) {
		lastTT = tt;
		tableNames = new ArrayList<String>(tt.getReferredTables());
		pickFrom = new ArrayList<ResultSet>();
		for( int i = 0; i < tableNames.size(); ++ i ) pickFrom.add(null);
	}
	
	/**
	 * To be called each time I end filling a schema
	 */
	public void reset(){
		for( ResultSet rs : pickFrom ){
			try {
				if( rs != null ) rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		this.pickFrom.clear();
		pickIndex = 0;
		lastTT = null;
		tableNames = null;
	}

	private boolean needsInit(TupleTemplate tt) {
		return !tt.equals(lastTT);
	}
	
	/**
	 * It finds a suitable <b>pickIndex</b> to pick values
	 * from the <b>pickFrom</b> resultSet vector
	 * @param dbToPump
	 * @param curTable
	 * @param tt
	 */
	private void setValidPickIndex(DBMSConnection dbToPump, String curTable, TupleTemplate tt) {
		
		if( !(pickIndex < pickFrom.size()) ) return;
		
		try{
			if( pickFrom.get(pickIndex) != null && !pickFrom.get(pickIndex).isAfterLast()){
				// You can pick from here
				return;
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		if( pickFrom.get(pickIndex) == null ){
			if( createNewResultSet(dbToPump, curTable, tt) )
				setValidPickIndex(dbToPump, curTable, tt);
			else{
				++pickIndex;
				setValidPickIndex(dbToPump, curTable, tt);
			}
		}
		else{
			++pickIndex;
			setValidPickIndex(dbToPump, curTable, tt);
		}
	}

	private boolean createNewResultSet(DBMSConnection dbToPump, String curTable, TupleTemplate tt) {
		
		String referredTable = tableNames.get(pickIndex);
		
		if( referredTable.equals(curTable) ) return false;
		
		logger.debug(tt);
		
		PreparedStatement stmt = 
				dbToPump.getPreparedStatement(createTakeTuplesQueryString(dbToPump, curTable, referredTable, tt));
		
		ResultSet rs = null;
		
		try {
			rs = stmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pickFrom.set(pickIndex, rs);
		
		boolean result = false;
		try {
			result = !rs.isAfterLast();
			logger.debug("isAfterLast????" + !result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	/**
	 *   <i>table_a SetMinus table_b</i><br/>
		 SELECT a.x, a.y
		 <br />FROM table_a a LEFT JOIN table_b b
		 ON a.x = b.x AND a.y = b.y
		 WHERE b.x IS NULL;
		 <br /><br />
		 <i>referredTable SetMinus curTable</i><br/>
		 SELECT refRn.x, refRn.y
		 <br />FROM referredTable refRn LEFT JOIN curTable curRn
		 ON refRn.x = curRn.x AND refRn.y = curRn.y
		 WHERE curRn.x IS NULL;
	 * @param curTable
	 * @param referredTable
	 * @param tt
	 */
	private String createTakeTuplesQueryString(DBMSConnection dbToPump, String curTable,
			String referredTable, TupleTemplate tt) {
		
		Template templ = new Template("SELECT ? FROM ? ? LEFT JOIN ? ? ON ? WHERE ? IS NULL LIMIT 300000");
		
		templ.setNthPlaceholder(1, projectionList(referredTable, tt));
		templ.setNthPlaceholder(2, referredTable);
		templ.setNthPlaceholder(3, rename(referredTable));
		templ.setNthPlaceholder(4, curTable);
		templ.setNthPlaceholder(5, rename(curTable));
		templ.setNthPlaceholder(6, onCondition(dbToPump, curTable, referredTable, tt));
		templ.setNthPlaceholder(7, lastInOnCondition(curTable, tt));
		
		logger.debug(templ.getFilled());
		
		return templ.getFilled();
	}

	private String rename(String tableName){
		return tableName + renamePostfix;
	}
	
	/**
	 * WHERE curRn.x IS NULL;
	 * @param curTable
	 * @param tt
	 * @return
	 */
	private String lastInOnCondition(String curTable, TupleTemplate tt) {
		
		String lastCol = tt.getColumnsInTable(curTable).get(tt.getColumnsInTable(curTable).size()-1);
		
		return rename(curTable) + "." + lastCol;
	}

	/**
	 * ON refRn.x = curRn.x AND refRn.y = curRn.y
	 * @param dbToPump 
	 * @param curTable
	 * @param referredTable
	 * @param tt
	 * @return
	 */
	private String onCondition(DBMSConnection dbToPump, String curTable, String referredTable,
			TupleTemplate tt) {
		
		List<String> colsCur = tt.getColumnsInTable(curTable);
		List<String> colsRef = tt.getColumnsInTable(referredTable);
		
		assert colsCur.size() == colsRef.size();
		
		List<String> pkNames = new ArrayList<String>();
		
		// If the tuple strictly subsumes a pk, then the join ON clause 
		// has to involve columns in that pk ONLY
		for( ColumnPumper cP : dbToPump.getSchema(curTable).getPk() ){
			pkNames.add(cP.getName());
		}
		
		StringBuilder builder = new StringBuilder();
		if( colsCur.containsAll(pkNames) ){
			
			for( int i = 0; i < pkNames.size(); ++i ){
				if( !(i == 0) ){
					builder.append(" AND ");
				}
				builder.append(rename(referredTable) + "." + pkNames.get(i));
				builder.append("=");
				builder.append( rename(curTable) + "." + pkNames.get(i) );
			}
		}
		else{
			for( int i = 0; i < colsCur.size(); ++i ){
				if( !(i == 0) ){
					builder.append(" AND ");
				}
				builder.append(rename(referredTable) + "." + colsCur.get(i));
				builder.append("=");
				builder.append( rename(curTable) + "." + colsCur.get(i) );
			}
		}
		
		logger.debug("ON CLAUSE: " + builder.toString());
		
		return builder.toString();
	}

	/**
	 * SELECT refRn.x, curRn.y
	 * @param curTable
	 * @param referredTable
	 * @param tt
	 * @return
	 */
	private String projectionList(String referredTable,
			TupleTemplate tt) {
		
		List<String> cols = tt.getColumnsInTable(referredTable);
		
		StringBuilder builder = new StringBuilder();
		
		for( String col : cols ){
			if( !(builder.length() == 0) ){
				builder.append(", ");
			}
			builder.append(rename(referredTable) + "." + col);
		}
		
		logger.debug("PROJ:" + builder.toString());
		
		return builder.toString();
	}

	public void setMissedInserts(TupleTemplateDecorator candidate, int nMissed) {
		TupleStore tS = TupleStoreFactory.getInstance().getTupleStoreInstance();
		missedInserts.put(tS.getTupleOfID(candidate.belongsToTuple()) + "_" + candidate.getTemplatesString(), nMissed);
	}
	
};