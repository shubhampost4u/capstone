package collaborativecaching;

import java.util.Random;

import simulation.Block;
import simulation.Client;

public class CachingClient extends Client{

	private int cacheReferenceTicks;
		
	private int networkHopTicks;
	
	private CachingServer server;
	
	private static final int MIN_LRU_COUNT = 1;
	
	private static final int MAX_LRU_COUNT = 10;
	
	private Block response;
	
	private int requestCost;
	
	private int cacheHit;
	
	private int cacheMiss;
	
	private int[] cacheLRUCount;
	
	public CachingClient(long clientId, int cacheSize, int cacheReferenceTicks,
			int networkHopTicks, CachingServer server) {
		super(cacheSize, clientId);
		this.cacheReferenceTicks = cacheReferenceTicks;
		this.networkHopTicks = networkHopTicks;
		this.server = server;
		this.cacheLRUCount = new int[cacheSize];
	}

	public boolean cacheWarmUp(Block[] contents) {
		super.cacheWarmUp(contents);
		Random random = new Random();
		for(int i = 0; i < cacheSize; i++) {
			cacheLRUCount[i] = random.nextInt((MAX_LRU_COUNT - MIN_LRU_COUNT)
					+ 1) + MIN_LRU_COUNT;
		}
		return true;
	}
	
	public boolean requestData(int ticksPerRequest, int cacheMiss, 
			int cacheHit, CachingClient requester, String block,
			boolean sentByServer) {
		int index = cacheLookup(block);
		ticksPerRequest += cacheReferenceTicks;
		if(requester == null) {
			requester = this;
		}
		
		if(index != -1) {
			ticksPerRequest += networkHopTicks;
			cacheHit += 1;
			if(cacheLRUCount[index] != MAX_LRU_COUNT) {
				cacheLRUCount[index] += 1;
			}
			requester.setResponse(cache.getBlock(index), ticksPerRequest,
					cacheMiss, cacheHit);
		} else {
			if(sentByServer) {
				server.updateClientContents();
				return false;
			}
			ticksPerRequest += networkHopTicks;
			return server.requestData(ticksPerRequest, cacheMiss, cacheHit,
					requester, block);
		}
		return true;
	}
	
	public void setResponse(Block block, int cost, int cacheMiss, int cacheHit)
	{
		response = block;
		requestCost = cost;
		this.cacheMiss = cacheMiss;
		this.cacheHit = cacheHit;
	}
	
	public Block getResponse() {
		return response;
	}
	
	public int getResponseCost() {
		return requestCost;
	}
	
	public int getCacheHit() {
		return cacheHit;
	}
	
	public int getCacheMiss() {
		return cacheMiss;
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
