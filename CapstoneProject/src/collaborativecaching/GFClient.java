package collaborativecaching;

import simulation.Block;

/**
 * Sub class of CachingClient class to form clients used in Greedy Forwarding
 * algorithm. Apart from having the generic CachingClient functionalities,
 *  this class performs additional functionalities needed by the Greedy 
 *  Forwarding algorithm.
 *  
 * @author Shridhar Bhalekar
 */
public class GFClient extends CachingClient {

	/**
	 * Creates Object of Greedy Forwarding client
	 * 
	 * @param clientId id of this client
	 * @param cacheSize cache size of client cache
	 * @param cacheReferenceTicks ticks required for referencing the cache
	 * @param networkHopTicks ticks required to forward request over network
	 * @param server server object reference in the system
	 */
	public GFClient(long clientId, int cacheSize, int cacheReferenceTicks,
			int networkHopTicks, CachingServer server) {
		super(clientId, cacheSize, cacheReferenceTicks, networkHopTicks, server);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Overridden method of Caching Client class to update the local cache.
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
			Block ret = cache.getBlock(minIndex);
			cache.update(minIndex, data);
			cacheLRUCount[minIndex] = MAX_LRU_COUNT;
			server.updateCache(ret);
		}
	}
	
	/**
	 * Check for the data in client cache
	 */
	@Override
	public boolean hasMember(String data) {
		return cache.lookup(data) == -1;
	}
}
