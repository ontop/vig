package vig_test_unit.exceptions;

public class AssertionFailedException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -1811448991252628905L;

    public AssertionFailedException(String msg){
	super(msg);
    }
    
}
