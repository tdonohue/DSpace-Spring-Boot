/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Override our default settings for Spring WebMVC, in order to customize our
 * "content negotiation" strategy for Spring Boot, so that we can support
 * both REST (JSON or XML) and HTML requests.
 * <p>
 * See also https://spring.io/blog/2013/05/11/content-negotiation-using-spring-mvc
 * @author Tim Donohue
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter
{
    /**
     * Customize our "content negotiation" settings, allowing Spring Boot
     * to return different types of responses based on the path extension.
     * @param configurer 
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) 
    {
        configurer.favorPathExtension(true).                // Determine type of request based on path extension (e.g: .json, .xml)
            ignoreAcceptHeader(true).                       // Ignore the "accept" header
            useJaf(false).                                  // Don't use JAF (Java Beans Activation Framework)
            defaultContentType(MediaType.TEXT_HTML).        // Default to returning HTML
            mediaType("html", MediaType.TEXT_HTML).         // Respond with HTML if path ends in ".html"
            mediaType("xml", MediaType.APPLICATION_XML).    // Respond with XML if path ends in ".xml"
            mediaType("json", MediaType.APPLICATION_JSON);  // Respond with JSON if path ends in ".json"
    }
}
