/*
 * Created on Feb 21, 2005 5:51:45 PM
 */
package org.inca.util.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * @author achim
 * reads url and returns a string. also works for secure http, ignoring any ssl
 * issues such as invalid hostnames in certificates 
 */
public class HTTPReader {
    private class NodThroughVerifier implements HostnameVerifier {
        public boolean verify(String arg0, SSLSession arg1) {
            return true;
        }
        
    }
    private URL _url = null;
    public HTTPReader(URL url) {
        this._url = url;
    }
    
    public String read() throws IOException {
        StringBuffer result = new StringBuffer();
        if (_url.getProtocol().compareToIgnoreCase("http") == 0) {
            BufferedReader in = new BufferedReader( new InputStreamReader( _url.openStream() ) );
            
            String s;
            while ( null != (s = in.readLine()) ){
                result.append(s);
            }
            
            in.close();
        } else if (_url.getProtocol().compareToIgnoreCase("https") == 0) {
//            HttpsURLConnection.setDefaultSSLSocketFactory(null);
            HttpsURLConnection.setDefaultHostnameVerifier(new NodThroughVerifier());
            HttpsURLConnection connection = (HttpsURLConnection)_url.openConnection();
            
            System.out.println(connection.getSSLSocketFactory().getSupportedCipherSuites());
            
            BufferedReader in = new BufferedReader( new InputStreamReader( _url.openStream() ) );
            
            String s;
            while ( null != (s = in.readLine()) ){
                result.append(s);
            }
            
            in.close();
        } else {
            throw new IOException("unsupported protocol: " + _url.getProtocol() );
        }
        
        return result.toString();
    }
}
