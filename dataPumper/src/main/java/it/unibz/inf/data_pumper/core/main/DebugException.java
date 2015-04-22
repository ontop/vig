package it.unibz.inf.data_pumper.core.main;

public class DebugException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3656607310489186723L;
    
    
    public DebugException(String msg){
        super(msg);
    }
    
    public DebugException(){
        super();
    }
}
