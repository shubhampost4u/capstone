package simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import collaborativecaching.CachingAlgorithm;
import collaborativecaching.GreedyForwarding;
import collaborativecaching.NChance;
import collaborativecaching.RobinHood;
import collaborativecaching.SummaryCache;
import bloomfilters.BloomFilter;
import bloomfilters.ImportanceAwareBloomFilter;

/**
 * Controller of the experiments. Reads in all the experiment parameters, 
 * creates the system of clients / server, warmup the cache, simulates the 
 * request forwarding on clients and keeps track of the mesurment parameters
 * 
 * @author Shridhar Bhalekar
 *
 */
public class Simulator {
	
	/** Total clients in the system */
	private int nClients;
	
	/** Cache size of each client */
	private int clientCacheSize;
	
	/** */
	private int bloomFilterSize;
	
	/** Cache size of server */
	private int serverCacheSize;
	
	/** disk size of server */
	private int serverDiskSize;
	
	/** Ticks required for cache reference */
	private int cacheReferenceTicks;
	
	/** Ticks required for disk reference */
	private int diskToCacheTicks;
	
	/** Ticks required for transferring data over network */
	private int networkHopTicks;
	
	/** Total requests to be handled*/
	private int totalRequests;
	
	/** Type of experiment */
	private int experimentType;
	
	/** File containing data to be added to the client/server storage*/
	private String dataFile;
	
	/** Data for client caches */
	private Block[][] clientCaches;
	
	/** Data for server cache */
	private Block[] serverCache;
	
	/** data for server disk */
	private Block[] serverDisk;
	
	/** Read file data */
	private List<String> dataList;
	
	/**
	 * Creates simulator object
	 */
	public Simulator() {
		dataList = new ArrayList<String>();
	}
	
