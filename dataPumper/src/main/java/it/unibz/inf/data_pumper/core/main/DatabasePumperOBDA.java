package it.unibz.inf.data_pumper.core.main;

import it.unibz.inf.data_pumper.basic_datatypes.QualifiedName;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.configuration.Conf;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.connection.exceptions.InstanceNullException;
import it.unibz.inf.vig_mappings_analyzer.core.JoinableColumnsFinder;
import it.unibz.inf.vig_mappings_analyzer.datatypes.Argument;
import it.unibz.inf.vig_mappings_analyzer.datatypes.Field;
import it.unibz.inf.vig_mappings_analyzer.datatypes.FunctionTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;

/**
 * @author tir
 *
 */
public class DatabasePumperOBDA extends DatabasePumperDB {
	
	// Aggregated classes
	private CorrelatedColumnsExtractor cCE;
		
	public DatabasePumperOBDA() {
		super();
		try {
			JoinableColumnsFinder jCF = new JoinableColumnsFinder(Conf.getInstance().mappingsFile());
			this.cCE = new CorrelatedColumnsExtractor(jCF);
		} catch (Exception e) {
			e.printStackTrace();
			DatabasePumperOBDA.closeEverything();
		}
	}
	
	@Override
	protected <T> void establishColumnBounds(List<ColumnPumper<? extends Object>> listColumns) throws ValueUnsetException, DEBUGEXCEPTION, InstanceNullException, SQLException{
		for( ColumnPumper<? extends Object> cP : listColumns ){
			cP.fillFirstIntervalBoundaries(cP.getSchema(), dbOriginal);
		}
		// At this point, each column is initialized with statistical information
		// like null, dups ratio, num rows and freshs to insert, etc.
		
		// search for correlated columns and order them by fresh values to insert
		List<CorrelatedColumnsList<T>> correlatedCols = this.cCE.extractCorrelatedColumns();
		
		try {
			updateColumnBoundsWRTCorrelated(correlatedCols);
		} catch (BoundariesUnsetException | DEBUGEXCEPTION e) {
			e.printStackTrace();
			DatabasePumper.closeEverything();
			System.exit(1);
		}
	}

    /**
	 * Update the boundaries of those columns in a correlated set
	 * @param correlatedCols (It MUST include foreign keys)
	 * @throws ValueUnsetException 
	 * @throws BoundariesUnsetException 
	 * @throws DEBUGEXCEPTION 
     * @throws InstanceNullException 
     * @throws SQLException 
	 */
	private <T> void updateColumnBoundsWRTCorrelated(
			List<CorrelatedColumnsList<T>> correlatedCols) 
			        throws ValueUnsetException, BoundariesUnsetException, DEBUGEXCEPTION, SQLException, InstanceNullException {
			    
	    for( CorrelatedColumnsList<T> cCL : correlatedCols){
	        IntervalsBoundariesFinder<T> utils = new IntervalsBoundariesFinder<T>(this);
	        List<Interval<T>> insertedIntervals = new LinkedList<Interval<T>>();
	        for( int i = 0; i < cCL.size(); ++i ){
	            ColumnPumper<T> cP = cCL.get(i);
	            utils.insert(insertedIntervals, cP);
	        }
	        // All interval boundaries are set
	    }
	}
};

class IntervalsBoundariesFinder<T>{
    
    private DatabasePumperOBDA dbPumperInstance;
    
    public IntervalsBoundariesFinder(
            DatabasePumperOBDA databasePumperOBDA) {
        dbPumperInstance = databasePumperOBDA;
    }

    /**
     * Side effect on insertedIntervals and cP and related ColumnPumper objects. <br>
     * 
     * This method inserts intervals of cP in every of the insertedIntervals (so as to
     * try out all the intersections of columns)
     * 
     * @throws DEBUGEXCEPTION 
     * @throws ValueUnsetException 
     * @throws InstanceNullException 
     * @throws SQLException 
     * @throws BoundariesUnsetException 
     */
    void insert(List<Interval<T>> insertedIntervals, ColumnPumper<T> cP) 
            throws DEBUGEXCEPTION, ValueUnsetException, SQLException, InstanceNullException, BoundariesUnsetException{
             
        long maxEncodingEncountered = 0;
        
        // Assert 
        if( cP.getIntervals().size() != 1 ){
            throw new DEBUGEXCEPTION("Intervals size != 1");
        }
        
        if( insertedIntervals.isEmpty() ){
            insertedIntervals.add(cP.getIntervals().get(0));
        }
        else{
            for( ListIterator<Interval<T>> it = insertedIntervals.listIterator() ; it.hasNext(); ){
                Interval<T> previouslyInserted = it.next();
                
                if( maxEncodingEncountered < previouslyInserted.getMaxEncoding() ) maxEncodingEncountered = previouslyInserted.getMaxEncoding();
                
                long nToInsertInPreviousInterval = makeIntersectionQuery(cP, previouslyInserted);
                
                if( nToInsertInPreviousInterval > 0 ){ // Create a new "SubInterval"
                    
                    // Make sub interval ( with the right boundaries )
                    Interval<T> toInsert = makeSubInterval(previouslyInserted, cP, nToInsertInPreviousInterval);
                    
                    // Split
                    boolean killOldInterval = previouslyInserted.adaptBounds(toInsert);
                    
                    if( killOldInterval ){
                        it.remove();
                        previouslyInserted.suicide(); 
                    }
                    insertedIntervals.add(toInsert);
                }
            }
            
            long nFreshsInFirstInterval = cP.getNumFreshsToInsert() - cP.countFreshsInIntersectedIntervals();
            
            if( nFreshsInFirstInterval > 0 ){
                // Find fresh values for cP.getIntervals.get(0);
                cP.getIntervals().get(0).updateMinEncodingAndValue(maxEncodingEncountered);
                cP.getIntervals().get(0).updateMaxEncodingAndValue( (maxEncodingEncountered) + nFreshsInFirstInterval );
            }
        }
    }

