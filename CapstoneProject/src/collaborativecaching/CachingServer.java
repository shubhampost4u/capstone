package collaborativecaching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import simulation.Block;
import simulation.Server;

/**
 * Sub class of server class to be used for the comparison of cooperative 
 * caching algoritms like NChance, Greedy Forwarding, Robinhood, Summary Cache
 * 
 * @author Shridhar Bhalekar
 *
 */
public abstract class CachingServer extends Server {

	/** Total clients in the system */
	protected int nClients;
	
	/** Ticks required for cache reference */
	protected int cacheReferenceTicks;
		
	/** Ticks required for transferring data over network */
	protected int networkHopTicks;
	
	/** Ticks required for server disk reference */
	protected int diskToCacheTicks;
	
	/** List containing the set of hash values of each client cache */
	protected List<Set<Integer>> clientContents;
	
	/** Array of clients in the system */
	protected CachingClient[] clients;
	
	/** Minimum LRU count */
	protected static final int MIN_LRU_COUNT = 1;
	
	/** Maximum LRU count */
	protected static final int MAX_LRU_COUNT = 10;
		
	/** LRU count of cache blocks */
	protected int[] cacheLRUCount;
	
	/**
	 * Server object used in analyzing the Caching Algorithms
	 *   
	 * @param serverId id of client
	 * @param cacheSize size of server cache
	 * @param diskSize size of server disk
	 * @param cacheReferenceTicks ticks required to reference cache
	 * @param diskToCacheTicks ticks required to reference disk
	 * @param networkHopTicks ticks required to send block over network
	 */
	public CachingServer(long serverId, int cacheSize, int diskSize, 
			int cacheReferenceTicks, int diskToCacheTicks, int networkHopTicks)
	{
		super(cacheSize, diskSize, serverId);
		this.cacheReferenceTicks = cacheReferenceTicks;
		this.diskToCacheTicks = diskToCacheTicks;
		this.networkHopTicks = networkHopTicks;
		clientContents = new ArrayList<Set<Integer>>();
		cacheLRUCount = new int[cacheSize];
	}

	/**
	 * Update the client references.
	 * 
	 * @param clients references to clients in system
	 */
	public void updateClients(CachingClient[] clients) {
		nClients = clients.length;
		this.clients = new CachingClient[nClients];
		for(int i = 0; i < nClients; i++) {
			clientContents.add(new HashSet<Integer>());
			this.clients[i] = clients[i];
		}
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
	 * Records the contents of each client cache 
	 */
	public void updateClientContents() {
		for(int i = 0; i < nClients; i++) {
			CachingClient client = clients[i];
			for(int j = 0; j < client.getCacheSize(); j++) {
				clientContents.get(i).add(client.getCacheBlock(j).getData().
						hashCode());
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
	 * 
	 * @return boolean to represent status  
	 */
	public boolean requestData(int ticksPerRequest, int cacheMiss, 
			int localCacheHit, int globalCacheHit, CachingClient requester,
			String block) {
		int hash = block.hashCode(); 
		// check the block in each client cache
		for(int i = 0; i < nClients; i++) {
			if(requester.getClientId() != clients[i].getClientId() &&
					clientContents.get(i).contains(hash)) {
				ticksPerRequest += networkHopTicks;
				if(clients[i].requestData(ticksPerRequest, cacheMiss, 
						localCacheHit, globalCacheHit, requester, block, true)) {
					return true;
				}
			}
		}
		ticksPerRequest += cacheReferenceTicks;
		int index = cacheLookup(block);
		// reduce the LRU count of each cache element after cache reference
		for(int i = 0; i < cacheLRUCount.length; i++) {
			if(i != index && cacheLRUCount[i] > 0) {
				cacheLRUCount[i] -= 1;
			}
		}
		if(index != -1) {
			globalCacheHit += 1;
			ticksPerRequest += networkHopTicks;
			if(cacheLRUCount[index] != MAX_LRU_COUNT) {
				cacheLRUCount[index] += 1;
			}
			requester.setResponse(cache.getBlock(index), ticksPerRequest,
					cacheMiss, localCacheHit, globalCacheHit);
			return true;
		}
		cacheMiss += 1;
		ticksPerRequest += diskToCacheTicks;
		index = disk.lookup(block);
		if(index != -1) {
			requester.setResponse(disk.getBlock(index), ticksPerRequest,
					cacheMiss, localCacheHit, globalCacheHit);
			requester.updateCache(disk.getBlock(index));
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
