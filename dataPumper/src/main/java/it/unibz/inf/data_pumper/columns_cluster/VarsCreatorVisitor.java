package it.unibz.inf.data_pumper.columns_cluster;

import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.utils.Pair;
import it.unibz.inf.data_pumper.utils.traversers.Node;
import it.unibz.inf.data_pumper.utils.traversers.visitors.Visitor;

import java.util.List;

import abstract_constraint_program.ACPLongVar;
import abstract_constraint_program.ACPOperator;
import abstract_constraint_program.AbstractConstraintProgram;


/**
 * 
 * This class creates the variables for the constraint program. For each interval I [min,..,max], (remember that either min or max uniquely determine I)
 * for each column X in the cluster, two new variables X_min, X_max are created having domain [min...max].
 * If X is multi-interval, and I is an interval of X, then the constraints:
 * 
 * <ul>
 *    <li>X_min = I.min</li>
 *    <li>X_max = I.max</li>
 * </ul>
 * 
 * are added. If I is not in X, then I should be "empty" in X and this is reflected
 * by adding a constraint
 * 
 * <ul>
 *    <li>X_min = X_max</li>
 * </ul>
 * 
 * If X is single-interval, then a constraint for specifying that X_min and X_max represent an
 * interval are added:
 * 
 * <ul>
 *    <li>X_max >= X_min</li>
 * </li>
 * 
 * @author Davide Lanti
 *
 * @param <VarType> Type of the variable in the constraint program
 * @param <ConstrType> Type of the constraint in the constraint program
 */
class VarsCreatorVisitor<VarType, ConstrType> implements Visitor {

    private AbstractConstraintProgram<VarType,ConstrType> program;
    private CPIntervalKeyToBoundariesVariablesMapper<VarType> mIntervalsToBoundVars;
    private List<SimpleIntervalKey> intervalKeys;

    public VarsCreatorVisitor(AbstractConstraintProgram<VarType, ConstrType> cProg, CPIntervalKeyToBoundariesVariablesMapper<VarType> mIntervalsToBoundVars, List<SimpleIntervalKey> intervalKeys) {
	this.program = cProg;
	this.mIntervalsToBoundVars = mIntervalsToBoundVars;
	this.intervalKeys = intervalKeys;
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

    /**
     * Make the boundaries variables, then 
     * 
     * E.g., 
     * // Force the fact that they are intervals

		Constraint wInt1 = IntConstraintFactory.arithm(w_2, ">=", w_1);
		Constraint wInt2 = IntConstraintFactory.arithm(w_4, ">=", w_3);
		Constraint wInt3 = IntConstraintFactory.arithm(w_6, ">=", w_5);
     * @param cPIC
     */
    private void visitSingle(ColumnPumperInCluster<?> cPIC){
	for( SimpleIntervalKey related : this.intervalKeys ){

	    // For each interval, add 2 variables
	    CPIntervalKey cPInt = CPIntervalKey.promote(related, cPIC.cP.getQualifiedName().toString());
	    String cPIntKey = cPInt.toString();
	    ACPLongVar<VarType> lwBoundVar = program.addLongVar(cPIntKey+"1", related.getLwBound(), related.getUpBound());
	    ACPLongVar<VarType> upBoundVar = program.addLongVar(cPIntKey+"2", related.getLwBound(), related.getUpBound());

	    long lwBound = related.getLwBound();
	    long upBound = related.getUpBound();
	    
	    assert (lwBound >= 0 && upBound >= 0) : "Negative encoding for column CPIntervalKey "+ cPInt; 
	    
	    this.mIntervalsToBoundVars.addVarsForInterval(cPInt, new Pair<>(lwBoundVar, upBoundVar));

	    // Constraint: 
	    // Force the fact that they are intervals

	    // W
	    program.addLongConstraint(upBoundVar, ACPOperator.GEQ, lwBoundVar);
	}
	this.program.post();
    }

    /**
     * 
     * Make the boundaries variables, then
     * 
     * // Set known intervals
		// E.g., 
		Constraint cX12 = IntConstraintFactory.arithm(x_1, "=", x_2); ( Non-present interval )
		Constraint cX3 = IntConstraintFactory.arithm(x_3, "=", 3);
		Constraint cX4 = IntConstraintFactory.arithm(x_4, "=", 8);
		Constraint cX5 = IntConstraintFactory.arithm(x_5, "=", 9);
		Constraint cX6 = IntConstraintFactory.arithm(x_6, "=", 12);
	      @param cPIC
     **/
    private void visitMulti(ColumnPumperInCluster<?> cPIC){
	for( SimpleIntervalKey related : this.intervalKeys ){

	    // For each interval, add 2 variables
	    CPIntervalKey cPInt = CPIntervalKey.promote(related, cPIC.cP.getQualifiedName().toString());
	    String cPIntKey = cPInt.toString();
	    
	    long lwBound = related.getLwBound();
	    long upBound = related.getUpBound();
	    
	    assert (lwBound >= 0 && upBound >= 0) : "Negative encoding for column CPIntervalKey "+ cPInt; 
	    
	    ACPLongVar<VarType> lwBoundVar = program.addLongVar(cPIntKey+"1", lwBound, upBound);
	    ACPLongVar<VarType> upBoundVar = program.addLongVar(cPIntKey+"2", lwBound, upBound);

	    this.mIntervalsToBoundVars.addVarsForInterval(cPInt, new Pair<>(lwBoundVar, upBoundVar));

	    boolean in = false;
	    for( Interval<?> i : cPIC.cP.getIntervals() ){
		if( i.getKey().equals(related.getKey() ) ){
		    in = true;
		    break;
		}
	    }
	    if( in ){			

		// Constraint cX3 = IntConstraintFactory.arithm(x_3, "=", 3);
		this.program.addLongConstraint(lwBoundVar, ACPOperator.EQUALS, lwBound);
		this.program.addLongConstraint(upBoundVar, ACPOperator.EQUALS, upBound);
	    }
	    else{
		// Set equality
		// Constraint cX12 = IntConstraintFactory.arithm(x_1, "=", x_2); ( Non-present interval )
		this.program.addLongConstraint(lwBoundVar, ACPOperator.EQUALS, upBoundVar);
	    }
	}
	this.program.post();
    }
};
