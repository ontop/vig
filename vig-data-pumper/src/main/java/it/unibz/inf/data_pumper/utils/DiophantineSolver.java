package it.unibz.inf.data_pumper.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DiophantineSolver {
        
    public List<Integer> solve(DiophantineLinearEquation eq){
	
	// Termination condition 1
	if( eq.getArity() == 2 ){
	    DiophantineLinearBinaryEquation result = 
		    new DiophantineLinearBinaryEquation(eq.getCoeff(1), eq.getCoeff(2));
	    return null; // TODO Return result for binary
	}
	
	// Termination condition 2
	if( eq.containsUnaryCoefficient() ){
	    // TODO
	}
	
	// Try to reduce
	// 1) Find the maximum coefficient
	eq.orderCoefficients();
	
	// 2) Find some divisor
	int divisorIndex = eq.getIndexCandidateForDivision();
	
	// 3 q = a_1 / a_i ; r = a_1 mod a_i
	
	int a_1 = eq.getCoeff(1);
	int a_i = eq.getCoeff(eq.getCoeff(divisorIndex));
		
	int q = a_1 / a_i;
	int r = a_1 % a_i;
	
	int a_1Prime = a_i;
	int a_iPrime = r;
	
	DiophantineLinearEquation eq1;
	
	return null;
    }
    
   
    
}

// ---- Datatypes ---- //

final class DiophantineLinearEquation{
    private List<Integer> coeffs;
    private List<Integer> vars;
    
    DiophantineLinearEquation(List<Integer> coeffs){
	this.coeffs = coeffs;
    }
    
    public DiophantineLinearEquation getCopy(){
	
	List<Integer> coeffsNew = new ArrayList<>();
	coeffsNew.addAll(coeffs);
	
	DiophantineLinearEquation result = new DiophantineLinearEquation(coeffsNew);
	
	return result;
    }
    
    public int getIndexCandidateForDivision() {
	
	// Warning: The coefficients must be ordered 
	assert getIndexMaxCoeff() == 1 : "Method orderCoefficients() has to be invoked first";
	
	for( int i = 1; i < coeffs.size(); ++i ){
	    if( this.coeffs.get(0) % this.coeffs.get(i) == 0 ){
		return this.coeffs.get(i);
	    }
	}
	
	return this.coeffs.get(1);
    }

    public boolean containsUnaryCoefficient() {
	// TODO Auto-generated method stub
	return false;
    }

    int getCoeff(int index){
	
	assert index > 0 : "Indexes start from 1";
	
	return coeffs.get(index -1);
    }
    
    int getArity(){
	return coeffs.size();
    }
    
    int getIndexMaxCoeff(){
	int max = 0;
	int index = 0;
	
	for( int i = 0; i < coeffs.size(); ++i ){
	    if( coeffs.get(i) > max ){
		max = coeffs.get(i);
		index = i + 1;
	    }
	}
	
	return index;
    }

    /**
     * Descending order
     */
    public void orderCoefficients(){
	Collections.sort(this.coeffs, new Comparator<Integer>() {
	    
	    @Override
	    public int compare(Integer a, Integer b){
		if( a.intValue() > b.intValue() ) return -1;
		if( a.intValue() == b.intValue() ) return 0;
		return 1;
	    }
	});
    }
    
}

final class DiophantineLinearBinaryEquation{
    
    private int coeff1;
    private int coeff2;
    
    private int var1;
    private int var2;
    
    DiophantineLinearBinaryEquation(int coeff1, int coeff2) {
	this.coeff1 = coeff1;
	this.coeff2 = coeff2;
    }
    
    int getFirstCoeff(){
	return this.coeff1;
    }
    
    int getSecCoeff(){
	return this.coeff2;
    }
}