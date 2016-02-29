/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.util.UrlPathHelper;

/**
 * DSpace Error Controller. This extends our DSpaceController so that it
 * can include our theme, breadcrumbs, etc.
 * @author Tim Donohue
 */
@ControllerAdvice
public class DSpaceErrorController extends DSpaceController
{
    public static final String DEFAULT_ERROR_VIEW = "error";

    /**
     * Responds for all Exceptions or RuntimeException
     * @param model
     * @param request
     * @param e
     * @return 
     */
    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    public String defaultErrorHandler(Model model, HttpServletRequest request, Exception e) 
    {
        // Add exception info to our model
        // By default, Spring Boot SHOULD set its own ErrorAttributes, but if the
        // exception is within Spring Boot's processing, this sometimes fails. 
        // So, our error page also uses the underlying "exception" as a backup 
        // source for the error message.
        model.addAttribute("exception", e);
        model.addAttribute("exception_type", e.getClass().getName());
        model.addAttribute("exception_path", new UrlPathHelper().getPathWithinApplication(request));

        // Just display our default (themed) error page for all exceptions
        return DEFAULT_ERROR_VIEW;
    }
}
