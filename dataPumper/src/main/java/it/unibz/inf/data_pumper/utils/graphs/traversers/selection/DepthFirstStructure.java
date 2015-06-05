package it.unibz.inf.data_pumper.utils.graphs.traversers.selection;

import java.util.Stack;

public class DepthFirstStructure<T> implements BreadthFirstOrDepthFirstDataStructure<T> {
   private Stack<T> stack;
    
    public DepthFirstStructure(){
	stack = new Stack<T>();
    }
    
    
    @Override
    public void add(T n) {
	stack.push(n);
    }

    @Override
    public T getAndRemove() {
	return stack.pop();
    }

    @Override
    public boolean isEmpty() {
	return stack.isEmpty();
    }


    @Override
    public void clear() {
	stack.clear();
    }
}
