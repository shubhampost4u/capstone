package collaborativecaching;

import java.util.Random;

import simulation.Block;
import simulation.Client;

public abstract class CachingClient extends Client{

	/** Ticks required for cache reference */
	protected int cacheReferenceTicks;
		
	/** Ticks required for transferring data over network */
	protected int networkHopTicks;
	
	/** CachingServer object in the system */
	protected CachingServer server;
	
	/** Response of the cache block query */
	protected Block response;
	
	/** Total ticks required for getting the response */
	protected int responseCost;
	
	/** Total Cache Hit for the request */
	protected int cacheHit;
	
	/** Total cache miss for the request */
	protected int cacheMiss;
	
	/** LRU count of cache blocks */
	protected int[] cacheLRUCount;
	
	/** Minimum LRU count */
	protected static final int MIN_LRU_COUNT = 1;
	
	/** Maximum LRU count */
	protected static final int MAX_LRU_COUNT = 10;
	
	/**
	 * Client object used in analyzing the Caching Algorithms
	 *   
	 * @param clientId id of client
	 * @param cacheSize size of client cache
	 * @param cacheReferenceTicks ticks required to reference cache
	 * @param networkHopTicks ticks required to send block over network
	 * @param server CachingServer object
	 */
	public CachingClient(long clientId, int cacheSize, int cacheReferenceTicks,
			int networkHopTicks, CachingServer server) {
		super(cacheSize, clientId);
		this.cacheReferenceTicks = cacheReferenceTicks;
		this.networkHopTicks = networkHopTicks;
		this.server = server;
		this.cacheLRUCount = new int[cacheSize];
	}

	/**
	 * Initialize client cache by putting data into cache
	 * 
	 * @param contents data to be transferred to client cache
	 */
	public boolean cacheWarmUp(Block[] contents) {
		super.cacheWarmUp(contents);
		Random random = new Random();
		for(int i = 0; i < cacheSize; i++) {
			cacheLRUCount[i] = random.nextInt((MAX_LRU_COUNT - MIN_LRU_COUNT)
					+ 1) + MIN_LRU_COUNT;
		}
		return true;
	}
	
	/**
	 * Set the client response to a request along with the response attributes
	 * like ticks, cache hit and cache miss. 
	 *  
	 * @param block response block
	 * @param cost ticks for the response
	 * @param cacheMiss cache miss count for the response
	 * @param cacheHit cache hit count for the response
	 */
	public void setResponse(Block block, int cost, int cacheMiss, int cacheHit)
	{
		response = block;
		responseCost = cost;
		this.cacheMiss = cacheMiss;
		this.cacheHit = cacheHit;
	}
	
	/**
	 * Method to return the query response
	 * @return response block
	 */
	public Block getResponse() {
		return response;
	}
	
	/**
	 * Method to return the ticks required for the query response.
	 * @return ticks
	 */
	public int getResponseCost() {
		return responseCost;
	}
	
	/**
	 * Method to return the total cache hit happened to get query response
	 * @return total cache hit count
	 */
	public int getCacheHit() {
		return cacheHit;
	}
	
	/**
	 * Method to return the total cache miss happened to get query response
	 * @return total cache miss count
	 */
	public int getCacheMiss() {
		return cacheMiss;
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
			int cacheHit, CachingClient requester, String block,
			boolean sentByServer) {
		int index = cacheLookup(block);
		ticksPerRequest += cacheReferenceTicks;
		if(requester == null) {
			requester = this;
		}
		// reduce the LRU count of each cache element after cache reference
		for(int i = 0; i < cacheLRUCount.length; i++) {
			if(i != index && cacheLRUCount[i] > 0) {
				cacheLRUCount[i] -= 1;
			}
		}
		// if found in current client cache the return to requester
		if(index != -1) {
			ticksPerRequest += networkHopTicks;
			cacheHit += 1;
			if(cacheLRUCount[index] != MAX_LRU_COUNT) {
				cacheLRUCount[index] += 1;
			}
			requester.setResponse(cache.getBlock(index), ticksPerRequest,
					cacheMiss, cacheHit);
		} else {
			if(sentByServer) {
				server.updateClientContents();
				return false;
			}
			ticksPerRequest += networkHopTicks;
			return server.requestData(ticksPerRequest, cacheMiss, cacheHit,
					requester, block);
		}
		return true;
	}
	
	/**
	 * Method to update the least recently used cache block.
	 * 
	 * @param data block to be updated
	 */
	public abstract void updateCache(Block data);
}
