package mappings;

import java.util.Map;

/**
 * It enriches the Tuple class with 
 * features like the duplicate ratio
 * @author tir
 *
 */
public class TupleTemplateDecorator {
	private Map<String, Float> mTableName_inDupR;
	private float dupR;
	
	private final TupleTemplate decorated;
	
	TupleTemplateDecorator(TupleTemplate tt){
		decorated = tt;
	}
	
	public TupleTemplate undecorate(){
		return decorated;
	}
	
	/**
	 * Associates the duplicate ratio <b>dupRatio</b> to the table <b>tableName<b>
	 * for <b>this</b> tuple
	 * @param tableName
	 * @param dupRatio
	 */
	public void setInDupR(String tableName, float dupRatio){
		mTableName_inDupR.put(tableName, dupRatio);
	}
	
	/**
	 * 
	 * @param tableName
	 * @return The duplicate ratio of <b>this</b> tuple template relative to
	 * table <b>tableName</b>
	 */
	public float getInDupR(String tableName){
		return mTableName_inDupR.get(tableName);
	}
	
	/**
	 * 
	 * @return The duplicate ratio for the whole relation w.r.t. <b>this</b> template
	 */
	public float getDupR() {
		return dupR;
	}

	public void setDupR(float dupR) {
		this.dupR = dupR;
	}
}
