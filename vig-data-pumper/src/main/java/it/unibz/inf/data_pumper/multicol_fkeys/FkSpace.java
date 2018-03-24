package it.unibz.inf.data_pumper.multicol_fkeys;

import java.util.List;

public class FkSpace {
    private Label label;
    private List<FkInterval> fkIntervals;
    
    public void intersect(FkInterval that){
		
	for( FkInterval f : this.fkIntervals ){
	    f.intersect(that);
	}
    }
};
