package net.ratcash.sqlite.zipvfs.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 *
 */
public class ZipVfsFile {

	protected ZipVfsHeader header;
	
	public void parse(FileChannel fc) throws IOException {
		int bytesRead;
		
		// reading the header
		ByteBuffer headerBuffer = ByteBuffer.allocate(ZipVfsHeader.SIZE);
		bytesRead = fc.read(headerBuffer);
		headerBuffer.flip();
		
		assert bytesRead == ZipVfsHeader.SIZE;

		this.header = new ZipVfsHeader(headerBuffer.array());

		// reading the page Maps
		ByteBuffer pageMapBuffer = ByteBuffer.allocate(ZipVfsPageMap.SIZE);
		int nbPageMap = this.header.countPageMap();

		for (int i = 0; i < nbPageMap; i++) {
			bytesRead = fc.read(pageMapBuffer);
			pageMapBuffer.flip();

			assert bytesRead == ZipVfsPageMap.SIZE;

			ZipVfsPageMap pageMap = new ZipVfsPageMap(pageMapBuffer.array());
			this.header.addPageMap(pageMap);

			pageMapBuffer.clear();
		}

		// reading the page Data
		long pageNumber = 0;

		for (ZipVfsPageMap pageMap : this.header.getPageMap()) {
			int bufferSize = Math.toIntExact(ZipVfsDataArea.HEADER_SIZE + pageMap.getSize());
			ByteBuffer pageBuffer = ByteBuffer.allocate(bufferSize);

			bytesRead = fc.read(pageBuffer, pageMap.getOffset());
			pageBuffer.flip();

			assert bytesRead == bufferSize;

			ZipVfsDataArea dataArea = new ZipVfsDataArea(pageBuffer.array(), bufferSize);
			pageMap.setDataArea(dataArea);

			assert ++pageNumber == dataArea.getPageNumber();
		}
	}
	
	public void pipe(OutputStream outputStream) throws DataFormatException, IOException {
		Inflater inflater = new Inflater(false);
		byte[] outBytes = new byte[1024];

		for (ZipVfsPageMap pageMap : this.header.getPageMap()) {
			ZipVfsDataArea dataArea = pageMap.getDataArea();
			
			int magicOffset = dataArea.getZLibMagicOffset();

			if (magicOffset == 0) {
				inflater.setInput(dataArea.getData());

				while (!inflater.finished()) {  
				    int count = inflater.inflate(outBytes);  
				    outputStream.write(outBytes, 0, count); 
				}  

				inflater.reset();
			} else {
				System.out.println("unrecognized data: " + dataArea.getHexData().substring(0, 12) + "...\n"); // TODO
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(this.header.toString()).append('\n');
		
		for (ZipVfsPageMap pageMap : this.header.getPageMap()) {
			sb.append(pageMap.toString()).append('\n');
			sb.append(pageMap.getDataArea().toString()).append('\n');
		}

		return sb.toString();
	}

}
