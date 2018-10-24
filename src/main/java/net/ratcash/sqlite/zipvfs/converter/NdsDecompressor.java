package net.ratcash.sqlite.zipvfs.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

public class NdsDecompressor {

	public static void main(String[] args) {
		System.out.println("specs: http://www.sqlite.org/zipvfs/doc/trunk/www/fileformat.wiki\n");

		try {
			new NdsDecompressor().decodeNDS(args[0]);
		} catch (IOException | DataFormatException ex) {
			Logger.getLogger(NdsDecompressor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void decodeNDS(String filepath) throws FileNotFoundException, IOException, DataFormatException {
		File dbFile = new File(filepath);
		String convertedFile = dbFile.getAbsolutePath() + ".sqlite";
		FileOutputStream outputStream = new FileOutputStream(new File(convertedFile));

		try (FileChannel fc = FileChannel.open(dbFile.toPath(), StandardOpenOption.READ)) {
			ZipVfsFile zipvfs = new ZipVfsFile();

			zipvfs.parse(fc);

			System.out.println(zipvfs);

			zipvfs.pipe(outputStream);

			outputStream.close();

			System.out.println("Conversion done.\nOpen '" + convertedFile + "' in your faviroute Sqlite Front-End.");
		}
	}

}
