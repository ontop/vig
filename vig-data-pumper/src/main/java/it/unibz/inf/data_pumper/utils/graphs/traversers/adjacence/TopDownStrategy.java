package it.unibz.inf.data_pumper.utils.graphs.traversers.adjacence;

import it.unibz.inf.data_pumper.utils.traversers.Node;

import java.util.List;

public class TopDownStrategy implements TopDownOrBottomUpStrategy{
    
    @Override
    public List<? extends Node> getNodes(Node n) {
	return n.getOutNodes();
    }
}
