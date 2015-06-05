package it.unibz.inf.data_pumper.utils.traversers;

import java.util.List;

public abstract class Node {
    
    private boolean visited = false;
    
    public abstract List<? extends Node> getOutNodes();
    public abstract List<? extends Node> getInNodes();
    
    public void unmarkVisited(){
	this.visited = false;
    }
    public void markVisited(){
	this.visited = true;
    }
    public boolean isVisited(){
	return this.visited;
    }
    
}
