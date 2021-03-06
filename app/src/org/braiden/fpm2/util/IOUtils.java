package org.braiden.fpm2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

public class IOUtils {

	public static final int BLOCK_SIZE=1024;
	
	public static String read(InputStream is) throws IOException {
		Reader reader = new InputStreamReader(is);
		StringBuffer result = new StringBuffer();
		char buffer[] = new char[2048];
		int count;
		while ((count = reader.read(buffer)) >= 0) {
			result.append(buffer, 0, count);
		}
		return result.toString();
	}

	public static void write(OutputStream os, InputStream is) throws IOException {
		byte[] buffer = new byte[BLOCK_SIZE];
		int n;
		
		while ((n = is.read(buffer)) >= 0)
		{
			os.write(buffer, 0, n);
		}
	}
	
}
