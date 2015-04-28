package it.unibz.inf.data_pumper.column_types.intervals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.core.main.DebugException;

public class StringInterval extends Interval<String> {
    
    public static String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHILMNOPKRSTUVWXYZ"; // Ordered from the least to the bigger (String.compareTo)
    protected final int datatypeLength;
        
    public StringInterval(String key,
            MySqlDatatypes type,
            long nValues, int datatypeLength, List<ColumnPumper<String>> involvedCols) {
        super(key, type, nValues, involvedCols);
        this.datatypeLength = datatypeLength;
    }

    @Override
    public void updateMinEncodingAndValue(long newMin) throws DebugException {
        this.minEncoding = newMin;
        this.min = encode(newMin); 
    }
    
    @Override
    public void updateMaxEncodingAndValue(long newMax) throws DebugException{
        this.maxEncoding = newMax;
        this.max = encode(newMax);
    }

    @Override
    public long getMaxEncoding() throws BoundariesUnsetException {
        long base = 1;
        for( int i = 0; i < datatypeLength && i < 10; ++i ){ // 1.568336881×10¹⁸
            base *= characters.length();
        }
        long proposed = this.minEncoding + this.nFreshsToInsert;
        if( base >= proposed )
            return proposed;
        else {
            return Long.MAX_VALUE; // Some kind of error
        }
    }

    @Override
    public long getMinEncoding() throws BoundariesUnsetException {
        return this.minEncoding;
    }
    
 // Encode in base 62
    public String encode(long value) throws DebugException{
        
        List<Integer> number = new ArrayList<Integer>();
        
        while( !(value == 0) ){
            int remainder = (int) value % characters.length();
            number.add(0, remainder);
            
            value = value / characters.length();
            
        }
        
        StringBuilder result = new StringBuilder();
        for( int i = 0; i < number.size(); ++i ){
            result.append(characters.charAt((number.get(i))));
        }
        
        String trail = result.toString();
        
	String lowerBouldValue = lowerBoundValue();
	
	StringBuilder zeroes = new StringBuilder();
	
	if( lowerBouldValue.length() >= trail.length() ){
		for( int j = 0; j < lowerBouldValue.length() - trail.length(); ++j ){
			zeroes.append("0");
		}
		this.max = zeroes.toString() + trail;
	}
	else{
//	    throw 
//	    new DebugException("The string "+trail+" is too long for the chosen datatype "
//		    + "length "+datatypeLength+ " of column "
//		    + getInvolvedColumnPumpers().iterator().next().getQualifiedName().toString());
	    this.max = upperBoundValue();
	    this.nFreshsToInsert = 1;
	    for( int i = 0; i < this.max.length(); ++i ){
		this.nFreshsToInsert *= characters.length() -1;
	    }
	}
        
        return trail;
    }
    
    private String lowerBoundValue(){
	StringBuilder builder = new StringBuilder();

	for( int i = 0; i < datatypeLength; ++i ){
	    builder.append(characters.charAt(0)); // Minimum
	}

	return builder.toString();
    }

    private String upperBoundValue(){
	StringBuilder builder = new StringBuilder();

	for( int i = 0; i < datatypeLength; ++i ){
	    builder.append(characters.charAt(characters.length()-1)); // Maximum
	}

	return builder.toString();
    }
    
    @Override
    public Interval<String> getCopyInstance() throws DebugException {
        
        StringInterval result =
                new StringInterval(
                        this.getKey(), this.getType(), 
                        this.nFreshsToInsert, this.datatypeLength, 
                        new LinkedList<>(this.intervalColumns));
        result.updateMinEncodingAndValue(this.minEncoding);
        result.updateMaxEncodingAndValue(this.maxEncoding);
        
        return result;
    }
};