	/**
	 * Read the properties file to create experiment
	 * 
	 * @param filename properties file to be read
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void readConfiguration(String filename) 
			throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(filename));
//		ResourceBundle resource = ResourceBundle.getBundle(filename);
		nClients = Integer.parseInt(properties.
				getProperty("nClients"));
		clientCacheSize = Integer.parseInt(properties.
				getProperty("clientCacheSize"));
		bloomFilterSize = Integer.parseInt(properties.
				getProperty("bloomFilterSize"));
		serverCacheSize = Integer.parseInt(properties.
				getProperty("serverCacheSize"));
		serverDiskSize = Integer.parseInt(properties.
				getProperty("serverDiskSize"));
		cacheReferenceTicks = Integer.parseInt(properties.
				getProperty("cacheReferenceTicks"));
		diskToCacheTicks = Integer.parseInt(properties.
				getProperty("diskToCacheTicks"));
		networkHopTicks = Integer.parseInt(properties.
				getProperty("networkHopTicks"));
		totalRequests = Integer.parseInt(properties.
				getProperty("totalRequests"));
		experimentType = Integer.parseInt(properties.
				getProperty("experimentType"));
		dataFile = properties.getProperty("dataFile");
		
		clientCaches = 	new Block[nClients][clientCacheSize];
		serverCache = new Block[serverCacheSize];
		serverDisk = new Block[serverDiskSize];
	}
	
	/**
	 * Read data file and store the contents in dataList
	 * @return true if data read successfully
	 */
	public boolean readData() {
		BufferedReader fileReader = null;
		try {
			String currentLine;
			fileReader = new BufferedReader(
					new FileReader(new File(dataFile)));
			while((currentLine = fileReader.readLine()) != null) {
				dataList.add(currentLine);
			}
			return true;
		} catch (FileNotFoundException e) {
			System.err.println("Data File Not Found!!!");
		} catch (IOException e) {
			System.err.println("Error Reading File!!!");
		} finally {
			if(fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					System.err.println("Error closing file!!!");
				}
			}
		}
		return false;
	}
	
	/**
	 * Check the experiment parameters and initialize experiment accordingly
	 */
	public void initializeExperiment() {
		if(!parameterSanityCheck()) {
			System.err.println("data in the file not enough to fill all"
					+ "the storage");
			return;
		}
		if(this.experimentType == 1) {
			bloomFilterComparison();
		} else if(this.experimentType == 2) {
			cachingComparison();
		} else {
			System.err.println("Experiment type should be 1 or 2");
		}
	}
	
	/**
	 * Checks if there is enough data to be filled in all the caches and disk
	 * @return true/false
	 */
	private boolean parameterSanityCheck() {
		if(this.dataList.size() >= 
				((this.nClients * this.clientCacheSize) + this.serverCacheSize
						 + this.serverDiskSize)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Experiment to compare Bloom Filter with Importance Aware Bloom Filter
	 */
	private void bloomFilterComparison() {
		BloomFilter bf = new BloomFilter(nClients, clientCacheSize, 
				bloomFilterSize, serverCacheSize, serverDiskSize, totalRequests, 
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
//		BloomFilter ibf = new ImportanceAwareBloomFilter(nClients,
//				clientCacheSize, bloomFilterSize, serverCacheSize,
//				serverDiskSize, totalRequests, cacheReferenceTicks,
//				diskToCacheTicks, networkHopTicks);
		this.warmup();
		
		bf.warmup(clientCaches, serverCache, serverDisk);		
//		ibf.warmup(clientCaches, serverCache, serverDisk);
		
		// get the result of the false positives for both the algorithms
		// method not complete
		// will be complete after implemeting bloom filters
	}
	
	/**
	 * Experiment to compare Summary Cache with N-chance, Robinhood and
	 * Greedy Forwarding
	 */
	private void cachingComparison() {
		CachingAlgorithm traceObject = new CachingAlgorithm(nClients,
				clientCacheSize, serverCacheSize, serverDiskSize, totalRequests
				, cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		
		CachingAlgorithm nChance = new NChance(nClients, clientCacheSize, 
				serverCacheSize, serverDiskSize, totalRequests,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		
		CachingAlgorithm robinhood = new RobinHood(nClients, clientCacheSize, 
				serverCacheSize, serverDiskSize, totalRequests,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		
		CachingAlgorithm greedy = new GreedyForwarding(nClients, clientCacheSize
				, serverCacheSize, serverDiskSize, totalRequests,
				cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		
		CachingAlgorithm summaryCache = new SummaryCache(nClients,
				clientCacheSize, serverCacheSize, serverDiskSize, totalRequests
				, cacheReferenceTicks, diskToCacheTicks, networkHopTicks);
		
		this.warmup();
		traceObject.warmup(clientCaches, serverCache, serverDisk);
		nChance.warmup(clientCaches, serverCache, serverDisk);
		robinhood.warmup(clientCaches, serverCache, serverDisk);
		greedy.warmup(clientCaches, serverCache, serverDisk);
		summaryCache.warmup(clientCaches, serverCache, serverDisk);
		 
		// get the result of traces and algorithms
		// method not complete
		// will be complete after implemeting bloom filters
	}
	
	/**
	 * Distribute the data in dataList read from file to all the client caches,
	 * server cache and server disk
	 */
	private void warmup() {
		int serverCacheIndex = 0;
		int serverDiskIndex = 0;
		int serverCacheStartIndex = (nClients * clientCacheSize);
		int serverCacheEndIndex = (nClients * clientCacheSize) + 
				serverCacheSize;
		// copy data from list in sequential manner into client cache
		for(int i = 0; i < nClients; i++) {
			int k = 0;
			for(int j = i * clientCacheSize; j < (clientCacheSize * (i + 1));
					j++) {
				clientCaches[i][k++] = new Block(dataList.get(j));
			}
		}
		// copy data to server cache
		for(int i = serverCacheStartIndex; i < serverCacheEndIndex; i++) {
			serverCache[serverCacheIndex++] = new Block(dataList.get(i));
		}
		
		// copy data to server disk
		for(int i = serverCacheEndIndex; i < serverCacheEndIndex + 
				serverDiskSize; i++) {
			serverDisk[serverDiskIndex++] = new Block(dataList.get(i)); 
		}
	}

}
