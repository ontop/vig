package it.unibz.inf.data_pumper.core.statistics.xml_model;

import it.unibz.inf.data_pumper.core.statistics.xml_model.exceptions.ErrorInModelException;

import java.util.HashSet;
import java.util.Set;


public class DatabaseModelUtils {
    public static void sanityCheck(DatabaseModel dM) throws ErrorInModelException{
	findProblemsDeclarationTablesOrCols(dM);
    }
    
    private static void findProblemsDeclarationTablesOrCols(DatabaseModel dM) throws ErrorInModelException{
	class LocalUtils{
	    void fixedEitherMinMaxOrDomain(ColumnModel cM) throws ErrorInModelException{
		if( cM.getDomain() != null ){
		    if( cM.getDomain().getMax() != null || cM.getDomain().getMin() != null ){
			if( cM.getDomain().getValues() != null ){
			    throw new ErrorInModelException("Column "+cM.getName()+ " has both min/max and explicit domain");
			}
		    }
		    if( cM.getDomain().getMax() != null && cM.getDomain().getMin() == null){
			throw new ErrorInModelException("Column "+cM.getName()+" has Max but not Min");
		    }
		    if( cM.getDomain().getMin() != null && cM.getDomain().getMax() == null){
			throw new ErrorInModelException("Column "+cM.getName()+" has Min but not Max");
		    }
		}
	    }
	}
	
	Set<String> declaredTableNames = new HashSet<>();
	Set<String> declaredCols = new HashSet<>();
	
	LocalUtils utils = new LocalUtils();
	
	for( TableModel tM : dM.getTables().getTablesList() ){
	    if( declaredTableNames.contains(tM.getName()) ){
		throw new ErrorInModelException("Table "+tM.getName()+" defined twice");
	    }
	    declaredTableNames.add(tM.getName());
	    
	    for( ColumnModel cM : tM.getColumns() ){
		String colFullName = tM.getName() + "." + cM.getName();
		if( declaredCols.contains(colFullName) ){
		    throw new ErrorInModelException("Column "+cM.getName()+" defined twice");
		}
		declaredCols.add(colFullName);
		
		utils.fixedEitherMinMaxOrDomain(cM);
	    }
	}
	
	for( SharingValues sV : dM.getSharing().getSharingList() ){
	    for( ClusterElement cE : sV.getInvolvedCols() ){
		if( !declaredTableNames.contains(cE.getTableName()) ){
		    throw new ErrorInModelException("Table "+cE.getTableName()+ " is undefined");
		}
		String colFullName = cE.getTableName() + "." + cE.getColName();
		if( !declaredCols.contains(colFullName) ){
		    throw new ErrorInModelException("Column " + colFullName + " is undefined");
		}
	    }
	}
    }
}
