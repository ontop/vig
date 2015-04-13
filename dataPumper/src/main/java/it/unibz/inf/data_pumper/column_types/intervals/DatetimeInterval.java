package it.unibz.inf.data_pumper.column_types.intervals;

import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.log4j.Logger;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.column_types.exceptions.DateOutOfBoundariesException;

public class DatetimeInterval extends Interval<Timestamp> {
    
    public static final int MILLISECONDS_PER_DAY=86400000;
    private static Logger logger = Logger.getLogger(DatetimeInterval.class.getCanonicalName());

    public DatetimeInterval(String key,
            MySqlDatatypes type,
            long nValues) {
        super(key, type, nValues);
    }
    
    @Override
    public void updateMinValueByEncoding(long newMin) {
        min = new Timestamp(newMin * MILLISECONDS_PER_DAY);
    }

    @Override
    public void updateMaxValueByEncoding(long newMax) {

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

}
