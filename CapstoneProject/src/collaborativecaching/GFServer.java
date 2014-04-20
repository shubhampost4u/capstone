package collaborativecaching;

import simulation.Block;

/**
 * Sub class of CachingServer class to form server used in Greedy Forwarding
 * algorithm. Apart from having the generic CachingServer functionalities,
 * this class performs additional functionalities needed by the Greedy 
 * Forwarding algorithm.
 *  
 * @author Shridhar Bhalekar
 */
public class GFServer extends CachingServer {

	/**
	 * Creates Object of Greedy Forwarding Server
	 * 
	 * @param serverId id of server
	 * @param cacheSize size of server cache
	 * @param diskSize size of server disk
	 * @param cacheReferenceTicks ticks required to reference server cache
	 * @param diskToCacheTicks ticks required to reference server disk
	 * @param networkHopTicks ticks required to send request over network
	 */
	public GFServer(long serverId, int cacheSize, int diskSize,
			int cacheReferenceTicks, int diskToCacheTicks, int networkHopTicks) {
		super(serverId, cacheSize, diskSize, cacheReferenceTicks, diskToCacheTicks,
				networkHopTicks);
	}
	
	/**
	 * Overridden method of Caching Server class to update the local cache.
	 * Block with the minimum LRU count is replaced with this new block. 
	 * 
	 * @param data block to be updated
	 */
	public void updateCache(Block data) {
		int min = MAX_LRU_COUNT;
		int minIndex = -1;
		for(int i = 0; i < cacheSize; i++) {
			if(cacheLRUCount[i] < min) {
				min = cacheLRUCount[i];
				minIndex = i;
			}
		}
		if(min > -1 && min < 10) {
			cache.update(minIndex, data);
			cacheLRUCount[minIndex] = MAX_LRU_COUNT;
		}
	}
		
	@Override
	public boolean isMember(String data) {
		return cache.lookup(data) == -1;
	}
}
