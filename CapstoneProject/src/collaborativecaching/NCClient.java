package collaborativecaching;

import simulation.Block;

/**
 * Sub class of CachingClient class to form clients used in NChance algorithm.
 * Apart from having the generic CachingClient functionalities, this class 
 * performs additional functionalities needed by the NChance algorithm.
 *  
 * @author Shridhar Bhalekar
 */
public class NCClient extends CachingClient {

	/** Singlet recirculation count */
	private static final int reCirculationCount = 2;
	
	/**
	 * Creates Object of NChance client
	 * 
	 * @param clientId id of this client
	 * @param cacheSize cache size of client cache
	 * @param cacheReferenceTicks ticks required for referencing the cache
	 * @param networkHopTicks ticks required to forward request over network
	 * @param server server object reference in the system
	 */
	public NCClient(long clientId, int cacheSize, int cacheReferenceTicks,
			int networkHopTicks, CachingServer server) {
		super(clientId, cacheSize, cacheReferenceTicks, networkHopTicks, server);
		// TODO Auto-generated constructor stub
	}

	/**
	 * This method will be called by the NCServer to update the singlet 
	 * forwarded by another client.
	 * 
	 * @param data block to be updated in this client cache.
	 */
	public void updateSinglet(Block data) {
		int min = MAX_LRU_COUNT;
		int minIndex = -1;
		// check for duplicate blocks in system and replace that block
		for(int i = 0; i < cacheSize; i++) {
			if(cacheLRUCount[i] < min && !((NCServer)server).
					checkSinglet(cache.getBlock(i), this)) {
				min = cacheLRUCount[i];
				minIndex = i;
			}
		}
		// if no duplicate block in this cache then replace singlet
		if(minIndex == -1) {
			min = MAX_LRU_COUNT;
			for(int i = 0; i < cacheSize; i++) {
				if(cacheLRUCount[i] <= min) {
					min = cacheLRUCount[i];
					minIndex = i;
				}
			}
		}
		cache.update(minIndex, data);
		cacheLRUCount[minIndex] = MAX_LRU_COUNT;
	}
	
	@Override
	/**
	 * This method id overridden method of Caching Client class to update the 
	 * local cache. This method keeps track of singlets while updating the
	 * local cache. If the block to be updated is singlet then that block is
	 * forwarded to another client and replaced from local cache. 
	 * 
	 * @param data block to be updated
	 */
	public void updateCache(Block data) {
		int min = MAX_LRU_COUNT;
		int minIndex = -1;
		boolean forwardedSinglet = false;
		// get the min from the LRU list
		for(int i = 0; i < cacheSize; i++) {
			if(cacheLRUCount[i] < min) {
				min = cacheLRUCount[i];
				minIndex = i;
			}
		}
		if(min >= 0 && min < 10) {
			Block ret = cache.getBlock(minIndex);
			// check if block is singlet and forward accordingly
			if(((NCServer)server).checkSinglet(ret, this)) {
				if(ret.recirculationCount == -1) {
					ret.recirculationCount = NCClient.reCirculationCount;
					((NCServer) server).forwardSinglet(ret, this);
					forwardedSinglet = true;
				} else if(ret.recirculationCount > 0) {
					ret.recirculationCount -= 1;
					if(ret.recirculationCount != 0) {
						((NCServer) server).forwardSinglet(ret, this);
						forwardedSinglet = true;
					}
				}
			} else {
				ret.recirculationCount = -1;
			}
			cache.update(minIndex, data);
			cacheLRUCount[minIndex] =  MAX_LRU_COUNT;
			if(!forwardedSinglet) {
				server.updateCache(ret);
			}
		}
	}
	
	/**
	 * This method is called by another client/server to request the data from
	 * the system of collaborative caches.
	 * 
	 * @param ticksPerRequest ticks associated with the current request
	 * @param cacheMiss cache miss associated with the current request
	 * @param cacheHit cache hit associated with the current request
	 * @param requester client who requested the data
	 * @param block data requested
	 * @param sentByServer flag to determine if the request is sent by the 
	 * 		  server
	 * @return boolean to represent status  
	 */
	public boolean requestData(int ticksPerRequest, int cacheMiss, 
			int localCacheHit, int globalCacheHit, CachingClient requester,
			String block, boolean sentByServer) {
		int index = cacheLookup(block);
		ticksPerRequest += cacheReferenceTicks;
		if(requester == null) {
			requester = this;
		}
		decrementCacheLRU(index);
		// if found in current client cache the return to requester
		if(index != -1) {
			ticksPerRequest += networkHopTicks;
			if(requester.getClientId() == this.clientId)
				localCacheHit += 1;
			else
				globalCacheHit += 1;
			if(cacheLRUCount[index] != MAX_LRU_COUNT) {
				cacheLRUCount[index] += 1;
			}
			if(cache.getBlock(index).recirculationCount > -1) {
				cache.getBlock(index).recirculationCount = 
						NCClient.reCirculationCount; 
			}
			requester.setResponse(cache.getBlock(index), ticksPerRequest,
					cacheMiss, localCacheHit, globalCacheHit);
		} else {
			if (sentByServer) {
				server.updateClientContents();
				return false;
			}
			ticksPerRequest += networkHopTicks;
			return server.requestData(ticksPerRequest, cacheMiss, localCacheHit,
					globalCacheHit, requester, block);
		}
		return true;
	}

	/**
	 * Check for the data in client cache
	 */
	@Override
	public boolean hasMember(String data) {
		return (cacheLookup(data) != -1);
	}

}
