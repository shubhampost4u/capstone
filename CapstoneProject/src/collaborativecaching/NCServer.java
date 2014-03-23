package collaborativecaching;

import simulation.Block;

public class NCServer extends CachingServer {

	public NCServer(long serverId, int cacheSize, int diskSize,
			int cacheReferenceTicks, int diskToCacheTicks, int networkHopTicks)
	{
		super(serverId, cacheSize, diskSize, cacheReferenceTicks,
				diskToCacheTicks, networkHopTicks);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateCache(Block data) {
		// TODO Auto-generated method stub

	}
	
	public void forwardSinglet(Block singlet, CachingClient sender) {
		
	}
	
	public boolean checkSinglet(Block block, NCClient requester) {
		for(CachingClient client : clients) {
			if(requester.getClientId() != client.getClientId() && 
					client.cacheLookup(block.getData()) != -1) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean isMember(String data) {
		return cacheLookup(data) != -1;
	}
}
