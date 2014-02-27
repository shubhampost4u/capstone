package bloomfilters;

import simulation.Block;
import simulation.Client;
import simulation.ClientWithBF;
import simulation.Server;
import simulation.ServerWithBF;

/**
 * Implements the bloom filter algorithm
 * @author Shridhar Bhalekar
 *
 */
public class BloomFilter {
	/** Total clients in the system */
	protected int nClients;
	
	/** Cache size of each client */
	protected int clientCacheSize;
	
	/** Cache size of server */
	protected int serverCacheSize;
	
	/** disk size of server */
	protected int serverDiskSize;
	
	/** Ticks required for cache reference */
	protected int cacheReferenceTicks;
	
	/** Ticks required for disk reference */
	protected int diskToCacheTicks;
	
	/** Ticks required for transferring data over network */
	protected int networkHopTicks;
	
	/** Total requests to be handled*/
	protected int totalRequests;
	
	/** Clients to form the network */
	protected Client[] clients;
	
	/** Server to form network */
	protected Server server;
	
	/** 
	 * Create object for executing bloom filter algorithm
	 * 
	 * @param nClients total clients
	 * @param clientCacheSize client cache size
	 * @param serverCacheSize server cache size
	 * @param serverDiskSize server disk size
	 * @param totalRequests total requests in the server
	 * @param cacheReferenceTicks ticks for cache reference
	 * @param diskToCacheTicks ticks for disk reference
	 * @param networkHopTicks ticks for network transfer
	 */
	public BloomFilter(int nClients, int clientCacheSize, int serverCacheSize,
			int serverDiskSize, int totalRequests, int cacheReferenceTicks,
			int diskToCacheTicks, int networkHopTicks) {
		this.nClients = nClients;
		this.clientCacheSize = clientCacheSize;
		this.serverCacheSize = serverCacheSize;
		this.serverDiskSize = serverDiskSize;
		this.totalRequests = totalRequests;
		this.cacheReferenceTicks = cacheReferenceTicks;
		this.diskToCacheTicks = diskToCacheTicks;
		this.networkHopTicks = networkHopTicks;
	}

	/**
	 * Fill the clients/server cache and disk before executing the experiment
	 * 
	 * @param clientCaches data to be inserted in client caches
	 * @param serverCache data to be inserted in server cache
	 * @param serverDisk data to be inserted in server disk
	 */
	public void warmup(Block[][] clientCaches, Block[] serverCache,
			Block[] serverDisk) {
		clients = new Client[nClients];
		server = new ServerWithBF(serverCacheSize, serverDiskSize, 1);
		server.cacheWarmUp(serverCache);
		server.diskWarmUp(serverDisk);
		for (int i = 0; i < nClients; i++) {
			clients[i] = new ClientWithBF(clientCacheSize, i);
			clients[i].cacheWarmUp(clientCaches[i]);
		}
	}

	/**
	 * Get the false positives encountered during the experiment
	 * @return false positives
	 */
	public int getFalsePositives() {
		int falsePositives = 0;
		return falsePositives;
	}
}
