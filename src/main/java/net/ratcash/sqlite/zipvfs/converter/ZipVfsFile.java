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
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class ZipVfsFile {

	protected ZipVfsHeader header;
	protected Cipher cipher;
	
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
		int iPageMap;
		int nbPageMaps = this.header.getPageMap().size();
		CipherKey cipherKey = new CipherKey(keyLength);
		
		// TODO resume cracking
		// 0000 0000 0000 0000 0000 0000 10AB 13AD
		/*
		byte[] previousKey = 
			{ 0x00, 0x00, 0x00, 0x00
			, 0x00, 0x00, 0x00, 0x00
			, 0x00, 0x00, 0x00, 0x00
			, 0x10, (byte) 0xAB, (byte) 0x13, (byte) 0xAD };
		cipherKey.setKey(previousKey);
		*/

		this.cipher = Cipher.getInstance(cipherName);
		
		do {
			SecretKeySpec skeyspec = new SecretKeySpec(cipherKey.getKey(), cipherName);
			this.cipher.init(Cipher.DECRYPT_MODE, skeyspec);
			iPageMap = 0;
			
			while (iPageMap < nbPageMaps) {
				ZipVfsPageMap pageMap = this.header.getZipPageMap(iPageMap);
				ZipVfsDataArea dataArea = pageMap.getDataArea();
				byte[] data = dataArea.getData();

				data = this.cipher.doFinal(data);
				iPageMap++;
				
				if (dataArea.isZLibContent(data)) {
					System.out.println("test #" + iPageMap + '\t' + cipherKey.getKeyToString());
				} else {
					break;
				}
			}
		} while (iPageMap!=nbPageMaps && cipherKey.increment());
		
		return iPageMap == nbPageMaps;
	}
	
	public void pipe(OutputStream outputStream) throws DataFormatException, IOException {
		Inflater inflater = new Inflater(false);
		byte[] outBytes = new byte[1024];

		for (ZipVfsPageMap pageMap : this.header.getPageMap()) {
			ZipVfsDataArea dataArea = pageMap.getDataArea();
			byte[] data = dataArea.getData();
			
			if (null != this.cipher) {
				try {
					data = this.cipher.doFinal(data);
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
		
		for (ZipVfsPageMap pageMap : this.header.getPageMap()) {
			sb.append(pageMap.toString()).append('\n');
			sb.append(pageMap.getDataArea().toString()).append('\n');
		}

		return sb.toString();
	}

}
