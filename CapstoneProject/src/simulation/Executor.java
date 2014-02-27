package simulation;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Main class to create simulator objects for different experiment 
 * configuration
 * @author Shridhar Bhalekar
 *
 */
public class Executor {

	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage: java Executor <config-file>");
			System.exit(-1);
		}
		Simulator simulator = new Simulator();
		try {
			simulator.readConfiguration(args[0]);
			if(simulator.readData())
				simulator.initializeExperiment();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
