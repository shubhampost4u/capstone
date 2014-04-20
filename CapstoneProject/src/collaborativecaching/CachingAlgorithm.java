package collaborativecaching;

import java.util.List;
import java.util.Random;

import simulation.Block;
import simulation.Client;
import simulation.Server;

/**
 * Base class for all cooperative caching algorithms. Implement methods for
 * tracing four known cases
 * 
 * Best Case - Data on requested client cache
 * Case 2 - Data on remote client cache
 * Case 3 - Data on server cache
 * Worst Case - Data on server disk
 * 
 * @author Shridhar Bhalekar
 *
 */
public abstract class CachingAlgorithm {
	
	/** Total clients in the system */
	protected int nClients;
	
	/** Cache size of each client */
	protected int clientCacheSize;
	
	/** Cache size of server */
	protected int serverCacheSize;
	
	/** disk size of server */
	protected int serverDiskSize;
	
	/** Ticks required for cache reference */
	protected int cacheReferenceTicks;
	
	/** Ticks required for disk reference */
	protected int diskToCacheTicks;
	
	/** Ticks required for transferring data over network */
	protected int networkHopTicks;
	
	/** Total requests to be handled*/
	protected int totalRequests;
	
	/** Clients to form the network */
	protected Client[] clients;
	
	/** Server to form network */
	protected Server server;
	
	/** Ticks required for satisfying each requests */
	protected double ticksPerRequest;
	
	/** Local Cache Hit ratio per request */
	protected double localCacheHitPerRequest;
	
	/** Global Cache Hit ratio per request */
	protected double globalCacheHitPerRequest;
	
	/** Cache Miss ratio per request */
	protected double cacheMissPerRequest;
	

	/** 
	 * Create object for executing caching algorithm
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
	public CachingAlgorithm(int nClients, int clientCacheSize, 
			int serverCacheSize, int serverDiskSize, int cacheReferenceTicks,
			int diskToCacheTicks, int networkHopTicks)
	{
		this.nClients = nClients;
		this.clientCacheSize = clientCacheSize;
		this.serverCacheSize = serverCacheSize;
		this.serverDiskSize = serverDiskSize;
		this.cacheReferenceTicks = cacheReferenceTicks;
		this.diskToCacheTicks = diskToCacheTicks;
		this.networkHopTicks = networkHopTicks;
	}
		
	/**
	 * Best Case where every requested data will be on the requested client 
	 * cache
	 * 
	 * @param clientCaches data in the client caches
	 * @return ticks
	 */
	public double traceBestCase(Block[][] clientCaches) {
		Random random = new Random();
		double result = 0.0;
		for(int i = 0; i < totalRequests; i++) {
			int index = random.nextInt() % (nClients * clientCacheSize);
			int clientId = (int) index / clientCacheSize;
			int requestId = index % clientCacheSize;
			clients[clientId].cacheLookup(
					clientCaches[clientId][requestId].getData());
		}
		return result;
	}
	
	/**
	 * Case where requested data will be on the remote client
	 * 
	 * @param clientCaches data in the client caches
	 * @return ticks
	 */
	public double traceSecondBestCase(Block[][] clientCaches) {
		Random randomClient = new Random();
		Random randomRequest = new Random();
		int chosenClientId;
		double result = 0.0;
		for(int i = 0; i < totalRequests; i++) {
			int requestId = randomRequest.nextInt() % (nClients * 
					clientCacheSize);
			while(true) {
				chosenClientId = randomClient.nextInt() % nClients;
				int clientId = (int) requestId / clientCacheSize;
				if(chosenClientId != clientId) {
					clients[chosenClientId].cacheLookup(clientCaches[clientId]
							[requestId].getData());
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Case where requested data is on the server cache
	 * @param serverCache data on server cache
	 * @return ticks
	 */
	public double traceThirdBestCase(Block[] serverCache) {
		Random randomClient = new Random();
		Random randomRequest = new Random();
		double result = 0.0;
		for(int i = 0; i < totalRequests; i++) {
			int requestId = randomRequest.nextInt() % serverCacheSize;
			int clientId = randomClient.nextInt() % nClients;
			clients[clientId].cacheLookup(serverCache[requestId].getData());
		}
		return result;
	}
	
	/**
	 * Worst case where requested data on the server disk
	 * @param serverDisk server disk contents
	 * 
	 * @return ticks
	 */
	public double traceWorstCase(Block[] serverDisk) {
		Random randomClient = new Random();
		Random randomRequest = new Random();
		double result = 0.0;
		for(int i = 0; i < totalRequests; i++) {
			int requestId = randomRequest.nextInt() % serverDiskSize;
			int clientId = randomClient.nextInt() % nClients;
			clients[clientId].cacheLookup(serverDisk[requestId].getData());
		}
		return result;
	}
		
	/**
	 * Method to execute the cooperative caching experiments
	 * 
	 * @param requests list of requests to be fired on clients
	 */
	public void executeExperiment(List<String> requests) {
		int localCacheHit = 0;
		int globalCacheHit = 0;
		int cacheMiss = 0;
		int ticks  = 0;
		for(String request : requests) {
			CachingClient client = ((CachingClient)clients
					[new Random().nextInt(nClients)]); 
			client.requestData(0, 0, 0, 0, null, request, false);
			ticks += client.getResponseCost();
			cacheMiss += client.getCacheMiss();
			localCacheHit += client.getLocalCacheHit();
			globalCacheHit += client.getGlobalCacheHit();
		}
		ticksPerRequest = (double) ticks / (double)requests.size();
		cacheMissPerRequest = (double) cacheMiss / (double) requests.size();
		localCacheHitPerRequest = (double) localCacheHit / (double) requests.size();
		globalCacheHitPerRequest = (double) globalCacheHit / (double) requests.size();
	}
	
	/**
	 * Method to return ticks per request
	 * @return ticks per request
	 */
	public double getTicksPerRequest() {
		return ticksPerRequest;
	}
	
	/**
	 * Method to return cache miss per request
	 * @return cache miss ratio
	 */
	public double getCacheMiss() {
		return cacheMissPerRequest;
	}
	
	/**
	 * Method to return cache hit ratio
	 * @return cache hit ratio
	 */
	public double getLocalCacheHit() {
		return localCacheHitPerRequest;
	}
	
	public double getGlobalCacheHit() {
		return globalCacheHitPerRequest;
	}
	
	/**
	 * Overridden method of CachingAlgorithm class to distribute data to clients
	 * and server in the system. This is an initial configuration of the system
	 * 
	 * @param clientCaches data to be inserted in client caches
	 * @param serverCache data to be inserted in server cache
	 * @param serverDisk data to be inserted in server disk
	 */
	public abstract void warmup(Block[][] clientCaches, Block[] serverCache,
			Block[] serverDisk);
}
