package simulation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Subclass of Client class with the importance aware bloom filter component
 * 
 * @author Shridhar Bhalekar
 *
 */
public class ClientWithIBF extends Client{

	/** Array representing importance aware bloom filter */
	private int[] iBloomFilter;
	
	/** Size of bloom filter */
	private int bloomFilterSize;
	
	/** Importance function criteria */
	private final int dataSizeLimit = 10;
	
	/** Maximum value that can be stored in bloom filter */
	private final int M = 7;
	
	/** Random indexes to be decremented by 1 */
	private final int P = 10;
	
	/**
	 * Create Object with cache size and client id
	 * @param cacheSize
	 * @param clientId
	 */
	public ClientWithIBF(int cacheSize, int bloomFilterSize, long clientId) {
		super(cacheSize, clientId);
		iBloomFilter = new int[cacheSize];
		this.bloomFilterSize = bloomFilterSize;
	}
	
	/**
	 * Fills the cache contents before starting the experiment
	 */
	public boolean cacheWarmUp(Block[] contents) {
		super.cacheWarmUp(contents);
		for(Block block : contents) {
			updateBF(block.getData());
		}
		return true;
	}
	
	/**
	 * Checks if the data is present in bloom filter. Get k indices by getting
	 * the k hash values of the data and then check if value at these k indices
	 * are non zero
	 * 
	 * @param data query to fired
	 */
	public boolean isMember(String data) {
		int[] indexes = getHashIndexes(data);
		for(int i : indexes) {
			if(iBloomFilter[i] == 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Updates the contents of cache and bloom filter. First checks if the 
	 * data is already present in cache. If not then decrement values at P
	 * random indices in bloom filter by 1 and set the k indices to importance
	 * value returned by importance function
	 * 
	 * @param block to be updated in cache
	 */
	public void updateBF(String block) {
		if(!isMember(block)) {
			List<Integer> pIndexes = new ArrayList<Integer>();
			while(pIndexes.size() != P) {
				Random random = new Random();
				int index = random.nextInt() % bloomFilterSize;
				if(index < 0)
					index *= (-1);
				if(!pIndexes.contains(index)) {
					pIndexes.add(index);
				}
			}
			for(int index : pIndexes) {
				if(iBloomFilter[index] >= 1) {
					iBloomFilter[index] -= 1;
				}
			}
			int[] indexes = getHashIndexes(block);
			for(int i : indexes) {
				if(iBloomFilter[i] < importanceFunction(block))
					iBloomFilter[i] = importanceFunction(block);
			}
		}
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

	/**
	 * Importance function which maps the data to importance value.
	 * 
	 * @param data content to be given the importance value
	 * @return importance value
	 */
	private int importanceFunction(String data) {
		if(data.length() < dataSizeLimit) {
			return (M/2);
		}
		return M;
	}

}
