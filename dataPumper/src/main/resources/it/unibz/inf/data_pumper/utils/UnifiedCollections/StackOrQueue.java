package it.unibz.inf.data_pumper.utils.UnifiedCollections;

public interface StackOrQueue<T> {
    abstract void add(T n);
    abstract T getAndRemove();
    abstract boolean isEmpty();
}
