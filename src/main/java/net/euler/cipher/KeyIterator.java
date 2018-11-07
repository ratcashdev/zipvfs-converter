package net.euler.cipher;

import java.util.Iterator;

public class KeyIterator implements Iterator<byte[]> {
	
	public class NoKeyException extends Exception {
		private static final long serialVersionUID = 7774352835418800787L;
	};

	protected KeySet keySet;
	protected int positionMax;
	protected int[] positions;
	protected byte[] key;

	public KeyIterator(KeySet keySet, int length) {
		this.keySet = keySet;
		this.setPositionMax();
		this.positions = new int[length];
		
		for (int i=0; i<this.positions.length; i++) {
			this.positions[i] = 0;
		}
		
		this.makeKey();
	}

	public KeyIterator(KeySet keySet, String key) {
		this.keySet = keySet;
		this.setPositionMax();
		
		int length = key.length();
		this.positions = new int[length/2];

		for (int i=0; i<length; i+=2) {
			byte b = (byte) ((Character.digit(key.charAt(i), 16) << 4) + Character.digit(key.charAt(i+1), 16));
			int p = this.keySet.getAlphabet().indexOf(b);
			this.positions[i/2] = -1 == p ? 0 : p;
		}
		
		this.makeKey();
	}
	
	protected void setPositionMax() {
		this.positionMax = this.keySet.getAlphabet().size() - 1;
	}

	protected void makeKey() {
		this.key = new byte[this.positions.length];
		
		for (int i=0; i<this.positions.length; i++) {
			this.key[i] = this.keySet.getAlphabet().get(this.positions[i]);
		}
	}
	
	public byte[] getKey() {
		return this.key;
	}
	
	public boolean hasNext() {
		boolean isMax = true;
		
		for (int i=0; i<this.positions.length && isMax; i++) {
			isMax = this.positions[i] == this.positionMax;
		}
		
		return !isMax;
	}
	
	synchronized public byte[] next() {
		int i = this.positions.length;

		while (i-->0) {
			if (this.positions[i] == this.positionMax) {
				int position = 0;
				this.positions[i] = position;
				
				this.key[i] = this.keySet.getAlphabet().get(position);
			} else {
				int position = this.positions[i] + 1;
				this.positions[i] = position;
				
				this.key[i] = this.keySet.getAlphabet().get(position);
				
				break;
			}
		}
		
		byte[] bytes = new byte[this.key.length];
		System.arraycopy(this.key, 0, bytes, 0, this.key.length);
		
		return bytes;
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

		sb.append("========= keyIterator =\n");
		sb.append("max:      ").append(this.positionMax).append('\n');
		sb.append("position: ");
		
		for (int p : this.positions) {
			sb.append(p).append('\t');
		}
		
		sb.append('\n');

		return sb.toString();
	}

}
