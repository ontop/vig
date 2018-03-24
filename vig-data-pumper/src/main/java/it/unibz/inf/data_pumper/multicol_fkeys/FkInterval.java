package it.unibz.inf.data_pumper.multicol_fkeys;

import it.unibz.inf.data_pumper.columns.intervals.Interval;

import java.util.List;

public class FkInterval {
    private Label label;
    private List<Interval<?>> fkTupleSchema;

    public void intersect(FkInterval that){
	assert this.schema().equals(that.schema()) : "Schema not matching";
	
	for( int i = 0; i < this.fkTupleSchema.size(); ++i ){
	    this.fkTupleSchema.get(i).intersect(that.fkTupleSchema.get(i));
	}
    }
    
    public boolean isEmpty(){
	for( Interval<?> i : this.fkTupleSchema ){
	    if( i.isEmpty() ) return true;
	}
	return false;
    }
    
    public String schema(){
	
	StringBuilder result = new StringBuilder();
	
	for( Interval<?> i : this.fkTupleSchema ){
	    result.append(i.getKey());
	}
	
	return result.toString();
    }
};


