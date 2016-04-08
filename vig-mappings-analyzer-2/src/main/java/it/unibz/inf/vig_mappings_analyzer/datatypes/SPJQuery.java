package it.unibz.inf.vig_mappings_analyzer.datatypes;

import static org.junit.Assert.assertEquals;
import it.unibz.inf.vig_mappings_analyzer.obda.OBDAModelFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.parser.SQLQueryParser;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import it.unibz.krdb.sql.api.RelationJSQL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.JSQLParserException;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Davide Lanti
 * 
 * Immutable view of a select-project-join query
 *
 */
public class SPJQuery{
    private final List<Field> projList;
    private final List<String> tables;
    
    private final SPJQueryHelper helper;
    
    private SPJQuery(List<Field> projList, List<String> tables, SPJQueryHelper helper){
	this.projList = projList;
	this.tables = tables;
	this.helper = helper;
    }

    /**
     * Factory Method
     * 
     * @param sqlQueryString
     * @param parser
     * @return
     * @throws JSQLParserException
     */
    public static SPJQuery makeInstanceFromSQL(String sqlQueryString, SQLQueryParser parser) throws JSQLParserException{
	SPJQueryHelper helper = new SPJQueryHelper(sqlQueryString, parser);
	
	List<Field> projList = helper.getProjectionList();
	List<String> tablesNames = helper.getTableNames();
	
	SPJQuery result = new SPJQuery(projList, tablesNames, helper);
	
	return result;
    } 
    
    /**
     * 
     * @return The projection list of <b>this</b> spj query
     */
    public List<Field> getProjList(){
	return Collections.unmodifiableList(this.projList);
    }
    
    /**
     * 
     * @return The list of all tables' names in the spj query
     */
    public List<String> getTables(){
	return Collections.unmodifiableList(this.tables);
    }
    
    public SPJQueryHelper getSPJQueryHelper(){
	return this.helper;
    }
    
    @Override
    public String toString(){
	StringBuilder resultBuilder = new StringBuilder();
	
	resultBuilder.append("PROJ List: " + this.getProjList());
	resultBuilder.append("\n");
	resultBuilder.append("Tables' List: " + this.getTables());
	
	return resultBuilder.toString();
    }
    
    public static class SPJQueryHelper{
	ParsedSQLQuery queryParsed;

	public SPJQueryHelper(String sqlQueryString, SQLQueryParser parser){
	    parser.parseShallowly(sqlQueryString);
	    this.queryParsed = parser.parseShallowly(sqlQueryString);
	}

	private List<String> getTableNames() throws JSQLParserException{
	    List<String> result = new ArrayList<>();

	    for( RelationJSQL rJSQL : queryParsed.getTables() ){
		String tN = rJSQL.getTableName();
		if( !result.contains(tN) ){
		    result.add(tN);
		}
	    }
	    return result;
	}
	
	/**
	 * @return A projection list with the original table names (no renaming)
	 * @throws JSQLParserException
	 */
	private List<Field> getProjectionList() throws JSQLParserException{
	    List<Field> result = new ArrayList<>();

	    List<String> colNames = this.queryParsed.getProjection().getColumnNameList(); 
	    List<String> tableNames = getTableNames();

	    if( colNames.get(0).contains(".") ){
		// ALIAS.ColName
		for( String qualifiedColName : colNames ){
		    String[]  mAliasTNameToColName = qualifiedColName.split("\\.");
		    String aliasTName = mAliasTNameToColName[0];
		    String aliasColName = mAliasTNameToColName[1];

		    // Now one should check if the table name is an alias
		    aliasTName = getOriginalTableName(aliasTName);

		    // Now one should check if the column name is an alias
		    aliasColName = getOriginalColName(aliasColName);

		    Field f = new Field(aliasTName, aliasColName);
		    result.add(f);
		}
	    }
	    else{
		String tableName = tableNames.get(0);
		for( String cN : colNames ){
		    cN = getOriginalColName(cN);
		    Field f = new Field(tableName, cN);
		    result.add(f);
		}
	    }
	    return result;
	}
	
	public String getOriginalTableName(String alias) throws JSQLParserException{
	    
	    List<String> tableNames = getTableNames();
	    
	    if( !tableNames.contains(alias) ){ // ALIAS
		for( RelationJSQL rJSQL : queryParsed.getTables() ){
		    if( rJSQL.getAlias().equals(alias) ){
			alias = rJSQL.getTableName();
		    }
		}
	    }
	    return alias;
	}
	
