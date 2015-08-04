package com.mihaelisaev.dnsshop;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1 {
	/**
	 * Sha1 from zero
	 */
	private static String SHA1_FROM_ZERO = "b6589fc6ab0dc82cf12099d1c2d40ab994e8410c";

	/**
	 * String to sha1 encoder
	 * if error while encoding you get sha1 from zero
	 * @param String data to encode
	 * @return String sha1
	 */
	public static String encode(String data) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(data.getBytes("iso-8859-1"), 0, data.length());
			return convertToHex(md.digest());

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return SHA1_FROM_ZERO;

	}
	
	/**
	 * Bytes to hex converter
	 * @param byte[] data bytes
	 * @return String sha1
	 */
	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}
}
