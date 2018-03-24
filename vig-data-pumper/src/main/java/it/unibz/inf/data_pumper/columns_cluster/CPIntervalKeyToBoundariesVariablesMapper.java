package it.unibz.inf.data_pumper.columns_cluster;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.utils.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import abstract_constraint_program.ACPLongVar;

class CPIntervalKeyToBoundariesVariablesMapper<VarType>{
    private Map<CPIntervalKey, Pair<ACPLongVar<VarType>,ACPLongVar<VarType>>> mIntervalsToBoundVars = new HashMap<>();

    void addVarsForInterval(CPIntervalKey cpIntervalKey, Pair<ACPLongVar<VarType>,ACPLongVar<VarType>> minMaxBounds){
	this.mIntervalsToBoundVars.put(cpIntervalKey, minMaxBounds);
    }

    Pair<ACPLongVar<VarType>, ACPLongVar<VarType>> getVarsForKey(CPIntervalKey key){
	return this.mIntervalsToBoundVars.get(key);
    }

    Set<CPIntervalKey> keySet() {
	return mIntervalsToBoundVars.keySet();
    }

    /**
     * 
     * @param cP
     * @return It returns all the CPIntervalKeys for the specified ColumnPumper cP
     */
    Set<CPIntervalKey> getKeySetForCP(ColumnPumper<?> cP){
	Set<CPIntervalKey> result = new HashSet<>();
	for( CPIntervalKey i : keySet() ){
	    if( i.getFullName().equals(cP.getQualifiedName().toString()) ){
		result.add(i);
	    }
	}
	return result;
    }
};
