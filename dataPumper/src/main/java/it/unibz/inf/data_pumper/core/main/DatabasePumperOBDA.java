package it.unibz.inf.data_pumper.core.main;

import it.unibz.inf.data_pumper.basic_datatypes.QualifiedName;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.column_types.exceptions.ValueUnsetException;
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

public class DatabasePumperOBDA extends DatabasePumperDB {
	
	// Aggregated classes
	private CorrelatedColumnsExtractor cCE;
		
	public DatabasePumperOBDA() {
		try {
			JoinableColumnsFinder jCF = new JoinableColumnsFinder(Conf.getInstance().mappingsFile());
			this.cCE = new CorrelatedColumnsExtractor(jCF);
		} catch (Exception e) {
			e.printStackTrace();
			DatabasePumperOBDA.closeEverything();
		}
	}
	
	@Override
	protected void establishColumnBounds(List<ColumnPumper> listColumns) throws ValueUnsetException{
		for( ColumnPumper cP : listColumns ){
			cP.fillDomainBoundaries(cP.getSchema(), dbOriginal);
		}
		// At this point, each column is initialized with statistical information
		// like null, dups ratio, num rows and freshs to insert, etc.
		
		
		// search for correlated columns and order them by fresh values to insert
		List<CorrelatedColumnsList> correlatedCols = this.cCE.extractCorrelatedColumns();
	
		// sono gia in ordine, il primo NON va aggiornato
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