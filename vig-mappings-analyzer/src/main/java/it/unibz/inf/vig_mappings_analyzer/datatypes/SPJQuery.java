package it.unibz.inf.vig_mappings_analyzer.datatypes;

import static org.junit.Assert.assertEquals;

import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.parser.SQLQueryDeepParser;
import it.unibz.inf.ontop.sql.DBMetadata;
import it.unibz.inf.ontop.sql.QuotedID;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.sql.api.ParsedSQLQuery;
import it.unibz.inf.vig_mappings_analyzer.obda.OBDAModelFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

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
    public static SPJQuery makeInstanceFromSQL(String sqlQueryString, DBMetadata meta) throws JSQLParserException{
	SPJQueryHelper helper = new SPJQueryHelper(sqlQueryString, meta);
	
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
	DBMetadata meta;

	public SPJQueryHelper( String sqlQueryString, DBMetadata meta ){
	    // Create parser
	    this.queryParsed = SQLQueryDeepParser.parse(meta, sqlQueryString);
	    this.meta = meta;
	}
	
	private List<String> getTableNames() throws JSQLParserException{
	    List<String> result = new ArrayList<>();

	    for( RelationID rID : queryParsed.getRelations() ){
		String tN = rID.getTableName();
		if( !result.contains(tN) ){
		    result.add(tN);
		}
	    }
	    return result;
	}
	
//	private List<RelationID> getRelationsID() throws JSQLParserException{
//	    List<RelationID> result = new ArrayList<>();
//
//	    for( RelationID rID : queryParsed.getRelations() ){
//		if( !result.contains(rID) ){
//		    result.add(rID);
//		}
//	    }
//	    return result;
//	}
	
	/**
	 * @return A projection list with the original table names (no renaming)
	 * @throws JSQLParserException
	 */
	private List<Field> getProjectionList() throws JSQLParserException{
	    List<Field> result = new ArrayList<>();
	    List<Column> cols = new ArrayList<>();
	    
	    for( SelectExpressionItem col : this.queryParsed.getProjection().getColumnList() ){
		cols.add((Column)col.getExpression());
	    }
	    
	    for( Column c : cols ){
		String aliasTName = c.getTable().getName();
		String aliasCollName = c.getColumnName();
		
		String originalTName = getOriginalTableName(aliasTName);
		String originalCName = getOriginalColName(aliasCollName);
		
		Field f = new Field(originalTName, originalCName);
		result.add(f);
	    }
	    return result;
	}
	
	public String getOriginalTableName(String alias) throws JSQLParserException{
	    RelationID aliasID = meta.getQuotedIDFactory().createRelationID(null, alias);
	    Map<RelationID, RelationID> tables = queryParsed.getTables(); // alias -> originalTableName
	    RelationID originalTable = tables.get(aliasID);
	    return originalTable.toString();
	}
	
	public String getOriginalColName(String aliasName){
	    
	    String result = aliasName;
	    
	    // aliases = {cazzarolina=A.prlNpdidLicence}
	    Map<QuotedID, Expression> aliases = queryParsed.getAliasMap();
	    QuotedID alias = QuotedID.createIdFromDatabaseRecord(meta.getQuotedIDFactory(), "cazzarolina");
	    if( aliases.containsKey(alias) ){
		Column col = (Column) aliases.get(alias);
		result = col.getColumnName();
	    }
	    return result;
	}
	
	public String getOriginalTableNameForCol(String aliasColName) {
	    assert aliasColName.contains(".") : "Dot not present for aliasColName " + aliasColName;
	    return aliasColName.substring(0, aliasColName.indexOf("."));
	}
	
//	public SPJQueryHelper(String sqlQueryString, SQLQueryShallowParser parser){
//	    parser.parse(sqlQueryString);
//	    this.queryParsed = parser.parse(sqlQueryString);
//	}
    };
    
    public static class TestSPJQuery{
	private static OBDAModel model;
	private static DBMetadata meta;
	private static String sql = "SELECT A.prlNpdidLicence AS caf, B.prlTaskID FROM licence_task AS A, licence_task AS B WHERE A.prlTaskRefID=B.prlTaskID";
	private static SPJQuery spj;
	
	@BeforeClass
	public static void init(){
	    try {
		model = OBDAModelFactory.makeOBDAModel("src/main/resources/npd-v2-ql_a.obda");
		meta = OBDAModelFactory.makeDBMetadata(model);
		spj = SPJQuery.makeInstanceFromSQL(sql, meta);
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

    /** 
     * Test Class
     * @author Davide Lanti
     *
     */
    public static class TestSPJQueryHelper{

	private static OBDAModel model;
	private static DBMetadata meta;
	
	@BeforeClass
	public static void init(){
	    try {
		model = OBDAModelFactory.makeOBDAModel("src/main/resources/npd-v2-ql_a.obda");
		meta = OBDAModelFactory.makeDBMetadata(model);
	    } catch (Exception e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	    }
	}
	  
	@Test
	public void testGetProjectionList(){
	    try {
		String sqlQueryStringRenamedTables = "SELECT A.prlNpdidLicence AS caf, B.prlTaskID FROM licence_task AS A, licence_task AS B WHERE A.prlTaskRefID=B.prlTaskID";
		SPJQueryHelper helper = new SPJQueryHelper(sqlQueryStringRenamedTables, meta);

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
		SPJQueryHelper helper = new SPJQueryHelper(sqlQueryStringRenamedTables, meta);

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
	    SPJQueryHelper helper = new SPJQueryHelper(sqlQueryStringRenamedColsAndTables, meta);
	    String expected = "prlNpdidLicence";
	    assertEquals(expected, helper.getOriginalColName("cazzarolina"));
	}
    }
}


