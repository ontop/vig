package cyclesFinder;

import java.util.List;

import utils.Pair;
import connection.DBMSConnection;

public class Main {

private static String jdbcConnector = "jdbc:mysql";
private static String databaseUrl = "localhost/provaNpdOriginal";
private static String username = "tir";
private static String password = "";

public static void main(String[] args){
	
	DBMSConnection dbmsConn = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
	CyclesFinder c = new CyclesFinder();
	
	Pair<List<String>, List<String>> v_w = c.columnsToMap(dbmsConn, "licence_task", "prlTaskID", "prlTaskRefID");
	
	List<String> v = v_w.first;
	List<String> w = v_w.second;
	
	List<List<String>> paths = c.chasePaths(v, w);
	
	System.err.println(paths);
	
	// Find the path of maximal length
	int max = 0;
	for( List<String> path : paths ){
		max = path.size() > max ? path.size() : max;
	}
	System.err.println(max);
}


}
