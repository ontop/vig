package it.unibz.inf.data_pumper.utils.traversers.visitors;

import it.unibz.inf.data_pumper.utils.traversers.ConditionChecker;
import it.unibz.inf.data_pumper.utils.traversers.Node;

public class BooleanVisitor implements VisitorWithResult<Boolean> {

    private ConditionChecker condChecker;
    
    // State
    private boolean result;
    
    public BooleanVisitor(ConditionChecker condChecker){
	this.condChecker = condChecker;
	result = true;
    }
    
    @Override
    public void visit(Node node) {
	if( result && !condChecker.checkCondition() ){
	    result = false;
	}
    }

    @Override
    public Boolean result() {
	return result;
    }
}
