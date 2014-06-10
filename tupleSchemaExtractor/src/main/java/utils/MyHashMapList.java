package utils;

/*
 * #%L
 * tupleSchemasExtractor
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import core.CSVPlayer;

public class MyHashMapList<Key,Value> {
	private HashMap<Key, List<Value>> map;
	
	public MyHashMapList(){
		map = new HashMap<Key, List<Value>>();
	}
	
	public MyHashMapList(MyHashMapList<Key, Value> m){
		map = m.map;
	}
	
	public Set<Key> keyset(){
		return map.keySet();
	}
	
	public static MyHashMapList<String, String> parse(String csv) {
		MyHashMapList<String, String> tempMap = new MyHashMapList<String, String>();
		// TODO Auto-generated method stub
		String[] splits = csv.split("\\r?\\n");
		for( String s: splits ){
			List<String> fields = CSVPlayer.parseRow(s, " ");
			if( fields.isEmpty() ) continue;
			String key = fields.get(0);
			String value = CSVPlayer.toCSVString(fields.subList(1, fields.size()));
			tempMap.put(key, value);			
		}
		return tempMap;
	}

	public void put(Key k, Value v){
		if( map.containsKey(k) ){
			map.get(k).add(v);
		}
		else{
			map.put(k, new ArrayList<Value>());
			map.get(k).add(v);
		}
	}
	
	/**
	 * The list values is <b>copied</b>
	 * @param k
	 * @param values
	 */
	public void putAll(Key k, List<Value> values){
		if( map.containsKey(k) ){
			map.get(k).addAll(values);
		}
		else{
			map.put(k, new ArrayList<Value>());
			map.get(k).addAll(values);
		}
	}
	
	public List<Value> get(Key key){
		return map.get(key);
	}
	
	public boolean containsKey(Key key){
		return map.containsKey(key);
	}
	
	public void removeAll(Collection<?> keys){
		for(Object k : keys){
			map.remove(k);
		}
	}
	
	public String toCSV(){
		StringBuilder builder = new StringBuilder();
		
		for( Key key : map.keySet() ){
			for( Value value : map.get(key) ){
				builder.append(key.toString());
				builder.append(" " + value.toString());
				builder.append("\n");
			}
		}
		return builder.toString();
	}
	
}