    /** 
     *  It creates a new interval <b>result</b> starting from the boundaries of a given
     *  <b>previouslyInserted</b> interval. Then, it adds <b>result</b> to <b>cP.getIntervals()</b>.
     *  
     * @param previouslyInserted
     * @param cP
     * @param nToInsertInPreviouslyInserted
     * @return
     * @throws BoundariesUnsetException 
     */
    private Interval<T> makeSubInterval(
            Interval<T> previouslyInserted, ColumnPumper<T> cP, long nToInsertInPreviouslyInserted) 
                    throws BoundariesUnsetException {
        
        Interval<T> result = previouslyInserted.getCopyInstance();
        result.updateMaxEncodingAndValue(previouslyInserted.getMinEncoding() + nToInsertInPreviouslyInserted);
        result.addInvolvedColumnPumper(cP);
        
        return result;
    }

    /**
     * 
     * @param cP
     * @param previouslyInserted
     * @return The shared ratio of cP Join {cP_i | cP_i \in previouslyInserted}
     * @throws SQLException
     * @throws InstanceNullException
     * @throws ValueUnsetException
     * @throws DEBUGEXCEPTION 
     */
    private long makeIntersectionQuery(ColumnPumper<T> cP, Interval<T> previouslyInserted) 
            throws SQLException, InstanceNullException, ValueUnsetException, DEBUGEXCEPTION {
        
        long result = 0;
        
        List<ColumnPumper<T>> cols = new ArrayList<ColumnPumper<T>>();
        
        cols.add(cP); 
        cols.addAll(previouslyInserted.getInvolvedColumnPumpers());
        
        // Assert
        if( ! cols.get(0).getQualifiedName().equals(cP.getQualifiedName()) ){
            throw new DEBUGEXCEPTION("The being inserted column HAS TO BE IN FIRST POSITION");
        }
        float sharedRatio = this.dbPumperInstance.tStatsFinder.findSharedRatio(cols);
        
        result = (long) (cP.getNumRowsToInsert() * sharedRatio);
        
        return result; 
    }
};


class CorrelatedColumnsList<T>{
	
	// This is always sorted w.r.t. the number of fresh values to insert
	private LinkedList<ColumnPumper<T>> columns;
	
	public CorrelatedColumnsList(){
		this.columns = new LinkedList<ColumnPumper<T>>();
	}
	
	public int size(){
		return columns.size();
	}
	
	public ColumnPumper<T> get(int index){
		return this.columns.get(index);
	}
	
	/**
	 * Insert cP in the list of correlated columns, while
	 * satisfying the ordering constraint on this.columns
	 * @param cP
	 */
	public void insert(ColumnPumper<T> cP){
		try{
			long nFreshs = cP.getNumFreshsToInsert();
			if( this.columns.size() == 0 ){
				this.columns.add(cP);
			}
			for( int i = 0; i < this.columns.size(); ++i ){
				ColumnPumper<T> el = this.columns.get(i);
				long elNFreshs = el.getNumFreshsToInsert();
				if( nFreshs > elNFreshs ){
					this.columns.add(i, cP);
					break;
				}
			}
		}catch(ValueUnsetException e){
			e.printStackTrace();
			DatabasePumper.closeEverything();
		}
	}
	
