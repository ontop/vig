package examples;

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


import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.JDBCConnectionManager;
import it.unibz.krdb.sql.api.VisitedQuery;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.io.IOCase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class PlayWithMappings {
	final String owlfile = "resources/npd-v2-ql_a.owl";
	final String obdafile = "resources/npd-v2-ql_a.obda";
	private SQLQueryTranslator translator;
	
	public void play() throws Exception {
		/*
		 * Load the ontology from an external .owl file.
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		
		/*
		 * Load the OBDA model from an external .obda file
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
		
		Collection<OBDADataSource> sources = obdaModel.getSources();
		OBDADataSource source = sources.iterator().next();
		
		
		String url = source.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = source.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = source.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = source.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e1) {
			// Does nothing because the SQLException handles this problem also.
		}
		Connection localConnection = DriverManager.getConnection(url, username, password);
		
		// The SQL Translator
		translator = new SQLQueryTranslator(new DBMetadata(localConnection.getMetaData()));
		
		// Y shall do something similar foreach dataproperty, and object property
		for( URI uri: obdaModel.getMappings().keySet() ){
			System.out.println(uri);
			int greaterTwoCnt = 0;
			int twoCnt = 0;
			int cntNaryRules = 0;
			int cntNoJoinsNoWhere = 0; // 430 over 1000 and something
			for( OBDAMappingAxiom a : obdaModel.getMappings(uri) ){
				
				CQIE targetQuery = (CQIE) a.getTargetQuery();
				
				if( targetQuery.getBody().size() > 1 ){
					++cntNaryRules;
				} // ZERO. So, every mapping assertion models EXACTLY one thing
				
				boolean isRoleOrData = false;
				// For each atom in the body, get the arity
				for( Function f : targetQuery.getBody() ){
					if( f.getArity() == 2 ){ // role or data property
						++twoCnt;
						isRoleOrData = true;
					}
					if( f.getArity() > 2 ){
						++greaterTwoCnt;
					}
				}
				
				OBDASQLQuery sourceQuery = (OBDASQLQuery) a.getSourceQuery();
				
				// Construct the SQL query tree from the source query
				System.out.println(sourceQuery.toString());
				VisitedQuery queryParsed = translator.constructParser(sourceQuery.toString());
				
				if( isRoleOrData && queryParsed.getJoinCondition().size() == 0 ){ // No joins
					if( queryParsed.getSelection() == null ){
						++cntNoJoinsNoWhere; // 430
						// If the tableSet is greater than one, then there is some problem
						if( queryParsed.getTableSet().size() > 1 ){
							System.err.println("Table Set for the query > 1");
							System.exit(1);
						}
						System.out.println(queryParsed.getProjection().getColumnNameList());  // TODO Now get the projection list, tipo ( so that I can identify the tuple )
					}
				}
				
				
				
				// Now, I want to express something like 
				//
				// "If it is of the form SELECT a,b,c FROM wellbore"
				// WITHOUT THE WHERE for now. WITHOUT JOINS for now.
				// 
				// So that I can start implementing something as fast as I can ...
				
//				queryParsed.
			}
			System.out.println("greater than two: "+ greaterTwoCnt); // 1028
			System.out.println("two: "+twoCnt); // 0 ;)
			System.out.println("cntNaryRules "+cntNaryRules);
			System.out.println("cntNoJoinsNoWhere " + cntNoJoinsNoWhere);
		}	
	}
	
	public static void main(String[] args){
		PlayWithMappings a = new PlayWithMappings();
		
		try {
			a.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
