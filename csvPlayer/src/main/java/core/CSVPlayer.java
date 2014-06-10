package core;

/*
 * #%L
 * csvPlayer
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import exceptions.InvalidColumnIndexException;


/**
 * Utilities to work on csv files
 * :'( I want AWK, though
 * @author tir
 *
 */
public class CSVPlayer {
	
	private String file;
	private String separator;
	
	public CSVPlayer(String csvFile){
		file = csvFile;
		separator = " ";
	}
	
	public CSVPlayer(String csvFile, String separator){
		this.file = csvFile;
		this.separator = separator;
	}
	/**
	 * 
	 * @return String format of the CSV fileA
	 */
	public String printCSVFile(){
		StringBuilder builder = new StringBuilder();
		try{
			BufferedReader in = new BufferedReader(new FileReader(this.file));
			
			String s;
			while( (s = in.readLine()) != null ){
				builder.append(s);
				builder.append("\n");
			}
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return builder.toString();
	}
	
	/**
	 * It takes a csv row <b>csvRow</b> and returns the list of all column values in <b>csvRow</b>
	 * @param csvRow
	 * @param separator
	 * @return
	 */
	public static List<String> parseRow(String csvRow, String separator){
		
		List<String> result = new ArrayList<String>();
		
		String[] s = csvRow.split("\\"+separator);
		for( String colValue : s){
			result.add(colValue);
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param cols
	 * @return The list content in csv format
	 */
	public static <T> String toCSVString(List<T> cols){
		StringBuilder builder = new StringBuilder();
		
		for( T s : cols ){
			builder.append(s.toString());
			builder.append(" ");
		}
		builder.delete(builder.length() -1, builder.length());
		
		return builder.toString();
	}
	
	/**
	 * It returns all those lines where the string <b>tag</b> occurs in column <b>columnIndex</b>
	 * 
	 * @param tag
	 * @param columnIndex: Like in awk, the first column is 1
	 * @return List of strings
	 * @throws InvalidColumnIndexException
	 */
	public List<String> searchAllOccurrencesOfTag(String tag, int columnIndex) throws InvalidColumnIndexException{
		
		List<String> result = new ArrayList<String>();
		
		try{
			BufferedReader in = new BufferedReader(new FileReader(this.file));
			String s;
			
			while( (s = in.readLine()) != null ){
				String[] s2 = s.split(separator);
				if( s2.length < columnIndex ){
					in.close();
					throw new InvalidColumnIndexException("Out of range");
				}
				if( s2[columnIndex-1].equals(tag) ){
					result.add(s);
				}
			}
			in.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return result;
	}
	
	public String searchFirstOccurrenceOfTagInFirstColumn(String tag){
		try{
			BufferedReader in = new BufferedReader(
					new FileReader(this.file));
			String s;
			String[] s2 = new String[2];
			while ((s = in.readLine()) != null){
				s2 = s.split(" ");
					if (s2[0].equals(tag)){ in.close(); return s2[1]; }
			}
			in.close();
		}catch(IOException e){
			e.printStackTrace();
			}
		return "error";
	}
};
