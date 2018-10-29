package net.ratcash.sqlite.zipvfs.converter;

public class LongBigEndian {

	public static long toLong(byte[] buffer, int... x) {
		int len = x.length;
		long result = 0;
		int shifter = (x.length - 1) * 8;
		for (int i = 0; i < len; i++) {
			result += (buffer[x[i]] & 0xFF) << shifter;
			shifter -= 8;
		}
		return result;
	}

	public static long toLong(byte... values) {
		int len = values.length;
		long result = 0;
		int shifter = (values.length - 1) * 8;
		for (int i = 0; i < len; i++) {
			result += (values[i] & 0xFF) << shifter;
			shifter -= 8;
		}
		return result;
	}

	public static long toLong(int from, int len, byte[] buffer) {
		int[] indices = new int[len];
		for (int i = from; i < from + len; i++)
			indices[i - from] = i;

		return LongBigEndian.toLong(buffer, indices);
	}
}
