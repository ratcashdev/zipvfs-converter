package net.euler.cipher;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.function.Predicate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CipherRead {

	protected Cipher cipher;
	protected KeyIterator keyIterator;
	protected List<byte[]> pages;
	protected int pageSize;
	protected Predicate<byte[]> predicate;
	protected byte[] key;
	protected boolean canLoop;

	public CipherRead(String algorithmName, KeyIterator keyIterator, List<byte[]> pages, Predicate<byte[]> predicate) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.cipher = Cipher.getInstance(algorithmName);
		this.keyIterator = keyIterator;
		this.pages = pages;
		this.pageSize = this.pages.size();
		this.predicate = predicate;
		this.canLoop = true;
	}
	
	public void stop() {
		this.canLoop = false;
	}
	
	public Cipher getCipher() {
		return this.cipher;
	}
	
	public byte[] getKey() {
		return this.key;
	}
	
	public byte[] convert(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
		return this.cipher.doFinal(data);
	}
	
	public boolean test() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		int i = this.pageSize;

		SecretKeySpec skeyspec = new SecretKeySpec(this.key, this.cipher.getAlgorithm());
	
		synchronized (this.cipher) {
			this.cipher.init(Cipher.DECRYPT_MODE, skeyspec);
			
			while (i-->0 && this.predicate.test(this.convert(this.pages.get(i)))) {
				System.out.println("test #" + i + '\t' + this.cipher.getAlgorithm() + '\t' + this.getKeyToString());
			}
		}
		
		return -1 == i;
	}
	
	protected boolean testFirst() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		this.key = this.keyIterator.getKey();

		return this.test();
	}
	
	protected boolean testNext() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		this.key = this.keyIterator.next();

		return this.test();
	}
	
	public boolean testAll() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		boolean found = this.testFirst();
			
		while (this.canLoop && !found) {
			found = this.testNext();
		}
		
		return found;
	}

	public String getKeyToString() {
		final char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[this.key.length * 2];

		for (int j=0; j<this.key.length; j++) {
			int v = this.key[j] & 0xFF;
			hexChars[j*2] = hexArray[v >>> 4];
			hexChars[j*2+1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("===== cipherRead =\n");
		sb.append("name: ").append(this.cipher.getAlgorithm()).append('\n');
		sb.append("key:  ").append(this.getKeyToString()).append('\n');

		return sb.toString();
	}

}
