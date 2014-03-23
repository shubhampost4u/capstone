package collaborativecaching;

import java.util.Random;

import simulation.Block;
import simulation.Client;

public abstract class CachingClient extends Client{

	protected int cacheReferenceTicks;
		
	protected int networkHopTicks;
	
	protected CachingServer server;
	
	protected static final int MIN_LRU_COUNT = 1;
	
	protected static final int MAX_LRU_COUNT = 10;
	
	protected Block response;
	
	protected int requestCost;
	
	protected int cacheHit;
	
	protected int cacheMiss;
	
	protected int[] cacheLRUCount;
	
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
			for(int i = 0; i < cacheLRUCount.length; i++) {
				if(i != index && cacheLRUCount[i] > 0) {
					cacheLRUCount[i] -= 1;
				}
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
	
	public abstract void updateCache(Block data);
}
