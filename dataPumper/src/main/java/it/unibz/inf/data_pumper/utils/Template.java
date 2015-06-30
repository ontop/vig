package it.unibz.inf.data_pumper.utils;

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

/**
 * 
 * @author tir
 *
 * Constraints: 
 * 
 * 1) The template string CANNOT start with a placeholder '?'
 * 2) There cannot be two consecutive placeholders --this would not make sense, anyway
 */
public class Template{
	private String[] splits;
	private String[] fillers;
	private String template;
	private String placeholder;
	
	public Template(String templateString){
		template = templateString;
		placeholder = "?";
		parseTemplate();
	}
	
	public Template(String templateString, String placeholder){
		template = templateString;
		this.placeholder = placeholder;
		parseTemplate();
	}
	
	/** 
	 * 
	 * @param n value greater than 1
	 * @param filler
	 */
	public void setNthPlaceholder(int n, String filler) {
		fillers[n-1] = filler;
	}
	
	private void parseTemplate(){
		splits = template.split("\\"+placeholder);
		int cnt = 0;
		for( int i = 0; i < template.length(); i++ ){
			if( template.charAt(i) == placeholder.charAt(0) ) cnt++;
		}
		fillers = new String[cnt];
	}
	
	public String getFilled(){
		StringBuilder temp = new StringBuilder();
		for( int i = 0; i < splits.length; i++ ){
			temp.append(splits[i]);
			if( i < fillers.length ) temp.append(fillers[i]);
		}
		return temp.toString(); 
	}
	
	public int getNumPlaceholders(){
		return fillers.length;
	}
	
	public String toString(){
		return template;
	}
};