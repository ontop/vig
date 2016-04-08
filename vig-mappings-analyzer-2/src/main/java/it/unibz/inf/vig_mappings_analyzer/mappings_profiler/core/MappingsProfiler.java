package it.unibz.inf.vig_mappings_analyzer.mappings_profiler.core;

import it.unibz.inf.vig_mappings_analyzer.core.JoinableColumnsFinder;
import it.unibz.inf.vig_mappings_analyzer.datatypes.Argument;
import it.unibz.inf.vig_mappings_analyzer.datatypes.Field;
import it.unibz.inf.vig_mappings_analyzer.datatypes.FunctionTemplate;
import it.unibz.inf.vig_mappings_analyzer.datatypes.SPJQuery;
import it.unibz.inf.vig_mappings_analyzer.obda.OBDAModelFactory;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.parser.SQLQueryParser;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class MappingsProfiler {

    // Internal State
    private SQLQueryParser sqlParser;
    private OBDAModel obdaModel;

    private static Logger logger = Logger.getLogger(JoinableColumnsFinder.class.getCanonicalName());

    private MappingsProfiler(OBDAModel obdaModel, SQLQueryParser sqlParser) {
	this.obdaModel = obdaModel;
	this.sqlParser = sqlParser;
    }
    
    public static MappingsProfiler makeInstance(OBDAModel obdaModel, SQLQueryParser sqlParser) {
	return new MappingsProfiler(obdaModel, sqlParser);
    }

    public List<MappingAssertion> constructMappingAssertions() throws Exception {

	List<MappingAssertion> result = new ArrayList<>();

	for( URI uri : obdaModel.getMappings().keySet() ){
	    logger.info(uri);
	    for( OBDAMappingAxiom obdaMappingAxiom : obdaModel.getMappings(uri) ){

		CQIE targetQuery = (CQIE) obdaMappingAxiom.getTargetQuery();
		OBDASQLQuery sourceQuery = (OBDASQLQuery) obdaMappingAxiom.getSourceQuery();		    
		SPJQuery spj = SPJQuery.makeInstanceFromSQL(sourceQuery.toString(), sqlParser);
		
		// Each element in targetQuery.getBody() defines a triple pattern
		// I SPLIT a mapping assertion defining multiple triple patterns, and create a <MappingAssertion> instance 
		// for each of them
		for( int i = 0; i < targetQuery.getBody().size(); ++i ){
		    
		    Function atom = targetQuery.getBody().get(i);
		    MappingAssertion.Builder mABuilder = new MappingAssertion.Builder();
		    
		    boolean isClass = atom.getTerms().size() == 1;
		    
		    // Get the terms in the atom, and build the related FunctionTemplate classes
		    for( int j = 0; j < 2; ++j ){
			
			if( isClass && j == 1 ) break; // A class, ODDLY, does not have the rhs in ontop
			
			Term t = atom.getTerm(j);
			
			FunctionTemplate fT = null;
			if( t instanceof Function ){
			    Function f = (Function) t;
			    fT = new FunctionTemplate(f);

			    // Retrieve list of variables
			    List<Variable> varList = new ArrayList<>(f.getVariables());
			    for( int cnt = 0; cnt < varList.size(); ++cnt ){
				Variable v = varList.get(cnt);
				addFieldToFunctionTemplateFromVariable(v, fT, spj);
			    }
			}
			else if( t instanceof Variable ){
			    // It is some shit like {pipName}
			    Variable v = (Variable) t;
//			    fT = new FunctionTemplate(v); TODO Uncomment
			    addFieldToFunctionTemplateFromVariable(v, fT, spj);
			}
			if( j == 0 ){
			    mABuilder.lhs(fT);
			}
			else{
			    mABuilder.rhs(fT);
			}
		    }
		    if( isClass ){
			mABuilder.predicate(UriPredicate.makeInstance("rdf:type"));
//			mABuilder.rhs(FunctionTemplate.makeISA()); TODO Uncomment
		    }
		    else{
			mABuilder.predicate(UriPredicate.makeInstance(atom.getFunctionSymbol().getName()));
			// rhs was set in the "else" above
		    }
		    mABuilder.spjQuery(spj);
		    result.add(mABuilder.build());
		} 		
	    }
	}	
	return result;
    }

    private void addFieldToFunctionTemplateFromVariable(
	    Variable v,
	    FunctionTemplate fT, 
	    SPJQuery spj) {
	
//	fT = new FunctionTemplate(v); TODO Uncomment
	
	String aliasColName = v.toString();
	
	// Apply renaming, if any
	aliasColName = spj.getSPJQueryHelper().getOriginalColName(aliasColName);
	String tableName = spj.getTables().get(0);
	if( spj.getTables().size() != 1 ){ // No joins
	    tableName = spj.getSPJQueryHelper().getOriginalTableNameForCol(aliasColName);
	}
	
	Field field = new Field(tableName, aliasColName);
	Argument arg = new Argument();
	arg.addFillingField(field);
//	fT.addArgument(arg); TODO Uncomment
    }

    public static void main(String[] args){
	try {
	    
	    OBDAModel model = OBDAModelFactory.makeOBDAModel("src/main/resources/npd-v2-ql_a.obda");
	    SQLQueryParser parser = OBDAModelFactory.makeSQLParser(model);
	    MappingsProfiler a = MappingsProfiler.makeInstance(model, parser);

	    List<MappingAssertion> mappingAssertions = a.constructMappingAssertions();
	    
	    System.out.println(mappingAssertions);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }	
    class PredStringToFunctTemplMap{
	String predicateString;
	Set<FunctionTemplate> functionTemplates;
    }
};





