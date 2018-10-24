package net.ratcash.sqlite.zipvfs.converter;

import java.util.Arrays;

/**
 *
 */
public class ZipVfsDataArea {
	public static final int HEADER_SIZE = 6;

	protected long pageNumber = 0; // ) pageNumber + payloadSize = Slot-Header
	protected long payloadSize = 0; // )
	protected byte[] data;

	public ZipVfsDataArea(byte[] buffer, int length) {
		if (length < ZipVfsDataArea.HEADER_SIZE)
			throw new IllegalArgumentException("Buffer must be at a minimum 6 bytes.");

		this.pageNumber = LongBigEndian.toLong(0, 4, buffer) >> 1; // 4bytes - 1bit = 31 bits
		this.payloadSize = LongBigEndian.toLong(3, 3, buffer) & 0x1FFFF; // last 17 bits only

		this.data = Arrays.copyOfRange(buffer, ZipVfsDataArea.HEADER_SIZE, length);
	}

	public long getPageNumber() {
		return this.pageNumber;
	}

	public byte[] getData() {
		return this.data;
	}

	public int getZLibMagicOffset() {
		int offset = 0; // hard coded
		// for (int offset = 0; offset < this.data.length -1; offset++) {
		if ((this.data[offset] & 0xFF) == 0x78) {
			switch (this.data[offset + 1] & 0xFF) {
			case 0x01: // no compression
			case 0x9C: // default
			case 0xDA: // high compression
				return offset;
			}
		}
		// }

		return -1;
	}

	public String getHexData() {
		final char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[this.data.length * 2];

		for (int j = 0; j < this.data.length; j++) {
			int v = this.data[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("============ data area =\n");
		sb.append("pageNumber:  ").append(this.pageNumber).append('\n');
		sb.append("payloadSize: ").append(this.payloadSize).append('\n');
		sb.append("|data|:      ").append(this.data.length).append('\n');

		return sb.toString();
	}

}
