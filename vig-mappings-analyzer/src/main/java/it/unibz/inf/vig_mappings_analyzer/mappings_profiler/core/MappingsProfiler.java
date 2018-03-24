package it.unibz.inf.vig_mappings_analyzer.mappings_profiler.core;

import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.sql.DBMetadata;
import it.unibz.inf.vig_mappings_analyzer.core.JoinableColumnsFinder;
import it.unibz.inf.vig_mappings_analyzer.datatypes.Argument;
import it.unibz.inf.vig_mappings_analyzer.datatypes.Field;
import it.unibz.inf.vig_mappings_analyzer.datatypes.FunctionTemplate;
import it.unibz.inf.vig_mappings_analyzer.datatypes.SPJQuery;
import it.unibz.inf.vig_mappings_analyzer.obda.OBDAModelFactory;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * FIXME Currently broken
 * @author Davide Lanti
 *
 */
public class MappingsProfiler {

    // Internal State
    private DBMetadata meta;
    private OBDAModel obdaModel;

    private static Logger logger = Logger.getLogger(JoinableColumnsFinder.class.getCanonicalName());

    private MappingsProfiler(OBDAModel obdaModel, DBMetadata meta) {
	this.obdaModel = obdaModel;
	try {
	    meta = OBDAModelFactory.makeDBMetadata(obdaModel);
	} catch (SQLException e) {
	    e.printStackTrace();
	    System.err.println("Empty medatada");
	    System.exit(1);
	}
	this.meta = meta;
    }
    
    public static MappingsProfiler makeInstance(OBDAModel obdaModel, DBMetadata meta) {
	return new MappingsProfiler(obdaModel, meta);
    }

    public List<MappingAssertion> constructMappingAssertions() throws Exception {

	List<MappingAssertion> result = new ArrayList<>();

	for( URI uri : obdaModel.getMappings().keySet() ){
	    logger.info(uri);
	    for( OBDAMappingAxiom obdaMappingAxiom : obdaModel.getMappings(uri) ){
		
		List<Function> targetQuery = obdaMappingAxiom.getTargetQuery();
		OBDASQLQuery sourceQuery = (OBDASQLQuery) obdaMappingAxiom.getSourceQuery();		    
		SPJQuery spj = SPJQuery.makeInstanceFromSQL(sourceQuery.toString(), meta);
		
		// Each element in targetQuery.getBody() defines a triple pattern
		// I SPLIT a mapping assertion defining multiple triple patterns, and create a <MappingAssertion> instance 
		// for each of them
		for( int i = 0; i < targetQuery.size(); ++i ){
		    
		    Function atom = targetQuery.get(i);
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
			    
			    List<Variable> varList = new ArrayList<>(toVariables(f.getTerms()));
			    for( int cnt = 0; cnt < varList.size(); ++cnt ){
				Variable v = varList.get(cnt);
				addFieldToFunctionTemplateFromVariable(v, fT, spj);
			    }
			}
			else if( t instanceof Variable ){
			    // It is some shit like {pipName}
			    Variable v = (Variable) t;
			    fT = new FunctionTemplate(v); 
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
			mABuilder.rhs(FunctionTemplate.makeISA()); 
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

    private List<Variable> toVariables(List<Term> terms) throws WrongCastException {
	
	List<Variable> result = new ArrayList<>();
	for( Term t : terms ){
	    if( t instanceof Variable ){
		result.add((Variable)t);
	    }
	    else{
		throw new WrongCastException("Expecting a variable, was " + t.toString() + " member of " + t.getClass());
	    }
	}
	
	return result;
    }

    private void addFieldToFunctionTemplateFromVariable(
	    Variable v,
	    FunctionTemplate fT, 
	    SPJQuery spj) {
	
	fT = new FunctionTemplate(v); 
	
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
	fT.addArgument(arg); 
    }

    public static void main(String[] args){
	try {
	    
	    OBDAModel model = OBDAModelFactory.makeOBDAModel("src/main/resources/npd-v2-ql_a.obda");
	    DBMetadata meta = OBDAModelFactory.makeDBMetadata(model);
	    MappingsProfiler a = MappingsProfiler.makeInstance(model, meta);

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





