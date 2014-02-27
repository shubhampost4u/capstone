package simulation;

/**
 * Represents a server for simulation
 * 
 * @author Shridhar Bhalekar
 *
 */
public class Server {
	
	/** Cache component of server */
	protected Storage cache;
	
	/** Disk component of server */
	protected Storage disk;
	
	/** Server cache size */
	protected int cacheSize;
	
	/** Server disk size */
	protected int diskSize;
	
	/** Server id */
	protected long serverId;
	
	/**
	 * Creates a server object with cache size, disk size and id
	 * 
	 * @param cacheSize size of cache component
	 * @param diskSize size of disk component
	 * @param serverId server id
	 */
	public Server(int cacheSize, int diskSize, long serverId) {
		this.cacheSize = cacheSize;
		this.diskSize = diskSize;
		this.serverId = serverId;
		cache = new Cache(cacheSize);
		disk = new Storage(diskSize);
	}
	
	/**
	 * Fill the cache with data blocks before starting the experiment
	 * 
	 * @param contents actual data blocks to be filled in cache
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
	 * Fill the disk with data blocks before starting the experiment
	 * 
	 * @param contents actual data blocks to be filled in disk
	 * @return true/false
	 */
	public boolean diskWarmUp(Block[] contents) {
		if(this.diskSize != contents.length) {
			return false;
		}
		disk.fillStorageBlocks(contents);
		return true;
	}
	
	/**
	 * Locate the data in the cache blocks
	 * 
	 * @param request data to be located in the cache
	 * @return true/false
	 */
	public boolean cacheLookup(String request) {
		// lookup operation, add access ticks 
		return false;
	}
	
	/**
	 * Locate the data in the disk blocks
	 * 
	 * @param request data to be located in the cache
	 * @return true/false
	 */
	public boolean diskLookup(String request) {
		// lookup operation add ticks
		return false;
	}
	
	/**
	 * Get server id
	 * @return server id
	 */
	public long getServerId() {
		return this.serverId;
	}
	
}
