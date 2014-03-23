package collaborativecaching;

import java.util.Random;

import simulation.Block;

public class GFServer extends CachingServer {

	public GFServer(long serverId, int cacheSize, int diskSize,
			int cacheReferenceTicks, int diskToCacheTicks, int networkHopTicks) {
		super(serverId, cacheSize, diskSize, cacheReferenceTicks, diskToCacheTicks,
				networkHopTicks);
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
			cache.update(minIndex, data);
			cacheLRUCount[minIndex] =  new Random().nextInt((MAX_LRU_COUNT
					- MIN_LRU_COUNT) + 1) + MIN_LRU_COUNT;
		}
	}
		
	@Override
	public boolean isMember(String data) {
		return false;
	}
}
