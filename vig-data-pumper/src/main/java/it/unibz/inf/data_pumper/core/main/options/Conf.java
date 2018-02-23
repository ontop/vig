package it.unibz.inf.data_pumper.core.main.options;

import java.util.List;

import it.unibz.inf.data_pumper.core.main.VigMain;
import it.unibz.inf.data_pumper.core.main.VigMain.PumperType;
import it.unibz.inf.vig_mappings_analyzer.core.utils.QualifiedName;

public class Conf {
    
    private String jdbcConnector;
    private String dbUrl;
    private String dbUser;
    private String dbPwd;
    private boolean randomGen;
    private String mappingsFile;
    private VigMain.PumperType pumperType;
    private List<QualifiedName> fixed;
    private List<QualifiedName> nonFixed;
    private int ccAnalysisTimeout;
    private double scale;
    private String resourcesDir;
    private String configurationFile;
    private List<QualifiedName> tables;
    private List<QualifiedName> columns;
           
    public Conf(String jdbcConnector, String dbUrl, String dbUser, String dbPwd, boolean randomGen, String mappingsFile,
	    PumperType pumperType, List<QualifiedName> fixed, List<QualifiedName> nonFixed, int ccAnalysisTimeout, double scale,
	    String resourcesDir, String configurationFile, List<QualifiedName> tables, List<QualifiedName> columns) {
	super();
	this.jdbcConnector = jdbcConnector;
	this.dbUrl = dbUrl;
	this.dbUser = dbUser;
	this.dbPwd = dbPwd;
	this.randomGen = randomGen;
	this.mappingsFile = mappingsFile;
	this.pumperType = pumperType;
	this.fixed = fixed;
	this.nonFixed = nonFixed;
	this.ccAnalysisTimeout = ccAnalysisTimeout;
	this.scale = scale;
	this.resourcesDir = resourcesDir;
	this.configurationFile = configurationFile;
	this.tables = tables;
	this.columns = columns;
    }

    /** Restrict the generation to a list of columns. E.g., --columns="table1.col1 table2.col" **/
    public List<QualifiedName> restrictToColumns() {
	return this.columns;
    }
    
    /** Restrict the generation to a list of tables. E.g., --tables="table1 table2 table3"  **/
    public List<QualifiedName> restrictToTables() {
	return this.tables;
    }
    
    /** Path to the configuration file **/
    public String configurationFile() {
	return this.configurationFile;
    }
    
    /** Location of the resources directory **/
    public String resourcesDir() {
	return this.resourcesDir;
    }
    
    /** It specifies the scaling factor **/
    public double scale() {
	return this.scale;
    }
    
    /** Returns the name of the database driver **/
    public String jdbcConnector()  {
	return this.jdbcConnector;
    }
    /** Returns the url of the original database (this will not be pumped. A copy of it will) 
     * **/
    public  String dbUrl() {
	return this.dbUrl; 
    }
    /** Returns the username for the original database (this will not be pumped. A copy of it will) 
     * **/
    public  String dbUser() {
	return this.dbUser;
    }
    /** Returns the password for the original database (this will not be pumped. A copy of it will) 
     * **/
    public  String dbPwd() {
	return this.dbPwd;
    }
    public boolean pureRandom() {
	return this.randomGen;
    }
    /** Returns the obda file containing the mappings 
     * **/
    public  String mappingsFile() {
	return this.mappingsFile;
    }
    /** Returns the configuration scheme for the data generation **/
    public VigMain.PumperType pumperType() {
	return this.pumperType;
    }

    public List<QualifiedName> fixed() {
	return this.fixed;
    }

    public List<QualifiedName> nonFixed() {
	return this.nonFixed;
    }

    public int ccAnalysisTimeout()  {
	return this.ccAnalysisTimeout;
    }
}
