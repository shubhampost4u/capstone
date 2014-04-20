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

	 /** Size of bloom filter */
	private int bloomFilterSize;
	
	/** Size of bloom filter as compared to cache size */
	private static final double BLOOMFILTER_QUOTA = 1.0; 
	
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
			int serverDiskSize, int cacheReferenceTicks, int diskToCacheTicks,
			int networkHopTicks) {
		super(nClients, clientCacheSize, serverCacheSize, serverDiskSize, 
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		this.bloomFilterSize = (int)(SummaryCache.BLOOMFILTER_QUOTA *
				(double)this.clientCacheSize);
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
		clients = new SCClient[nClients];
		server = new SCServer(1, serverCacheSize, serverDiskSize,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		for(int i = 0; i < nClients; i++) {
			clients[i] = new SCClient(i, clientCacheSize, cacheReferenceTicks,
					networkHopTicks, (SCServer)server);
			((SCClient) clients[i]).setBloomFilterSize(bloomFilterSize);
			clients[i].cacheWarmUp(clientCaches[i]);
		}
		for(int i = 0; i < nClients; i++) {
			((SCClient) clients[i]).setPeers(clients);
		}
//		((SCServer) server).updateClients((SCClient[])clients);
		server.cacheWarmUp(serverCache);
		server.diskWarmUp(serverDisk);
	}
	
	@Override
	public void executeExperiment(List<String> requests) {
		System.out.println("Executing SummaryCache for " + nClients + 
				" cacheSize = "+ clientCacheSize + " diskSize = " + 
				serverDiskSize);
		super.executeExperiment(requests);
	}
}
