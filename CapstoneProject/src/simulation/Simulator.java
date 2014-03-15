package simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

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
	private int cacheSizeLow;
	
	private int cacheSizeHigh;
	
	private int cacheSizeRampUp;
	
	/** disk size of server */
	private int serverDiskSize;

	/** Ticks required for cache reference */
	private int cacheReferenceTicks;

	/** Ticks required for disk reference */
	private int diskToCacheTicks;

	/** Ticks required for transferring data over network */
	private int networkHopTicks;

	/** Lower bound on total requests to be handled */
	private int totalRequestLow;

	/** Upper bound on total requests to be handled */
	private int totalRequestHigh;

	/** Ramp up for total requests to be handled */
	private int totalRequestRampUp;

	/** Type of experiment */
	private int experimentType;
	


	/** File containing data to be added to the client/server storage */
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
	 * @param filename
	 *            properties file to be read
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void readConfiguration(String filename)
			throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(filename));
		nClients = Integer.parseInt(properties.getProperty("nClients"));
		cacheSizeLow = Integer.parseInt(properties
				.getProperty("cacheSizeLow"));
		cacheSizeHigh = Integer.parseInt(properties
				.getProperty("cacheSizeHigh"));
		cacheSizeRampUp = Integer.parseInt(properties
				.getProperty("cacheSizeRampUp"));
		serverDiskSize = Integer.parseInt(properties
				.getProperty("serverDiskSize"));
		cacheReferenceTicks = Integer.parseInt(properties
				.getProperty("cacheReferenceTicks"));
		diskToCacheTicks = Integer.parseInt(properties
				.getProperty("diskToCacheTicks"));
		networkHopTicks = Integer.parseInt(properties
				.getProperty("networkHopTicks"));
		totalRequestLow = Integer.parseInt(properties
				.getProperty("totalRequestLow"));
		totalRequestHigh = Integer.parseInt(properties
				.getProperty("totalRequestHigh"));
		totalRequestRampUp = Integer.parseInt(properties
				.getProperty("totalRequestRampUp"));
		experimentType = Integer.parseInt(properties
				.getProperty("experimentType"));
		dataFile = properties.getProperty("dataFile");
	}

	/**
	 * Read data file and store the contents in dataList
	 * 
	 * @return true if data read successfully
	 */
	public boolean readData() {
		BufferedReader fileReader = null;
		try {
			String currentLine;
			fileReader = new BufferedReader(new FileReader(new File(dataFile)));
			while ((currentLine = fileReader.readLine()) != null) {
				dataList.add(currentLine);
			}
			return true;
		} catch (FileNotFoundException e) {
			System.err.println("Data File Not Found!!!");
		} catch (IOException e) {
			System.err.println("Error Reading File!!!");
		} finally {
			if (fileReader != null) {
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
		if (this.experimentType == 1) {
			bloomFilterComparison();
		} else if (this.experimentType == 2) {
			cachingComparison();
		} else {
			System.err.println("Experiment type should be 1 or 2");
		}
	}

	/**
	 * Experiment to compare Bloom Filter with Importance Aware Bloom Filter.
	 * Initializes the two bloom filter objects for experiment execution.
	 * Distributes data evenly between clients. Clients with same client id
	 * will have same cache contents in BF and IBF. Executes the experiment 
	 * with different number of requests and saves the result in file.
	 */
	private void bloomFilterComparison() {
		BloomFilter bf = new BloomFilter(nClients);
		BloomFilter ibf = new ImportanceAwareBloomFilter(nClients);
		try {
			variableRequests(bf, ibf);
			variableBloomFilterSize(bf, ibf);
			System.out.println("Experiment Complete");
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * 
	 * @param filename
	 * @return
	 */
	private String getFileNameWithoutExt(String filename) {
				
		return dataFile.replaceFirst("[.][^.]+$", "");
	}
	
	/**
	 * 
	 * @param bf
	 * @param ibf
	 * @throws IOException
	 */
	private void variableBloomFilterSize(BloomFilter bf, BloomFilter ibf) 
			throws IOException {
		int cacheSize = cacheSizeLow;
		File resultFile = new File(getFileNameWithoutExt(dataFile) + 
				"_result_2.csv");
		double[] requestPercentage = {0.5, 1.0, 1.5};
		FileWriter writer = new FileWriter(resultFile, true);
		while(cacheSizeRampUp > 0 && cacheSize <= cacheSizeHigh) {
			int totalCacheSize = (nClients + 1) * cacheSize;
			warmup(cacheSize, cacheSize);
			List<String> testRequests = getRequests(totalCacheSize);
			writer.write(new Integer(cacheSize).toString());
			for(double per : requestPercentage) {
				int bloomFilterSize = (int) (per * (double) cacheSize);
				bf.setupParameters(cacheSize, cacheSize, serverDiskSize,
						bloomFilterSize).warmup(clientCaches, serverCache,
								serverDisk);
				ibf.setupParameters(cacheSize, cacheSize, serverDiskSize,
						bloomFilterSize).warmup(clientCaches, serverCache,
								serverDisk);
				writer.write(","+cacheSize+"/"+bloomFilterSize);
				writer.write(","+ bf.executeExperiment(testRequests) + ","
						+ ibf.executeExperiment(testRequests));
				bf.resetFalsePositive();
				ibf.resetFalsePositive();
			}
			writer.write("\n");
			cacheSize += cacheSizeRampUp;
		}
		writer.close();
	}

	private void variableRequests(BloomFilter bf, BloomFilter ibf) 
			throws IOException {
		int cacheSize = cacheSizeLow;
		File resultFile = new File(getFileNameWithoutExt(dataFile) + 
				"_result_1.csv");
		double[] requestPercentage = {0.5, 1.0, 1.5};
		FileWriter writer = new FileWriter(resultFile, true);
		while(cacheSizeRampUp > 0 && cacheSize <= cacheSizeHigh) {
			int totalCacheSize = (nClients + 1) * cacheSize;
			warmup(cacheSize, cacheSize);
			bf.setupParameters(cacheSize, cacheSize, serverDiskSize, cacheSize)
				.warmup(clientCaches, serverCache, serverDisk);
			ibf.setupParameters(cacheSize, cacheSize, serverDiskSize, cacheSize)
				.warmup(clientCaches, serverCache, serverDisk);
			writer.write(new Integer(cacheSize).toString());
			for(double per : requestPercentage) {
				int requests = (int) (per * (double) totalCacheSize);
				writer.write(","+totalCacheSize+"/"+requests);
				List<String> testRequests = getRequests(requests);
				writer.write(","+ bf.executeExperiment(testRequests) + ","
						+ ibf.executeExperiment(testRequests));
				bf.resetFalsePositive();
				ibf.resetFalsePositive();
			}
			writer.write("\n");
			cacheSize += cacheSizeRampUp;
		}
		writer.close();
	}

	/**
	 * Randomly selects data from the data list for requesting.
	 * 
	 * @param totalRequests total data needed
	 * @return List of data from the list
	 */
	private List<String> getRequests(int totalRequests) {
		List<String> requests = new ArrayList<String>();
		Random random = new Random();
		while (requests.size() != totalRequests) {
			int index = random.nextInt() % dataList.size();
			if (index < 0) {
				index *= (-1);
			}
			String request = dataList.get(index);
			requests.add(request);
		}
		return requests;
	}

	/**
	 * Experiment to compare Summary Cache with N-chance, Robinhood and Greedy
	 * Forwarding
	 */
	private void cachingComparison() {
		// get the result of traces and algorithms
		// method not complete
		// will be complete after implemeting bloom filters
	}

	/**
	 * Distribute the data in dataList read from file to all the client caches,
	 * server cache and server disk
	 */
	private void warmup(int clientCacheSize, int serverCacheSize) {
		int serverCacheIndex = 0;
		int serverDiskIndex = 0;
		int serverCacheStartIndex = (nClients * clientCacheSize);
		int serverCacheEndIndex = (nClients * clientCacheSize)
				+ serverCacheSize;
		
		clientCaches = new Block[nClients][clientCacheSize];
		serverCache = new Block[serverCacheSize];
		serverDisk = new Block[serverDiskSize];

		// copy data from list in sequential manner into client cache
		for (int i = 0; i < nClients; i++) {
			int k = 0;
			for (int j = i * clientCacheSize; j < (clientCacheSize * (i + 1));
					j++) {
				clientCaches[i][k++] = new Block(dataList.get(j));
			}
		}
		// copy data to server cache
		for (int i = serverCacheStartIndex; i < serverCacheEndIndex; i++) {
			serverCache[serverCacheIndex++] = new Block(dataList.get(i));
		}

		// copy data to server disk
		for (int i = serverCacheEndIndex; i < serverCacheEndIndex
				+ serverDiskSize; i++) {
			serverDisk[serverDiskIndex++] = new Block(dataList.get(i));
		}
	}

}
