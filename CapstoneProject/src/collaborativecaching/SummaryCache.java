package collaborativecaching;

import java.util.List;

import bloomfilters.ClientWithIBF;
import bloomfilters.ServerWithIBF;
import simulation.Block;
import simulation.Client;

/**
 * Summary Cache with Importance Aware Bloom Filter
 * @author Shridhar Bhalekar
 *
 */
public class SummaryCache extends CachingAlgorithm {

	private int bloomFilterSize;
	
	/** 
	 * Create object for executing Summary Cache algorithm
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
	public SummaryCache(int nClients, int clientCacheSize, int serverCacheSize,
			int serverDiskSize, int totalRequests, int bloomFilterSize,
			int cacheReferenceTicks, int diskToCacheTicks,
			int networkHopTicks) {
		super(nClients, clientCacheSize, serverCacheSize, serverDiskSize, 
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
	}
	
	/**
	 * This algorithm requires Client/Server with importance aware bloom filter.
	 * 
	 * @param clientCaches data to be inserted in client caches
	 * @param serverCache data to be inserted in server cache
	 * @param serverDisk data to be inserted in server disk
	 */
	public void warmup(Block[][] clientCaches, Block[] serverCache,
			Block[] serverDisk) {
		clients = new Client[nClients];
		server = new ServerWithIBF(serverCacheSize, bloomFilterSize,
				serverDiskSize, 1);
		server.cacheWarmUp(serverCache);
		server.diskWarmUp(serverDisk);
		
		for(int i = 0; i < nClients; i++) {
			clients[i] = new ClientWithIBF(clientCacheSize, bloomFilterSize, i);
			clients[i].cacheWarmUp(clientCaches[i]);
		}
	}
	@Override
	public void executeExperiment(List<String> requests) {
	}

	@Override
	public double getTicksPerRequest() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCacheMiss() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCacheHit() {
		// TODO Auto-generated method stub
		return 0;
	}

}
