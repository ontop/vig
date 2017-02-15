package it.unibz.inf.vig_mappings_analyzer.obda;

//import it.unibz.inf.vig_mappings_analyzer.core.JoinableColumnsFinder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.RDBMSourceParameterConstants;
import it.unibz.inf.ontop.sql.DBMetadata;
import it.unibz.inf.ontop.sql.DBMetadataExtractor;

//import org.apache.log4j.Logger;

public class OBDAModelFactory {
    
    private static OBDAModel instance = null;
    
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
    
    public static OBDAModel getSingletonOBDAModel(String obdaFile) throws Exception{
	if( instance == null )	instance = makeOBDAModel(obdaFile);
	return instance;
    }
           

    
    /**
     * Davide> Support to ontop 1.18.0
     * @param obdaModel
     * @return
     * @throws SQLException
     */
    public static DBMetadata makeDBMetadata(OBDAModel obdaModel) throws SQLException{
	Collection<OBDADataSource> sources = obdaModel.getSources();
	OBDADataSource source = sources.iterator().next();

	String url = source.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
	String username = source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
	String password = source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
	//		String driver = source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

	Connection localConnection = DriverManager.getConnection(url, username, password);

	DBMetadata meta = DBMetadataExtractor.createMetadata(localConnection);
	DBMetadataExtractor.loadMetadata(meta, localConnection, null);
	return meta;
    }
    
//  /** Retrieve the connection parameters in order to instantiate the
//  * SQL parser.
//  * @throws SQLException 
//  */
// public static SQLQueryParser makeSQLParser(OBDAModel obdaModel) throws SQLException{
//	
//	Collection<OBDADataSource> sources = obdaModel.getSources();
//	OBDADataSource source = sources.iterator().next();
//
//	String url = source.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
//	String username = source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
//	String password = source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
//	//		String driver = source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
//
//	Connection localConnection = DriverManager.getConnection(url, username, password);
//
//
//	// Init metadata
//	// Parse mappings. Just to get the table names in use
//	//		MappingParser mParser = new MappingParser(localConnection, obdaModel.getMappings(source.getSourceID()));
//	//		List<RelationJSQL> realTables = mParser.getRealTables();
//	//		DBMetadata metadata = JDBCConnectionManager.getMetaData(localConnection, realTables);
//
//	// The SQL Translator TODO Continue this
//	SQLQueryParser translator = new SQLQueryParser(new DBMetadata(localConnection.getMetaData()));
//	return translator;
// }
};
