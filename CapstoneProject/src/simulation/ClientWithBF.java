package simulation;

/**
 * Subclass of Client class with the bloom filter component
 * 
 * @author Shridhar Bhalekar
 *
 */
public class ClientWithBF extends Client {

	/** Array representing bloom filter */
	private int[] bloomFilter;
	
	/**
	 * Create Object with cache size and client id
	 * @param cacheSize
	 * @param clientId
	 */
	public ClientWithBF(int cacheSize, long clientId) {
		super(cacheSize, clientId);
		bloomFilter = new int[cacheSize];
	}
	
	/**
	 * Locate the data in the cache blocks
	 * 
	 * @param data to be located in the cache
	 * @return true/false
	 */
	public boolean cacheLookup(String data) {
		return false;
	}
	
	/**
	 * Fill the bloom filter according to cache contents
	 */
	public void fillBloomFilter() {
		
	}
}
