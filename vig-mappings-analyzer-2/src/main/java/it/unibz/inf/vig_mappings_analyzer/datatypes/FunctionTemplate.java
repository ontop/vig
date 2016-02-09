package it.unibz.inf.vig_mappings_analyzer.datatypes;

import static org.junit.Assert.assertEquals;
import it.unibz.inf.vig_mappings_analyzer.core.exceptions.WrongArityException;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Variable;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class FunctionTemplate{
    private String templateString;
    private List<Argument> arguments = new ArrayList<>(); 
    private int arity;
    private boolean isURI;
  
    private static FunctionTemplate isa;
    
    public FunctionTemplate(Function f){
	
	templateString = cleanURIFromVariables(f.toString());
	arity = isURI() ? f.getArity() -1 : f.getArity(); 
    }
    
    public FunctionTemplate(Variable v){
	// Untyped datatype, e.g. {pipName}
	templateString = "{}";
	arity = 1;
    }
    
    private FunctionTemplate(){
	templateString = "";
	arity = 0;
    }
    
    /**
     * 
     * @return An empty instance marker for an ISA. This instance is UNIQUE.
     */
    public static FunctionTemplate makeISA(){
	isa = isa == null ? new FunctionTemplate() : isa;
	return isa;
    }

    public String getTemplateString(){
	return this.templateString;
    }

    boolean isUriOrDatatype(){
	return templateString != null;
    }

    public void addArgument(Argument arg) throws WrongArityException{
	if( this.arity == arguments.size() ) throw new WrongArityException("Trying to add an argument " + arg.toString() + "beyond the term arity.");
	this.arguments.add(arg);
    }

    public boolean hasArgumentOfIndex(int index){
	if( arguments.size() > index ) return true;
	return false;
    }

    public Argument getArgumentOfIndex(int index){
	return this.arguments.get(index);
    }

    public int getArity(){
	return this.arity;
    }

    @Override
    public boolean equals(Object other) {
	boolean result = true;
	if( other instanceof FunctionTemplate ){
	    FunctionTemplate that = (FunctionTemplate) other;
	    boolean sameTemplate = this.templateString.equals(that.templateString);
	    boolean sameArity = this.arity == that.arity;
	    if( sameTemplate && sameArity ){
		for( int i = 0; i < this.arguments.size(); ++i ){
		    Argument thisArg = this.arguments.get(i);
		    Argument thatArg = ((FunctionTemplate) other).arguments.get(i);
		    if( !thisArg.hasSameFillingFields(thatArg) ){
			result = false;
			break;
		    }
		}
	    }
	    else result = false;
	}
	else result = false;
	return result;
    }

    public boolean isURI(){
	return this.isURI;
    }

    @Override
    public int hashCode() {
	return templateString.hashCode();
    };

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("\nTEMPLATE: " + this.templateString.toString() + "\n");
	builder.append("ARGS: " + this.arguments.toString());
	return builder.toString();
    }

    /**
     * 
     * @param uri
     * @return It removes the variable names from the URI or the datatype, so that then it is 
     *         sufficient to do string equality in order to understand whether the terms can join.
     */
    private String cleanURIFromVariables(String uri){
	// URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}",wlbNpdidWellbore,wlbCoreNumber), 
	// http://www.w3.org/2001/XMLSchema#decimal(wlbCoreIntervalBottomFT)
	String result = null;

	if( uri.startsWith("URI(") ){
	    this.isURI = true;
	    int begin = uri.indexOf("\"") + 1;
	    int end = uri.lastIndexOf("\"");
	    result = uri.substring(begin, end);
	}
	else{
	    this.isURI = false;
	    int endIndex = uri.indexOf("(");
	    String prefix = uri.substring(0, endIndex);
	    result = prefix + "()";
	}
	return result;
    }
    
    public static class TestFunctionTemplate{

	FunctionTemplate emptyF = new FunctionTemplate();

	@Test
	public void testCleanURIFromVariables(){
	    String result = emptyF.
		    cleanURIFromVariables("URI(\"http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}\",wlbNpdidWellbore,wlbCoreNumber)");
	    assertEquals("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/core/{}", result);

	    result = emptyF.cleanURIFromVariables("http://www.w3.org/2001/XMLSchema#decimal(wlbCoreIntervalBottomFT)");
	    assertEquals("http://www.w3.org/2001/XMLSchema#decimal()", result);
	}
    };
  
};