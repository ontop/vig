package it.unibz.inf.data_pumper.column_types.intervals;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.DateOutOfBoundariesException;

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

    @Override
    public void updateMaxEncodingAndValue(long newMax) {

        Calendar upperBound = Calendar.getInstance();
        upperBound.set(9999,11,31);

        if( upperBound.getTimeInMillis() > newMax * MILLISECONDS_PER_DAY ){        
            max = new Timestamp(newMax * MILLISECONDS_PER_DAY);
        }
        else{
            newMax = upperBound.getTimeInMillis() / MILLISECONDS_PER_DAY;
            max = new Timestamp(newMax * MILLISECONDS_PER_DAY);
//            throw new DateOutOfBoundariesException("The Date field cannot hold this many rows, interval is "+this.getKey());
        }
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

}
