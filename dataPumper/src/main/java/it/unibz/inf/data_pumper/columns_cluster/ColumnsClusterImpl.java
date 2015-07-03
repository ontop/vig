package it.unibz.inf.data_pumper.columns_cluster;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.columns.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.core.main.DebugException;
import it.unibz.inf.data_pumper.tables.QualifiedName;
import it.unibz.inf.data_pumper.utils.traversers.Node;
import it.unibz.inf.data_pumper.utils.traversers.ReachConnectedTraverser;
import it.unibz.inf.data_pumper.utils.traversers.Traverser;
import it.unibz.inf.data_pumper.utils.traversers.TraverserAbstractFactory;
import it.unibz.inf.data_pumper.utils.traversers.TraverserFactory;
import it.unibz.inf.data_pumper.utils.traversers.visitors.VisitorWithResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import abstract_constraint_program.AbstractConstraintProgram;
import abstract_constraint_program.ChocoConstraintProgram;

interface ColumnsClusterConstants{
    static final String ANONYMOUS_ID="AN";
}

public class ColumnsClusterImpl<T> extends ColumnsCluster<T> {

    private List<ColumnPumperInCluster<T>> columns;
    private TraverserAbstractFactory traverserFactory;
    
    
    public ColumnsClusterImpl(ColumnPumper<T> undecoratedCol) {
	this.columns = new ArrayList<>();
	ColumnPumperInCluster<T> decoratedCol = new ColumnPumperInCluster<T>(undecoratedCol, this);
	this.columns.add(decoratedCol);
	
	this.traverserFactory = new TraverserFactory();
	
	// Discover all nodes
	ReachConnectedTraverser fullTraverser = this.traverserFactory.makeReachConnectedTraverser(this.columns);
	EmptyVisitor visitor = this.traverserFactory.makeEmptyVisitor();
	
	fullTraverser.traverse(visitor);	
    }
    
    protected List<ColumnPumperInCluster<T>> getClusterCols(){
	return Collections.unmodifiableList(columns);
    }
    
    @Override
    public List<ColumnPumper<T>> getColumnPumpersInCluster(){
	List<ColumnPumper<T>> result = new ArrayList<>();
	for( ColumnPumperInCluster<T> col : columns ){
	    result.add(col.cP);
	}
	return Collections.unmodifiableList(result);
    }
    
    @Override
    public boolean hasMultiInterval() {
	boolean result = false;
	for( ColumnPumperInCluster<T> cP : columns ){
	    if( !cP.isSingleInterval() ){
		result = true;
		break;
	    }
	}
	return result;
    }

    @Override
    public void classicStrategy() {
	Queue<ColumnPumper<? extends Object>> toUpdateBoundaries = new LinkedList<ColumnPumper<? extends Object>>();
	for( ColumnPumperInCluster<T> cPIC : columns){
	    toUpdateBoundaries.add(cPIC.cP);
	}

	while( !toUpdateBoundaries.isEmpty() ){
	    ColumnPumper<? extends Object> first = toUpdateBoundaries.remove();
	    
	    if( first.getIntervals().size() != 1 ) continue;
	    
	    long firstMinEncoding = first.getIntervals().get(0).getMinEncoding();
	    for( QualifiedName referredName : first.referencesTo() ){
		ColumnPumper<? extends Object> referred = DBMSConnection.getInstance().getSchema(referredName.getTableName()).getColumn(referredName.getColName());
		long refMinEncoding = referred.getIntervals().get(0).getMinEncoding();
		if( firstMinEncoding > refMinEncoding ){
		    Interval<? extends Object> interval = first.getIntervals().get(0);
		    interval.updateMinEncodingAndValue(refMinEncoding);
		    interval.updateMaxEncodingAndValue(refMinEncoding + interval.getNFreshsToInsert());
		    // Update the boundaries for all the kids
		    for( QualifiedName kidName : first.referencedBy() ){
			ColumnPumper<? extends Object> kid = (ColumnPumper<? extends Object>) DBMSConnection.getInstance().getSchema(kidName.getTableName()).getColumn(kidName.getColName());
			toUpdateBoundaries.add(kid);
		    }
		}
	    }
	}
    }

