package it.unibz.inf.data_pumper.utils.traversers;

import java.util.List;

import it.unibz.inf.data_pumper.utils.graphs.traversers.adjacence.BottomUpStrategy;
import it.unibz.inf.data_pumper.utils.graphs.traversers.adjacence.TopDownOrBottomUpStrategy;
import it.unibz.inf.data_pumper.utils.graphs.traversers.adjacence.TopDownStrategy;
import it.unibz.inf.data_pumper.utils.graphs.traversers.selection.BreadthFirstOrDepthFirstDataStructure;
import it.unibz.inf.data_pumper.utils.graphs.traversers.selection.BreadthFirstStructure;
import it.unibz.inf.data_pumper.utils.graphs.traversers.selection.DepthFirstStructure;
import it.unibz.inf.data_pumper.utils.traversers.visitors.BooleanVisitor;
import it.unibz.inf.data_pumper.utils.traversers.visitors.CollectVisitedVisitor;

public abstract class TraverserAbstractFactory {
    
    public abstract ListTraverser makeListTraverser(List<? extends Node> v);
    public abstract GraphTraverser makeGraphTraverser(List<? extends Node> v, TopDownOrBottomUpStrategy adjacenceSelectionStrategy, BreadthFirstOrDepthFirstDataStructure<Node> toVisit);
    public abstract ReachConnectedTraverser makeReachConnectedTraverser(List<? extends Node> v);
    
    // Strategies
    public abstract BottomUpStrategy makeBottomUpStrategy();
    public abstract TopDownStrategy makeTopDownStrategy();
    
    // Adjacent Selection
    public abstract BreadthFirstStructure<Node> makeBreadthFirstStructure();
    public abstract DepthFirstStructure<Node> makeDepthFirstStructure();
    
    // Predefined visitors
    public abstract BooleanVisitor makeBooleanVisitor(ConditionChecker checker);
    public abstract CollectVisitedVisitor makeCollectVisitedVisitor();    
}
