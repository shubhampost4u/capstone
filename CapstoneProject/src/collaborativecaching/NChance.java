package collaborativecaching;

import simulation.Block;

/**
 * Class to implement the NChance algorithm by creating a system of clients and 
 * server.
 * 
 * @author Shridhar Bhalekar
 */
public class NChance extends CachingAlgorithm {

	/** 
	 * Create object for executing N-chance cooperative caching algorithm
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
	public NChance(int nClients, int clientCacheSize, int serverCacheSize,
			int serverDiskSize,int cacheReferenceTicks,
			int diskToCacheTicks, int networkHopTicks) {
		super(nClients, clientCacheSize, serverCacheSize, serverDiskSize,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
	}

	/**
	 * Overridden method of CachingAlgorithm class to distribute data to clients
	 * and server in the system. This is an initial configuration of the system
	 * 
	 * @param clientCaches data to be inserted in client caches
	 * @param serverCache data to be inserted in server cache
	 * @param serverDisk data to be inserted in server disk
	 */
	@Override
	public void warmup(Block[][] clientCaches, Block[] serverCache,
			Block[] serverDisk) {
		clients = new NCClient[nClients];
		server = new NCServer(1, serverCacheSize, serverDiskSize,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		for(int i = 0; i < nClients; i++) {
			clients[i] = new NCClient(i, clientCacheSize, 
					cacheReferenceTicks, networkHopTicks, (NCServer)server);
			clients[i].cacheWarmUp(clientCaches[i]);
		}
		((NCServer) server).updateClients((NCClient[])clients);
		((NCServer) server).updateClientContents();
		server.cacheWarmUp(serverCache);
		server.diskWarmUp(serverDisk);
	}
}
