package it.unibz.inf.data_pumper.utils.traversers;

import it.unibz.inf.data_pumper.utils.traversers.visitors.Visitor;


public abstract class CleanableTraverser implements Traverser {
    
    private static int visitID = 0;
    
    public CleanableTraverser(){
	increaseID();
    }
    
    private void increaseID(){
	++visitID;
    }
    
    public int visitID(){
	return visitID;
    }
    
    @Override
    public void traverse(Visitor v){
	increaseID();
	traverseImpl(v);
    }
    
    protected abstract void traverseImpl(Visitor v);
}
