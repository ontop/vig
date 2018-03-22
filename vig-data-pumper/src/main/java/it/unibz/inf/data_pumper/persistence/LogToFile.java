package it.unibz.inf.data_pumper.persistence;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogToFile {

  final static String LOGPATH = "csvs/";
  final static int FLUSH_INTERVAL = 10000;

  PrintWriter outCSV = null;

  private static LogToFile instance = null;
  private long cnt;

  private LogToFile(){cnt = 0;}

  public static LogToFile getInstance(){
    if( instance == null ) instance = new LogToFile();
    return instance;
  }

  public void openFile(String fileName) throws IOException{
    outCSV = new PrintWriter(new BufferedWriter(new FileWriter(LOGPATH + fileName)));
  }

  public void appendLine( String line ){
    outCSV.println(line);
    ++cnt;
    if( cnt % FLUSH_INTERVAL == 0 ) outCSV.flush();
  }

  public void closeFile(){
    if( outCSV != null ){
      outCSV.flush();
      outCSV.close();
    }
  }

  public void flush(){
    outCSV.flush();
  }

};
