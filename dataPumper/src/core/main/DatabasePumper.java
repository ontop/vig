package core.main;

public abstract class DatabasePumper {
	
	protected boolean pureRandom = false;
	
	public abstract void pumpDatabase(float percentage, String fromTable);
	public abstract void pumpDatabase(float percentage);
	
	public void setPureRandomGeneration() {
		pureRandom = true;
	}
}
