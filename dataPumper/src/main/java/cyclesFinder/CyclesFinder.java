package cyclesFinder;

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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.Pair;
import connection.DBMSConnection;

/** 
 * Everything is handled in-memory
 * @author tir
 *
 */
public class CyclesFinder {
	
	
	/**
	 * v[i],w[i] is a tuple in the table, for each i in the cardinality of the table
	 * @param v
	 * @param w
	 * @return
	 */
	public List<List<String>> chasePaths(List<String> v, List<String> w){
		
		if( v.size() != w.size() ) return null;
		
		List<List<String>> paths = new ArrayList<List<String>>();
		
		for( int i = 0; i < v.size(); ++i ){
			List<String> path = new ArrayList<String>();
			path.add(v.get(i));
			find_path(i, v, w, path);
			paths.add(path);
		}
		
		return paths;
	}
	
	private void find_path(int i, List<String> v, List<String> w,
			List<String> arrayList) {
		
		if( ! v.contains(w.get(i)) ) return; // The cycle terminates in a NULL
		
		int j = v.indexOf(w.get(i));
		
		if( arrayList.contains(v.get(j)) ) return;
		
		arrayList.add(v.get(j));
		find_path(j, v, w, arrayList);
	}
	public Pair<List<String>, List<String>> columnsToMap(DBMSConnection dbmsConn, String tableName, 
			String keyColName, String referringColName){
		
		PreparedStatement stmt = dbmsConn.getPreparedStatement("SELECT "+keyColName+", "+referringColName+" FROM "+tableName);
		
		List<String> v = new ArrayList<String>();
		List<String> w = new ArrayList<String>();
		
		try {
			ResultSet rs = stmt.executeQuery();
			while( rs.next() ){
				v.add(rs.getString(1)); // This is UNIQUE
				w.add(rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new Pair<List<String>, List<String>>(v,w);		
	}
	
	public Map<String,String> columnsToMap(DBMSConnection dbmsConn, String referringTableName, 
			String referringColName, String referencedTableName, String referencedColumnName){
		
		return null;
	}
	
}
