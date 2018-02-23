package it.unibz.inf.data_pumper.utils.traversers;

import java.util.List;

public abstract class Node {
    
    private int visitedBy = 0;
    
    public abstract List<? extends Node> getOutNodes();
    public abstract List<? extends Node> getInNodes();
    
    public void unmarkVisited(){
	this.visitedBy = Integer.MAX_VALUE;
    }
    public void markVisited(int traversalID){
	this.visitedBy = traversalID;
    }
    public boolean isVisited(int traversalID){
	return this.visitedBy == traversalID;
    }
    
}
