package mappings;

import configuration.Conf;
import connection.DBMSConnection;
import utils.MyHashMapList;
import main.java.csvPlayer.core.CSVPlayer;
import core.TuplesToCSV;

/**
 * 
 * @author tir
 * @note This is a singleton class
 */
public class TupleStoreFactory {
	private final String obdaFile;
	private final TupleStore store;
	private final TuplesPicker picker;
	private final DBMSConnection dbmsConnOriginal;
	
	private static String outCSVFile = "resources/mappingsCSV.csv";
	private static TupleStoreFactory instance = null;
	
	private TupleStoreFactory(DBMSConnection dbmsConnOriginal){
		
		this.obdaFile = Conf.mappingsFile();
		this.dbmsConnOriginal = dbmsConnOriginal;
		this.picker = TuplesPicker.getInstance();
		
		TuplesToCSV tuplesExtractor = new TuplesToCSV(obdaFile, outCSVFile);
		try {
			tuplesExtractor.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
		CSVPlayer csvParser = new CSVPlayer(outCSVFile);
		System.err.println(csvParser.printCSVFile());
		MyHashMapList<String, String> tuplesHash = 
				MyHashMapList.parse(csvParser.printCSVFile());
		
		this.store = TupleStore.getInstance(tuplesHash);
	}
	
	public static TupleStoreFactory getInstance(){
		return instance;
	}
	
	public static void setInstance(DBMSConnection dbmsConnOriginal, String obdaFile){
		if( instance != null ) return;
		instance = new TupleStoreFactory(dbmsConnOriginal);
	}

	public DBMSConnection getDBMSConnection(){
		return this.dbmsConnOriginal;
	}
	
	public TupleStore getTupleStoreInstance(){
		return store;
	}
	
	public TuplesPicker getTuplesPickerInstance(){
		return picker;
	}
	
}
