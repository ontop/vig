package examples;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

public class FunctionalDependenciesEvaluation2 extends AbstractProblem {
//    Constraint subsetEq(SetVar[] SETS)
    


    IntVar[] vars;

    public static void main(String[] args) {
	new FunctionalDependenciesEvaluation2().execute(args);
    }

    @Override
    public void createSolver() {
	solver = new Solver("FunctionalDependenciesEvaluation2");
    }

    @Override
    public void buildModel() {
	
	vars = new IntVar[2];

	IntVar I1 = vars[0] = VariableFactory.bounded("I2", 1, 10, solver);
	IntVar I2 = vars[1] = VariableFactory.bounded("I1", 5, 10, solver);
	
	Constraint I1I2 = IntConstraintFactory.arithm(I2, "=", I1);
	
		
	solver.post(I1I2);
	
	
    }

    @Override
    public void configureSearch() {
	// TODO Auto-generated method stub
//	solver.set(IntStrategyFactory.lexico_LB(vars));
    }

    @Override
    public void solve() {
	// TODO Auto-generated method stub
	solver.findAllSolutions();
    }

    @Override
    public void prettyOut() {
	
	System.out.println("FunctionalDependenciesEvaluation2({})");
	StringBuilder st = new StringBuilder();
	st.append("\t");
	for (int i = 0; i < vars.length - 1; i++) {
	    st.append(String.format("[%d, %d] ", vars[i].getLB(), vars[i].getUB()));
	    if (i % 10 == 9) {
		st.append("\n\t");
	    }
	}
	st.append(String.format("[%d, %d]", vars[vars.length - 1].getLB(), vars[vars.length-1].getUB()));
	System.out.println(st.toString());
    }


}
