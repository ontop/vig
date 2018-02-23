package it.unibz.inf.data_pumper.columns_cluster;

import it.unibz.inf.data_pumper.utils.traversers.Node;
import it.unibz.inf.data_pumper.utils.traversers.visitors.Visitor;

public enum EmptyVisitor implements Visitor {
    
    INSTANCE;
    
    @Override
    public void visit(Node node) {
	// Do nothing
    }

}
