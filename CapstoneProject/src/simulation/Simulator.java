package simulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import collaborativecaching.AlgorithmType;
import collaborativecaching.CachingAlgorithm;
import collaborativecaching.CachingAlgorithmFactory;
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

	/** Lower bound on total clients in the system */
	private int nClientsLow;

	/** Upper bound on total clients in the system */
	private int nClientsHigh;

	/** Ramp up u=interval on total clients in the system */
	private int nClientsRampUp;

	/** Low bound on cache size */
	private int cacheSizeLow;

	/** High bound on cache size */
	private int cacheSizeHigh;

	/** Ramp up on cache size */
	private int cacheSizeRampUp;

	/** disk size of server */
	private int serverDiskSize;

	/** Ticks required for cache reference */
	private int cacheReferenceTicks;

	/** Ticks required for disk reference */
	private int diskToCacheTicks;

	/** Ticks required for transferring data over network */
	private int networkHopTicks;

	/** Type of experiment */
	private int experimentType;

	private int totalRequests;
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
		nClientsLow = Integer.parseInt(properties.getProperty("nClientsLow"));
		nClientsHigh = Integer.parseInt(properties.getProperty("nClientsHigh"));
		nClientsRampUp = Integer.parseInt(properties
				.getProperty("nClientsRampUp"));
		cacheSizeLow = Integer.parseInt(properties.getProperty("cacheSizeLow"));
		cacheSizeHigh = Integer.parseInt(properties
				.getProperty("cacheSizeHigh"));
		cacheSizeRampUp = Integer.parseInt(properties
				.getProperty("cacheSizeRampUp"));
		serverDiskSize = Integer.parseInt(properties
				.getProperty("serverDiskSize"));
		totalRequests = Integer.parseInt(properties
				.getProperty("totalRequests"));
		cacheReferenceTicks = Integer.parseInt(properties
				.getProperty("cacheReferenceTicks"));
		diskToCacheTicks = Integer.parseInt(properties
				.getProperty("diskToCacheTicks"));
		networkHopTicks = Integer.parseInt(properties
				.getProperty("networkHopTicks"));
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
	 * Distributes data evenly between clients. Clients with same client id will
	 * have same cache contents in BF and IBF. Executes the experiment with
	 * different number of requests and saves the result in file.
	 */
	private void bloomFilterComparison() {
		BloomFilter bf = new BloomFilter(nClientsHigh);
		BloomFilter ibf = new ImportanceAwareBloomFilter(nClientsHigh);
		try {
			variableRequests(bf, ibf);
			variableBloomFilterSize(bf, ibf);
			System.out.println("Experiment Complete");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Returns filename without the extension
	 * 
	 * @param filename
	 * @return
	 */
	private String getFileNameWithoutExt(String filename) {
		return dataFile.replaceFirst("[.][^.]+$", "");
	}

	/**
	 * This method executes one type of bloom filter comparison experiment which
	 * compares false positives with the cache size and bloom filter size.
	 * 
	 * @param bf
	 *            Bloom Filter object
	 * @param ibf
	 *            Importance Aware Bloom Filter object
	 * 
	 * @throws IOException
	 *             Exception while file writing
	 */
	private void variableBloomFilterSize(BloomFilter bf, BloomFilter ibf)
			throws IOException {
		int cacheSize = cacheSizeLow;
		int nClients = nClientsHigh;
		File resultFile = new File(getFileNameWithoutExt(dataFile)
				+ "_result_2.csv");
		double[] requestPercentage = { 0.5, 1.0, 1.5 };
		FileWriter writer = new FileWriter(resultFile, true);
		while (cacheSizeRampUp > 0 && cacheSize <= cacheSizeHigh) {
			int totalCacheSize = (nClients + 1) * cacheSize;
			warmup(cacheSize, cacheSize, nClients);
			List<String> testRequests = getRequests(totalCacheSize);
			writer.write(new Integer(cacheSize).toString());
			for (double per : requestPercentage) {
				int bloomFilterSize = (int) (per * (double) cacheSize);
				bf.setupParameters(cacheSize, cacheSize, serverDiskSize,
						bloomFilterSize).warmup(clientCaches, serverCache,
						serverDisk);
				ibf.setupParameters(cacheSize, cacheSize, serverDiskSize,
						bloomFilterSize).warmup(clientCaches, serverCache,
						serverDisk);
				writer.write("," + cacheSize + "/" + bloomFilterSize);
				writer.write("," + bf.executeExperiment(testRequests) + ","
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
	 * This method executes one type of bloom filter comparison experiment which
	 * compares false positives with the cache size and total number of
	 * requests.
	 * 
	 * @param bf
	 *            Bloom Filter object
	 * @param ibf
	 *            Importance Aware Bloom Filter object
	 * 
	 * @throws IOException
	 *             Exception while file writing
	 */
	private void variableRequests(BloomFilter bf, BloomFilter ibf)
			throws IOException {
		int cacheSize = cacheSizeLow;
		int nClients = nClientsHigh;
		File resultFile = new File(getFileNameWithoutExt(dataFile)
				+ "_result_1.csv");
		double[] requestPercentage = { 0.5, 1.0, 1.5 };
		FileWriter writer = new FileWriter(resultFile, true);
		// varying cache sizes
		while (cacheSizeRampUp > 0 && cacheSize <= cacheSizeHigh) {
			int totalCacheSize = (nClients + 1) * cacheSize;
			warmup(cacheSize, cacheSize, nClients);
			bf.setupParameters(cacheSize, cacheSize, serverDiskSize, cacheSize)
					.warmup(clientCaches, serverCache, serverDisk);
			ibf.setupParameters(cacheSize, cacheSize, serverDiskSize, cacheSize)
					.warmup(clientCaches, serverCache, serverDisk);
			writer.write(new Integer(cacheSize).toString());
			for (double per : requestPercentage) {
				int requests = (int) (per * (double) totalCacheSize);
				writer.write("," + totalCacheSize + "/" + requests);
				List<String> testRequests = getRequests(requests);
				writer.write("," + bf.executeExperiment(testRequests) + ","
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
	 * @param totalRequests
	 *            total data needed
	 * @return List of data from the list
	 */
	private List<String> getRequests(int totalRequests) {
		return getRequests(totalRequests, dataList);
	}

	private List<String> getRequests(int totalRequests, List<String> dataPool) {
		List<String> requests = new ArrayList<String>();
		Random random = new Random();
		while (requests.size() != totalRequests) {
			int index = random.nextInt(dataPool.size());
			String request = dataPool.get(index);
			requests.add(request);
		}
		return requests;
	}

	/**
	 * Experiment to compare Summary Cache with N-chance, Robinhood and Greedy
	 * Forwarding. For n number of clients this method executes all the four
	 * algorithms for varying cache size and disk size.
	 */
	private void cachingComparison() {
		FileWriter writer = null;
		// headers in result file
		String[] headers = { "nClients", "GFTicks", "GFCacheMiss",
				"GFlocalCacheHit","GFglobalCacheHit", "NCTicks", "NCcacheMiss",
				"NClocalCacheHit", "NCglobalCacheHit", "RHTicks" ,
				"RHcacheMiss", "RHlocalCacheHit", "RHglobalCacheHit",
				"SCTicks", "SCcacheMiss", "SClocalCacheHit" ,"SCglobalCacheHit"
				};
		File resultFile = new File(getFileNameWithoutExt(dataFile)
				+ "_caching_comparison.csv");
		try {
			writer = new FileWriter(resultFile, true);
			for (String header : headers) {
				writer.write(header + ",");
			}
			writer.write("\n");
			System.out.println("Starting Caching Algorithms Comparison");
			int nClients = nClientsLow;
			while(nClientsRampUp > 0 && nClients <= nClientsHigh) {
				writer.write("\n" + nClients);
				cachingComparisonRound(nClients, 0.0, writer);
				nClients += nClientsRampUp;
			}
			System.out.println("Ending Caching Algorithms Comparison");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method takes in the number of clients and disk percentage and
	 * executes the experiment on all the caching algorithms for varying cache
	 * size. Finally store the results to file.
	 * 
	 * @param nClients
	 *            total clients in the system
	 * @param per
	 *            percentage disk to be used compared to total cache size
	 * @param writer
	 *            file writer
	 * 
	 * @throws IOException
	 */
	private void cachingComparisonRound(int nClients, double per,
			FileWriter writer) throws IOException {
		List<String> reqPool = randomWarmup(cacheSizeLow, cacheSizeLow, 
				totalRequests, nClients);
		for(AlgorithmType type : AlgorithmType.values()) {
			double ticksPerRequest = 0.0;
			double cacheMiss = 0.0;
			double localCacheHit = 0.0;
			double globalCacheHit = 0.0;
			CachingAlgorithm ca = CachingAlgorithmFactory
					.createCachingAlgorithm(type, nClients, cacheSizeLow,
							cacheSizeLow, totalRequests, cacheReferenceTicks,
							diskToCacheTicks, networkHopTicks);
			if (ca != null) {
				ca.warmup(clientCaches, serverCache, serverDisk);
				ca.executeExperiment(reqPool);
				ticksPerRequest += ca.getTicksPerRequest();
				localCacheHit += ca.getLocalCacheHit();
				globalCacheHit += ca.getGlobalCacheHit();
				cacheMiss += ca.getCacheMiss();
			}
			writer.write(","
			+ String.format("%.2f", ticksPerRequest)
			+ ","
			+ String.format("%.2f", cacheMiss)
			+ ","
			+ String.format("%.2f", localCacheHit)
			+ "," 
			+ String.format("%.2f", globalCacheHit));
		}
	}

	/**
	 * This method creates the random distribution of client caches, server
	 * cache and server disk
	 * 
	 * @param clientCacheSize
	 *            cache size of client
	 * @param serverCacheSize
	 *            cache size of server
	 * @param serverDiskSize
	 *            disk size of server
	 * @param nClients
	 *            total clients in the system
	 * 
	 * @return List of requests
	 */
	private List<String> randomWarmup(int clientCacheSize, int serverCacheSize,
			int serverDiskSize, int nClients) {
		int index = 0;
		List<Block> contents = new ArrayList<Block>();
		List<String> requests = new ArrayList<String>();
		clientCaches = new Block[nClients][clientCacheSize];
		serverCache = new Block[serverCacheSize];
		serverDisk = new Block[serverDiskSize];
		Random random = new Random();
		// server disk data
		while (contents.size() != serverDiskSize) {
			Block block = new Block(dataList.get(random.nextInt((dataList
					.size()))));
			if (!contents.contains(block)) {
				contents.add(block);
				requests.add(block.getData());
				serverDisk[index++] = block;
			}
		}
		// client cache data
		for (int i = 0; i < nClients; i++) {
			index = 0;
			contents.clear();
			while (contents.size() != clientCacheSize) {
				Block block = serverDisk[random.nextInt(serverDiskSize)];
				if (!contents.contains(block)) {
					contents.add(block);
					requests.add(block.getData());
					block.isCached = true;
					clientCaches[i][index++] = block;
				}
			}
		}
		index = 0;
		contents.clear();
		// server cache data
		while (contents.size() != serverCacheSize) {
			Block block = serverDisk[random.nextInt(serverDiskSize)];
			if (!contents.contains(block)) {
				contents.add(block);
				requests.add(block.getData());
				block.isCached = true;
				serverCache[index++] = block;
			}
		}
		return requests;
	}

	/**
	 * Distribute the data in dataList read from file to all the client caches,
	 * server cache and server disk
	 */
	private void warmup(int clientCacheSize, int serverCacheSize, int nClients) {
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
