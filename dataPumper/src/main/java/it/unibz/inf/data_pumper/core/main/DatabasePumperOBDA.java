package it.unibz.inf.data_pumper.core.main;

import it.unibz.inf.data_pumper.basic_datatypes.QualifiedName;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
import it.unibz.inf.data_pumper.column_types.intervals.BigDecimalInterval;
import it.unibz.inf.data_pumper.column_types.intervals.DatetimeInterval;
import it.unibz.inf.data_pumper.column_types.intervals.IntInterval;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.column_types.intervals.StringInterval;
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
	protected void establishColumnBounds(List<ColumnPumper> listColumns) throws ValueUnsetException, DEBUGEXCEPTION, InstanceNullException{
		for( ColumnPumper cP : listColumns ){
			cP.fillFirstIntervalBoundaries(cP.getSchema(), dbOriginal);
		}
		// At this point, each column is initialized with statistical information
		// like null, dups ratio, num rows and freshs to insert, etc.
		
		
		// search for correlated columns and order them by fresh values to insert
		List<CorrelatedColumnsList> correlatedCols = this.cCE.extractCorrelatedColumns();
		
		// Now, correlatedCols contains sets of correlated columns (closed under referencesTo and referredBy).
		// I need to identify the intervals, now.
		identifyIntervals(correlatedCols);
		
		try {
			updateColumnBoundsWRTCorrelated(correlatedCols);
		} catch (BoundariesUnsetException | DEBUGEXCEPTION e) {
			e.printStackTrace();
			DatabasePumper.closeEverything();
			System.exit(1);
		}
	}
	
	/**
	 * Identify the intervals, and put into each of them the number of fresh values to insert
	 * 
	 * 
	 * @param correlatedCols
	 * @throws DEBUGEXCEPTION 
	 * @throws InstanceNullException 
	 */
	@SuppressWarnings("rawtypes")
    private void identifyIntervals(
            List<CorrelatedColumnsList> correlatedCols) throws DEBUGEXCEPTION, InstanceNullException {
	    
	    class LocalUtils{
	        private Interval obtainIntersectionInterval(Interval g, Interval toAdd) throws DEBUGEXCEPTION{
	            Interval newInt = null; 
	            switch(g.getType()){
                    case BIGINT: case DOUBLE: 
                        newInt = new BigDecimalInterval(g.getKey() + "-" + toAdd.getKey(), g.getType(), 0); 
                        break;
                    case CHAR:
                        break;
                    case DATETIME:
                        newInt = new DatetimeInterval(g.getKey() + "-" + toAdd.getKey(), g.getType(), 0);
                        break;
                    case INT:
                        newInt = new IntInterval(g.getKey() + "-" + toAdd.getKey(), g.getType(), 0); 
                        break;
                    case LINESTRING:
                        break;
                    case LONGTEXT:
                        break;
                    case MULTILINESTRING:
                        break;
                    case MULTIPOLYGON:
                        break;
                    case POINT:
                        break;
                    case POLYGON:
                        break;
                    case TEXT:
                        break;
                    case VARCHAR:
                        newInt = new StringInterval(g.getKey() + "-" + toAdd.getKey(), g.getType(), 0);
                        break;
                    default:
                        break;
	                
	            }
	            if( newInt == null ){ 
	                throw new DEBUGEXCEPTION();
	            }
	            return newInt;
	        }

	        @SuppressWarnings("unchecked")
            void addNewIntervalToCPs(Interval toAdd) throws InstanceNullException {
	            String[] splits = toAdd.getKey().split("-");
	            for( String s : splits ){
	                String[] splits1 = s.split(".");
	                QualifiedName qF = new QualifiedName(splits1[0], splits1[1]);
	                ColumnPumper cP = DBMSConnection.getInstance().getSchema(qF.getTableName()).getColumn(qF.getColName());
	                cP.getIntervals().add(toAdd);
	            }
            }

            public void checkIfEmpty(
                    Interval toAdd, Interval curInt) throws InstanceNullException {
                String[] splits = toAdd.getKey().split("-");
                for( String s : splits ){
                    String[] splits1 = s.split(".");
                    QualifiedName qF = new QualifiedName(splits1[0], splits1[1]);
                    ColumnPumper cP = DBMSConnection.getInstance().getSchema(qF.getTableName()).getColumn(qF.getColName());
                    splits1 = curInt.getKey().split(".");
                    QualifiedName curQF = new QualifiedName(splits1[0], splits1[1]);
                    ColumnPumper curCP = DBMSConnection.getInstance().getSchema(curQF.getTableName()).getColumn(curQF.getColName());
                    // TODO findNElementsInRatio
                }
            }
	    }
	    
	    LocalUtils utils = new LocalUtils();
	    
	    for( CorrelatedColumnsList cCL : correlatedCols ){
	        Queue<Interval> groups = new LinkedList<Interval>();
	        // Add all intervals with a single column
	        for( int i = 0; i < cCL.size(); ++i ){
	            ColumnPumper cP = cCL.get(i);
	            groups.add(cP.getIntervals().get(0));
	        }
	        
	        while( groups.isEmpty() ){
	            Interval g = groups.poll();

	            // Add intervals with n columns
	            for( int i = 0; i < cCL.size(); ++i ){
	                ColumnPumper cP = cCL.get(i);           
	                Interval curInt = cP.getIntervals().get(0);
	                // If this combination has not been considered already
	                if( !g.getKey().contains(curInt.getKey()) ){
	                    Interval toAdd = utils.obtainIntersectionInterval(g, curInt);
	                    utils.checkIfEmpty(toAdd, curInt);
	                    utils.addNewIntervalToCPs(toAdd);
	                    groups.add(toAdd);
	                }
	            }
	        }
	    }
	}

    /**
	 * Update the boundaries of those columns in a correlated set
	 * @param correlatedCols
	 * @throws ValueUnsetException 
	 * @throws BoundariesUnsetException 
	 * @throws DEBUGEXCEPTION 
	 */
	private void updateColumnBoundsWRTCorrelated(
			List<CorrelatedColumnsList> correlatedCols) throws ValueUnsetException, BoundariesUnsetException, DEBUGEXCEPTION {
		
		for( CorrelatedColumnsList cCL : correlatedCols ){
			for( int i = 1; i < cCL.size(); ++i ){
				setInContiguousInterval(i, cCL);
				ColumnPumper referenced = cCL.get(i-1);
				ColumnPumper current = cCL.get(i);

				// Check if there is a fk constraint (all values are shared)
				QualifiedName refName = new QualifiedName(referenced.getSchema().getTableName(), referenced.getName());
				if( current.referencesTo().contains(refName) ){ 
					long minEncoding = referenced.getMinEncoding();
					// TODO Continua!!
					continue;
				}
//				Statistics.addInt("unskipped_correlated", 1);
				int numSharedFreshs = findNumSharedFreshsToInsert(current, referenced);
				long maxEncoding = referenced.getMaxEncoding();	
				if( maxEncoding != Long.MAX_VALUE){
					long newMinEncoding = maxEncoding - numSharedFreshs;
					current.updateMinValueByEncoding(maxEncoding - numSharedFreshs);
					current.updateMaxValueByEncoding(newMinEncoding + current.getNumFreshsToInsert());
				}
				else throw new DEBUGEXCEPTION();
			}
		}
		
	}

	private void setInContiguousInterval(int curIndex,
			CorrelatedColumnsList cCL) throws ValueUnsetException, BoundariesUnsetException {
		
		ColumnPumper current = cCL.get(curIndex);
		int numFreshs = current.getNumFreshsToInsert();
		
		long min = current.getMinEncoding();
		long max = current.getMaxEncoding();
		long intervalLength = max - min;
		
		// TODO Use an SMT Solver
	}

	private int findNumSharedFreshsToInsert(ColumnPumper col, ColumnPumper referenced) {
		int numSharedFreshs = 0;
		try {
			float sharedRatio = this.tStatsFinder.findSharedRatio(col, referenced);
			numSharedFreshs = (int) (col.getNumFreshsToInsert() * sharedRatio);
		} catch (SQLException | ValueUnsetException | InstanceNullException e) {
			e.printStackTrace();
			DatabasePumper.closeEverything();
			System.exit(1);
		}
		return numSharedFreshs;
	}
	
}

