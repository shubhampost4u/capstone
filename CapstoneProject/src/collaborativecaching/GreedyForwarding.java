package collaborativecaching;

import simulation.Client;
import simulation.Server;

/**
 * Greedy forwarding algorithm
 * @author Shridhar Bhalekar
 *
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
			int serverCacheSize, int serverDiskSize, int totalRequests,
			int cacheReferenceTicks, int diskToCacheTicks, int networkHopTicks) {
		super(nClients, clientCacheSize, serverCacheSize, serverDiskSize,
				totalRequests, cacheReferenceTicks, diskToCacheTicks,
				networkHopTicks);
		clients = new Client[nClients];
		server = new Server(serverCacheSize, serverDiskSize, 1);
	}

	@Override
	public void runAlgorithm() {
		// TODO Auto-generated method stub
		
	}

}
