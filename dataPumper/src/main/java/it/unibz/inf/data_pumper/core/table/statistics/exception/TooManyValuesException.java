package it.unibz.inf.data_pumper.core.table.statistics.exception;

public class TooManyValuesException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6386582676442383302L;
	
	
	public TooManyValuesException(String msg){
		super(msg);
	}
}
