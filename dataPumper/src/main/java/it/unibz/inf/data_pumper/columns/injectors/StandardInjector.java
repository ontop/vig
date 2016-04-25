package it.unibz.inf.data_pumper.columns.injectors;

import java.util.List;

import it.unibz.inf.data_pumper.columns.CyclicGroupGenerator;
import it.unibz.inf.data_pumper.columns.intervals.Interval;

public abstract class StandardInjector<T> extends InjectorSkeleton<T> {
    
    public StandardInjector(long numFreshs, long numNulls, CyclicGroupGenerator gen, List<Interval<?>> intervals)  {
	super(numFreshs, numNulls, gen, intervals);
    }
}
