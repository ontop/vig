package it.unibz.inf.data_pumper.utils.traversers;

import it.unibz.inf.data_pumper.utils.traversers.visitors.Visitor;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ReachConnectedTraverser extends CleanableTraverser {
    
    private List<? extends Node> nodes; 
    private List<Node> visitedNodes;
    
    public ReachConnectedTraverser(List<? extends Node> nodes){
	this.nodes = nodes;
	this.visitedNodes = new LinkedList<>();
    }

    @Override
    protected void traverseImpl(Visitor visitor) {
	
	Queue<Node> toVisit = new LinkedList<>(); 
	
	toVisit.addAll(this.nodes);
	
	while( !toVisit.isEmpty() ){
	    Node n = toVisit.poll();
	    if( n.isVisited(this.visitID()) ) continue;
	    n.markVisited(this.visitID());
	    visitor.visit(n);
	    toVisit.addAll(n.getInNodes());
	    toVisit.addAll(n.getOutNodes());
	    this.visitedNodes.add(n);
	}
    }
}
