package net.euler.cipher;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.ratcash.sqlite.zipvfs.converter.NdsDecompressor;

public class CipherReadFactory {
	
	int nbProcessor;
	List<CipherRead> cipherReads;
	
	public CipherReadFactory() {
		this.nbProcessor = Runtime.getRuntime().availableProcessors();
		this.cipherReads = new ArrayList<CipherRead>(this.nbProcessor);
	}

	public int fill(String algorithmName, KeyIterator keyIterator, List<byte[]> pages, Predicate<byte[]> predicate) throws NoSuchAlgorithmException, NoSuchPaddingException {
		for (int i=0; i<=this.nbProcessor; i++) {
			CipherRead cipherRead = new CipherRead(algorithmName, keyIterator, pages, predicate);
			this.cipherReads.add(cipherRead);
		}
		
		return this.nbProcessor;
	}
	
	public void stopAll() {
		for (CipherRead cipherRead : this.cipherReads) {
			cipherRead.stop();
		}
	}
		
	public CipherRead promote() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		for (CipherRead cipherRead : this.cipherReads) {
			if (cipherRead.test()) {
				return cipherRead;
			}
		}
		
		return null;
	}
	
	public void process() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		this.cipherReads.parallelStream().forEach(cipherRead -> {
			try {
				cipherRead.testAll();
				this.stopAll();
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
				Logger.getLogger(NdsDecompressor.class.getName()).log(Level.SEVERE, null, ex);
			}
	    });
	}

}
