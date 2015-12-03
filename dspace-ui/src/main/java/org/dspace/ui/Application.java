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
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Define the Spring Boot Application settings itself. This class takes the place 
 * of a web.xml file, and configures all Filters/Listeners as methods (see below).
 * <P>
 * NOTE: Requires a Servlet 3.0 container, e.g. Tomcat 7.0 or above.
 * <p>
 * NOTE: This extends SpringBootServletInitializer in order to allow us to build
 * a deployable WAR file with Spring Boot. See:
 * http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-create-a-deployable-war-file
 *
 * @author Tim
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer
{
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    /**
     * Override the default SpringBootServletInitializer.configure() method,
     * passing it this Application class.
     * <P>
     * This is necessary to allow us to build a deployable WAR, rather than
     * always relying on embedded Tomcat.
     * <P>
     * See: http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-create-a-deployable-war-file
     * @param application
     * @return
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    /**
     * This override of SpringBootServletInitializer.onStartup() allows an
     * EXTERNAL Tomcat to properly work with out required Listeners.
     * <P>
     * For some strange reason, our required Listeners load properly on embedded
     * Tomcat, but NOT on an external one.
     * <P>
     * However, the AnnotationConfigWebApplicationContext() doesn't seem to work
     * for EMBEDDED Tomcat containers. Odd.
     * @param servletContext
     * @throws ServletException
     */
    /**@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		AnnotationConfigWebApplicationContext webApplicationContext = new AnnotationConfigWebApplicationContext();
        //webApplicationContext.register(Application.class);
        //webApplicationContext.setServletContext(servletContext);
        webApplicationContext.setConfigLocation(Application.class.getName());

        servletContext.setInitParameter("dspace.dir", dspaceHome);
        servletContext.addListener(dspaceKernelListener());
        servletContext.addListener(dspaceContextListener());
		servletContext.addServlet("dispatcherServlet",
				new DispatcherServlet(webApplicationContext)).addMapping("/*");
	}*/

    /**
     * Register the "DSpaceKernelServletContextListener" so that it is loaded
     * for this Application.
     * @return DSpaceKernelServletContextListener
     */
    @Bean
    public TestListener testListener() {
        return new TestListener();
    }

    /**
     * Register the "DSpaceKernelServletContextListener" so that it is loaded
     * for this Application.
     * @return DSpaceKernelServletContextListener
     */
    @Bean
    @Order(1)
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
    @Order(2)
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
    @Order(1)
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
    @Order(2)
    protected Filter dspaceRequestContextFilter() {
        return new DSpaceRequestContextFilter();
    }
    
    /**
     * Actually run/initialize this Spring Application
     * @param args 
     */
    public static void main(String[] args) throws Exception
    {
        SpringApplication.run(Application.class, args);
        
        // BELOW IS TEST CODE which just lists all Beans that were loaded by Spring Boot
        // It is left commented out as it's useful for debugging bean issues.
        /*ApplicationContext ctx = SpringApplication.run(Application.class, args);

        System.out.println("LIST OF BEANS loaded/provided by Spring Boot:");

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }*/
    }
    
}
