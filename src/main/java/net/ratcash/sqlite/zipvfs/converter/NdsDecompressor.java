package net.ratcash.sqlite.zipvfs.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.euler.cipher.CipherRead;
import net.euler.cipher.CipherReadFactory;
import net.euler.cipher.KeyIterator;
import net.euler.cipher.KeySet;
import net.euler.cipher.KeySet.Alphabet;

public class NdsDecompressor {
	
	public enum OPTION {
		filePath,
		cipherName,
		startKey,
		nbProc,
	};

	public static void main(String[] args) {
		System.out.println("specs: http://www.sqlite.org/zipvfs/doc/trunk/www/fileformat.wiki\n");

		// fixed block size: Blowfish AES RSA DES RC2
		// candidate: RC4 ARCFOUR
		
		EnumMap<OPTION, String> config = new EnumMap<OPTION, String>(OPTION.class);

		config.put(OPTION.filePath, args[0]);
		config.put(OPTION.cipherName, "RC4");
		config.put(OPTION.startKey, "00000000000000000000000000000000");
		
		switch (args.length) {
		case 3:
			config.put(OPTION.startKey, args[2]);
		case 2: 
			config.put(OPTION.cipherName, args[1]);
		}
		
		KeyIterator keyIterator; 
		{
			List<Alphabet> alphabets = new ArrayList<Alphabet>();
			for (int i=3; i<args.length; i++) {
				alphabets.add(Alphabet.valueOf(args[i]));
			}
			
			KeySet keySet = new KeySet(alphabets);
			keyIterator = new KeyIterator(keySet, config.get(OPTION.startKey));
		}
		
		if ("dump".equalsIgnoreCase(config.get(OPTION.cipherName))) {
			try {
				new NdsDecompressor().dumpNDS(config.get(OPTION.filePath));
			} catch (IOException ex) {
				Logger.getLogger(NdsDecompressor.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			
			try {
				new NdsDecompressor().decodeNDS(config, keyIterator);
			} catch (IOException | DataFormatException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
				Logger.getLogger(NdsDecompressor.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public void decodeNDS(EnumMap<OPTION, String> config, KeyIterator keyIterator) throws FileNotFoundException, IOException, DataFormatException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		File dbFile = new File(config.get(OPTION.filePath));
		String convertedFile = dbFile.getAbsolutePath() + ".sqlite";
		String dumpFile = dbFile.getAbsolutePath() + ".txt";
		String keyFile = dbFile.getAbsolutePath() + "-key.txt";
		
		try (FileChannel fc = FileChannel.open(dbFile.toPath(), StandardOpenOption.READ)) {
			ZipVfsFile zipvfs = new ZipVfsFile();

			zipvfs.parse(fc);

			//System.out.println(zipvfs);

			if (!zipvfs.isReadable()) {
				/*
				List<byte[]> pages = zipvfs.getSampleDataPages(2);
				Predicate<byte[]> predicate = ZipVfsDataArea::isReadable;
				CipherRead cipherRead = new CipherRead(config.get(OPTION.cipherName), keyIterator, pages, predicate);
				
				if (cipherRead.testAll()) {
					this.info(cipherRead.toString(), keyFile);
					
					zipvfs.setCipher(cipherRead.getCipher());
				} else {
					System.out.println("CipherKey not found.");
				}
				*/
				List<byte[]> pages = zipvfs.getSampleDataPages(2);
				Predicate<byte[]> predicate = ZipVfsDataArea::isReadable;
				CipherReadFactory cipherReadFactory = new CipherReadFactory();
				
				cipherReadFactory.fill(config.get(OPTION.cipherName), keyIterator, pages, predicate);
				cipherReadFactory.process();
				
				CipherRead cipherRead = cipherReadFactory.promote();
				
				if (null != cipherRead && cipherRead.testAll()) {
					this.info(cipherRead.toString(), keyFile);
					
					zipvfs.setCipher(cipherRead.getCipher());
				} else {
					System.out.println("CipherKey not found.");
				}
			}

			this.info(zipvfs.toString(), dumpFile);
			
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
	
	public void info(String content, String filepath) throws DataFormatException, IOException {
		FileOutputStream outputStream = new FileOutputStream(new File(filepath), false);
		
		outputStream.write(content.getBytes());
		outputStream.close();
		
		System.out.println("Info done.\nOpen '" + filepath + "' to see the cipher Key.");
	}

}
