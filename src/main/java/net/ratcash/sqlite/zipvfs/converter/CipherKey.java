package net.ratcash.sqlite.zipvfs.converter;

public class CipherKey {

	protected byte[] key;

	public CipherKey(int length) {
		this.key = new byte[length];
	}
	
	public void reset() {
		for (int i=0; i<this.key.length; i++) {
			this.key[i] = 0;
		}
	}

	public void setKey(byte[] key) {
		this.key = key;
	}
	
	public byte[] getKey() {
		return this.key;
	}
	
	public boolean increment() {
		int i = this.key.length;
		
		while (i-->0 && ++this.key[i] == 0);
		
		return i != -1;
	}

	public String getKeyToString() {
		final char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[this.key.length * 2];

		for (int j = 0; j < this.key.length; j++) {
			int v = this.key[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("==== cipherKey =\n");
		sb.append("key: ").append(this.getKeyToString()).append('\n');

		return sb.toString();
	}

}
