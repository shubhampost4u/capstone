package bloomfilters;

import simulation.Block;
import simulation.Client;

/**
 * Implements the importance aware bloom filter
 * @author Shridhar Bhalekar
 *
 */
public class ImportanceAwareBloomFilter extends BloomFilter {

	/** 
	 * Create object for executing importance aware bloom filter algorithm
	 * 
	 * @param nClients total clients
	 */
	public ImportanceAwareBloomFilter(int nClients) {
		super(nClients);
		clients = new ClientWithIBF[nClients];
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
		server = new ServerWithIBF(serverCacheSize, bloomFilterSize,
				serverDiskSize, 1);
		server.cacheWarmUp(serverCache);
		server.diskWarmUp(serverDisk);
		
		for(int i = 0; i < nClients; i++) {
			clients[i] = new ClientWithIBF(clientCacheSize, bloomFilterSize, i);
			clients[i].cacheWarmUp(clientCaches[i]);
		}
	}
}
