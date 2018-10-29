package net.ratcash.sqlite.zipvfs.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class NdsDecompressor {

	public static void main(String[] args) {
		System.out.println("specs: http://www.sqlite.org/zipvfs/doc/trunk/www/fileformat.wiki\n");

		// fixed block size: Blowfish AES RSA DES RC2
		// candidate: RC4 ARCFOUR

		String filepath = args[0];
		String cipherName = "RC4";
		String cipherKey = "00000000000000000000000000000000";
		
		switch (args.length) {
		case 3:
			cipherKey = args[2];
		case 2: 
			cipherName = args[1];
		}


		if ("dump".equalsIgnoreCase(cipherName)) {
			try {
				new NdsDecompressor().dumpNDS(filepath);
			} catch (IOException ex) {
				Logger.getLogger(NdsDecompressor.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			try {
				new NdsDecompressor().decodeNDS(filepath, cipherName, cipherKey);
			} catch (IOException | DataFormatException ex) {
				Logger.getLogger(NdsDecompressor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public void decodeNDS(String filepath, String cipherName, String cipherKey) throws FileNotFoundException, IOException, DataFormatException {
		File dbFile = new File(filepath);
		String convertedFile = dbFile.getAbsolutePath() + ".sqlite";
		String dumpFile = dbFile.getAbsolutePath() + ".txt";
		
		try (FileChannel fc = FileChannel.open(dbFile.toPath(), StandardOpenOption.READ)) {
			ZipVfsFile zipvfs = new ZipVfsFile();

			zipvfs.parse(fc);

			//System.out.println(zipvfs);

			if (!zipvfs.isReadable()) {
				try {
					zipvfs.findCipherKey(cipherName, cipherKey);
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
						| IllegalBlockSizeException | BadPaddingException ex) {
					Logger.getLogger(NdsDecompressor.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

			this.info(zipvfs, dumpFile);
			
			this.convert(zipvfs, convertedFile);
		}
	}
	
	public void dumpNDS(String filepath) throws IOException {		
		File dbFile = new File(filepath);
		String dumpPath = dbFile.getAbsolutePath();
		
		try (FileChannel fc = FileChannel.open(dbFile.toPath(), StandardOpenOption.READ)) {
			ZipVfsFile zipvfs = new ZipVfsFile();
	
			zipvfs.parse(fc);
	
			//System.out.println(zipvfs);
	
			this.dump(zipvfs, dumpPath + ".pkg");
		}
	}
	
	public void convert(ZipVfsFile zipvfs, String filepath) throws DataFormatException, IOException {
		FileOutputStream outputStream = new FileOutputStream(new File(filepath));
		
		zipvfs.pipe(outputStream);
		outputStream.close();
		
		System.out.println("Conversion done.\nOpen '" + filepath + "' in your faviroute Sqlite Front-End.");
	}
	
	public void dump(ZipVfsFile zipvfs, String directorypath) throws IOException {
		String metapath = directorypath + File.separatorChar + "_meta.txt";
		File metafile = new File(metapath);
		
		if (!metafile.getParentFile().exists()) {
			metafile.getParentFile().mkdirs(); 
		}
		
		{
			FileOutputStream outputStream = new FileOutputStream(metafile, false);
			outputStream.write(zipvfs.toString().getBytes());
			outputStream.close();
		}
		
		List<ZipVfsPageMap> pages = zipvfs.getHeader().getPageMap();
		int pageSize = pages.size();
		String lastPageNumber = String.valueOf(pages.get(pageSize - 1).getDataArea().getPageNumber());
		String pattern = "%0" + lastPageNumber.length() + "d.dat";
				
		for (ZipVfsPageMap page : zipvfs.getHeader().getPageMap()) {
			ZipVfsDataArea data = page.getDataArea();
			String filename = String.format(pattern, data.getPageNumber());
			File outputFile = new File(directorypath + File.separatorChar + filename);
			
			FileOutputStream outputStream = new FileOutputStream(outputFile, false);
			outputStream.write(data.getData());
			outputStream.close();
		}
		
		System.out.println("Dump done.\nOpen '" + directorypath + "'.");
	}
	
	public void info(ZipVfsFile zipvfs, String filepath) throws DataFormatException, IOException {
		FileOutputStream outputStream = new FileOutputStream(new File(filepath), false);
		
		outputStream.write(zipvfs.toString().getBytes());
		outputStream.close();
		
		System.out.println("Info done.\nOpen '" + filepath + "' to see the cipher Key.");
	}

}
