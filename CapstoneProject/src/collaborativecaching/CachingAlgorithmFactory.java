package collaborativecaching;

/**
 * Factory pattern to create objects of CachingAlgorithm class
 * @author Shridhar Bhalekar
 */
public class CachingAlgorithmFactory {
	
	/**
	 * Method which creates object of appropriate CachingAlgorithm sub class
	 * according to type.
	 * 
	 * @param type type of CachingAlgorithm
	 * @param nClients total number of clients
	 * @param clientCacheSize cache size of client cache
	 * @param serverCacheSize cache size of server cache
	 * @param serverDiskSize disk size of server
	 * @param cacheReferenceTicks ticks required to reference client/server cache
	 * @param diskToCacheTicks ticks required to reference server disk
	 * @param networkHopTicks ticks required to forward query over network
	 * 
	 * @return CachingAlgorithm object
	 */
	public static CachingAlgorithm createCachingAlgorithm(AlgorithmType type,
			int nClients, int clientCacheSize, int serverCacheSize,
			int serverDiskSize, int cacheReferenceTicks, int diskToCacheTicks,
			int networkHopTicks) {
		
		CachingAlgorithm ca = null;
		switch(type) {
		case GreedyForwarding:
			ca = new GreedyForwarding(nClients, clientCacheSize,
					serverCacheSize, serverDiskSize, cacheReferenceTicks,
					diskToCacheTicks, networkHopTicks);
			break;
		case NChance:
			ca = new NChance(nClients, clientCacheSize,
					serverCacheSize, serverDiskSize, cacheReferenceTicks,
					diskToCacheTicks, networkHopTicks);
			break;
		case Robinhood:
//			ca = new RobinHood(nClients, clientCacheSize,
//					serverCacheSize, serverDiskSize, cacheReferenceTicks,
//					diskToCacheTicks, networkHopTicks);
			break;
		case SummaryCache:
//			ca = new SummaryCache(nClients, clientCacheSize,
//					serverCacheSize, serverDiskSize, cacheReferenceTicks,
//					diskToCacheTicks, networkHopTicks);
			break;
		default:
			break;
		}
		return ca;
	}
}
