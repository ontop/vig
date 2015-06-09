package abstract_constraint_program;

public abstract class ACPLongVar<T> {
    
    protected final T wrapped;
    
    public ACPLongVar(T wrapped){
	this.wrapped = wrapped;
    }
    
    
    public final T getWrapped(){
	return this.wrapped;
    }
    
    public abstract long getValue();
    
    @Override
    public String toString(){
	return wrapped.toString();
    }
}
