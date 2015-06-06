package it.unibz.inf.data_pumper.column_types.aggregate_types.constraintProgram;

import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.utils.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import abstract_constraint_program.ACPLongVar;

public class CPIntervalKeyToBoundariesVariablesMapper<VarType>{
    private Map<CPIntervalKey, Pair<ACPLongVar<VarType>,ACPLongVar<VarType>>> mIntervalsToBoundVars = new HashMap<>();

    public void addVarsForInterval(CPIntervalKey cpIntervalKey, Pair<ACPLongVar<VarType>,ACPLongVar<VarType>> minMaxBounds){
	this.mIntervalsToBoundVars.put(cpIntervalKey, minMaxBounds);
    }

    public Pair<ACPLongVar<VarType>, ACPLongVar<VarType>> getVarsForInterval(String intervalKey){
	Pair<ACPLongVar<VarType>,ACPLongVar<VarType>> result = mIntervalsToBoundVars.get(intervalKey);
	return (Pair<ACPLongVar<VarType>, ACPLongVar<VarType>>) result;
    }
    
    public Pair<ACPLongVar<VarType>, ACPLongVar<VarType>> getVarsForKey(CPIntervalKey key){
	return this.mIntervalsToBoundVars.get(key);
    }

    public Set<CPIntervalKey> keySet() {
	return mIntervalsToBoundVars.keySet();
    }
    
    /**
     * 
     * @param cP
     * @return It returns all the CPIntervalKeys for the specified ColumnPumper cP
     */
    public Set<CPIntervalKey> getKeySetForCP(ColumnPumper<?> cP){
	Set<CPIntervalKey> result = new HashSet<>();
	for( CPIntervalKey i : keySet() ){
	    if( i.getColName().equals(cP.getName()) ){
		result.add(i);
	    }
	}
	return result;
    }
};
