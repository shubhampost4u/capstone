/**
 * 
 */
package collaborativecaching;

import java.util.Random;
import simulation.Block;

/**
 * Sub class of CachingServer class to form server used in Robinhood algorithm.
 * Apart from having the generic CachingServer functionalities, this class 
 * performs additional functionalities needed by the Robinhood algorithm.
 *  
 * @author Shridhar Bhalekar
 */
public class RHServer extends CachingServer {

	/** Server chooses this client as victim having victim block */
	public RHClient victimClient;

	/**
	 * Creates server object for Robinhood algorithm 
	 * 
	 * @param serverId
	 * @param cacheSize
	 * @param diskSize
	 * @param cacheReferenceTicks
	 * @param diskToCacheTicks
	 * @param networkHopTicks
	 */
	public RHServer(long serverId, int cacheSize, int diskSize,
			int cacheReferenceTicks, int diskToCacheTicks, int networkHopTicks) {
		super(serverId, cacheSize, diskSize, cacheReferenceTicks,
				diskToCacheTicks, networkHopTicks);
		victimClient = null;
	}

	/**
	 * Add the clients containing the block in the block hash set
	 */
	public void updateDataClients() {
		for (int i = 0; i < diskSize; i++) {
			Block block = disk.getBlock(i);
			for (CachingClient client : clients) {
				if (client.hasMember(block.getData())) {
					block.clients.add(client.getClientId());
				}
			}
		}
	}

	/**
	 * Update the set of clients containing the block.
	 * 
	 * @param block to be updated
	 */
	public void updateBlockClient(Block block) {
		for (CachingClient client : clients) {
			if (client.hasMember(block.getData())) {
				block.clients.add(client.getClientId());
			} else {
				if (block.clients.contains(client.getClientId())) {
					block.clients.remove(client.getClientId());
				}
			}
		}
	}

	/**
	 * Method implementing the NChance forwarding. If Robinhood algorithm fails
	 * to select victim client or chunk then it falls to NChance.
	 * 
	 * @param block to be forwarded
	 * 
	 * @param sender client sending the block
	 */
	public void nChanceForward(Block block, CachingClient sender) {
		Random random = new Random();
		long clientId = sender.getClientId();
		int clientCovered = 0;
		while (clientCovered < nClients) {
			clientId = random.nextInt(nClients);
			if (clientId != sender.getClientId()) {
				break;
			}
			clientCovered += 1;
		}
		if (clientId != sender.getClientId()) {
			((RHClient) clients[(int) clientId]).updateSinglet(block);
			super.updateClientContents();
		}
	}

	/**
	 * Method to select victim client having the victim block to be replaced 
	 * by the singlet block
	 * 
	 * @param requester client requesting the singlet
	 * 
	 * @return index of singlet
	 */
	public int selectVictim(RHClient requester) {
		Block victimChunk = null;
		long max = 0;
		int index = -1;
		for (int i = 0; i < diskSize; i++) {
			if (max < disk.getBlock(i).clients.size()) {
				max = disk.getBlock(i).clients.size();
				victimChunk = disk.getBlock(i);
			}
		}
		updateBlockClient(victimChunk);
		if (victimChunk.clients.size() != 0) {
			int clientIndex = new Random().nextInt(victimChunk.clients.size());
			long victimClientId = (Long) victimChunk.clients.toArray()[clientIndex];

			for (CachingClient client : clients) {
				if (client.getClientId() == victimClientId) {
					victimClient = (RHClient) client;
					break;
				}
			}
			index = victimClient.cacheLookup(victimChunk.getData());
		}
		return index;
	}

	/**
	 * Method to check if the block is a singlet cached in the system
	 * 
	 * @param block singlet block
	 * @param requester client requesting singlet
	 * 
	 * @return status if block is singlet or not
	 */
	public boolean checkSinglet(Block block, RHClient requester) {
		for (CachingClient client : clients) {
			if (requester.getClientId() != client.getClientId()
					&& client.cacheLookup(block.getData()) != -1) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see collaborativecaching.CachingServer#updateCache(simulation.Block)
	 */
	@Override
	public void updateCache(Block data) {
		int min = MAX_LRU_COUNT;
		int minIndex = -1;
		// find the cache block with min LRU count
		for (int i = 0; i < cacheSize; i++) {
			if (cacheLRUCount[i] < min) {
				min = cacheLRUCount[i];
				minIndex = i;
			}
		}
		if (min > -1 && min < 10) {
			cache.update(minIndex, data);
			cacheLRUCount[minIndex] = MAX_LRU_COUNT;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see simulation.Server#isMember(java.lang.String)
	 */
	@Override
	public boolean isMember(String data) {
		return cacheLookup(data) != -1;
	}

}
