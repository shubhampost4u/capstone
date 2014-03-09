package simulation;

import java.math.BigInteger;

/**
 * Subclass of Server class with the bloom filter component
 * @author Shridhar Bhalekar
 *
 */
public class ServerWithBF extends Server{

	/** Array representing bloom filter */
	private int[] bloomFilter;
	
	/** Size of bloom filter */
	private int bloomFilterSize;
	
	/**
	 * Create Object with cache size, disk size and client id
	 * 
	 * @param cacheSize size of cache component
	 * @param diskSize size of disk component
	 * @param serverId server id
	 */
	public ServerWithBF(int cacheSize, int bloomFilterSize,
			int diskSize, long serverId) {
		super(cacheSize, diskSize, serverId);
		bloomFilter = new int[cacheSize];
		this.bloomFilterSize = bloomFilterSize;
	}
	
	/**
	 * Checks if the data is present in bloom filter. Get k indices by getting
	 * the k hash values of the data and then check if value at these k indices
	 * are non zero
	 * 
	 * @param data query to fired
	 */
	public boolean isMember(String data) {
		int[] result = getHashIndexes(data);
		for(int i : result) {
			if(bloomFilter[i] == 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Fills the cache contents before starting the experiment
	 */
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
	
	/**
	 * Gets the hash values of block by giving this data as input to k hash
	 * functions
	 *  
	 * @param block to be stored in cache
	 * @return array of indices in bloom filter
	 */
	private int[] getHashIndexes(String block) {
		int[] indexes = new int[5];
		BigInteger bfSize = 
				new BigInteger(new Integer(bloomFilterSize).toString());
		DataHash hash = new DataHash(block);
		indexes[0] = hash.sha1().mod(bfSize).intValue();
		indexes[1] =  hash.md5().mod(bfSize).intValue();
		indexes[2] = hash.sha256().mod(bfSize).intValue();
		indexes[3] = hash.sha384().mod(bfSize).intValue();
		indexes[4] = hash.djb2().mod(bfSize).intValue();

		return indexes;
	}
}
