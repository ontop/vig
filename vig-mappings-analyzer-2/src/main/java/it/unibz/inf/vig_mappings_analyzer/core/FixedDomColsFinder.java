package it.unibz.inf.vig_mappings_analyzer.core;

import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.parser.SQLQueryDeepParser;
import it.unibz.inf.ontop.sql.DBMetadata;
import it.unibz.inf.ontop.sql.api.ParsedSQLQuery;
import it.unibz.inf.vig_mappings_analyzer.core.utils.QualifiedName;
import it.unibz.inf.vig_mappings_analyzer.obda.OBDAModelFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FixedDomColsFinder extends OntopConnection {
    
    private FixedDomColsFinder(OBDAModel obdaModel, DBMetadata meta) {
	super(obdaModel, meta);
    }
    
    public static FixedDomColsFinder makeInstance(OBDAModel model, DBMetadata meta){
	return new FixedDomColsFinder(model, meta);
    }

    public Set<QualifiedName> findFixedDomainCols() throws Exception {

	Set<QualifiedName> result = new HashSet<>();

	// Y shall do something similar for each dataproperty, and object property
	for( URI uri : obdaModel.getMappings().keySet() ){
	    logger.info(uri);
	    for( OBDAMappingAxiom a : obdaModel.getMappings(uri) ){

		List<Function> targetQuery =  a.getTargetQuery();

		for( int i = 0; i < targetQuery.size(); ++i ){

		    OBDASQLQuery sourceQuery = (OBDASQLQuery) a.getSourceQuery();

		    // Construct the SQL query tree from the source query
		    String viewString = sourceQuery.toString();
		    logger.info(viewString);
		    ParsedSQLQuery queryParsed = SQLQueryDeepParser.parse(meta, viewString); 
		    if( queryParsed.getTables().size() != 1 ){ /*++skipped;*/ continue; } // Support for single table only
		    
		    if( queryParsed.getWhereClause() != null ){
			// Format: colName = 'JACK-UP 3 LEGS'
			
			String raw = queryParsed.getWhereClause().toString();
			List<String> splits = Arrays.asList( raw.split("\\s*=\\s*") );
			if( splits.size() == 2 ){
			    String value = splits.get(1);
			    if( value.startsWith("'") ){ // String only, for the moment
				QualifiedName toAdd = new QualifiedName( queryParsed.getTables().get(0).toString(), splits.get(0) );
				result.add(toAdd);
			    }
			}
		    }
		}
	    }
	}
	return result;
    }
    
    public static void main(String[] args){
	
	try {
	    OBDAModel model = OBDAModelFactory.getSingletonOBDAModel("src/main/resources/test/npd-v2-ql_a.obda");
	    DBMetadata meta = OBDAModelFactory.makeDBMetadata(model);
	    
	    FixedDomColsFinder instance = FixedDomColsFinder.makeInstance(model, meta);
	    System.out.println( instance.findFixedDomainCols() );
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
};


