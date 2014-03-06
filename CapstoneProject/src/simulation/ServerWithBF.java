package simulation;

/**
 * Subclass of Server class with the bloom filter component
 * @author Shridhar Bhalekar
 *
 */
public class ServerWithBF extends Server{

	/** Array representing bloom filter */
	private int[] bloomFilter;
	
	
	/**
	 * Create Object with cache size, disk size and client id
	 * 
	 * @param cacheSize size of cache component
	 * @param diskSize size of disk component
	 * @param serverId server id
	 */
	public ServerWithBF(int cacheSize, int bloomFilterSize,
			int diskSize, long serverId) {
		super(cacheSize, bloomFilterSize, diskSize, serverId);
		bloomFilter = new int[cacheSize];
	}
	
	
	public boolean isMember(String data) {
		int[] result = getHashIndexes(data);
		for(int i : result) {
			if(bloomFilter[i] == 0) {
				return false;
			}
		}
		return true;
	}
	
	public boolean cacheWarmUp(Block[] contents) {
		super.cacheWarmUp(contents);
		
		for(Block block : contents) {
			int[] indexes = getHashIndexes(block.getData());
			for(int i : indexes) {
				bloomFilter[i] = 1;
			}
		}
		return true;
	}
}
