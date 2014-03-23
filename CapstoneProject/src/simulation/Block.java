package simulation;

/**
 * Represents data block in a cache or disk
 * 
 * @author Shridhar Bhalekar
 */
public class Block implements Comparable<Block>{
	/** Actual data in the block */
	private String data;
	
	public int recirculationCount;
	
	public boolean isSinglet;
	/**
	 * Creates object storing data
	 * 
	 * @param data Actual data to be stored
	 */
	public Block(String data) {
		this.data = data;
	}
	
	/**
	 * Getter for data
	 * 
	 * @return Block data
	 */
	public String getData() {
		return data;
	}
	
	/**
	 * Compares two data block by comparing actual data in the blocks
	 * 
	 * @param block Block to be compared with the current block
	 * 
	 * @return true/false
	 */
	public boolean isEqual(String block) {
		if(this.data.equals(block))
			return true;
		return false;
	}
	
	/**
	 * Overridden method to display contents
	 */
	@Override
	public String toString() {
		return this.data;
	}

	@Override
	public int compareTo(Block block) {
		return this.data.compareTo(block.getData());
	}
}
