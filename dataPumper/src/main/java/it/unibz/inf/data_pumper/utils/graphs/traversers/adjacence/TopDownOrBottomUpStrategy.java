package it.unibz.inf.data_pumper.utils.graphs.traversers.adjacence;

import it.unibz.inf.data_pumper.utils.traversers.Node;

import java.util.List;

public interface TopDownOrBottomUpStrategy {
    List<? extends Node> getNodes(Node n);
}
