package net.ratcash.sqlite.zipvfs.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZipVfsHeader {
	public static final int SIZE = 200; // 0xC8
	public static final int ZIPVFS_START = 100; // 0x64

	protected String encodingAlgorithm;
	protected long iFreeSlot;
	protected long iDataStart;
	protected long iDataEnd;
	protected long iGapStart;
	protected long iGapEnd;
	protected long iSize;
	protected long nFreeSlot;
	protected long nFreeByte;
	protected long nFreeFragment;
	protected int pgsz;
	protected int iVersion;

	protected List<ZipVfsPageMap> pageMapList;

	public ZipVfsHeader(byte[] buffer) {
		if (buffer.length < ZipVfsHeader.SIZE)
			throw new IllegalArgumentException("Buffer must be at a minimum 200 bytes.");

		this.pageMapList = new ArrayList<ZipVfsPageMap>();

		this.encodingAlgorithm = new String(Arrays.copyOfRange(buffer, 3, 16)); // ZV-%name% [16]
		/*
		 * String underlyingPageLayer = new String(Arrays.copyOfRange(buffer, 92, 100));
		 * System.out.println("underlyingPageLayer: " + underlyingPageLayer);
		 */

		this.iFreeSlot = LongBigEndian.toLong(100, 8, buffer);
		this.iDataStart = LongBigEndian.toLong(108, 8, buffer);
		this.iDataEnd = LongBigEndian.toLong(116, 8, buffer);
		this.iGapStart = LongBigEndian.toLong(124, 8, buffer);
		this.iGapEnd = LongBigEndian.toLong(132, 8, buffer);
		this.iSize = LongBigEndian.toLong(140, 8, buffer);
		this.nFreeSlot = LongBigEndian.toLong(148, 8, buffer);
		this.nFreeByte = LongBigEndian.toLong(156, 8, buffer);
		this.nFreeFragment = LongBigEndian.toLong(164, 8, buffer);

		this.pgsz = Math.toIntExact(LongBigEndian.toLong(172, 4, buffer));
		this.iVersion = Math.toIntExact(LongBigEndian.toLong(176, 4, buffer));
	}

	public long getPageMapSize() {
		return this.iDataStart - ZipVfsHeader.SIZE;
	}

	public int countPageMap() {
		long getPageMapSize = getPageMapSize();

		return Math.toIntExact(getPageMapSize / 64);
	}

	public void addPageMap(ZipVfsPageMap pageMap) {
		this.pageMapList.add(pageMap);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("================== header =\n");
		sb.append("encodingAlgorithm: ").append(this.encodingAlgorithm).append('\n');
		sb.append("iFreeSlot:         ").append(this.iFreeSlot).append('\n');
		sb.append("iDataStart:        ").append(this.iDataStart).append('\n');
		sb.append("iDataEnd:          ").append(this.iDataEnd).append('\n');
		sb.append("iGapStart:         ").append(this.iGapStart).append('\n');
		sb.append("iGapEnd:           ").append(this.iGapEnd).append('\n');
		sb.append("iSize:             ").append(this.iSize).append('\n');
		sb.append("nFreeSlot:         ").append(this.nFreeSlot).append('\n');
		sb.append("nFreeByte:         ").append(this.nFreeByte).append('\n');
		sb.append("nFreeFragment:     ").append(this.nFreeFragment).append('\n');
		sb.append("pgsz:              ").append(this.pgsz).append('\n');
		sb.append("iVersion:          ").append(this.iVersion).append('\n');
		sb.append("|pageMap|:         ").append(this.pageMapList.size()).append('\n');

		return sb.toString();
	}

	public ZipVfsPageMap getZipPageMap(int pageNumber) {
		return this.pageMapList.get(pageNumber);
	}

	public List<ZipVfsPageMap> getPageMap() {
		return this.pageMapList;
	}
}
