package it.unibz.inf.data_pumper.utils.traversers.visitors;

import it.unibz.inf.data_pumper.utils.traversers.Node;

import java.util.ArrayList;
import java.util.List;

public class CollectVisitedVisitor implements VisitorWithResult<List<? extends Node>> {

    List<Node> result = new ArrayList<>();
    
    @Override
    public void visit(Node node) {
	result.add(node);
    }

    @Override
    public List<? extends Node> result() {
	return result;
    }
    
}
