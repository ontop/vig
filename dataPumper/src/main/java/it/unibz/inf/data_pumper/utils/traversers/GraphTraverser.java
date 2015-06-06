package it.unibz.inf.data_pumper.utils.traversers;

import it.unibz.inf.data_pumper.utils.graphs.traversers.adjacence.TopDownOrBottomUpStrategy;
import it.unibz.inf.data_pumper.utils.graphs.traversers.selection.BreadthFirstOrDepthFirstDataStructure;
import it.unibz.inf.data_pumper.utils.traversers.visitors.Visitor;

import java.util.List;

public class GraphTraverser extends CleanableTraverser {

    private final List<? extends Node> nodes; 
    private final TopDownOrBottomUpStrategy traversalStrategy;
    private final BreadthFirstOrDepthFirstDataStructure<Node> toVisit;
        
    /**
     * 
     * @param nodes The nodes from where the traversal will start
     */
    GraphTraverser(List<? extends Node> nodes, TopDownOrBottomUpStrategy traversalStrategy, BreadthFirstOrDepthFirstDataStructure<Node> toVisit) {
	this.nodes = nodes;
	this.traversalStrategy = traversalStrategy;
	this.toVisit = toVisit;
    }
    
    @Override
    protected void traverseImpl(Visitor v) {	
	
	// Init queue
	toVisit.clear();
	for( Node node : this.nodes ){
	    toVisit.add(node);
	}

	while( !toVisit.isEmpty() ){
	    Node node = toVisit.getAndRemove();
	    if( node.isVisited(visitID()) ) continue;
	    node.markVisited(visitID());
	    v.visit(node);
	    for( Node in : traversalStrategy.getNodes(node) ){
		toVisit.add(in);
	    }
	}
    }
};

