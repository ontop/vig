package it.unibz.inf.vig_mappings_analyzer.core;

/*
 * #%L
 * tupleSchemasExtractor
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

import it.unibz.inf.vig_mappings_analyzer.datatypes.Argument;
import it.unibz.inf.vig_mappings_analyzer.datatypes.Field;
import it.unibz.inf.vig_mappings_analyzer.datatypes.FunctionTemplate;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.parser.SQLQueryParser;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import it.unibz.krdb.sql.api.RelationJSQL;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class JoinableColumnsFinder {
	
	// Parameters
	private String obdaFile = "resources/npd-v2-ql_a.obda";
	
	// Internal State
	private SQLQueryParser translator;
	private OBDAModel obdaModel;
	
	private static Logger logger = Logger.getLogger(JoinableColumnsFinder.class.getCanonicalName());
	
	public JoinableColumnsFinder(String obdaFile) throws Exception {
		this.obdaFile = obdaFile;
		init();
	}

	public void findJoinsInMappings() {
		// TODO
	}
	
	private void init() throws Exception{
		/*
		 * Load the OBDA model from an external .obda file
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		this.obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdaFile);

 		
		/* Retrieve the connection parameters in order to instantiate the
		 * SQL parser.
		 */
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
		this.translator = new SQLQueryParser(new DBMetadata(localConnection.getMetaData()));
	}
	
	public List<FunctionTemplate> findFunctionTemplates() throws Exception {

		List<FunctionTemplate> result = new ArrayList<FunctionTemplate>();
		
		// Y shall do something similar for each dataproperty, and object property
		for( URI uri : obdaModel.getMappings().keySet() ){
			logger.info(uri);
			for( OBDAMappingAxiom a : obdaModel.getMappings(uri) ){

				CQIE targetQuery = (CQIE) a.getTargetQuery();

				for( int i = 0; i < targetQuery.getBody().size(); ++i ){

					Function atom = targetQuery.getBody().get(0);
					OBDASQLQuery sourceQuery = (OBDASQLQuery) a.getSourceQuery();
					
					// Construct the SQL query tree from the source query
					String viewString = sourceQuery.toString();
					logger.info(viewString);
					ParsedSQLQuery queryParsed = translator.parseShallowly(viewString); 
					if( queryParsed.getTables().size() != 1 ){ /*++skipped;*/ continue; } // Skip joins for the moment
					
//					++unskipped;
					
					RelationJSQL table = queryParsed.getTables().get(0);
					String tableName = table.getTableName();
					
					// Get the terms in the atom, and build the related FunctionTemplate classes
					for( Term t : atom.getTerms() ){
						if( t instanceof Function ){
							Function f = (Function) t;
							FunctionTemplate fT = new FunctionTemplate(f);
							boolean old = false;
							for( FunctionTemplate fTOld : result ){
								if( fTOld.getTemplateString().equals(fT.getTemplateString()) ){
									fT = fTOld;
									old = true;
									break;
								}
							}
							
							if( fT.isURI() ){ // Consider only Joins over URIs
								Map<String, String> aliases = queryParsed.getAliasMap();	
								
//							    @Override
//							    public List<Variable> getVariablesList() {
//							        List<Variable> variables = new ArrayList<Variable>();
//							        for (Term t : terms) {
//							            for (Variable v : t.getReferencedVariables())
//							                variables.add(v);
//							        }
//							        return variables;
//								}
								
								// Retrieve list of variables
								List<Variable> varlist = new ArrayList<Variable>();
								for( Variable v : f.getVariables() ){
								    varlist.add(v);
								}
								
//								List<Variable> varlist = new LinkedList<>();
//						        TermUtils.addReferencedVariablesTo(varlist, f); TODO For 1.14.1 (Next release)
								
								for( int cnt = 0; cnt < varlist.size(); ++cnt ){
								    
									Variable v = varlist.get(cnt);
									Argument arg = null;
									if( !old ){
										arg = new Argument();
									}
									else{
										arg = fT.getArgumentOfIndex(cnt);
									}
									String colName = v.toString();
									if( aliases.containsValue(v.toString()) ){
										for( String originalName : aliases.keySet() ){
											if( aliases.get(originalName).equals(colName) ){
												colName = originalName;
												break;
											}
										}
									}
									Field field = new Field(tableName, colName);
									arg.addFillingField(field);
									
									if( !old ){ fT.addArgument(arg); }	
								}
								if( !old ){ result.add(fT); }
							}
						}
					}
				} // END--FOR		
			}
		}	
		return result;
	}
		
	public static void main(String[] args){
		try {
			JoinableColumnsFinder a = new JoinableColumnsFinder("src/main/resources/npd-v2-ql_a.obda");
			List<FunctionTemplate> fTemplates = a.findFunctionTemplates();
			
			// Remove templates picking from a single place
			List<FunctionTemplate> output = new ArrayList<FunctionTemplate>();
			
			for( FunctionTemplate t : fTemplates ){
				if( t.getArity() > 0 ){
					output.add(t);
				}
			}
			
			System.out.println(output);
			
//			Map<String, List<FunctionTemplate>> mPredStringToFunctTemplates = new HashMap<>();
//			
//			for( FunctionTemplate t : fTemplates ){
//				if( mPredStringToFunctTemplates.containsKey(t.getTemplateString()) ){
//					mPredStringToFunctTemplates.get(t.getTemplateString()).add(t);
//				}
//				else{
//					mPredStringToFunctTemplates.put(t.getTemplateString(), new LinkedList<FunctionTemplate>());
//					mPredStringToFunctTemplates.get(t.getTemplateString()).add(t);
//				}
//			}
//			// Remove templates picking from a single place
//			Map<String, List<FunctionTemplate>> mPredStringToMultipleFunctTemplates = new HashMap<>();
//			
//			for( String key : mPredStringToFunctTemplates.keySet() ){
//				if( mPredStringToFunctTemplates.get(key).size() > 1 ){
//					mPredStringToMultipleFunctTemplates.put(key, mPredStringToFunctTemplates.get(key));
//				}
//			}
//			System.out.println(mPredStringToMultipleFunctTemplates);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
};

class PredStringToFunctTemplMap{
	String predicateString;
	Set<FunctionTemplate> functionTemplates;
	
	
}







