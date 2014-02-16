package utils;

public class TrivialQueue<T> {
	private Entry<T> first;
	private Entry<T> last;
		
	public TrivialQueue(){
		first = null;
		last = null;
	}
	
	public boolean hasNext(){
		return first.hasNext();
	}
	
	public void enqueue(T value){
		
		Entry<T> newEntry = new Entry<T>(value);
		
		if( first == null ){
			first = new Entry<T>();
			first.setNext(newEntry);
			last = newEntry;
			return;
		}
		
		last.setNext(newEntry); 
		last = last.getNext();
	}
	
	public T dequeue(){
		
		first = first.getNext();
		
		return first.getValue();
	}
	
	public String toString(){
		
		StringBuilder temp = new StringBuilder();
		
		temp.append("[");
		
		Entry<T> current = first;
		
		while( current.hasNext() ){
			temp.append(current.getNext().getValue().toString());
			temp.append(", ");
			current = current.getNext();
		}
		temp.delete(temp.lastIndexOf(","), temp.length());
		temp.append("]");
		
		return temp.toString();
	}
};

class Entry<T>{
	private final T value;
	private Entry<T> next;
	
	Entry(){
		value = null;
		next = null;
	}
	
	Entry(T element){
		value = element;
		next = null;
	}
	
	T getValue(){
		return value;
	}
	
	void setNext(Entry<T> next){
		this.next = next;
	}
	
	boolean hasNext(){
		return this.next != null;
	}
	
	Entry<T> getNext(){
		return next;
	}
};