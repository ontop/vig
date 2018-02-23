package it.unibz.inf.data_pumper.columns_cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.utils.Pair;
import it.unibz.inf.data_pumper.utils.traversers.Node;
import it.unibz.inf.data_pumper.utils.traversers.visitors.Visitor;
import it.unibz.inf.vig_mappings_analyzer.core.utils.QualifiedName;
import abstract_constraint_program.ACPLongVar;
import abstract_constraint_program.ACPOperator;
import abstract_constraint_program.AbstractConstraintProgram;

class ForeignKeysVarsSetterVisitor<VarType, ConstrType> implements Visitor {

    private AbstractConstraintProgram<VarType,ConstrType> program;
    private CPIntervalKeyToBoundariesVariablesMapper<VarType> mapper; 
    
    ForeignKeysVarsSetterVisitor(
	    AbstractConstraintProgram<VarType, ConstrType> constraintProgram,
	    CPIntervalKeyToBoundariesVariablesMapper<VarType> mIntervalsToBoundariesVars) {
	this.program = constraintProgram;
	this.mapper = mIntervalsToBoundariesVars;
    }

    /**
     * // W -> X
	Constraint w1x1 = IntConstraintFactory.arithm(w_1, ">=", x_1);
	Constraint w2x2 = IntConstraintFactory.arithm(w_2, "<=", x_2);
     */
    @Override
    public void visit(Node node) {
	
	ColumnPumperInCluster<?> cPIC = (ColumnPumperInCluster<?>) node;
	
	addFkConstraints(cPIC);
	
	this.program.post();
		
	if( cPIC.isSingleInterval() ){
	    addCoefficientsConstraints(cPIC);
	}
	
	this.program.post();
    }

    /**
     * x_2 - x1 + x_4 - x3 + x_6 - x_5 = cPIC.cP.numFreshsToInsert 
     * @param cPIC
     */
    private void addCoefficientsConstraints(ColumnPumperInCluster<?> cPIC) {
	
	
	Set<CPIntervalKey> thisKeys = this.mapper.getKeySetForCP(cPIC.cP);
	
	long nFreshs = cPIC.cP.getNumFreshsToInsert();
		
	// Prepare coefficients list
	List<Long> coeffs = new ArrayList<>();
	for( int i = 0; i < thisKeys.size() * 2; ++i ){ 
	    if( i % 2 == 0 ){
		coeffs.add((long)1);
	    }
	    else{
		coeffs.add((long)-1);
	    }
	}
	
	// Prepare variables' list
	List<ACPLongVar<VarType>> vars = new ArrayList<>();
	for( CPIntervalKey key : thisKeys ){
	    Pair<ACPLongVar<VarType>, ACPLongVar<VarType>> mlwToUpBound = this.mapper.getVarsForKey(key);
	    
	    vars.add(mlwToUpBound.second); // upbound
	    vars.add(mlwToUpBound.first);  // lwbound
	}
		
	program.addScalarLongConstraint(coeffs, vars, nFreshs);
    }

    private void addFkConstraints(ColumnPumperInCluster<?> cPIC) {
	Set<CPIntervalKey> thisKeys = this.mapper.getKeySetForCP(cPIC.cP);
	for( QualifiedName referredName : cPIC.cP.referencesTo() ){
	    ColumnPumper<? extends Object> referred = DBMSConnection.getInstance().getSchema(referredName.getTableName()).getColumn(referredName.getColName());
	    // Map corresponding intervals.
	    Set<CPIntervalKey> referredKeys = this.mapper.getKeySetForCP(referred);
	    for( CPIntervalKey key : thisKeys ){
		addConstraintToProgram(key, referredKeys);
	    }
	}
    }
    
    /** Constraint w1x1 = IntConstraintFactory.arithm(w_1, ">=", x_1);
        Constraint w2x2 = IntConstraintFactory.arithm(w_2, "<=", x_2);
     **/
    private void addConstraintToProgram(final CPIntervalKey key,
	    final Set<CPIntervalKey> referredKeys) {

	Pair<ACPLongVar<VarType>, ACPLongVar<VarType>> mlwToUpBoundVar = this.mapper.getVarsForKey(key);
	ACPLongVar<VarType> lwBoundVar = mlwToUpBoundVar.first;
	ACPLongVar<VarType> upBoundVar = mlwToUpBoundVar.second;
	
	for( CPIntervalKey refKey : referredKeys ){
	    if( refKey.getKey().equals(key.getKey()) ){
		Pair<ACPLongVar<VarType>, ACPLongVar<VarType>> mlwToUpBoundRefVar = this.mapper.getVarsForKey(refKey);
		ACPLongVar<VarType> lwBoundRefVar = mlwToUpBoundRefVar.first;
		ACPLongVar<VarType> upBoundRefVar = mlwToUpBoundRefVar.second;
		
		program.addLongConstraint(lwBoundVar, ACPOperator.GEQ, lwBoundRefVar);
		program.addLongConstraint(upBoundVar, ACPOperator.LEQ, upBoundRefVar);
	    }
	}
    }
}
