package it.unibz.inf.data_pumper.column_types.aggregate_types;

import it.unibz.inf.data_pumper.basic_datatypes.QualifiedName;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.aggregate_types.constraintProgram.CPIntervalKeyToBoundariesVariablesMapper;
import it.unibz.inf.data_pumper.column_types.aggregate_types.constraintProgram.ForeignKeysVarsSetterVisitor;
import it.unibz.inf.data_pumper.column_types.aggregate_types.constraintProgram.SimpleIntervalKey;
import it.unibz.inf.data_pumper.column_types.aggregate_types.constraintProgram.IntervalKeysCreatorVisitor;
import it.unibz.inf.data_pumper.column_types.aggregate_types.constraintProgram.VarsCreatorVisitor;
import it.unibz.inf.data_pumper.column_types.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.utils.traversers.Node;
import it.unibz.inf.data_pumper.utils.traversers.ReachConnectedTraverser;
import it.unibz.inf.data_pumper.utils.traversers.Traverser;
import it.unibz.inf.data_pumper.utils.traversers.TraverserAbstractFactory;
import it.unibz.inf.data_pumper.utils.traversers.TraverserFactory;
import it.unibz.inf.data_pumper.utils.traversers.visitors.CollectVisitedVisitor;
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
    
    
    @SuppressWarnings("unchecked")
    public ColumnsClusterImpl(ColumnPumper<T> undecoratedCol) {
	this.columns = new ArrayList<>();
	ColumnPumperInCluster<T> decoratedCol = new ColumnPumperInCluster<T>(undecoratedCol, this);
	this.columns.add(decoratedCol);
	
	this.traverserFactory = new TraverserFactory();
	
	// Discover all nodes
	ReachConnectedTraverser fullTraverser = this.traverserFactory.makeReachConnectedTraverser(this.columns);
	CollectVisitedVisitor visitor = this.traverserFactory.makeCollectVisitedVisitor();
	
	fullTraverser.traverse(visitor);
		
	this.columns = (List<ColumnPumperInCluster<T>>) visitor.result();
	
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
	
	int numAnonymousIntervals = 1; // TODO Some parameter of some sort
	
	Traverser listTraverser = this.traverserFactory.makeListTraverser(this.columns);
	
	CPIntervalKeyToBoundariesVariablesMapper<IntVar> mIntervalsToBoundariesVars = new CPIntervalKeyToBoundariesVariablesMapper<>();
	AbstractConstraintProgram<IntVar, Constraint> constraintProgram = new ChocoConstraintProgram();
	
	// Find the maximum encoding in already set intervals
	MaxEncodingFinder maxFinder = new MaxEncodingFinder();
	listTraverser.traverse(maxFinder);
	
	// Create the list of IntervalKeys
	List<SimpleIntervalKey> intervalKeys = new ArrayList<SimpleIntervalKey>();
	IntervalKeysCreatorVisitor intervalKeysCreator = new IntervalKeysCreatorVisitor(numAnonymousIntervals, maxFinder.result(), intervalKeys);
	listTraverser.traverse(intervalKeysCreator);
	
	// Create the variables for the CP program, and apply the common interval constraints
	VarsCreatorVisitor<IntVar, Constraint> varsCreator = new VarsCreatorVisitor<>( constraintProgram, mIntervalsToBoundariesVars, intervalKeys);
	listTraverser.traverse(varsCreator);
	
	// Apply the foreign-key related constraints
	ForeignKeysVarsSetterVisitor<IntVar, Constraint> fkConstrSetter = new ForeignKeysVarsSetterVisitor<>();
	listTraverser.traverse(fkConstrSetter);
	
	// Solve the program
	constraintProgram.solve();
	constraintProgram.prettyOut(); // TODO Pick the result, and transform it in intervals
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