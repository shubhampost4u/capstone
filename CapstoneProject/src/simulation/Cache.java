package simulation;

/**
 * Represents Cache component for client and server
 * @author Shridhar Bhalekar
 *
 */
public class Cache extends Storage {
	
	/**
	 * Create object with cache size
	 * @param cacheSize
	 */
	public Cache(int cacheSize) {
		super(cacheSize);
	}
		
	/**
	 * Replace block from cache at particular index
	 * 
	 * @param index index position for block replacement
	 * @param block block to be replace with
	 */
	public void replaceBlock(int index, Block block) {
		storageSpace[index] = block;
	}
}
