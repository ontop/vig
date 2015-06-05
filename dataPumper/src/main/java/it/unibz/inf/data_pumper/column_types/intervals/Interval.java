package it.unibz.inf.data_pumper.column_types.intervals;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.Column;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.core.main.DebugException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Interval<T> {
    
    private static final String NULL = "\\N";
    
    protected T max;
    protected T min;
    
    protected long minEncoding;
    protected long maxEncoding;
    
    protected List<T> domain;
    private int domainIndex;
    private final MySqlDatatypes type;
    
    private String key;
    protected long nFreshsToInsert;
    
    // ColumnPumpers in the interval
    List<ColumnPumper<T>> intervalColumns;
    
    
    public Interval(String key, MySqlDatatypes type, long nValues, List<ColumnPumper<T>> intervalColumns){
        this.domain = null;
        this.domainIndex = 0;
        this.key = key;
        this.type = type;
        this.nFreshsToInsert = nValues;
        this.intervalColumns = intervalColumns;
        
    }
    
    public void setNFreshsToInsert(long nFreshsToInsert){
        this.nFreshsToInsert = nFreshsToInsert;
    }
    
    public long getNFreshsToInsert(){
        return this.nFreshsToInsert;
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
        
//    public void setMaxValue(T max){
//        this.max = max;
//    }
    
    public T getMaxValue(){
        return max;
    }
    
//    public void setMinValue(T min){
//        this.min = min;
//    }
    
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
    public abstract void updateMinEncodingAndValue(long newMin);
    public abstract void updateMaxEncodingAndValue(long newMax);
    
    public abstract long getMinEncoding();
    public abstract long getMaxEncoding();

    /**
     * It adapts the boundaries of <b>this</b> interval
     * @param toInsert
     * @return
     */
    public boolean adaptBounds(Interval<T> toInsert) {
        
        boolean killMe = false;
        
        long splitterMaxEncoding = toInsert.maxEncoding;
        long splitterMinEncoding = toInsert.minEncoding;
        
        // Assert
        if( splitterMaxEncoding <= splitterMinEncoding ){
            throw new DebugException("Assertion failed: splitterMaxEncoding <= splitterMinEncoding");
        }
        // Assert
        if( this.getMinEncoding() < splitterMinEncoding ){
            throw new DebugException("The splitter minimum is not equal to the minimum of the to-be-splitted interval");
        }
        
        if( this.getMaxEncoding() > splitterMaxEncoding ){
            // Shrink
            this.updateMinEncodingAndValue(splitterMaxEncoding);
        
            this.setNFreshsToInsert( this.getMaxEncoding() - this.getMinEncoding() );
        }
        else{
            if(! (this.getMaxEncoding() == splitterMaxEncoding) ){
        	throw new DebugException("Assertion failed: this.getMaxEncoding() == splitterMaxEncoding");
            }
            // This interval HAS TO DIE!
            killMe/*please*/ = true;
            /*++evilLaugh*/
        }
        return killMe;
    }

    /**
     * 
     * @return The set of columns for which this interval is generating values
     */
    public Collection<? extends ColumnPumper<T>> getInvolvedColumnPumpers() {
        return Collections.unmodifiableCollection(this.intervalColumns);
    } 
    
    /**
     * Each interval represents the values in an intersection of one or more columns. This
     * method adds adds a column to the intersection of the columns. This method modifies the 
     * key of <b>this</b> interval with <b>this.getKey() + cP.getQualifiedName()</b>. 
     * 
     * @param cP
     */
    public void addInvolvedColumnPumper(ColumnPumper<T> cP){
        if( !this.intervalColumns.contains(cP) ){
            this.intervalColumns.add(cP);
            this.key = this.key + "---" + cP.getQualifiedName().toString();
        }
    }

    public abstract Interval<T> getCopyInstance();

    /**
     * It removes all the stable references to <b>this</b> interval.
     */
    public void suicide() {
        // For each involved column pumper, remove me from their lists of intervals
        for( ColumnPumper<T> cP : this.intervalColumns ){
            cP.removeIntervalOfKey(this.getKey());
        }   
    }
    
    public int sizeIntersection(){
        return this.getInvolvedColumnPumpers().size();
    }
    
    public Set<String> getKeysSet(){
        
        Set<String> result = new HashSet<String>();
        
        String[] splits = this.getKey().split("---");
        for( String s : splits ){
            result.add(s);
        }
        
        return result;
    }
    
    
    
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(this.key + "\n");
        builder.append("Min Encoding = " + this.minEncoding + "\n");
        builder.append("Max encoding = " + this.maxEncoding + "\n");
        builder.append("nFreshsToInsert = " + this.nFreshsToInsert + "\n");
        return builder.toString();
    }
};