class CorrelatedColumnsList{
	
	// This is always sorted w.r.t. the number of fresh values to insert
	private LinkedList<ColumnPumper> columns;
	
	public CorrelatedColumnsList(){
		this.columns = new LinkedList<ColumnPumper>();
	}
	
	public int size(){
		return columns.size();
	}
	
	public ColumnPumper get(int index){
		return this.columns.get(index);
	}
	
	/**
	 * Insert cP in the list of correlated columns, while
	 * satisfying the ordering constraint on this.columns
	 * @param cP
	 */
	public void insert(ColumnPumper cP){
		try{
			int nFreshs = cP.getNumFreshsToInsert();
			if( this.columns.size() == 0 ){
				this.columns.add(cP);
			}
			for( int i = 0; i < this.columns.size(); ++i ){
				ColumnPumper el = this.columns.get(i);
				int elNFreshs = el.getNumFreshsToInsert();
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
	
	public boolean isInCorrelated(ColumnPumper cP){
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
	
	List<CorrelatedColumnsList> extractCorrelatedColumns() {
		List<CorrelatedColumnsList> result = null;
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
	 * from foreign keys.. ( what about transitive closure? ) 
	 * 
	 * @param qCorrelatedFields
	 * @throws InstanceNullException
	 */
	private void addForeignKeys(Queue<Set<Field>> qCorrelatedFields) throws InstanceNullException {
	    
	    for( Set<Field> sF : qCorrelatedFields ){
	        for( Field f : sF ){
	            
	            Queue<QualifiedName> correlated = new LinkedList<QualifiedName>();
	            correlated.add(new QualifiedName(f.tableName, f.colName));
	            
	            List<QualifiedName> correlatedMax = new ArrayList<QualifiedName>();
	            // Side effect 
	            correlatedThroughForeignKeys(correlated, correlatedMax);
	            
	            for( QualifiedName qN : correlatedMax ){
	                Field toInsert = new Field(qN.getTableName(), qN.getColName());
	                sF.add(toInsert); // Try to insert if new
	            }
	        }
	    }
    }

    private void correlatedThroughForeignKeys(Queue<QualifiedName> correlated, List<QualifiedName> result) throws InstanceNullException {
        QualifiedName current = correlated.poll();
        if( result.contains(current) ){
            correlatedThroughForeignKeys(correlated, result);  // Recursion
        }
        else{
            ColumnPumper cP = DBMSConnection.getInstance().getSchema(current.getTableName()).getColumn(current.getColName());
            correlated.addAll(cP.referencedBy());
            correlated.addAll(cP.referencesTo());
            result.add(current);
            correlatedThroughForeignKeys(correlated, result);  // Recursion
        }
    }

    private List<CorrelatedColumnsList> constructCorrelatedColumnsList(List<Set<Field>> maximalMerge) {
		
		List<CorrelatedColumnsList> result = new ArrayList<CorrelatedColumnsList>();
		
		for( Set<Field> correlatedColsList : maximalMerge ){
			CorrelatedColumnsList cCL = new CorrelatedColumnsList();
			for( Field f : correlatedColsList ){
				try {
					ColumnPumper cP = DBMSConnection.getInstance().getSchema(f.tableName).getColumn(f.colName);
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