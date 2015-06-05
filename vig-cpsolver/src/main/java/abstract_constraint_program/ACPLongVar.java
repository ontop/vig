package abstract_constraint_program;

public class ACPLongVar<T> {
    
    private T wrapped;
    
    public ACPLongVar(T wrapped){
	this.wrapped = wrapped;
    }
    
    
    public T getWrapped(){
	return this.wrapped;
    }
}
