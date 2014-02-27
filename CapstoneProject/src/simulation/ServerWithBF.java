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
	public ServerWithBF(int cacheSize, int diskSize, long serverId) {
		super(cacheSize, diskSize, serverId);
		bloomFilter = new int[cacheSize];
	}
	
}
