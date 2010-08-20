package org.braiden.fpm2.crypto;

/**
 * Copyright (c) 2009 Braiden Kindt
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 */

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.ArrayUtils;

public class JCEDecrypter implements Decrypter {

	private final static String DEFAULT_CIPHER = "AES/ECB/NoPadding";

	private Cipher cipher;
	
	public JCEDecrypter() throws NoSuchAlgorithmException, NoSuchPaddingException {
		this(DEFAULT_CIPHER);
	}
	
	public JCEDecrypter(String cipherName) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.cipher = Cipher.getInstance(cipherName);
	}

	@Override
	public String decrypt(byte[] key, byte[] encryptedData) throws Exception {
		SecretKey cipherKey = new SecretKeySpec(key, cipher.getAlgorithm());

		cipher.init(Cipher.DECRYPT_MODE, cipherKey);
		byte[] result = cipher.doFinal(encryptedData);
		result = FpmCryptoUtils.unrotate(result, cipher.getBlockSize());
		int idxOfNil = ArrayUtils.indexOf(result, (byte)0);
		
		return new String(result, 0, idxOfNil);
	}
	
}
