package simulation;

/**
 * Subclass of Server class with the importance aware bloom filter component
 * @author Shridhar Bhalekar
 */
public class ServerWithIBF extends Server {

	/** Array representing importance aware bloom filter */
	private int[] iBloomFilter;
	
	/**
	 * Create Object with cache size, disk size and server id
	 * 
	 * @param cacheSize cache component size 
	 * @param diskSize disk component size
	 * @param serverId server id
	 */
	public ServerWithIBF(int cacheSize, int diskSize, long serverId) {
		super(cacheSize, diskSize, serverId);
		iBloomFilter = new int[cacheSize];
	}
	
}
