/**
 * 
 */
package collaborativecaching;

import simulation.Block;

/**
 * Sub class of CachingClient class to form clients used in Robinhood algorithm.
 * Apart from having the generic CachingClient functionalities, this class 
 * performs additional functionalities needed by the Robinhood algorithm.
 *  
 * @author Shridhar Bhalekar
 */
public class RHClient extends CachingClient {

	/** Singlet recirculation count */
	private static final int reCirculationCount = 2;

	/**
	 * @param clientId
	 * @param cacheSize
	 * @param cacheReferenceTicks
	 * @param networkHopTicks
	 * @param server
	 */
	public RHClient(long clientId, int cacheSize, int cacheReferenceTicks,
			int networkHopTicks, CachingServer server) {
		super(clientId, cacheSize, cacheReferenceTicks, networkHopTicks, server);
	}

	/**
	 * Update the singlet block in the client cache
	 * 
	 * @param singlet to be updated in the client
	 * @param victimChunkId id of the block to be removed
	 */
	public void updateSinglet(Block singlet, int victimChunkId) {
		Block victimBlock = cache.getBlock(victimChunkId);
		cache.update(victimChunkId, singlet);
		// singlet.clients.add(this.clientId);
		// victimBlock.clients.remove(this.clientId);
		((RHServer) server).updateBlockClient(victimBlock);
		server.updateCache(victimBlock);
	}

	/**
	 * Overloaded method to update block according to the cache LRU count
	 * 
	 * @param singlet to be updated
	 */
	public void updateSinglet(Block singlet) {
		int min = MAX_LRU_COUNT;
		int minIndex = -1;
		// check for duplicate blocks in system and replace that block
		for (int i = 0; i < cacheSize; i++) {
			if (cacheLRUCount[i] < min
					&& !((RHServer) server).checkSinglet(cache.getBlock(i),
							this)) {
				min = cacheLRUCount[i];
				minIndex = i;
			}
		}
		// if no duplicate block in this cache then replace singlet
		if (minIndex == -1) {
			min = MAX_LRU_COUNT;
			for (int i = 0; i < cacheSize; i++) {
				if (cacheLRUCount[i] <= min) {
					min = cacheLRUCount[i];
					minIndex = i;
				}
			}
		}
		cache.update(minIndex, singlet);
		((RHServer) server).updateBlockClient(cache.getBlock(minIndex));
		cacheLRUCount[minIndex] = MAX_LRU_COUNT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see collaborativecaching.CachingClient#updateCache(simulation.Block)
	 */
	@Override
	public void updateCache(Block data) {
		int min = MAX_LRU_COUNT;
		int minIndex = -1;
		boolean forwardedSinglet = false;
		// get the min from the LRU list
		for (int i = 0; i < cacheSize; i++) {
			if (cacheLRUCount[i] < min) {
				min = cacheLRUCount[i];
				minIndex = i;
			}
		}
		Block ret = cache.getBlock(minIndex);
		if (min >= 0 && min < 10) {
			// check if block is singlet and forward accordingly
			if (((RHServer) server).checkSinglet(ret, this)) {
				int victimChunkId = ((RHServer) server).selectVictim(this);
				if (victimChunkId == -1) {
					forwardedSinglet = switchToNChance(ret);
				} else {
					forwardedSinglet = 
							continueRobinhood(ret, victimChunkId);
				}
			} else {
				ret.recirculationCount = RHClient.reCirculationCount;
			}
			cache.update(minIndex, data);
			cacheLRUCount[minIndex] = MAX_LRU_COUNT;
			if (!forwardedSinglet) {
				server.updateCache(ret);
			}
			((RHServer) server).updateBlockClient(data);
			((RHServer) server).updateBlockClient(ret);
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
						RHClient.reCirculationCount; 
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see simulation.Client#isMember(java.lang.String)
	 */
	@Override
	public boolean hasMember(String data) {
		return (cacheLookup(data) != -1);
	}
	
	/**
	 * Method to change the Robinhood algorithm to NChance if server cannot 
	 * find a victim client/block
	 * 
	 * @param ret to be recirculated if found to be a singlet 
	 * @return
	 */
	private boolean switchToNChance(Block ret) {
		if(ret.recirculationCount == -1) {
			ret.recirculationCount = RHClient.reCirculationCount;
			((RHServer) server).nChanceForward(ret, this);
			return true;
		} else if(ret.recirculationCount > 0) {
			ret.recirculationCount -= 1;
			if(ret.recirculationCount != 0) {
				((RHServer) server).nChanceForward(ret, this);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Execute the Robinhood algorithm if the server is successful in finding
	 * victim client and block
	 * 
	 * @param singlet block to be updated as a singlet
	 * @param victimChunkId index of victim chunk
	 * @return
	 */
	private boolean continueRobinhood(Block singlet, int victimChunkId) {
		if (singlet.recirculationCount > 0) {
			singlet.recirculationCount -= 1;
			((RHServer) server).victimClient.updateSinglet(singlet,
					victimChunkId);
			return true;
		} else if (singlet.recirculationCount < 0) {
			singlet.recirculationCount = RHClient.reCirculationCount;
			((RHServer) server).victimClient.updateSinglet(singlet,
					victimChunkId);
			return true;
		}
		return false;
	}
}
