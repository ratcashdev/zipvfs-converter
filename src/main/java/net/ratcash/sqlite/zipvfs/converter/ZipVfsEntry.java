package net.ratcash.sqlite.zipvfs.converter;

public class ZipVfsEntry {
	public static final int HEADER_SIZE = 8;

	protected long size;
	protected long offset;
	protected boolean isBTree;

	public ZipVfsEntry(byte[] buffer, int length) {
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		// TODO

		return sb.toString();
	}

}
