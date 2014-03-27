package basicDatatypes;

public class QualifiedName {
	private String tableName;
	private String colName;
	
	public QualifiedName(){}
	
	public QualifiedName(String tableName, String colName){
		this.tableName = tableName;
		this.colName = colName;
	}
	
	public QualifiedName(String csvName){
		// "\\s+"
		String[] splits = csvName.split(" ");
		tableName = splits[0];
		colName = splits[1];
	}
		
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	
	public String toString(){
		return tableName + "." + colName;
	}
}
