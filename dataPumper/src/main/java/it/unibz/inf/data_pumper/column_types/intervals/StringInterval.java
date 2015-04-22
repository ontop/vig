package it.unibz.inf.data_pumper.column_types.intervals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;

public class StringInterval extends Interval<String> {
    
    public static String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHILMNOPKRSTUVWXYZ"; // Ordered from the least to the bigger (String.compareTo)
    private final int datatypeLength;
    
    public StringInterval(String key,
            MySqlDatatypes type,
            long nValues, int datatypeLength, List<ColumnPumper<String>> involvedCols) {
        super(key, type, nValues, involvedCols);
        this.datatypeLength = datatypeLength;
    }

    @Override
    public void updateMinEncodingAndValue(long newMin) {
        this.minEncoding = newMin;
        this.min = encode(newMin); 
    }
    
    @Override
    public void updateMaxEncodingAndValue(long newMax) {
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
    public static String encode(long value){
        
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
        
        return result.toString();
    }
    
    @Override
    public Interval<String> getCopyInstance() {
        
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
