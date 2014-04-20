package simulation;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class has the implementation of various hash functions used to get the hash
 * value of data to be stored in bloom filters.
 * 
 * @author Shridhar Bhalekar
 *
 */
public class DataHash {

	/** Data whose hash value is required */
	private String dataToBeHashed;
	
	/** Message digest to be generated*/
	private MessageDigest messageDigest;
	
	/**
	 * Creates object for calculating hash values of data
	 * @param data to be hashed
	 */
	public DataHash(String data) {
		this.dataToBeHashed = data;
	}
	
	/**
	 * Uses the java implementation of SHA-1
	 * 
	 * @return hashed value
	 */
	public BigInteger sha1() {
		try {
			messageDigest = MessageDigest.getInstance("SHA1"); 
			messageDigest.update(dataToBeHashed.getBytes(), 0,
					dataToBeHashed.length());
			return new BigInteger(1, messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Uses the java implementation of SHA-256
	 * 
	 * @return hashed value
	 */
	public BigInteger sha256() {
		try {
			messageDigest = MessageDigest.getInstance("SHA-256"); 
			messageDigest.update(dataToBeHashed.getBytes(), 0,
					dataToBeHashed.length());
			return new BigInteger(1, messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Uses the java implementation of SHA-384
	 * 
	 * @return hashed value
	 */
	public BigInteger sha384() {
		try {
			messageDigest = MessageDigest.getInstance("SHA-384"); 
			messageDigest.update(dataToBeHashed.getBytes(), 0,
					dataToBeHashed.length());
			return new BigInteger(1, messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Uses the java implementation of MD-5
	 * 
	 * @return hashed value
	 */
	public BigInteger md5() {
		try {
			messageDigest = MessageDigest.getInstance("MD5"); 
			messageDigest.update(dataToBeHashed.getBytes(), 0,
					dataToBeHashed.length());
			return new BigInteger(1, messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Starts with a prime number and calculates the hash value from the 
	 * ASCII values of the characters
	 * 
	 * @return hashed value
	 */
	public BigInteger djb2() {
		BigInteger hashVal = new BigInteger("5381");
		for(int i = 0; i < dataToBeHashed.length(); i++) {
			
			hashVal = hashVal.multiply(new BigInteger("33"));
			String ch = new Integer(dataToBeHashed.substring(i, i+1)
					.hashCode()).toString();
			BigInteger bi = new BigInteger(ch);
			hashVal = hashVal.add(bi);
		}
		return hashVal;
	}
	
//	public static void main(String[] args) {
//		DataHash dh1 = new DataHash("A Ratra");
//		DataHash dh2 = new DataHash("Aaqib Javed Sheikhupura  Punjab");
//		System.out.println(dh1.sha1());
//		System.out.println(dh2.sha1());
//		System.out.println(dh1.sha256());
//		System.out.println(dh1.sha1());
//		System.out.println(dh1.sha384());
//		System.out.println(dh1.md5());
//		System.out.println(dh1.djb2());
//	}
}
