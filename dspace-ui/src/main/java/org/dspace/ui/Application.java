/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui;

import javax.servlet.Filter;
import org.dspace.app.util.DSpaceContextListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.dspace.servicemanager.servlet.DSpaceKernelServletContextListener;
import org.dspace.ui.filter.DSpaceRequestContextFilter;
import org.dspace.utils.servlet.DSpaceWebappServletFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
    /**
     * Register the "DSpaceKernelServletContextListener" so that it is loaded
     * for this Application.
     * @return DSpaceKernelServletContextListener
     */
    @Bean
    @Order(value=1)
    protected DSpaceKernelServletContextListener dspaceKernelListener() {
        // This registers our listener which starts the DSpace Kernel
        return new DSpaceKernelServletContextListener();
    }
    
    /**
     * Register the "DSpaceContextListener" so that it is loaded
     * for this Application.
     * @return DSpaceContextListener
     */
    @Bean
    @Order(value=2)
    protected DSpaceContextListener dspaceContextListener() {
        // This listener initializes the DSpace Context object
        // (and loads all DSpace configs)
        return new DSpaceContextListener();
    }
    
    /**
     * Register the DSpaceWebappServletFilter, which initializes the
     * DSpace RequestService / SessionService
     * 
     * @return DSpaceWebappServletFilter
     */
    @Bean
    protected Filter dspaceWebappServletFilter() {
        return new DSpaceWebappServletFilter();
    }
    
    /**
     * Register the DSpaceRequestContextFilter, a Filter which checks for open
     * Context objects *after* a request has been fully processed, and closes them
     *
     * @return DSpaceRequestContextFilter
     */
    @Bean
    protected Filter dspaceRequestContextFilter() {
        return new DSpaceRequestContextFilter();
    }
    
    /**
     * Actually run/initialize this Spring Application
     * @param args 
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        
        // BELOW IS TEST CODE which just lists all Beans that were loaded by Spring Boot
        /*ApplicationContext ctx = SpringApplication.run(Application.class, args);

        System.out.println("LIST OF BEANS loaded/provided by Spring Boot:");

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }*/
    }

}
