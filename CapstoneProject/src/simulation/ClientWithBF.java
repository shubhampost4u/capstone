package simulation;

import java.math.BigInteger;

/**
 * Subclass of Client class with the bloom filter component
 * 
 * @author Shridhar Bhalekar
 *
 */
public class ClientWithBF extends Client {

	/** Array representing bloom filter */
	private int[] bloomFilter;
	
	private int bloomFilterSize;
	
	
	/**
	 * Create Object with cache size and client id
	 * @param cacheSize
	 * @param clientId
	 */
	public ClientWithBF(int cacheSize, int bloomFilterSize, long clientId) {
		super(cacheSize, clientId);
		bloomFilter = new int[bloomFilterSize];
		this.bloomFilterSize = bloomFilterSize;
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
