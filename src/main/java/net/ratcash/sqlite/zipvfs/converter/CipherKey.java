package net.ratcash.sqlite.zipvfs.converter;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CipherKey {

	protected Cipher cipher;
	protected byte[] key;

	public CipherKey(String algorithmName, int length) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.cipher = Cipher.getInstance(algorithmName);
		this.key = new byte[length];
		
		for (int i=0; i<this.key.length; i++) {
			this.key[i] = 0;
		}
	}

	public CipherKey(String algorithmName, String key) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.cipher = Cipher.getInstance(algorithmName);
		
		int length = key.length();
		this.key = new byte[length/2];

		for (int i=0; i<length; i+=2) {
			this.key[i/2] = (byte) ((Character.digit(key.charAt(i), 16) << 4) + Character.digit(key.charAt(i+1), 16));
		}
	}
	
	public Cipher getCipher() {
		return this.cipher;
	}

	public byte[] getKey() {
		return this.key;
	}
	
	public void prepare() throws InvalidKeyException {
		SecretKeySpec skeyspec = new SecretKeySpec(this.key, this.cipher.getAlgorithm());
	
		this.cipher.init(Cipher.DECRYPT_MODE, skeyspec);
	}

	public boolean increment() {
		int i = this.key.length;

		while (i-->0 && ++this.key[i]==0);

		return i != -1;
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

		sb.append("===== cipherKey =\n");
		sb.append("name: ").append(this.cipher.getAlgorithm()).append('\n');
		sb.append("key:  ").append(this.getKeyToString()).append('\n');

		return sb.toString();
	}

}
