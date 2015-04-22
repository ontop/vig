package it.unibz.inf.data_pumper.column_types.intervals;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.column_types.exceptions.DateOutOfBoundariesException;

public class DatetimeInterval extends Interval<Timestamp> {
    
    public static final int MILLISECONDS_PER_DAY=86400000;
    private static Logger logger = Logger.getLogger(DatetimeInterval.class.getCanonicalName());

    public DatetimeInterval(String key,
            MySqlDatatypes type,
            long nValues, List<ColumnPumper<Timestamp>> involvedCols ) {
        super(key, type, nValues, involvedCols);
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
            try{
                throw new DateOutOfBoundariesException();
            }catch(DateOutOfBoundariesException e){
                logger.error("The Date field cannot hold this many rows");
                System.exit(1);
            }
        }
        this.maxEncoding = newMax;
    }
    
    @Override
    public long getMinEncoding() throws BoundariesUnsetException {
        long encoding = (long) (this.min.getTime() / MILLISECONDS_PER_DAY);
        return encoding;
    }

    @Override
    public long getMaxEncoding() throws BoundariesUnsetException {
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
