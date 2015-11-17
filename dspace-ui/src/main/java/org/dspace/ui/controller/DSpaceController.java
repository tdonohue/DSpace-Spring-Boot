/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.controller;

import java.util.LinkedList;
import java.util.List;
import org.dspace.ui.utils.BreadCrumb;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Generic DSpace Controller which holds some shared properties about our
 * application.
 *
 * @author Tim Donohue
 */
@Controller
public class DSpaceController {

    // This reads the value of "spring.application.name" from application.properties
    // and assigns it to "applicationName"
    @Value("${spring.application.name}")
    protected String applicationName;

    /**
     * Add list of breadcrumbs.
     * <P>
     * Individual Controllers should override this, calling super.getBreadCrumbs()
     * and append additional crumbs to the list
     * @return
     */
    @ModelAttribute
    public List<BreadCrumb> getBreadCrumbs()
    {
        List<BreadCrumb> breadcrumbs = new LinkedList<>();

        breadcrumbs.add(new BreadCrumb("Home", "/"));

        return breadcrumbs;
    }
}
