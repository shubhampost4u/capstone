/**
 * 
 */
package collaborativecaching;

import simulation.Block;

/**
 * /**
 * Sub class of CachingServer class to form server used in NChance algorithm.
 * Apart from having the generic CachingServer functionalities, this class 
 * performs additional functionalities needed by the NChance algorithm.
 * 
 * @author Shridhar Bhalekar
 *
 */
public class SCServer extends CachingServer {

	/**
	 * Create object of Summary Cache Server
	 * @param serverId
	 * @param cacheSize
	 * @param diskSize
	 * @param cacheReferenceTicks
	 * @param diskToCacheTicks
	 * @param networkHopTicks
	 */
	public SCServer(long serverId, int cacheSize, int diskSize,
			int cacheReferenceTicks, int diskToCacheTicks, int networkHopTicks) {
		super(serverId, cacheSize, diskSize, cacheReferenceTicks,
				diskToCacheTicks, networkHopTicks);
	}

	/**
	 * This method is called by another client to request the data from
	 * the system of collaborative caches.
	 * 
	 * @param ticksPerRequest
	 *            ticks associated with the current request
	 * @param cacheMiss
	 *            cache miss associated with the current request
	 * @param localCacheHit
	 *            local cache hit associated with the current request
	 * @param globalCacheHit
	 *            global cache hit associated with the current request
	 * @param requester
	 *            client who requested the data
	 * @param block
	 *            data requested
	 * @param sentByServer
	 *            flag to determine if the request is sent by the server
	 * @return boolean to represent status
	 */
	public boolean requestData(int ticksPerRequest, int cacheMiss,
			int localCacheHit, int globalCacheHit, CachingClient requester,
			String block, boolean sentByServer) {
		int index = cacheLookup(block);
		ticksPerRequest += cacheReferenceTicks;
		if(index != -1) {
			globalCacheHit += 1;
			ticksPerRequest += networkHopTicks;
			if(cacheLRUCount[index] != MAX_LRU_COUNT) {
				cacheLRUCount[index] += 1;
			}
			requester.setResponse(cache.getBlock(index), ticksPerRequest,
					cacheMiss, localCacheHit, globalCacheHit);
			return true;
		} else {
			cacheMiss += 1;
			ticksPerRequest += diskToCacheTicks;
			index = disk.lookup(block);
			requester.setResponse(disk.getBlock(index), ticksPerRequest,
					cacheMiss, localCacheHit, globalCacheHit);
			requester.updateCache(disk.getBlock(index));
			return false;
		}
	}
	/* (non-Javadoc)
	 * @see collaborativecaching.CachingServer#updateCache(simulation.Block)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see simulation.Server#isMember(java.lang.String)
	 */
	@Override
	public boolean isMember(String data) {
		return cache.lookup(data) == -1;
	}

}
