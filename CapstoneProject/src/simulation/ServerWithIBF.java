package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Subclass of Server class with the importance aware bloom filter component
 * @author Shridhar Bhalekar
 */
public class ServerWithIBF extends Server {

	/** Array representing importance aware bloom filter */
	private int[] iBloomFilter;
	
	private final int dataSizeLimit = 10;
	
	private final int M = 7;
	
	private final int P = 10;
	
	/**
	 * Create Object with cache size, disk size and server id
	 * 
	 * @param cacheSize cache component size 
	 * @param diskSize disk component size
	 * @param serverId server id
	 */
	public ServerWithIBF(int cacheSize, int bloomFilterSize,
			int diskSize, long serverId) {
		super(cacheSize, bloomFilterSize, diskSize, serverId);
		iBloomFilter = new int[cacheSize];
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
