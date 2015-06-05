package abstract_constraint_program;

import java.util.List;

public interface AbstractConstraintProgram<VarType, ConstraintType> {
    
    public ACPLongVar<VarType> addLongVar(String varName, long min, long max);
    public void addLongConstraint(ACPLongVar<VarType> var1, ACPOperator operator, ACPLongVar<VarType> var2);
    public void addScalarLongConstraint(List<Long> coeffs, List<ACPLongVar<VarType>> vars, Long value);
    public void addLongConstraint(ACPLongVar<VarType> lwBoundVar, ACPOperator equals, long value);
    
    /** Post the un-posted constraints **/
    public void post();
    
    /** Solve 
     * @return **/
    public boolean solve();
    public void prettyOut();

}
