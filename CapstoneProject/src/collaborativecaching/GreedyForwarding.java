package collaborativecaching;

import java.util.List;

import simulation.Block;

/**
 * Class which implements the Greedy forwarding algorithm by creating a system
 * of clients and server.
 * 
 * @author Shridhar Bhalekar
 */
public class GreedyForwarding extends CachingAlgorithm {

	/** 
	 * Create object for executing greedy forwarding cooperative caching
	 *  algorithm
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
	public GreedyForwarding(int nClients, int clientCacheSize,
			int serverCacheSize, int serverDiskSize, int cacheReferenceTicks,
			int diskToCacheTicks, int networkHopTicks) {
		super(nClients, clientCacheSize, serverCacheSize, serverDiskSize,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
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
		clients = new GFClient[nClients];
		server = new GFServer(1, serverCacheSize, serverDiskSize,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		for(int i = 0; i < nClients; i++) {
			clients[i] = new GFClient(i, clientCacheSize, 
					cacheReferenceTicks, networkHopTicks, (GFServer)server);
			clients[i].cacheWarmUp(clientCaches[i]);
		}
		((GFServer) server).updateClients((GFClient[])clients);
		((GFServer) server).updateClientContents();
		server.cacheWarmUp(serverCache);
		server.diskWarmUp(serverDisk);
	}
	
	/**
	 * Overridden method to execute greedy forwarding
	 */
	public void executeExperiment(List<String> requests) {
		System.out.println("Executing GreedyForwarding for " + nClients + 
				" cacheSize = "+ clientCacheSize + " diskSize = " + 
				serverDiskSize);
		super.executeExperiment(requests);
	}
}
