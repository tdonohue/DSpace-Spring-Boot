/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Returns a 404 when a page is not found
 * @author Tim Donohue
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Page not found")
public class PageNotFoundException extends RuntimeException
{
    /**
     * Throw a PageNotFoundException for a given path
     * @param path
     */
    public PageNotFoundException(String path) {
		super(path + " not found");
	}
}
