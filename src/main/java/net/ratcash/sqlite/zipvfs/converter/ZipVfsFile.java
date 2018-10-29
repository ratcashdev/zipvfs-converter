package net.ratcash.sqlite.zipvfs.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ZipVfsFile {
	
	protected static final int BUFFER_SIZE = 1024;

	protected ZipVfsHeader header;
	protected CipherKey cipherKey;
	
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
	
	public ZipVfsHeader getHeader() {
		return this.header;
	}
	
	public boolean isReadable() {
		int nbFreeBlocks = 0;
		
		for (ZipVfsPageMap pageMap : this.header.getPageMap()) {
			ZipVfsDataArea dataArea = pageMap.getDataArea();
			
			if (dataArea.isZLibContent()) {
				nbFreeBlocks++;
			} else {
				System.out.println(dataArea.getPageNumber() + " | unrecognized data: " + dataArea.getHexData().substring(0, 12) + "... (" + dataArea.getData().length + ')');
			}
		}
		
		return nbFreeBlocks == this.header.getPageMap().size();
	}
	
	public boolean findCipherKey(String cipherName, int keyLength) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		CipherKey cipherKey = new CipherKey(cipherName, keyLength);
		
		return this.findCipherKey(cipherKey);
	}
	
	public boolean findCipherKey(String cipherName, String keyValue) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		CipherKey cipherKey = new CipherKey(cipherName, keyValue);
		
		return this.findCipherKey(cipherKey);
	}
	
	public boolean findCipherKey(CipherKey cipherKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		int iPageMap;
		int nbPageMaps = this.header.getPageMap().size();
		
		this.cipherKey = cipherKey;
		
		do {
			this.cipherKey.prepare();
			iPageMap = 0;
			
			while (iPageMap < nbPageMaps) {
				ZipVfsPageMap pageMap = this.header.getZipPageMap(iPageMap);
				ZipVfsDataArea dataArea = pageMap.getDataArea();
				byte[] data = dataArea.getData();

				data = this.cipherKey.getCipher().doFinal(data);
				iPageMap++;
				
				if (dataArea.isZLibContent(data)) {
					System.out.println("test #" + iPageMap + '\t' + this.cipherKey.getCipher().getAlgorithm() + '\t' + this.cipherKey.getKeyToString());
				} else {
					break;
				}
			}
		} while (iPageMap!=nbPageMaps && this.cipherKey.increment());
		
		return iPageMap == nbPageMaps;
	}
	
	public void pipe(OutputStream outputStream) throws DataFormatException, IOException {
		Inflater inflater = new Inflater(false);
		byte[] outBytes = new byte[ZipVfsFile.BUFFER_SIZE];

		for (ZipVfsPageMap pageMap : this.header.getPageMap()) {
			ZipVfsDataArea dataArea = pageMap.getDataArea();
			byte[] data = dataArea.getData();
			
			if (null != this.cipherKey) {
				try {
					data = this.cipherKey.getCipher().doFinal(data);
				} catch (IllegalBlockSizeException | BadPaddingException e) {
					e.printStackTrace();
				}
			}

			inflater.setInput(data);

			while (!inflater.finished()) {  
			    int count = inflater.inflate(outBytes);  
			    outputStream.write(outBytes, 0, count); 
			}  

			inflater.reset();
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(this.header.toString()).append('\n');
		
		if (null != this.cipherKey) {
			sb.append(this.cipherKey.toString()).append('\n');
		}
		
		for (ZipVfsPageMap pageMap : this.header.getPageMap()) {
			sb.append(pageMap.toString()).append('\n');
			sb.append(pageMap.getDataArea().toString()).append('\n');
		}

		return sb.toString();
	}

}
