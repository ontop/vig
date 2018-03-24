package it.unibz.inf.data_pumper.utils.traversers;

import it.unibz.inf.data_pumper.utils.traversers.visitors.Visitor;

import java.util.List;

public class ListTraverser implements Traverser {

    List<? extends Node> nodes;
    
    public ListTraverser(List<? extends Node> nodes) {
	this.nodes = nodes;
    }

    @Override
    public void traverse(Visitor v) {
	for( Node node : nodes ){
	    v.visit(node);
	}
    }
}
