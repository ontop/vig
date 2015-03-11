package persistence;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogToFile {

	final static String LOGPATH = "src/main/resources/csvs/";
	
	PrintWriter outCSV = null; 
	
	public void openFile(String fileName) throws IOException{
		outCSV = new PrintWriter(new BufferedWriter(new FileWriter(LOGPATH + fileName)));
	}
	
	public void appendLine( String line ){
		outCSV.println(line);
		outCSV.flush();
	}
	
	public void closeFile(){
		if( outCSV != null ) 
			outCSV.close();
	}
		
};
