package it.unibz.inf.data_pumper.utils.traversers;

import it.unibz.inf.data_pumper.utils.traversers.visitors.Visitor;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ReachConnectedTraverser implements Traverser {
    
    private final List<? extends Node> nodes; 
    
    public ReachConnectedTraverser(List<? extends Node> nodes){
	this.nodes = nodes;
    }
    
    public void traverse(Visitor visitor) {
	Queue<Node> toVisit = new LinkedList<>(); 
	
	toVisit.addAll(this.nodes);
	
	while( !toVisit.isEmpty() ){
	    Node n = toVisit.poll();
	    if( n.isVisited() ) continue;
	    n.markVisited();
	    visitor.visit(n);
	    toVisit.addAll(n.getInNodes());
	    toVisit.addAll(n.getOutNodes());
	}
    }
}
