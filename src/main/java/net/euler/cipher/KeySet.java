package net.euler.cipher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeySet {
	
	public static enum Alphabet {
		digits,
		hexa_low,
		hexa_up,
		ascii_low,
		ascii_up,
		letters,
		punctuation,
		controls, 
		extended,
		printable,
		all,
	};
	
	protected static final Byte[] DIGITS = { 
		(byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, 
		(byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39, 
	};
	
	protected static final Byte[] ASCII_LOWERCASE = { 
		(byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x64, (byte) 0x65, 
		(byte) 0x66, (byte) 0x67, (byte) 0x68, (byte) 0x69, (byte) 0x6A, 
		(byte) 0x6B, (byte) 0x6C, (byte) 0x6D, (byte) 0x6E, (byte) 0x6F, 
		(byte) 0x70, (byte) 0x71, (byte) 0x72, (byte) 0x73, (byte) 0x74, 
		(byte) 0x75, (byte) 0x76, (byte) 0x77, (byte) 0x78, (byte) 0x79, 
		(byte) 0x7A, 
	};
	
	protected static final Byte[] ASCII_UPPERCASE = { 
		(byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x44, (byte) 0x45, 
		(byte) 0x46, (byte) 0x47, (byte) 0x48, (byte) 0x49, (byte) 0x4A, 
		(byte) 0x4B, (byte) 0x4C, (byte) 0x4D, (byte) 0x4E, (byte) 0x4F, 
		(byte) 0x50, (byte) 0x51, (byte) 0x52, (byte) 0x53, (byte) 0x54, 
		(byte) 0x55, (byte) 0x56, (byte) 0x57, (byte) 0x58, (byte) 0x59, 
		(byte) 0x5A, 
	};
	
	protected static final Byte[] HEXA_LOWERCASE = {
		(byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, 
		(byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39, 
		(byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x64, (byte) 0x65, 
		(byte) 0x66, 
	};
	
	protected static final Byte[] HEXA_UPPERCASE = {
		(byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, 
		(byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39, 
		(byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x44, (byte) 0x45, 
		(byte) 0x46, 
	};
	
	protected static final Byte[] PUNCTUATION = {
		(byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, 
		(byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2A, 
		(byte) 0x2B, (byte) 0x2C, (byte) 0x2D, (byte) 0x2E, (byte) 0x2F, 
		(byte) 0x3A, (byte) 0x3B, (byte) 0x3C, (byte) 0x3D, (byte) 0x3E, 
		(byte) 0x3F, (byte) 0x40, 
		(byte) 0x5B, (byte) 0x5C, (byte) 0x5D, (byte) 0x5E, (byte) 0x5F, 
		(byte) 0x7B, (byte) 0x7C, (byte) 0x7D, (byte) 0x7E, 
	};
	
	protected static final Byte[] CONTROLS = {
		(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, 
		(byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, 
		(byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, 
		(byte) 0x0F,
		(byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, 
		(byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, 
		(byte) 0x1A, (byte) 0x1B, (byte) 0x1C, (byte) 0x1D, (byte) 0x1E, 
		(byte) 0x1F,
		(byte) 0x20,
	};
	
	protected static final Byte[] EXTENDED = {
		(byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, 
		(byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x89, 
		(byte) 0x8A, (byte) 0x8B, (byte) 0x8C, (byte) 0x8D, (byte) 0x8E, 
		(byte) 0x8F, 
		(byte) 0x90, (byte) 0x91, (byte) 0x92, (byte) 0x93, (byte) 0x94, 
		(byte) 0x95, (byte) 0x96, (byte) 0x97, (byte) 0x98, (byte) 0x99, 
		(byte) 0x9A, (byte) 0x9B, (byte) 0x9C, (byte) 0x9D, (byte) 0x9E, 
		(byte) 0x9F, 
		(byte) 0xA0, (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, 
		(byte) 0xA5, (byte) 0xA6, (byte) 0xA7, (byte) 0xA8, (byte) 0xA9, 
		(byte) 0xAA, (byte) 0xAB, (byte) 0xAC, (byte) 0xAD, (byte) 0xAE, 
		(byte) 0xAF, 
		(byte) 0xB0, (byte) 0xB1, (byte) 0xB2, (byte) 0xB3, (byte) 0xB4, 
		(byte) 0xB5, (byte) 0xB6, (byte) 0xB7, (byte) 0xB8, (byte) 0xB9, 
		(byte) 0xBA, (byte) 0xBB, (byte) 0xBC, (byte) 0xBD, (byte) 0xBE, 
		(byte) 0xBF, 
		(byte) 0xC0, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4, 
		(byte) 0xC5, (byte) 0xC6, (byte) 0xC7, (byte) 0xC8, (byte) 0xC9, 
		(byte) 0xCA, (byte) 0xCB, (byte) 0xCC, (byte) 0xCD, (byte) 0xCE, 
		(byte) 0xCF, 
		(byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, 
		(byte) 0xD5, (byte) 0xD6, (byte) 0xD7, (byte) 0xD8, (byte) 0xD9, 
		(byte) 0xDA, (byte) 0xDB, (byte) 0xDC, (byte) 0xDD, (byte) 0xDE, 
		(byte) 0xDF, 
		(byte) 0xE0, (byte) 0xE1, (byte) 0xE2, (byte) 0xE3, (byte) 0xE4, 
		(byte) 0xE5, (byte) 0xE6, (byte) 0xE7, (byte) 0xE8, (byte) 0xE9, 
		(byte) 0xEA, (byte) 0xEB, (byte) 0xEC, (byte) 0xED, (byte) 0xEE, 
		(byte) 0xEF, 
		(byte) 0xF0, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, 
		(byte) 0xF5, (byte) 0xF6, (byte) 0xF7, (byte) 0xF8, (byte) 0xF9, 
		(byte) 0xFA, (byte) 0xFB, (byte) 0xFC, (byte) 0xFD, (byte) 0xFE, 
		(byte) 0xFF, 
	};
	
	protected List<Byte> alphabet;

	public KeySet(List<Alphabet> alphabets) {
		this.alphabet = new ArrayList<Byte>();
		
		for (Alphabet a : alphabets) {
			this.addAlphabet(a);
		}
		
		this.prepare();
	}

	public KeySet(Alphabet... alphabets) {
		this.alphabet = new ArrayList<Byte>();
		
		for (Alphabet a : alphabets) {
			this.addAlphabet(a);
		}
		
		this.prepare();
	}
	
	public List<Byte> getAlphabet() {
		return this.alphabet;
	}
	
	protected void addAlphabet(Alphabet alphabet) {
		switch (alphabet) {
		case digits:
			this.concat(KeySet.DIGITS);
			break;
		case hexa_low:
			this.concat(KeySet.HEXA_LOWERCASE);
			break;
		case hexa_up:
			this.concat(KeySet.HEXA_UPPERCASE);
			break;
		case ascii_low:
			this.concat(KeySet.ASCII_LOWERCASE);
			break;
		case ascii_up:
			this.concat(KeySet.ASCII_UPPERCASE);
			break;
		case letters:
			this.concat(KeySet.ASCII_LOWERCASE, KeySet.ASCII_UPPERCASE);
			break;
		case punctuation:
			this.concat(KeySet.PUNCTUATION);
			break;
		case controls:
			this.concat(KeySet.CONTROLS);
			break;
		case extended:
			this.concat(KeySet.EXTENDED);
			break;
		case printable:
			this.addAlphabet(Alphabet.digits);
			this.addAlphabet(Alphabet.letters);
			this.addAlphabet(Alphabet.punctuation);
			break;
		case all:
			this.addAlphabet(Alphabet.controls);
			this.addAlphabet(Alphabet.digits);
			this.addAlphabet(Alphabet.letters);
			this.addAlphabet(Alphabet.punctuation);
			this.addAlphabet(Alphabet.extended);
			break;
		}
	}

	protected void concat(Byte[]... parts) {
		for (int i=0; i<parts.length; i++) {
			Byte[] part = parts[i];
			this.alphabet.addAll(Arrays.asList(part));
		}
	}
	
	protected void compactAlphabet() {
		Byte[] distincts = this.alphabet.stream().distinct().sorted().toArray(Byte[]::new);
		this.alphabet = new ArrayList<Byte>(Arrays.asList(distincts));
	}
	
	protected void prepare() {
		if (this.alphabet.isEmpty()) {
			this.addAlphabet(Alphabet.all);
		}
		
		this.compactAlphabet();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("======= keySet =\n");
		sb.append("length: ").append(this.alphabet.size()).append('\n');

		return sb.toString();
	}

}
