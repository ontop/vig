package it.unibz.inf.data_pumper.utils.graphs.traversers.selection;

import java.util.LinkedList;
import java.util.Queue;

public class BreadthFirstStructure<T> implements BreadthFirstOrDepthFirstDataStructure<T> {
    private Queue<T> queue;
    
    public BreadthFirstStructure(){
	queue = new LinkedList<T>();
    }
    
    @Override
    public void add(T n) {
	queue.add(n);
    }

    @Override
    public T getAndRemove() {
	return queue.poll();
    }

    @Override
    public boolean isEmpty() {
	return queue.isEmpty();
    }

    @Override
    public void clear() {
	queue.clear();
    }
};
