package it.unibz.inf.vig_mappings_analyzer.core;

import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.parser.SQLQueryDeepParser;
import it.unibz.inf.ontop.sql.DBMetadata;
import it.unibz.inf.ontop.sql.QuotedID;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.sql.api.ParsedSQLQuery;

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
import it.unibz.inf.vig_mappings_analyzer.obda.OBDAModelFactory;
import net.sf.jsqlparser.expression.Expression;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class JoinableColumnsFinder extends OntopConnection {

    private JoinableColumnsFinder( OBDAModel model, DBMetadata meta) {
	super(model, meta);
    }
    
    public static JoinableColumnsFinder makeInstance( OBDAModel model, DBMetadata meta ){
	return new JoinableColumnsFinder(model, meta);
    }
    
    public List<FunctionTemplate> findFunctionTemplates() throws Exception {

	List<FunctionTemplate> result = new ArrayList<FunctionTemplate>();

	// Y shall do something similar for each dataproperty, and object property
	for( URI uri : obdaModel.getMappings().keySet() ){
	    logger.info(uri);
	    for( OBDAMappingAxiom a : obdaModel.getMappings(uri) ){

		List<Function> targetQuery = a.getTargetQuery();

		for( int i = 0; i < targetQuery.size(); ++i ){

		    Function atom = targetQuery.get(i); 
		    OBDASQLQuery sourceQuery = (OBDASQLQuery) a.getSourceQuery();

		    // Construct the SQL query tree from the source query
		    String viewString = sourceQuery.toString();
		    logger.info(viewString);
		    ParsedSQLQuery queryParsed = SQLQueryDeepParser.parse(this.meta, viewString);
		    if( queryParsed.getTables().size() != 1 ){ /*++skipped;*/ continue; } // Skip joins for the moment

		    //					++unskipped;

		    // alias -> originalName
		    Map<RelationID, RelationID> tableMap = queryParsed.getTables();
		    RelationID table = new ArrayList<>(tableMap.values()).get(0);
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
				Map<QuotedID, Expression> aliases = queryParsed.getAliasMap();	

				// Retrieve list of variables
				List<Variable> varlist = new ArrayList<Variable>();
				
				for( Term term : f.getTerms() ){
				    if( term instanceof Variable ){
					Variable v = (Variable)term;
					varlist.add(v);
				    }
				}
				
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
					for( QuotedID originalName : aliases.keySet() ){
					    if( aliases.get(originalName).toString().equals(colName) ){
						colName = originalName.toString();
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
    
    public static class TestJoinableColumnsFinder{

	private static final String OUTFILE="src/main/resources/JoinableColumnFinderOutTest.txt";

	@Test
	public void testNPD(){
	    try{
		OBDAModel model = OBDAModelFactory.getSingletonOBDAModel("src/main/resources/npd-v2-ql_a.obda");
		SQLQueryParser parser = OBDAModelFactory.makeSQLParser(model);

		JoinableColumnsFinder a = new JoinableColumnsFinder(model, parser);
		List<FunctionTemplate> fTemplates = a.findFunctionTemplates();

		// Remove templates picking from a single place
		List<FunctionTemplate> output = new ArrayList<FunctionTemplate>();

		for( FunctionTemplate t : fTemplates ){
		    if( t.getArity() > 0 ){
			output.add(t);
		    }
		}
		StringBuilder testString = new StringBuilder();
		try(BufferedReader in = new BufferedReader(
			new FileReader(OUTFILE))){
		    String s;
		    while ((s = in.readLine()) != null){
			testString.append(s);
			testString.append("\n");
		    }
		}
		org.junit.Assert.assertEquals(testString.toString(), output.toString()+"\n");
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    };
	
	public static void main(String[] args){
	
	try {
	    OBDAModel model = OBDAModelFactory.getSingletonOBDAModel("src/main/resources/npd-v2-ql_a.obda");
	    DBMetadata meta = OBDAModelFactory.makeDBMetadata(model);

	    JoinableColumnsFinder a = new JoinableColumnsFinder(model, meta);
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







