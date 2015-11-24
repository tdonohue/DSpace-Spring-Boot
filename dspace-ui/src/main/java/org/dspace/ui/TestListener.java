/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a test ServletContextListener which is just to verify that Listeners
 * are being loaded by BOTH embedded Tomcat AND external Tomcat.
 * <P>
 * My early testing of Spring Boot shows some inconsistencies with regards to
 * how and when a Listener is loaded, based on whether you are using Embedded
 * Tomcat (more consistently loaded) or External Tomcat (seems to require major
 * hacking of the Application.class)
 * <P>
 * Leaving this test listener here for test purposes.
 *
 * @author Tim Donohue
 */
@WebListener
public class TestListener implements ServletContextListener
{
    private static final Logger log = LoggerFactory.getLogger(TestListener.class);

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)
    {
        log.info("TESTLISTENER contextInitialized() called");
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)
    {
        log.info("TESTLISTENER contextDestroyed() called");
    }

}
