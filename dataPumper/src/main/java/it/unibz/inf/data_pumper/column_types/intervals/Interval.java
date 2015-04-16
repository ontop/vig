package it.unibz.inf.data_pumper.column_types.intervals;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Interval<T> {
    
    private static final String NULL = "\\N";
    
    T max;
    T min;
    
    protected long minEncoding;
    protected long maxEncoding;
    
    protected List<T> domain;
    private int domainIndex;
    private final MySqlDatatypes type;
    
    private final String key;
    public final long nFreshsToInsert;
    
    // ColumnPumpers in the interval
    List<ColumnPumper> intervalColumns;
    
    
    public Interval(String key, MySqlDatatypes type, long nValues, List<ColumnPumper> intervalColumns){
        this.domain = null;
        this.domainIndex = 0;
        this.key = key;
        this.type = type;
        this.nFreshsToInsert = nValues;
        this.intervalColumns = intervalColumns;
        
    }
    
    public String getKey(){
        return this.key;
    }
       
    /** This method has to be called whenever information held for the column can be released **/
    public void reset(){
        if( domain != null ) domain.clear();
        domainIndex = 0;
        System.gc();
    }
    
    public String getNextValue(){
        String result = domain.get(domainIndex++).toString();
        return result;
    }
        
    public void setMaxValue(T max){
        this.max = max;
    }
    
    public T getMaxValue(){
        return max;
    }
    
    public void setMinValue(T min){
        this.min = min;
    }
    
    public T getMinValue(){
        return min;
    }
    
    public void setDomain(List<T> newDomain){
        if( domain == null ){
            domain = newDomain;
        }
    }
    
    public String getNthInDomain(int n){
        String result = domain.get(n) == null ? NULL : domain.get(n).toString() ;
        if( result == null ){
            return NULL;
        }
        if( this.type.equals(MySqlDatatypes.BIGINT) ){
            String value1 = result.substring(0, result.indexOf("."));
            result = value1;
        }
        return result;
    }
    
    public MySqlDatatypes getType(){
        return this.type;
    }

    // Public updatable interface
    public abstract void updateMinValueByEncoding(long newMin);
    public abstract void updateMaxValueByEncoding(long newMax);
    
    public abstract long getMinEncoding() throws BoundariesUnsetException;
    public abstract long getMaxEncoding() throws BoundariesUnsetException;

    /**
     * Make space for a new interval while destroying the old one
     * @param toInsert
     * @return
     */
    public abstract List<Interval<? extends Object>> split(Interval<? extends Object> toInsert);

    /**
     * 
     * @return The set of columns for which this interval is generating values
     */
    public Collection<? extends ColumnPumper> getInvolvedColumnPumpers() {
        return Collections.unmodifiableCollection(this.intervalColumns);
    } 
    
    /**
     * Each interval represents the values in an intersection of one or more columns. This
     * method adds adds a column to the intersection of the columns.
     * @param cP
     */
    public void addInvolvedColumnPumper(ColumnPumper cP){
        if( !this.intervalColumns.contains(cP) )
            this.intervalColumns.add(cP);
    }

    public abstract Interval<? extends Object> getCopyInstance();
}
