package simulation;

/**
 * Subclass of Client class with the importance aware bloom filter component
 * 
 * @author Shridhar Bhalekar
 *
 */
public class ClientWithIBF extends Client{

	/** Array representing importance aware bloom filter */
	private int[] iBloomFilter;
	
	/**
	 * Create Object with cache size and client id
	 * @param cacheSize
	 * @param clientId
	 */
	public ClientWithIBF(int cacheSize, long clientId) {
		super(cacheSize, clientId);
		iBloomFilter = new int[cacheSize];
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
	 * Fill the importance aware bloom filter according to cache contents
	 */
	public void fillBloomFilter() {
		
	}
}
