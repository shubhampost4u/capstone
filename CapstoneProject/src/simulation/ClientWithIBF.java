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
	
	private int bloomFilterSize;
	
	private final int dataSizeLimit = 10;
	
	private final int M = 7;
	
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

	private int importanceFunction(String data) {
		if(data.length() < dataSizeLimit) {
			return (M/2);
		}
		return M;
	}
	
	public boolean cacheWarmUp(Block[] contents) {
		super.cacheWarmUp(contents);
		for(Block block : contents) {
			updateBF(block.getData());
		}
		return true;
	}
	
	public boolean isMember(String data) {
		int[] indexes = getHashIndexes(data);
		for(int i : indexes) {
			if(iBloomFilter[i] == 0) {
				return false;
			}
		}
		return true;
	}
	
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
}
