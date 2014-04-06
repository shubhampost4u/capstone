package collaborativecaching;

import java.util.Random;

import simulation.Block;

/**
 * Sub class of CachingServer class to form server used in NChance algorithm.
 * Apart from having the generic CachingServer functionalities, this class 
 * performs additional functionalities needed by the NChance algorithm.
 *  
 * @author Shridhar Bhalekar
 */
public class NCServer extends CachingServer {

	/**
	 * Creates Object of NCServer
	 * 
	 * @param serverId id of server
	 * @param cacheSize size of server cache
	 * @param diskSize size of server disk
	 * @param cacheReferenceTicks ticks required to reference server cache
	 * @param diskToCacheTicks ticks required to reference server disk
	 * @param networkHopTicks ticks required to send request over network
	 */
	public NCServer(long serverId, int cacheSize, int diskSize,
			int cacheReferenceTicks, int diskToCacheTicks, int networkHopTicks)
	{
		super(serverId, cacheSize, diskSize, cacheReferenceTicks,
				diskToCacheTicks, networkHopTicks);
	}

	/**
	 * This method id overridden method of Caching Server class to update the 
	 * local cache. 
	 * 
	 * @param data block to be updated
	 */
	@Override
	public void updateCache(Block data) {
		int min = MAX_LRU_COUNT;
		int minIndex = -1;
		// find the cache block with min LRU count
		for(int i = 0; i < cacheSize; i++) {
			if(cacheLRUCount[i] < min) {
				min = cacheLRUCount[i];
				minIndex = i;
			}
		}
		if(min > -1 && min < 10) {
			cache.update(minIndex, data);
			cacheLRUCount[minIndex] =  new Random().nextInt((MAX_LRU_COUNT
					- MIN_LRU_COUNT) + 1) + MIN_LRU_COUNT;
		}
	}
	
	/**
	 * This method takes in a singlet block and forwards that block to random
	 * client other than the current client
	 * 
	 * @param singlet block to be forwarded
	 * @param sender client who sent the block
	 */
	public void forwardSinglet(Block singlet, CachingClient sender) {
		Random random = new Random();
		long clientId = sender.getClientId();
		int clientCovered = 0;
		while(clientCovered < nClients) {
			clientId = random.nextInt(nClients);
			if(clientId != sender.getClientId()) {
				break;
			}
			clientCovered += 1;
		}
		if(clientId != sender.getClientId()) {
			((NCClient)clients[(int)clientId]).updateSinglet(singlet);;
			super.updateClientContents();
		}
	}
	
	/**
	 * This method checks of the block under consideration is singlet or not.
	 * For that it requires to check if this block is present in multiple 
	 * clients cache.
	 * 
	 * @param block data block under consideration
	 * @param requester client who wants the confirmation
	 * 
	 * @return boolean value representing if it's a singlet or not
	 */
	public boolean checkSinglet(Block block, NCClient requester) {
		for(CachingClient client : clients) {
			if(requester.getClientId() != client.getClientId() && 
					client.cacheLookup(block.getData()) != -1) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check for the data in client cache
	 */
	@Override
	public boolean isMember(String data) {
		return cacheLookup(data) != -1;
	}
}
