package it.unibz.inf.data_pumper.columns.intervals;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.tables.MySqlDatatypes;

public class DatetimeInterval extends Interval<Timestamp> {
    
    public static final int MILLISECONDS_PER_DAY=86400000;
    
    public DatetimeInterval(String key,
            MySqlDatatypes type,
            long nValues, List<ColumnPumper<Timestamp>> involvedCols ) {
        super(key, type, nValues, involvedCols);
    }
    
    public DatetimeInterval(String key, MySqlDatatypes type, long minEncoding, long maxEncoding){
	super(key, type, minEncoding, maxEncoding);
	
    }
    
    @Override
    public void updateMinEncodingAndValue(long newMin) {
		
        min = new Timestamp(newMin * MILLISECONDS_PER_DAY);
        this.minEncoding = newMin;
    }

    public static void normalizeCalendar(Calendar cal){
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
    }
    
    @Override
    public void updateMaxEncodingAndValue(long newMax) {

        Calendar upperBound = Calendar.getInstance();
        upperBound.set(9999,11,31);
        
        normalizeCalendar(upperBound);

        assert upperBound.getTimeInMillis() > newMax * MILLISECONDS_PER_DAY : "Attempt to update "+this.toString()+ " with too big value";
        
        max = new Timestamp(newMax * MILLISECONDS_PER_DAY);
        
        this.maxEncoding = newMax;
    }
    
    @Override
    public long getMinEncoding() {
        long encoding = (long) (this.min.getTime() / MILLISECONDS_PER_DAY);
        return encoding;
    }

    @Override
    public long getMaxEncoding() {
        long encoding = (long) (this.max.getTime() / MILLISECONDS_PER_DAY);
        return encoding;
    }
    
    @Override
    public Interval<Timestamp> getCopyInstance() {
        
        DatetimeInterval result = 
                new DatetimeInterval(
                        this.getKey(), this.getType(), 
                        this.nFreshsToInsert, new LinkedList<>(this.intervalColumns));
        result.updateMinEncodingAndValue(this.minEncoding);
        result.updateMaxEncodingAndValue(this.maxEncoding);
        
        return result;
    }

    @Override
    public void synchronizeMinMaxNFreshs() {
	 Calendar upperBound = Calendar.getInstance();
	 upperBound.set(9999,11,31);
	 
	 normalizeCalendar(upperBound);
	 
	 assert upperBound.getTimeInMillis() >= this.getMaxEncoding() : "The long value is greater than upper bound year 9999 for interval "+this.toString() ;
	 
	 if( upperBound.getTimeInMillis() == this.getMaxEncoding() * MILLISECONDS_PER_DAY ){        
	     long newMin = this.getMaxEncoding() - this.nFreshsToInsert;
	     this.updateMinEncodingAndValue(newMin);	     
	 }
	 
    }

}
