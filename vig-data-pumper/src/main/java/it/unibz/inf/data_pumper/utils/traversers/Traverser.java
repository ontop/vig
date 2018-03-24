package it.unibz.inf.data_pumper.utils.traversers;

import it.unibz.inf.data_pumper.utils.traversers.visitors.Visitor;

public interface Traverser {
    
    public void traverse(Visitor v);
}
