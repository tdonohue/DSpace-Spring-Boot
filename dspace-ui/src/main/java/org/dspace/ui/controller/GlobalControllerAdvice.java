/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dspace.ui.controller;

import org.dspace.services.factory.DSpaceServicesFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Any annotated @InitBinder, @ModelAttribute or @ExceptionHandler methods in 
 * this Class are inherited to all our controllers
 * <P>
 * See: https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ControllerAdvice.html
 * 
 * @author Tim Donohue
 */
@ControllerAdvice(basePackages= {"org.dspace.ui.controller"})
public class GlobalControllerAdvice
{
    // Constants for Model Attributes used throughout application
    protected final static String APPLICATION_NAME_ATTR = "applicationName";

    /**
     * Make ${applicationName} available to all Controllers & all pages in Theme
     *
     * @return application name
     */
    @ModelAttribute(APPLICATION_NAME_ATTR)
    public String addApplicationName()
    {
        // Pull application name from DSpace configuration
        return DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.name");
    }
    
}
