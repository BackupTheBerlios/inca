/*
 * Created on Nov 30, 2004
 */
package org.inca.main;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.inca.util.logging.LogHelper;

import com.google.soap.search.GoogleSearch;
import com.google.soap.search.GoogleSearchDirectoryCategory;
import com.google.soap.search.GoogleSearchFault;
import com.google.soap.search.GoogleSearchResult;
import com.google.soap.search.GoogleSearchResultElement;

/**
 * @author achim
 */
public class CategoriserMain {
    private static final String[] KEYWORDS = { "Deutsche Bank", "IBM Corporation",
            "Daimler Chrysler" };

    static Logger logger = Logger.getLogger(CategoriserMain.class);
    static Configuration config;

    public static void main(String[] args) {
        ApplicationConfiguration.initInstance();
        config  = ApplicationConfiguration.getConfiguration();

        doSearch();        
    }
    
    private static void doSearch() {
        GoogleSearch gs = new GoogleSearch();
        gs.setKey(config.getString("googleapi.key"));
        gs.setSafeSearch(config.getBoolean("googleapi.safeSearch", true));
        
        int maxResults = config.getInt("categoriser.maxResults");     
        
        if (maxResults > 10) {
            gs.setMaxResults(10);
        } else {
            gs.setMaxResults(maxResults);
        }

        for (int i = 0; i < KEYWORDS.length; i++) {
            String keyword = KEYWORDS[i];

            logger.info("searching for " + keyword);
            gs.setQueryString(keyword);
            
            /* get maxResults search results, 10 at a time */

            try {
                logger.info("categories:");
                for (int results = 0; results < maxResults; results += 10) {
                    logger.info(results + " to " + (results + 10) + " of " + maxResults);
                    gs.setStartResult(results);

	                GoogleSearchResult gsr = gs.doSearch();            
	                
	                /* obtain global categories if any */
	                GoogleSearchDirectoryCategory[] gsdcs = gsr
	                        .getDirectoryCategories();
	                
	                for (int j = 0; j < gsdcs.length; j++) {
	                    GoogleSearchDirectoryCategory gsdc = gsdcs[j];
	                    String name = gsdc.getFullViewableName();
	                    
	                    if ( !name.equals("") ) {
	                        logger.info(name);
	                    }
	                }
	                
	                /* obtain category matches of search results */
	                GoogleSearchResultElement[] gsres = gsr.getResultElements();
	                
	                for (int j = 0; j < gsres.length; j++) {
	                    GoogleSearchDirectoryCategory gsdc = gsres[j].getDirectoryCategory();
	                    String name = gsdc.getFullViewableName();
	                    
	                    if ( !name.equals("") ) {
	                        logger.info(name);
	                    }
	                }
                }
                
            } catch (GoogleSearchFault e) {
                logger.error("The call to the Google Web APIs failed:");
                logger.error(e.toString());
            }
        }
    }
}