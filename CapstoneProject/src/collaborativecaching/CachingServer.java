package collaborativecaching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import simulation.Block;
import simulation.Server;

public abstract class CachingServer extends Server {

	protected int nClients;
	
	protected int cacheReferenceTicks;
	
	protected int networkHopTicks;
	
	protected int diskToCacheTicks;
	
	protected List<Set<Integer>> clientContents;
	
	protected CachingClient[] clients;
	
	protected static final int MIN_LRU_COUNT = 1;
	
	protected static final int MAX_LRU_COUNT = 10;
		
	protected int[] cacheLRUCount;
	
	public CachingServer(long serverId, int cacheSize, int diskSize, 
			int cacheReferenceTicks, int diskToCacheTicks, int networkHopTicks)
	{
		super(cacheSize, diskSize, serverId);
		this.cacheReferenceTicks = cacheReferenceTicks;
		this.diskToCacheTicks = diskToCacheTicks;
		this.networkHopTicks = networkHopTicks;
		clientContents = new ArrayList<Set<Integer>>();
		cacheLRUCount = new int[cacheSize];
	}

	public void updateClients(CachingClient[] clients) {
		nClients = clients.length;
		this.clients = new CachingClient[nClients];
		for(int i = 0; i < nClients; i++) {
			clientContents.add(new HashSet<Integer>());
			this.clients[i] = clients[i];
		}
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
	
	public void updateClientContents() {
		for(int i = 0; i < nClients; i++) {
			CachingClient client = clients[i];
			for(int j = 0; j < client.getCacheSize(); j++) {
				clientContents.get(i).add(client.getCacheBlock(j).getData().
						hashCode());
			}
		}
	}
	
	public boolean requestData(int ticksPerRequest, int cacheMiss, 
			int cacheHit, CachingClient requester, String block) {
		int hash = block.hashCode(); 
		for(int i = 0; i < nClients; i++) {
			if(requester.getClientId() != clients[i].getClientId() &&
					clientContents.get(i).contains(hash)) {
				ticksPerRequest += networkHopTicks;
				if(clients[i].requestData(ticksPerRequest, cacheMiss, cacheHit,
						requester, block, true)) {
					return true;
				}
			}
		}
		ticksPerRequest += cacheReferenceTicks;
		int index = cacheLookup(block);
		if(index != -1) {
			cacheHit += 1;
			ticksPerRequest += networkHopTicks;
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
			return true;
		}
		cacheMiss += 1;
		ticksPerRequest += diskToCacheTicks;
		index = disk.lookup(block);
		if(index != -1) {
			requester.setResponse(disk.getBlock(index), ticksPerRequest,
					cacheMiss, cacheHit);
			requester.updateCache(disk.getBlock(index));
		}
		return true;
	}
	
	public abstract void updateCache(Block data);
}