	public boolean isInCorrelated(ColumnPumper<T> cP){
		if( columns.contains(cP) ){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString(){
		return columns.toString();
	}
	
};

class CorrelatedColumnsExtractor{
	
	private final JoinableColumnsFinder jCF;
	
	CorrelatedColumnsExtractor(JoinableColumnsFinder jCF) {
		this.jCF = jCF;
	}
	
	<T> List<CorrelatedColumnsList<T>> extractCorrelatedColumns() {
		List<CorrelatedColumnsList<T>> result = null;
		try {
			
			List<FunctionTemplate> templates = jCF.findFunctionTemplates();
			
			Set<Set<Field>> correlatedFields = extractCorrelatedFields(templates);
			
			Queue<Set<Field>> qCorrelatedFields = new LinkedList<Set<Field>>();
			for( Set<Field> fields : correlatedFields ){
				qCorrelatedFields.add(fields);
			}
			correlatedFields.clear();
			
			addForeignKeys(qCorrelatedFields);
			
			// merge 
			List<Set<Field>> maximalMerge = new ArrayList<Set<Field>>();
			maximalMerge(qCorrelatedFields, maximalMerge);
			
			result = constructCorrelatedColumnsList(maximalMerge);
			
		} catch (Exception e) {
			e.printStackTrace();
			DatabasePumperOBDA.closeEverything();
		}
		return result;
	}

	/**
	 * It adds to each set of correlated fields (Set<Field>) all correlated columns that derive
	 * 
	 * @param qCorrelatedFields
	 * @throws InstanceNullException
	 */
	private void addForeignKeys(Queue<Set<Field>> qCorrelatedFields) throws InstanceNullException {
	    
	    Queue<Set<Field>> result = new LinkedList<Set<Field>>();
	    
	    while( !qCorrelatedFields.isEmpty() ){
	        Set<Field> sF = qCorrelatedFields.poll();
	        
	        Queue<Field> queueOfFields = new LinkedList<Field>(sF);
            
	        while( queueOfFields.isEmpty() ){
	            Field f = queueOfFields.poll();
	            Queue<QualifiedName> correlated = new LinkedList<QualifiedName>();
                correlated.add(new QualifiedName(f.tableName, f.colName));
                
                List<QualifiedName> correlatedMax = new ArrayList<QualifiedName>();
                
                // Side effect on correlatedMax
                correlatedThroughForeignKeys(correlated, correlatedMax);
                
                // Insert all columns that are correlated because of a foreign key constraint
                for( QualifiedName qN : correlatedMax ){
                    Field toInsert = new Field(qN.getTableName(), qN.getColName());
                    sF.add(toInsert); // Try to insert if new
                }
	        }
	        result.add(sF);
	    }
	    qCorrelatedFields.addAll(result);
	}

    private void correlatedThroughForeignKeys(Queue<QualifiedName> correlated, List<QualifiedName> result) throws InstanceNullException {
        
        if( correlated.isEmpty() ) return;
        
        QualifiedName current = correlated.poll();
        if( result.contains(current) ){
            correlatedThroughForeignKeys(correlated, result);  // Recursion
        }
        else{
            ColumnPumper<? extends Object> cP = DBMSConnection.getInstance().getSchema(current.getTableName()).getColumn(current.getColName());
            correlated.addAll(cP.referencedBy());
            correlated.addAll(cP.referencesTo());
            result.add(current);
            correlatedThroughForeignKeys(correlated, result);  // Recursion
        }
    }

    private <T> List<CorrelatedColumnsList<T>> constructCorrelatedColumnsList(List<Set<Field>> maximalMerge) {
		
		List<CorrelatedColumnsList<T>> result = new ArrayList<CorrelatedColumnsList<T>>();
		
		for( Set<Field> correlatedColsList : maximalMerge ){
			CorrelatedColumnsList<T> cCL = new CorrelatedColumnsList<T>();
			for( Field f : correlatedColsList ){
				try {
					@SuppressWarnings("unchecked")
                    ColumnPumper<T> cP = (ColumnPumper<T>) DBMSConnection.getInstance().getSchema(f.tableName).getColumn(f.colName);
					cCL.insert(cP);
				} catch (InstanceNullException e) {
					e.printStackTrace();
					DatabasePumperOBDA.closeEverything();
				}
			}
			result.add(cCL);
		}
		return result;
	}

	private void maximalMerge(Queue<Set<Field>> current, List<Set<Field>> result) {
		
		if(!current.isEmpty()){
			Set<Field> first = current.remove();
			boolean changed = false;
			for( Iterator<Set<Field>> iterator = current.iterator(); iterator.hasNext(); ){
				Set<Field> elem = iterator.next();
				for( Field f : first ){
					if( elem.contains(f) ){
						first.addAll(elem);
						changed = true;
						iterator.remove();
						break;
					}
				}
			}
			if( changed ){ // It could merge with more
				current.add(first);
			}
			else{
				result.add(first);
			}
			maximalMerge(current, result);
		}
				
	}

	private Set<Set<Field>> extractCorrelatedFields(
			List<FunctionTemplate> templates) {
		
		Set<Set<Field>> result = new HashSet<Set<Field>>();
				
		for( FunctionTemplate fT : templates ){
			for( int i = 0; i < fT.getArity(); ++i ){
				Argument arg = fT.getArgumentOfIndex(i);
				
				Set<Field> fillingFields = arg.getFillingFields();
				
				if( fillingFields.size() > 1 ){
					result.add(fillingFields);
				}
			}
		}
		return result;
	}	
}