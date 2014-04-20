
package collaborativecaching;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import simulation.Block;
import simulation.Client;
import simulation.DataHash;

/**
 * Sub class of CachingClient class to form clients used in Summary Cache
 * algorithm. Apart from having the generic CachingClient functionalities,
 * this class performs additional functionalities needed by the Summary Cache
 * algorithm.
 * @author Shridhar Bhalekar
 * 
 */
public class SCClient extends CachingClient {

	/** Array representing importance aware bloom filter */
	private int[] iBloomFilter;

	/** Array representing counting bloom filter */
	private int[] cBloomFilter;

	/** Map to store the peers of this client */
	private Map<Long, SCClient> peers;

	/** Size of bloom filter */
	private int bloomFilterSize;

	/** Maximum counter value for the counting bloom filter */
	private final int maxCounter = 15;

	/** Importance function criteria */
	private final int maxImportanceValue = 20;

	private Map<String, Integer> importanceValues;

	/** Maximum value that can be stored in bloom filter */
	private final int M = 5;

	/** Random indexes to be decremented by 1 */
	private int P;

	/**
	 * @param clientId
	 * @param cacheSize
	 * @param cacheReferenceTicks
	 * @param networkHopTicks
	 * @param server
	 */
	public SCClient(long clientId, int cacheSize, int cacheReferenceTicks,
			int networkHopTicks, CachingServer server) {
		super(clientId, cacheSize, cacheReferenceTicks, networkHopTicks, server);
		importanceValues = new HashMap<String, Integer>();
	}

	/**
	 * Method to store the peers of this client into hash map
	 * 
	 * @param peers SummaryCache clients
	 */
	public void setPeers(Client[] peers) {
		this.peers = new HashMap<Long, SCClient>();
		for (Client peer : peers) {
			if (peer.getClientId() != this.clientId)
				this.peers.put(peer.getClientId(), (SCClient) peer);
		}
	}

	/**
	 * Method to initialize the bloom filter size
	 * @param bloomFilterSize
	 */
	public void setBloomFilterSize(int bloomFilterSize) {
		this.bloomFilterSize = bloomFilterSize;
		this.iBloomFilter = new int[bloomFilterSize];
		this.cBloomFilter = new int[bloomFilterSize];
		P = (int) (0.1 * (double) this.bloomFilterSize);
	}

	/**
	 * Overriden method to warm up client cache contents
	 */
	public boolean cacheWarmUp(Block[] contents) {
		boolean ret = super.cacheWarmUp(contents);
		if (ret) {
			for (Block block : contents) {
				addBF(block.getData());
			}
		}
		return ret;
	}

	/**
	 * Delete operation of the counting bloom filter
	 * @param block
	 */
	public void deleteCBF(String block) {
		if (hasMember(block)) {
			importanceValues.remove(block);
			int[] indexes = getHashIndexes(block);
			for (int i : indexes) {
				if (cBloomFilter[i] > 0) {
					cBloomFilter[i] -= 1;
				}
			}
		}
	}

	/**
	 * Updates the contents of cache and bloom filter. First checks if the data
	 * is already present in cache. If not then decrement values at P random
	 * indices in bloom filter by 1 and set the k indices to importance value
	 * returned by importance function
	 * 
	 * @param block
	 *            to be updated in cache
	 */
	private void addBF(String block) {
		Random random = new Random();
		int[] indexes = getHashIndexes(block);
		int counter = 0;
		int index = random.nextInt(bloomFilterSize);
		while (counter != P) {
			if (iBloomFilter[index % bloomFilterSize] > 0) {
				iBloomFilter[index % bloomFilterSize] -= 1;
			}
			index += 1;
			counter += 1;
		}
		for (int i : indexes) {
			if ((importanceFunction(block) < M / 2)
					&& (iBloomFilter[i] < M / 2))
				iBloomFilter[i] = M / 2;
			else
				iBloomFilter[i] = M;
			if (cBloomFilter[i] < maxCounter) {
				cBloomFilter[i] += 1;
			}
		}
	}

	/**
	 * Gets the hash values of block by giving this data as input to k hash
	 * functions
	 * 
	 * @param block
	 *            to be stored in cache
	 * @return array of indices in bloom filter
	 */
	private int[] getHashIndexes(String block) {
		int[] indexes = new int[5];
		BigInteger bfSize = new BigInteger(
				new Integer(bloomFilterSize).toString());
		DataHash hash = new DataHash(block);
		indexes[0] = hash.sha1().mod(bfSize).intValue();
		indexes[1] = hash.md5().mod(bfSize).intValue();
		indexes[2] = hash.sha256().mod(bfSize).intValue();
		indexes[3] = hash.sha384().mod(bfSize).intValue();
		indexes[4] = hash.djb2().mod(bfSize).intValue();

		return indexes;
	}

