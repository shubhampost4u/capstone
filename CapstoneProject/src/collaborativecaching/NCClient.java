package collaborativecaching;

import java.util.Random;

import simulation.Block;

public class NCClient extends CachingClient {

	private static final int recirculationCount = 2;
	
	public NCClient(long clientId, int cacheSize, int cacheReferenceTicks,
			int networkHopTicks, CachingServer server) {
		super(clientId, cacheSize, cacheReferenceTicks, networkHopTicks, server);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateCache(Block data) {
		boolean flag = true;
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
			if(((NCServer)server).checkSinglet(ret, this) && !ret.isSinglet) {
				ret.recirculationCount = NCClient.recirculationCount;
				ret.isSinglet = true;
				((NCServer) server).forwardSinglet(ret, this);
				flag = false;
			} else if (ret.isSinglet) {
				ret.recirculationCount -= 1;
				if(ret.recirculationCount != 0) {
					((NCServer) server).forwardSinglet(ret, this);
					flag = false;
				} 
			} 
			if(flag)
			{
				cache.update(minIndex, data);
				cacheLRUCount[minIndex] =  new Random().nextInt((MAX_LRU_COUNT
						- MIN_LRU_COUNT) + 1) + MIN_LRU_COUNT;
				server.updateCache(ret);
			}
		}
	}

	@Override
	public boolean isMember(String data) {
		return (cacheLookup(data) != -1);
	}

}
