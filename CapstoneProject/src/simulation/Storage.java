package simulation;

/**
 * Represents storage component of client and server
 * @author Shridhar Bhalekar
 *
 */
public class Storage {
	/** Storage blocks to be allocated */
	protected Block[] storageSpace;
	
	/**
	 * Creates object with storage size
	 * 
	 * @param storageSize
	 */
	public Storage(int storageSize) {
		storageSpace = new Block[storageSize];
	}
	
	/**
	 * Lookup the data in storage array
	 * 
	 * @param data to be looked up
	 * @return true/false
	 */
	public int lookup(String data) {
		for(int i = 0; i < storageSpace.length; i++) {
			if(storageSpace[i].isEqual(data)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Takes in an array of blocks and fills the underlying storage
	 * @param contents actual blocks to be filled in storage
	 */
	public void fillStorageBlocks(Block[] contents) {
		for(int i = 0; i < contents.length; i++) {
			storageSpace[i] = contents[i];
		}
	}
	
	public void update(int index, Block block) {
		storageSpace[index] = block;
	}
	
	public Block getBlock(int index) {
		return storageSpace[index];
	}
}