	public String getOriginalColName(String alias){
	    Map<String, String> aliases = queryParsed.getAliasMap();

	    String result = alias;

	    if( aliases.containsValue(alias) ){
		for( String originalName : aliases.keySet() ){
		    if( aliases.get(originalName).equals(alias) ){
			result = originalName;
			// Strip from the "."
			if( result.contains(".") ){
			    result = result.substring(result.indexOf(".") + 1, result.length());
			}
			break;
		    }
		}
	    }
	    return result;
	}

	public String getOriginalTableNameForCol(String aliasColName) {
	    assert aliasColName.contains(".") : "Dot not present for aliasColName " + aliasColName;
	    return aliasColName.substring(0, aliasColName.indexOf("."));
	}
    };
    
    public static class TestSPJQuery{
	private static OBDAModel model;
	private static SQLQueryParser parser;
	private static String sql = "SELECT A.prlNpdidLicence AS caf, B.prlTaskID FROM licence_task AS A, licence_task AS B WHERE A.prlTaskRefID=B.prlTaskID";
	private static SPJQuery spj;
	
	@BeforeClass
	public static void init(){
	    try {
		model = OBDAModelFactory.makeOBDAModel("src/main/resources/npd-v2-ql_a.obda");
		parser = OBDAModelFactory.makeSQLParser(model);
		spj = SPJQuery.makeInstanceFromSQL(sql, parser);
	    } catch (Exception e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	    }
	}
	
	@Test
	public void testGetProjList(){
	    
	    List<String> expected = new ArrayList<>();
	    expected.add("licence_task.prlNpdidLicence");
	    expected.add("licence_task.prlTaskID");
	    
	    assertEquals(expected.toString(), spj.getProjList().toString());	    
	}
	
	@Test
	public void testGetTablesList(){
	    List<String> expected = new ArrayList<>();
	    expected.add("licence_task");
	    
	    assertEquals(expected, spj.getTables());
	}
    }

    public static class TestSPJQueryHelper{

	private static OBDAModel model;
	private static SQLQueryParser parser;
	
	@BeforeClass
	public static void init(){
	    try {
		model = OBDAModelFactory.makeOBDAModel("src/main/resources/npd-v2-ql_a.obda");
		parser = OBDAModelFactory.makeSQLParser(model);
	    } catch (Exception e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	    }
	}
	  
	@Test
	public void testGetProjectionList(){
	    try {
		String sqlQueryStringRenamedTables = "SELECT A.prlNpdidLicence AS caf, B.prlTaskID FROM licence_task AS A, licence_task AS B WHERE A.prlTaskRefID=B.prlTaskID";
		SPJQueryHelper helper = new SPJQueryHelper(sqlQueryStringRenamedTables, parser);

		List<String> expected = new ArrayList<>();
		expected.add("licence_task.prlNpdidLicence");
		expected.add("licence_task.prlTaskID");

		assertEquals(expected.toString(), helper.getProjectionList().toString());

	    } catch (Exception e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	    }
	}
	
	
	@Test
	public void testGetTableNames(){
	    try {
		String sqlQueryStringRenamedTables = "SELECT A.prlNpdidLicence, B.prlTaskID FROM licence_task AS A, licence_task AS B WHERE A.prlTaskRefID=B.prlTaskID";
		SPJQueryHelper helper = new SPJQueryHelper(sqlQueryStringRenamedTables, parser);

		List<String> expected = new ArrayList<>();
		expected.add("licence_task");

		assertEquals(expected, helper.getTableNames());

	    } catch (Exception e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	    }
	}
	
	@Test
	public void testGetOriginalColName(){
	    String sqlQueryStringRenamedColsAndTables = "SELECT A.prlNpdidLicence AS cazzarolina, B.prlTaskID FROM licence_task AS A, licence_task AS B WHERE A.prlTaskRefID=B.prlTaskID";
	    SPJQueryHelper helper = new SPJQueryHelper(sqlQueryStringRenamedColsAndTables, parser);
	    
	    String expected = "prlNpdidLicence".toLowerCase();
	    
	    assertEquals(expected, helper.getOriginalColName("cazzarolina"));
	}
    }
}