	/**
	 * Importance function which maps the data to importance value.
	 * 
	 * @param data
	 *            content to be given the importance value
	 * @return importance value
	 */
	private int importanceFunction(String data) {
		Integer impVal = importanceValues.get(data);
		Random random = new Random();
		if (impVal == null) {
			impVal = random.nextInt(maxImportanceValue);
			importanceValues.put(data, impVal);
		}
		if (impVal < maxImportanceValue / 2) {
			return (M / 2);
		}
		return M;
	}

	/**
	 * This method is called by another client/server to request the data from
	 * the system of collaborative caches.
	 * 
	 * @param ticksPerRequest
	 *            ticks associated with the current request
	 * @param cacheMiss
	 *            cache miss associated with the current request
	 * @param localCacheHit
	 *            local cache hit associated with the current request
	 * @param globalCacheHit
	 *            global cache hit associated with the current request
	 * @param requester
	 *            client who requested the data
	 * @param block
	 *            data requested
	 * @param sentByServer
	 *            flag to determine if the request is sent by the server
	 * @return boolean to represent status
	 */
	public boolean requestData(int ticksPerRequest, int cacheMiss,
			int localCacheHit, int globalCacheHit, CachingClient requester,
			String block, boolean sentByServer) {
		int index = cacheLookup(block);
		boolean toServer = false;
		ticksPerRequest += cacheReferenceTicks;
		decrementCacheLRU(index);
		if (requester == null) {
			requester = this;
			if (index != -1) {
				localCacheHit += 1;
				serverLocally(ticksPerRequest, cacheMiss, localCacheHit,
						globalCacheHit, requester, index);
				return true;
			} else {
				toServer = referencePeers(ticksPerRequest, cacheMiss,
						localCacheHit, globalCacheHit, requester, block,
						sentByServer);
				if (!toServer) {
					toServer = ((SCServer) server).requestData(ticksPerRequest,
							cacheMiss, localCacheHit, globalCacheHit,
							requester, block);
				}
			}
		} else {
			if (index != -1) {
				globalCacheHit += 1;
				serverLocally(ticksPerRequest, cacheMiss, localCacheHit,
						globalCacheHit, requester, index);
				return true;
			} else {
				return false;
			}
		}
		return toServer;
	}

	/**
	 * This method is called to refer peer client and forward query 
	 * accordingly
	 * 
	 * @param ticksPerRequest
	 *            ticks associated with the current request
	 * @param cacheMiss
	 *            cache miss associated with the current request
	 * @param localCacheHit
	 *            local cache hit associated with the current request
	 * @param globalCacheHit
	 *            global cache hit associated with the current request
	 * @param requester
	 *            client who requested the data
	 * @param block
	 *            data requested
	 * @param sentByServer
	 *            flag to determine if the request is sent by the server
	 * @return boolean to represent status
	 */
	private boolean referencePeers(int ticksPerRequest, int cacheMiss,
			int localCacheHit, int globalCacheHit, CachingClient requester,
			String block, boolean sentByServer) {
		boolean toServer = false;
		Set<Long> clientIds = peers.keySet();
		for (Long clientId : clientIds) {
			CachingClient peer = peers.get(clientId);
			if (peer.hasMember(block)) {
				toServer = peer.requestData(ticksPerRequest, cacheMiss,
						localCacheHit, globalCacheHit, requester, block,
						sentByServer);
				if (toServer) {
					ticksPerRequest += networkHopTicks;
					break;
				}
			}
		}
		return toServer;
	}

	/**
	 * Method to serve the request from the local cache
	 * 
	 * @param ticksPerRequest
	 * @param cacheMiss
	 * @param localCacheHit
	 * @param globalCacheHit
	 * @param requester
	 * @param index
	 */
	private void serverLocally(int ticksPerRequest, int cacheMiss,
			int localCacheHit, int globalCacheHit, CachingClient requester,
			int index) {
		if (cacheLRUCount[index] != MAX_LRU_COUNT) {
			cacheLRUCount[index] += 1;
		}
		ticksPerRequest += networkHopTicks;
		requester.setResponse(cache.getBlock(index), ticksPerRequest,
				cacheMiss, localCacheHit, globalCacheHit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see collaborativecaching.CachingClient#updateCache(simulation.Block)
	 */
	@Override
	public void updateCache(Block data) {
		int min = MAX_LRU_COUNT;
		int minIndex = -1;
		for (int i = 0; i < cacheSize; i++) {
			if (cacheLRUCount[i] < min) {
				min = cacheLRUCount[i];
				minIndex = i;
			}
		}
		if (min > -1 && min < 10) {
			Block ret = cache.getBlock(minIndex);
			cache.update(minIndex, data);
			cacheLRUCount[minIndex] = MAX_LRU_COUNT;
			deleteCBF(ret.getData());
			addBF(data.getData());
			// addCBF(data.getData());
			server.updateCache(ret);
		}
	}

	@Override
	public boolean hasMember(String data) {
		int[] indexes = getHashIndexes(data);
		for (int i : indexes) {
			if (cBloomFilter[i] == 0 || iBloomFilter[i] == 0) {
				return false;
			}
		}
		return true;
	}

}
