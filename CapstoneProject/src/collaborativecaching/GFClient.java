package collaborativecaching;

import java.util.Random;

import simulation.Block;

public class GFClient extends CachingClient {

	public GFClient(long clientId, int cacheSize, int cacheReferenceTicks,
			int networkHopTicks, CachingServer server) {
		super(clientId, cacheSize, cacheReferenceTicks, networkHopTicks, server);
		// TODO Auto-generated constructor stub
	}
	
	public void updateCache(Block data) {
		int min = MAX_LRU_COUNT;
		int minIndex = -1;
		for(int i = 0; i < cacheSize; i++) {
			if(cacheLRUCount[i] < min) {
				min = cacheLRUCount[i];
				minIndex = i;
			}
		}
		if(min > -1 && min < 10) {
			Block ret = cache.getBlock(minIndex);
			cache.update(minIndex, data);
			cacheLRUCount[minIndex] =  new Random().nextInt((MAX_LRU_COUNT
					- MIN_LRU_COUNT) + 1) + MIN_LRU_COUNT;
			server.updateCache(ret);
		}
	}
	
	@Override
	public boolean isMember(String data) {
		return false;
	}
}
