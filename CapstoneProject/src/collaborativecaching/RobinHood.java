package collaborativecaching;

import java.util.List;

import simulation.Block;

/**
 * Class to implement the Robinhood algorithm by creating a system of clients 
 * and server.
 * 
 * @author Shridhar Bhalekar
 */
public class RobinHood extends CachingAlgorithm{

	/** 
	 * Create object for executing Robinhood cooperative caching algorithm
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
	public RobinHood(int nClients, int clientCacheSize, int serverCacheSize,
			int serverDiskSize, int cacheReferenceTicks,
			int diskToCacheTicks, int networkHopTicks) {
		super(nClients, clientCacheSize, serverCacheSize, serverDiskSize,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
	}

	@Override
	public void warmup(Block[][] clientCaches, Block[] serverCache,
			Block[] serverDisk) {
		clients = new RHClient[nClients];
		server = new RHServer(1, serverCacheSize, serverDiskSize,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		for(int i = 0; i < nClients; i++) {
			clients[i] = new RHClient(i, clientCacheSize, 
					cacheReferenceTicks, networkHopTicks, (RHServer)server);
			clients[i].cacheWarmUp(clientCaches[i]);
		}
		((RHServer) server).updateClients((RHClient[])clients);
		((RHServer) server).updateClientContents();
		server.cacheWarmUp(serverCache);
		server.diskWarmUp(serverDisk);
		((RHServer) server).updateDataClients();
	}
	
	/**
	 * Overridden method to execute Robinhood
	 */
	public void executeExperiment(List<String> requests) {
		System.out.println("Executing Robinhood for " + nClients + 
				" cacheSize = "+ clientCacheSize + " diskSize = " + 
				serverDiskSize);
		super.executeExperiment(requests);
	}
}
