/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.controller;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * DSpace Error Controller. This extends our DSpaceController so that it
 * can include our theme, breadcrumbs, etc.
 * @author Tim Donohue
 */
@ControllerAdvice
public class DSpaceErrorController extends DSpaceController
{
    public static final String DEFAULT_ERROR_VIEW = "error";

    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    public String defaultErrorHandler(Model model, HttpServletRequest request, Exception e) 
    {
        // Just display our default (themed) error page for all exceptions
        return DEFAULT_ERROR_VIEW;
    }
    
    /**
     * This is an example exception mapped to "/foo" path, which lets us
     * just test out exception handling, etc.
     * @return 
     */
    @RequestMapping("/foo")
    public String foo() {
            throw new RuntimeException("Example Runtime Exception thrown from 'foo'");
    }
    
}
