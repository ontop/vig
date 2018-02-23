package it.unibz.inf.data_pumper.utils.graphs.traversers.selection;

public interface BreadthFirstOrDepthFirstDataStructure<T> {
    abstract void add(T n);
    abstract T getAndRemove();
    abstract boolean isEmpty();
    abstract void clear();
}