    @Override
    public void adaptIntervalsFromMultiIntervalCols() {
	
	/**
	 * 
	 * Encapsulating visitors' creation in the scope 
	 * of a method will trigger the GC when the method finish, 
	 * since the visitor will be out of scope.
	 *
	 */
	class LocalUtils{
	    
	    Traverser listTraverser;
	    
	    LocalUtils(ColumnsClusterImpl<?> cCI){
		listTraverser = cCI.traverserFactory.makeListTraverser(cCI.columns);
	    }
	    
	    /** Find the maximum encoding in already set intervals **/	
	    long findMaximum(){
		
		MaxEncodingFinder maxFinder = new MaxEncodingFinder();
		listTraverser.traverse(maxFinder);
		return maxFinder.result();
	    }
	    List<SimpleIntervalKey> createIntervalKeys(int numAnonymousIntervals, long maxEncodingEncountered){
		
		List<SimpleIntervalKey> intervalKeys = new ArrayList<SimpleIntervalKey>();
		IntervalKeysCreatorVisitor intervalKeysCreator = new IntervalKeysCreatorVisitor(numAnonymousIntervals, maxEncodingEncountered, intervalKeys);
		listTraverser.traverse(intervalKeysCreator);
		return intervalKeys;
	    }
	    /** Create the variables for the CP program, and apply the common interval constraints **/
	    void createVariables(
		    AbstractConstraintProgram<IntVar, Constraint> constraintProgram, 
		    CPIntervalKeyToBoundariesVariablesMapper<IntVar> mIntervalsToBoundariesVars,
		    List<SimpleIntervalKey> intervalKeys){
		
		VarsCreatorVisitor<IntVar, Constraint> varsCreator = new VarsCreatorVisitor<>(constraintProgram, mIntervalsToBoundariesVars, intervalKeys);
		listTraverser.traverse(varsCreator);
	    }
	    
	    /** Apply the foreign-key related constraints **/
	    void createFkConstraints(AbstractConstraintProgram<IntVar, Constraint> constraintProgram, 
		    CPIntervalKeyToBoundariesVariablesMapper<IntVar> mIntervalsToBoundariesVars){
		
		ForeignKeysVarsSetterVisitor<IntVar, Constraint> fkConstrSetter = 
			new ForeignKeysVarsSetterVisitor<IntVar, Constraint>(constraintProgram, mIntervalsToBoundariesVars);
		listTraverser.traverse(fkConstrSetter);
	    }

	    public void transformResults(
		    CPIntervalKeyToBoundariesVariablesMapper<IntVar> mIntervalsToBoundariesVars) {
		
		listTraverser.traverse(new CPResultsToIntervalsVisitor<IntVar>(mIntervalsToBoundariesVars));
	    }
	}
	
	LocalUtils utils = new LocalUtils(this);
	
	int numAnonymousIntervals = 1; // TODO Some parameter of some sort
	
	CPIntervalKeyToBoundariesVariablesMapper<IntVar> mIntervalsToBoundariesVars = new CPIntervalKeyToBoundariesVariablesMapper<>();
	AbstractConstraintProgram<IntVar, Constraint> constraintProgram = new ChocoConstraintProgram();
	
	// Find the maximum encoding in already set intervals
	long maxEncodingEncountered = utils.findMaximum();
	
	// Create the list of IntervalKeys
	List<SimpleIntervalKey> intervalKeys = utils.createIntervalKeys(numAnonymousIntervals, maxEncodingEncountered);
	
	// Create the variables for the CP program, and apply the common interval constraints
	utils.createVariables(constraintProgram, mIntervalsToBoundariesVars, intervalKeys);
	
	// Apply the foreign-key related constraints
	utils.createFkConstraints(constraintProgram, mIntervalsToBoundariesVars);
	
//	System.err.println(constraintProgram.humanFormat());
	
	// Solve the program
	boolean hasSolution = constraintProgram.solve();
	
//	System.err.println(constraintProgram.humanFormat());
	
	if( !hasSolution && constraintProgram.hasReachedLimit() ){
	    throw new DebugException("The solver could not find a solution however there might be one");
	}
	
	assert hasSolution : "The constraint program does not have a solution" + constraintProgram.toString();
	
	// Transform the results to column intervals
	utils.transformResults(mIntervalsToBoundariesVars);
    }

    @Override
    void registerColumnPumper(ColumnPumper<T> cP) {
	if( !getColumnPumpersInCluster().contains(cP) ){
	    this.columns.add(new ColumnPumperInCluster<T>(cP, this));
	}
    }

    @Override
    ColumnPumperInCluster<T> getColumnPumperInClusterWrapping(ColumnPumper<T> cP) {
	ColumnPumperInCluster<T> result = null;
	if( getColumnPumpersInCluster().contains(cP) ){
	    for( ColumnPumperInCluster<T> inCluster : getClusterCols() ){
		if( inCluster.cP.equals(cP) ){
		    result = inCluster;
		    break;
		}
	    }
	}
	if( result == null ) throw new NullPointerException("The cluster does not contain the column" + cP);
	return result; 
    } 
}

class MaxEncodingFinder implements VisitorWithResult<Long>{

    private long maxEncoding = 0;
    
    @Override
    public void visit(Node node) {
	ColumnPumperInCluster<?> cPIC = (ColumnPumperInCluster<?>) node;
	if( !cPIC.isSingleInterval() ){
	    for( Interval<?> i : cPIC.cP.getIntervals() ){
		if( i.getMaxEncoding() > maxEncoding ){
		    maxEncoding = i.getMaxEncoding();
		}
	    }
	}
    }

    @Override
    public Long result() {
	return maxEncoding;
    }
}