/*
 * Created on Feb 21, 2005 5:51:45 PM
 */
package org.inca.util.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;

/**
 * @author achim
 * reads url and returns a string. also works for secure http, ignoring any ssl
 * issues such as invalid hostnames in certificates 
 */
public class HTTPReader {
    private static Logger logger = Logger.getLogger(HTTPReader.class);
    private class NodThroughVerifier implements HostnameVerifier {
        public boolean verify(String arg0, SSLSession arg1) {
            return true;
        }
    }
    
    private URL _url = null;
    private URLConnection _connection = null;
    private BufferedInputStream _in = null;

    public HTTPReader(URL url) {
        this._url = url;
        HttpsURLConnection.setDefaultHostnameVerifier(new NodThroughVerifier());
        HttpsURLConnection httpsConnection = null;
    }
    
    public boolean canConnect() {
        if (null == _connection) {
            try {
	            _connection = _url.openConnection();
	        } catch (IOException e) {
	            return false;
	        }
        }
        
        return true;        
    }
    
    private void openConnection() throws IOException {
        try {
            _connection = (HttpURLConnection) _url.openConnection();
        } catch(ClassCastException e) {
            throw new ConnectionFailedException("no protocol handler for " + _url.getProtocol() );
        } catch (IOException e) {
            throw new ConnectionFailedException("error connection to " + _url.getHost() + ":" + _url.getPort());
        }
        
        _in = new BufferedInputStream(_connection.getInputStream() );
    }
    
    public String guessContentType() throws IOException {
        if (null == _connection) {
            openConnection();
        }

        String contentType = URLConnection.guessContentTypeFromStream(_in);
        _in.reset();
        
        return contentType;
    }
    
    public String read() throws IOException, ResourceNotFoundException {
        StringBuffer result = new StringBuffer();

        if (_url.getProtocol().compareToIgnoreCase("http") == 0) {
            if ( null == _connection) {
	            openConnection();
            }
            
            HttpURLConnection httpConnection = (HttpURLConnection) _connection; 
            int responseCode = httpConnection.getResponseCode();
            
            logger.debug(responseCode + " " + httpConnection.getResponseMessage());
            if (responseCode != HttpURLConnection.HTTP_OK) {
                httpConnection.disconnect();
                throw new ResourceNotFoundException(_url + ": " + httpConnection.getResponseMessage() );
            }
            
            BufferedReader in = new BufferedReader( new InputStreamReader( _in ) );
            
            String s;
            while ( null != (s = in.readLine()) ) {
                result.append(s);
            }
            
            in.close();
        } else if (_url.getProtocol().compareToIgnoreCase("https") == 0) {
//            HttpsURLConnection.setDefaultSSLSocketFactory(null);
            HttpsURLConnection.setDefaultHostnameVerifier(new NodThroughVerifier());
            HttpsURLConnection httpsConnection = null;
            
            if (null == _connection) {
                try {
                    httpsConnection = (HttpsURLConnection) _url.openConnection();
                } catch(IOException e) {
                    throw new ConnectionFailedException("error connection to " + _url.getHost() + ":" + _url.getPort());
                }
            } else {
                httpsConnection = (HttpsURLConnection) _connection;
            }

            httpsConnection.connect();
            int responseCode = httpsConnection.getResponseCode();
            
            if (responseCode == HttpsURLConnection.HTTP_NOT_FOUND) {
                httpsConnection.disconnect();
                throw new ResourceNotFoundException(_url + ": 404 not found.");
            }

            logger.debug(httpsConnection.getSSLSocketFactory().getSupportedCipherSuites());
            
            BufferedReader in = new BufferedReader( new InputStreamReader( httpsConnection.getInputStream() ) );
            
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