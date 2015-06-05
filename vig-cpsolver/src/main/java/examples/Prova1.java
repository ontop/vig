package examples;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

public class Prova1 {

    public void method(){
	
	// x + y < 5, where the x ∈ [[0, 5]] and y ∈ [[0, 5]]
	
	// 1. Create a Solver
	Solver solver = new Solver("my first problem");
	
	// 2. Create variables through the variable factory
	IntVar x = VariableFactory.bounded("X", 0, 5, solver);
	IntVar y = VariableFactory.bounded("Y", 0, 5, solver);
	
	// 3. Create and post constraints by using constraint factories
	solver.post(IntConstraintFactory.arithm(x, "+", y, "<", 5));
	
	// 4. Define the search strategy
	solver.set(IntStrategyFactory.lexico_LB(x, y));
	
	// 5. Launch the resolution process
	solver.findSolution();
		
	//6. Print search statistics
	Chatterbox.printStatistics(solver);
	
    }

    public static void main(String[] args){
	Prova1 p = new Prova1();
	
	p.method();
    }
    
}
