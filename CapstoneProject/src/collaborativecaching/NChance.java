package collaborativecaching;

import java.util.List;

import simulation.Block;
import simulation.Client;
import simulation.Server;

/**
 * Nchance algorithm
 * @author Shridhar Bhalekar
 *
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
			int serverDiskSize, int totalRequests, int cacheReferenceTicks,
			int diskToCacheTicks, int networkHopTicks) {
		super(nClients, clientCacheSize, serverCacheSize, serverDiskSize,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
	}

	@Override
	public void executeExperiment(List<String> requests) {
		// TODO Auto-generated method stub
	}

	@Override
	public void warmup(Block[][] clientCaches, Block[] serverCache,
			Block[] serverDisk) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getTicksPerRequest() {
		return ticksPerRequest;
	}

	@Override
	public double getCacheMiss() {
		return cacheMissPerRequest;
	}

	@Override
	public double getCacheHit() {
		return cacheHitPerRequest;
	}

}
