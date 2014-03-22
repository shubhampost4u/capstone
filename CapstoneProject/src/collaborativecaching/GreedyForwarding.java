package collaborativecaching;

import java.util.List;
import java.util.Random;

import simulation.Block;

/**
 * Greedy forwarding algorithm
 * @author Shridhar Bhalekar
 *
 */
public class GreedyForwarding extends CachingAlgorithm {

	private double ticksPerRequest;
	
	private double cacheHitPerRequest;
	
	private double cacheMissPerRequest;
	
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
		clients = new CachingClient[nClients];
		server = new CachingServer(1, serverCacheSize, serverDiskSize,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		for(int i = 0; i < nClients; i++) {
			clients[i] = new CachingClient(i, clientCacheSize, 
					cacheReferenceTicks, networkHopTicks, (CachingServer)server);
			clients[i].cacheWarmUp(clientCaches[i]);
		}
		((CachingServer) server).updateClients((CachingClient[])clients);
		((CachingServer) server).updateClientContents();
		server.cacheWarmUp(serverCache);
		server.diskWarmUp(serverDisk);
	}
	
	public double getTicksPerRequest() {
		return ticksPerRequest;
	}
	
	public double getCacheMiss() {
		return cacheMissPerRequest;
	}
	
	public double getCacheHit() {
		return cacheHitPerRequest;
	}
	
	@Override
	public void executeExperiment(List<String> requests) {
		int cacheHit = 0;
		int cacheMiss = 0;
		int ticks  = 0;
		for(String request : requests) {
			CachingClient client = ((CachingClient)clients
					[new Random().nextInt(nClients)]); 
			client.requestData(0, 0, 0, null, request, false);
			ticks += client.getResponseCost();
			cacheMiss += client.getCacheMiss();
			cacheHit += client.getCacheHit();
		}
		ticksPerRequest = (double) ticks / (double)requests.size();
		cacheMissPerRequest = (double) cacheMiss / (double) requests.size();
		cacheHitPerRequest = (double) cacheHit / (double) requests.size();
	}
}
