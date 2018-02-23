package it.unibz.inf.data_pumper.columns.injectors;

import java.util.List;

import it.unibz.inf.data_pumper.columns.CyclicGroupGenerator;
import it.unibz.inf.data_pumper.columns.intervals.Interval;

public abstract class InjectorSkeleton<T> implements Injector<T> {
    
    protected long numFrehs;
    protected long numNulls;
    protected CyclicGroupGenerator gen;
    protected List<Interval<?>> intervals;
    
    public InjectorSkeleton(long numFreshs, long numNulls, CyclicGroupGenerator gen, List<Interval<?>> intervals){
	this.numFrehs = numFreshs;
	this.numNulls = numNulls;
	this.gen = gen;
	this.intervals = intervals;
    }    
}
