package net.ratcash.sqlite.zipvfs.converter;

import java.util.ArrayList;
import java.util.List;

public class ZipVfsNode {
	public static final int HEADER_SIZE = 4;

	protected long height;
	protected long numberOfEntries;

	protected List<ZipVfsEntry> entryList;

	public ZipVfsNode(byte[] buffer, int length) {
		this.entryList = new ArrayList<ZipVfsEntry>();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		// TODO

		return sb.toString();
	}

}
