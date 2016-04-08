package it.unibz.inf.vig_mappings_analyzer.obda;

//import it.unibz.inf.vig_mappings_analyzer.core.JoinableColumnsFinder;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.parser.SQLQueryParser;
import it.unibz.krdb.sql.DBMetadata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

//import org.apache.log4j.Logger;

public class OBDAModelFactory {
        
    // Parameters
//    private String obdaFile = "resources/npd-v2-ql_a.obda";
//
//    private static Logger logger = Logger.getLogger(JoinableColumnsFinder.class.getCanonicalName());

    
    /**
     * Load the OBDA model from an external .obda file
     */
    public static OBDAModel makeOBDAModel(String obdaFile) throws Exception{
	
	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	OBDAModel obdaModel = fac.getOBDAModel();
	ModelIOManager ioManager = new ModelIOManager(obdaModel);
	ioManager.load(obdaFile);
	return obdaModel;
    }
    
    /** Retrieve the connection parameters in order to instantiate the
     * SQL parser.
     * @throws SQLException 
     */
    public static SQLQueryParser makeSQLParser(OBDAModel obdaModel) throws SQLException{
	
	Collection<OBDADataSource> sources = obdaModel.getSources();
	OBDADataSource source = sources.iterator().next();

	String url = source.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
	String username = source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
	String password = source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
	//		String driver = source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

	Connection localConnection = DriverManager.getConnection(url, username, password);


	// Init metadata
	// Parse mappings. Just to get the table names in use
	//		MappingParser mParser = new MappingParser(localConnection, obdaModel.getMappings(source.getSourceID()));
	//		List<RelationJSQL> realTables = mParser.getRealTables();
	//		DBMetadata metadata = JDBCConnectionManager.getMetaData(localConnection, realTables);

	// The SQL Translator TODO Continue this
	SQLQueryParser translator = new SQLQueryParser(new DBMetadata(localConnection.getMetaData()));
	return translator;
    }
}
