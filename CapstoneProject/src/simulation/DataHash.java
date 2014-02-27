package simulation;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DataHash {

	private String dataToBeHashed;
	
	private MessageDigest messageDigest;
	
	public DataHash(String data) {
		this.dataToBeHashed = data;
	}
	
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
	
	public BigInteger md5() {
		try {
			messageDigest = MessageDigest.getInstance("MD2"); 
			messageDigest.update(dataToBeHashed.getBytes(), 0,
					dataToBeHashed.length());
			return new BigInteger(1, messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
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
//		DataHash dh1 = new DataHash("123abc");
//		DataHash dh2 = new DataHash("a123bc");
//		System.out.println(dh1.djb2());
//		System.out.println(dh2.djb2());
//	}
}
