package abstract_constraint_program;

import org.chocosolver.solver.variables.IntVar;

public final class ChocoACPLongVar extends ACPLongVar<IntVar> {

    public ChocoACPLongVar(IntVar wrapped) {
	super(wrapped);
    }
    
    public final long getValue(){
	long result = this.wrapped.getValue();
	return result;
    }

    @Override
    public String getName() {
	return this.wrapped.getName();
    }
}
