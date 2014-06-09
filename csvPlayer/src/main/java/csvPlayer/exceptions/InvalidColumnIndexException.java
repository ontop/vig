package main.java.csvPlayer.exceptions;

public class InvalidColumnIndexException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InvalidColumnIndexException() {
		super();
	};
	
	public InvalidColumnIndexException(String message){
		super(message);
	}
	
}
