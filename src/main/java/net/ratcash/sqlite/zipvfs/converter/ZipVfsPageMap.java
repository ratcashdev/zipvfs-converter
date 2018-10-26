package net.ratcash.sqlite.zipvfs.converter;

public class ZipVfsPageMap {
	public static final int SIZE = 8;

	protected long offset;
	protected long size;
	protected long unusedBytes;

	protected ZipVfsDataArea dataArea;

	public ZipVfsPageMap(byte[] buffer) {
		if (buffer.length != ZipVfsPageMap.SIZE)
			throw new IllegalArgumentException("Buffer must be at least 8 bytes long from start.");

		this.offset = LongBigEndian.toLong(0, 5, buffer); // 5 bytes = 40 bits
		this.size = LongBigEndian.toLong(5, 3, buffer) >> 7; // 3 bytes - 7 bits = 17 bits
		this.unusedBytes = LongBigEndian.toLong(7, 1, buffer) & 0x7F; // last 7 bits only
	}

	public long getOffset() {
		return this.offset;
	}

	public long getSize() {
		return this.size;
	}

	public long getUnusedBytes() {
		return this.unusedBytes;
	}
	
	public ZipVfsDataArea getDataArea() {
		return this.dataArea;
	}
	
	public void setDataArea(ZipVfsDataArea dataArea) {
		this.dataArea = dataArea;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("============ page-map =\n");
		sb.append("offset:      ").append(this.offset).append('\n');
		sb.append("size:        ").append(this.size).append('\n');
		sb.append("unusedBytes: ").append(this.unusedBytes).append('\n');

		return sb.toString();
	}
}
