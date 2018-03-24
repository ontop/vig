package it.unibz.inf.vig_mappings_analyzer.mappings_profiler.core;

import it.unibz.inf.vig_mappings_analyzer.datatypes.FunctionTemplate;
import it.unibz.inf.vig_mappings_analyzer.datatypes.SPJQuery;

/**
 * 
 * @author Davide Lanti
 * Immutable View of a mapping over a SPJ query (i.e., immutable view of a GAV mapping)
 *
 */
public class MappingAssertion{
    private final FunctionTemplate lhs;
    private final FunctionTemplate rhs;
    private final UriPredicate predicate;
    private final SPJQuery query;
    
    private MappingAssertion(Builder builder){
	this.lhs = builder.lhs;
	this.rhs = builder.rhs;
	this.predicate = builder.predicate;
	this.query = builder.query;
    }
    
    public static class Builder{
	private FunctionTemplate lhs = null;
	private FunctionTemplate rhs = null;
	private UriPredicate predicate = null;
	private SPJQuery query = null;
	
	public Builder(){
	}
	
	public Builder lhs(FunctionTemplate lhs){
	    this.lhs = lhs; return this;
	}
	
	public Builder rhs(FunctionTemplate rhs){
	    this.rhs = rhs; return this;
	}
	
	public Builder predicate(UriPredicate predicate){
	    this.predicate = predicate; return this;
	}
	
	public Builder spjQuery(SPJQuery query){
	    this.query = query; return this;
	}
	
	public MappingAssertion build(){
	    assert this.lhs != null && this.rhs != null && this.predicate != null && this.query != null : "The builder has null values";
		return new MappingAssertion(this);
	}
    }
    
    public final FunctionTemplate getLhs(){
	return this.lhs;
    }
    
    public final FunctionTemplate getRhs(){
	return this.rhs;
    }
    
    public final UriPredicate getPredicate(){
	return this.predicate;
    }
    
    public final SPJQuery getQuery(){
	return this.query;
    }
    
    @Override
    public String toString(){
	StringBuilder resultBuilder = new StringBuilder();
	
	resultBuilder.append("\nLHS: " + this.getLhs().toString());
	resultBuilder.append("\n");
	resultBuilder.append("Predicate: " + this.getPredicate().toString());
	resultBuilder.append("\n");
	resultBuilder.append("RHS: " + this.getRhs().toString());
	resultBuilder.append("\n");
	resultBuilder.append("SPJ: " + this.getQuery() + "\n");
	
	return resultBuilder.toString();
    }
      
};

class UriPredicate{
    private final String name;
    
    private UriPredicate(String name){
	this.name = name;
    }
    
    public static UriPredicate makeInstance(String name){
	return new UriPredicate(name);
    }
    
    public final String getName(){
	return this.name;
    }
    
    @Override
    public String toString(){
	return name;
    }
};