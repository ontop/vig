package it.unibz.inf.vig_mappings_analyzer.datatypes;

//import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Argument{
    private Set<Field> fillingFields;

    public Argument(){
	this.fillingFields = new HashSet<Field>();
    }

    public void addFillingField(Field field){
	this.fillingFields.add(field);
    }

    public Set<Field> getFillingFields(){
	// return Collections.unmodifiableSet(fillingFields);
	return fillingFields;
    }

    /**
     * It returns true if this.fillingFields.equals(other.fillingFields)
     * @param other
     * @return
     */
    public boolean hasSameFillingFields(Argument other){
	boolean result = this.fillingFields.equals(other.fillingFields);
	return result;
    }

    @Override
    public String toString() {
	return fillingFields.toString();
    }
};
