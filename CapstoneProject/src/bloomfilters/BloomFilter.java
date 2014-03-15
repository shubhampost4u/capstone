package bloomfilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import simulation.Block;
import simulation.Client;
import simulation.ClientWithBF;
import simulation.Server;
import simulation.ServerWithBF;

/**
 * Implements the bloom filter algorithm
 * @author Shridhar Bhalekar
 *
 */
public class BloomFilter {
	
	/** Total clients in the system */
	protected int nClients;
	
	/** Cache size of each client */
	protected int clientCacheSize;
	
	/** Size of bloom filter */
	protected int bloomFilterSize;
	
	/** Cache size of server */
	protected int serverCacheSize;
	
	/** disk size of server */
	protected int serverDiskSize;
		
	/** Clients to form the network */
	protected Client[] clients;
	
	/** Server to form network */
	protected Server server;
	
	/** False positive count during the experiment */
	protected long falsePositive;
	
	/** 
	 * Create object for executing bloom filter algorithm
	 * 
	 * @param nClients total clients
	 */
	public BloomFilter(int nClients) {
		this.nClients = nClients;
		this.falsePositive = 0;
	}

	
	public BloomFilter setupParameters(int clientCacheSize, int serverCacheSize,
			int serverDiskSize, int bloomFilterSize) {
		this.clientCacheSize = clientCacheSize;
		this.serverCacheSize = serverCacheSize;
		this.serverDiskSize = serverDiskSize;
		this.bloomFilterSize = bloomFilterSize;
		return this;
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
		server = new ServerWithBF(serverCacheSize, bloomFilterSize,
				serverDiskSize, 1);
		server.cacheWarmUp(serverCache);
		server.diskWarmUp(serverDisk);
		for (int i = 0; i < nClients; i++) {
			clients[i] = new ClientWithBF(clientCacheSize, bloomFilterSize, i);
			clients[i].cacheWarmUp(clientCaches[i]);
		}
	}

	/**
	 * Takes in a list of requests to be fired on clients. Each request is
	 * fired on a random client which checks it's bloom filter for the query
	 * and if bloom filter returns true checks the actual cache contents. If
	 * bloom filter returns true but data not present in client cache means
	 * increment false positive count and forward that query to another client.
	 *  
	 * @param requests List of requests
	 * 
	 * @return false positive count
	 */
	public long executeExperiment(List<String> requests) {
		Random random = new Random();
		// for each request
		for(String request : requests) {
			List<Integer> coveredClients = new ArrayList<Integer>();
			// check in all the clients
			while(coveredClients.size() != nClients) {
				int clientIndex = random.nextInt() % nClients;
				if(clientIndex < 0) {
					clientIndex *= -1;
				}
				if(!coveredClients.contains(clientIndex)) {
					// check bloom filter
					if(clients[clientIndex].isMember(request)) {
						if(!clients[clientIndex].cacheLookup(request)) {
							falsePositive += 1;
						} else {
							break;
						}
					}
					coveredClients.add(clientIndex);
				}
			}
			// check server
			if(coveredClients.size() == nClients) {
				if(server.isMember(request)) {
					if(!server.cacheLookup(null)) {
						falsePositive += 1;
					}
				}
			}
		}
		return falsePositive;
	}
	
	/**
	 * Resets the false positive count to 0
	 */
	public void resetFalsePositive() {
		this.falsePositive = 0;
	}
}
