package it.unibz.inf.data_pumper.columns_cluster;

import java.util.Set;

import abstract_constraint_program.ACPLongVar;
import it.unibz.inf.data_pumper.utils.Pair;
import it.unibz.inf.data_pumper.utils.traversers.Node;
import it.unibz.inf.data_pumper.utils.traversers.visitors.Visitor;

class CPResultsToIntervalsVisitor<VarType> implements Visitor {

    private CPIntervalKeyToBoundariesVariablesMapper<VarType> mIntervalsToBoundVars;
    
    CPResultsToIntervalsVisitor(
	    CPIntervalKeyToBoundariesVariablesMapper<VarType> mIntervalsToBoundVars) {
	this.mIntervalsToBoundVars = mIntervalsToBoundVars;
    }
    
    @Override
    public void visit(Node node) {
	ColumnPumperInCluster<?> cPIC = (ColumnPumperInCluster<?>) node;
	
	if( cPIC.isSingleInterval() ){
	    visitSingle(cPIC);
	}
	else{
	    visitMulti(cPIC);
	}
    }

    private void visitMulti(ColumnPumperInCluster<?> cPIC) {
	// Do nothing
    }

    private void visitSingle(ColumnPumperInCluster<?> cPIC) {
	Set<CPIntervalKey> list = mIntervalsToBoundVars.getKeySetForCP(cPIC.cP);
	
	cPIC.cP.removeIntervalOfKey(cPIC.cP.getQualifiedName().toString());
	
	for( CPIntervalKey key : list ){
	    Pair<ACPLongVar<VarType>, ACPLongVar<VarType>> mMinToMaxEnc = mIntervalsToBoundVars.getVarsForKey(key);
	    ACPLongVar<VarType> minEnc = mMinToMaxEnc.first;
	    ACPLongVar<VarType> maxEnc = mMinToMaxEnc.second;
	    cPIC.cP.addInterval(key.getKey(), minEnc.getValue(), maxEnc.getValue());
	}
    }
    
}
