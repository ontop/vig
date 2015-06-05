package it.unibz.inf.data_pumper.utils.traversers.visitors;

public interface VisitorWithResult<ResultType> extends Visitor{
    
    public ResultType result();
}
