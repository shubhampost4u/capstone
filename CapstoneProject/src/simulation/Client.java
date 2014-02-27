package simulation;

/**
 * Represents a client proxy or machine for simulation
 * @author Shridhar Bhalekar
 *
 */
public class Client {
	
	/** Cache of the client */
	protected Storage cache;
	
	/** Id for this client */
	protected long clientId;
	
	/** Size of the cache */
	protected int cacheSize;
	
	/**
	 * Create Object with cache size and id
	 * 
	 * @param cacheSize size of the cache component
	 * @param clientId id for this client
	 */
	public Client(int cacheSize, long clientId) {
		this.cache = new Cache(cacheSize);
		this.cacheSize = cacheSize;
		this.clientId = clientId;
	}
	
	/**
	 * Fills the cache component before starting the experiment
	 * 
	 * @param contents array of data blocks to be inserted in the cache
	 * @return true/false
	 */
	public boolean cacheWarmUp(Block[] contents) {
		if(this.cacheSize != contents.length) {
			return false;
		}
		cache.fillStorageBlocks(contents);
		return true;
	}
	
	/**
	 * Locate the data in the cache blocks
	 * 
	 * @param data to be located in the cache
	 * @return true/false
	 */
	public boolean cacheLookup(String data) {
		// addd up lookup cost
		return false;
	}
	
	/**
	 * Get the client id
	 * @return client id
	 */
	public long getClientId() {
		return this.clientId;
	}
}
