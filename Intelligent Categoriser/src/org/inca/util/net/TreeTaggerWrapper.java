package org.inca.util.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * @author gieretmk
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class TreeTaggerWrapper {

	public static String getTaggedText(String data, String lang)
		throws Exception {
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		try {
			// Construct data
			data =
				URLEncoder.encode("text", "UTF-8")
					+ "="
					+ URLEncoder.encode(data, "UTF-8");

			// Send data
			URL url =
				new URL(
					"http://isny:8080/nlptools/tree-tagger.jsp?lang=" + lang);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();

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
			wr.close();
			rd.close();
		}

	}

	public static void main(String[] args) {
		try {
			System.out.println(
				TreeTaggerWrapper.getTaggedText(
					"Das ist ein schöner Text zum Taggen (a=b).",
					"de"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
