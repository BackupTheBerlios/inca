package org.inca.util.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * @author gieretmk
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class WordNetWrapper {

	public static String getTaggedText(String data, String lang, String search)
		throws Exception {
		BufferedReader rd = null;
		try {
			
			// Send data
			URL url =
				new URL(
					"http://isny:8080/nlptools/germa-net.jsp?text="
						+ URLEncoder.encode(data, "UTF-8")
						+ "&lang="
						+ lang+"&search="+search);
			URLConnection conn = url.openConnection();

			// Get the response
			rd =
				new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				buffer.append(line).append('\n');
			}
			return buffer.toString();
		} finally {
			rd.close();
		}

	}

	public static void main(String[] args) {
		try {
			System.out.println(
					WordNetWrapper.getTaggedText(
					"Tree",
					"en","-synsn"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
