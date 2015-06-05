package examples;


import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

public class FunctionalDependenciesEvaluation extends AbstractProblem {

    IntVar[] vars;

    public static void main(String[] args) {
	new FunctionalDependenciesEvaluation().execute(args);
    }

    @Override
    public void createSolver() {
	solver = new Solver("FunctionalDependenciesEvaluation");
    }

    @Override
    public void buildModel() {
	
	vars = new IntVar[24];

	// Intervals for each column
	
	IntVar z_1 = vars[0] = VariableFactory.bounded("Z_1", 1, 2, solver);
	IntVar z_2 = vars[1] = VariableFactory.bounded("Z_2", 1, 2, solver);
	IntVar z_3 = vars[2] = VariableFactory.bounded("Z_3", 3, 8, solver);
	IntVar z_4 = vars[3] = VariableFactory.bounded("Z_4", 3, 8, solver);
	IntVar z_5 = vars[4] = VariableFactory.bounded("Z_5", 9, 12, solver);
	IntVar z_6 = vars[5] = VariableFactory.bounded("Z_6", 9, 12, solver);
	
	IntVar w_1 = vars[6] = VariableFactory.bounded("W_1", 1, 2, solver);
	IntVar w_2 = vars[7] = VariableFactory.bounded("W_2", 1, 2, solver);	
	IntVar w_3 = vars[8] = VariableFactory.bounded("W_3", 3, 8, solver);
	IntVar w_4 = vars[9] = VariableFactory.bounded("W_4", 3, 8, solver);
	IntVar w_5 = vars[10] = VariableFactory.bounded("W_5", 9, 12, solver);
	IntVar w_6 = vars[11] = VariableFactory.bounded("W_6", 9, 12, solver);
		
	IntVar x_1 = vars[12] = VariableFactory.bounded("X_1", 1, 2, solver);
	IntVar x_2 = vars[13] = VariableFactory.bounded("X_2", 1, 2, solver);	
	IntVar x_3 = vars[14] = VariableFactory.bounded("X_3", 3, 8, solver);
	IntVar x_4 = vars[15] = VariableFactory.bounded("X_4", 3, 8, solver);
	IntVar x_5 = vars[16] = VariableFactory.bounded("X_5", 9, 12, solver);
	IntVar x_6 = vars[17] = VariableFactory.bounded("X_6", 9, 12, solver);
	
	IntVar y_1 = vars[18] = VariableFactory.bounded("Y_1", 1, 2, solver);
	IntVar y_2 = vars[19] = VariableFactory.bounded("Y_2", 1, 2, solver);	
	IntVar y_3 = vars[20] = VariableFactory.bounded("Y_3", 3, 8, solver);
	IntVar y_4 = vars[21] = VariableFactory.bounded("Y_4", 3, 8, solver);
	IntVar y_5 = vars[22] = VariableFactory.bounded("Y_5", 9, 12, solver);
	IntVar y_6 = vars[23] = VariableFactory.bounded("Y_6", 9, 12, solver);
	
	// Set known intervals
	// X
	Constraint cX12 = IntConstraintFactory.arithm(x_1, "=", x_2);
	Constraint cX3 = IntConstraintFactory.arithm(x_3, "=", 3);
	Constraint cX4 = IntConstraintFactory.arithm(x_4, "=", 8);
	Constraint cX5 = IntConstraintFactory.arithm(x_5, "=", 9);
	Constraint cX6 = IntConstraintFactory.arithm(x_6, "=", 12);
	
	// Y
	Constraint cY1 = IntConstraintFactory.arithm(y_1, "=", 1);
	Constraint cY2 = IntConstraintFactory.arithm(y_2, "=", 2);
	Constraint cY3 = IntConstraintFactory.arithm(y_3, "=", 4);
	Constraint cY4 = IntConstraintFactory.arithm(y_4, "=", 6);
	Constraint cY5 = IntConstraintFactory.arithm(y_5, "=", 9);
	Constraint cY6 = IntConstraintFactory.arithm(y_6, "=", 12);
	
	solver.post(cX12,cX3,cX4,cX5,cX5,cX6,cY1,cY2,cY3,cY4,cY5,cY6);

//	// Constraints for non-multi-interval nodes
	
	// Force the fact that they are intervals
	
	// W
	Constraint wInt1 = IntConstraintFactory.arithm(w_2, ">=", w_1);
	Constraint wInt2 = IntConstraintFactory.arithm(w_4, ">=", w_3);
	Constraint wInt3 = IntConstraintFactory.arithm(w_6, ">=", w_5);
	
	// Y
	Constraint yInt1 = IntConstraintFactory.arithm(y_2, ">=", y_1);
	Constraint yInt2 = IntConstraintFactory.arithm(y_4, ">=", y_3);
	Constraint yInt3 = IntConstraintFactory.arithm(y_6, ">=", y_5);

	// Z
	Constraint zInt1 = IntConstraintFactory.arithm(z_2, ">=", z_1);
	Constraint zInt2 = IntConstraintFactory.arithm(z_4, ">=", z_3);
	Constraint zInt3 = IntConstraintFactory.arithm(z_6, ">=", z_5);
	
	solver.post(wInt1,wInt2,wInt3,yInt1,yInt2,yInt3,zInt1,zInt2,zInt3);
	
	// Foreign Keys
	// W -> X
	Constraint w1x1 = IntConstraintFactory.arithm(w_1, ">=", x_1);
	Constraint w2x2 = IntConstraintFactory.arithm(w_2, "<=", x_2);
	Constraint w3x3 = IntConstraintFactory.arithm(w_3, ">=", x_3);
	Constraint w4x4 = IntConstraintFactory.arithm(w_4, "<=", x_4);
	Constraint w5x5 = IntConstraintFactory.arithm(w_5, ">=", x_5);
	Constraint w6x6 = IntConstraintFactory.arithm(w_6, "<=", x_6);
	
	solver.post(w1x1,w2x2,w3x3,w4x4,w5x5,w6x6);
	
	// W -> Y
	Constraint w1y1 = IntConstraintFactory.arithm(w_1, ">=", y_1);
	Constraint w2y2 = IntConstraintFactory.arithm(w_2, "<=", y_2);
	Constraint w3y3 = IntConstraintFactory.arithm(w_3, ">=", y_3);
	Constraint w4y4 = IntConstraintFactory.arithm(w_4, "<=", y_4);
	Constraint w5y5 = IntConstraintFactory.arithm(w_5, ">=", y_5);
	Constraint w6y6 = IntConstraintFactory.arithm(w_6, "<=", y_6);
	
	solver.post(w1y1,w2y2,w3y3,w4y4,w5y5,w6y6);

	// W -> Z
	Constraint w1z1 = IntConstraintFactory.arithm(w_1, ">=", z_1);
	Constraint w2z2 = IntConstraintFactory.arithm(w_2, "<=", z_2);
	Constraint w3z3 = IntConstraintFactory.arithm(w_3, ">=", z_3);
	Constraint w4z4 = IntConstraintFactory.arithm(w_4, "<=", z_4);
	Constraint w5z5 = IntConstraintFactory.arithm(w_5, ">=", z_5);
	Constraint w6z6 = IntConstraintFactory.arithm(w_6, "<=", z_6);
	
	solver.post(w1z1,w2z2,w3z3,w4z4,w5z5,w6z6);
	
	IntVar[] Ws = new IntVar[]{
                w_2, w_1, 
                w_4, w_3,
                w_6, w_5, 
	};
	
	IntVar[] Zs = new IntVar[]{
                z_2, z_1, 
                z_4, z_3,
                z_6, z_5, 
	};
	
	int[] COEFFS = new int[]{
                1, -1, 1, -1, 1, -1
        };
	
	Constraint lastW = IntConstraintFactory.scalar(Ws, COEFFS, VariableFactory.fixed(5, solver));
	Constraint lastZ = IntConstraintFactory.scalar(Zs, COEFFS, VariableFactory.fixed(5, solver));
	
	solver.post(lastW,lastZ); // W and Z are the only unknown columns
	
    }

    @Override
    public void configureSearch() {
//	solver.set(IntStrategyFactory.lexico_LB(vars));
    }

    @Override
    public void solve() {
	solver.findSolution();
    }

    @Override
    public void prettyOut() {
	System.out.println("FunctionalDependenciesEvaluation({})");
	StringBuilder st = new StringBuilder();
	st.append("\t");
	for (int i = 0; i < vars.length - 1; i++) {
	    st.append(String.format("%d ", vars[i].getValue()));
	    if (i % 6 == 5) {
		st.append("\n\t");
	    }
	}
	st.append(String.format("%d", vars[vars.length - 1].getValue()));
	System.out.println(st.toString());
    }
}
