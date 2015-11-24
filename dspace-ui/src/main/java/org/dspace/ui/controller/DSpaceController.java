/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.controller;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.WordUtils;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.services.ConfigurationService;
import org.dspace.ui.utils.BreadCrumb;
import org.dspace.ui.utils.ContextUtil;
import org.dspace.utils.DSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UrlPathHelper;

/**
 * Generic DSpace Controller which holds some shared properties about our
 * application.
 *
 * @author Tim Donohue
 */
@Controller
public class DSpaceController
{
    private static final Logger log = LoggerFactory.getLogger(DSpaceController.class);

    // Path which corresponds to an object homepage
    protected final String OBJECT_PATH_PREFIX = "/handle/";

    // This reads the value of "spring.application.name" from application.properties
    // and assigns it to "applicationName"
    @Value("${spring.application.name}")
    protected String applicationName;

    // Shared reference to HandleService
    protected HandleService handleService = HandleServiceFactory.getInstance().getHandleService();

    // Shared reference to ConfigurationService
    protected ConfigurationService configurationService = new DSpace().getConfigurationService();

    /**
     * Make ${applicationName} available to all pages in theme
     *
     * @return application name
     */
    @ModelAttribute("applicationName")
    public String getApplicationName()
    {
        return applicationName;
    }

    /**
     * Add list of breadcrumbs, based on our path within the application.
     *
     * @param request HttpServletRequest (automatically provided)
     * @return List of BreadCrumbs for display in theme
     */
    @ModelAttribute
    public List<BreadCrumb> getBreadCrumbs(HttpServletRequest request)
    {
        // Get full path (without context path)
        // This will return something like /handle/1/1
        String path = new UrlPathHelper().getPathWithinApplication(request);

        // List of breadcrumbs
        List<BreadCrumb> breadcrumbs = new LinkedList<>();

        // Check if this path refers to a specific object
        String objID = null;
        if(path.startsWith(OBJECT_PATH_PREFIX))
        {
            // Extract the object ID from our request path
            objID = getObjectIDFromPath(path);
            
            try
            {
                // Get DSpace context
                Context context = ContextUtil.obtainContext(request);

                // Now, get the object with this handle
                DSpaceObject dso = handleService.resolveToObject(context, objID);

                // Obtain the breadcrumbs for this object (which includes referencing all parent objects)
                addObjectBreadCrumbs(breadcrumbs, dso);
            }
            catch(SQLException e)
            {
                // do nothing
            }
        }

        // If our path referenced an object, remove that objID
        if(objID!=null && !objID.isEmpty())
            path = path.replace(OBJECT_PATH_PREFIX + objID, "");

        // At this point we may still have a non-empty path, if it didn't refer
        // to a specific object, or if there was extra info provided after the
        // objectID on the path
        
        // First, remove any starting slash (if any)
        if(path.startsWith("/"))
            path = path.substring(1);

        // Split path on slashes, so that we can generate a breadcrumb
        // for each part of this path.
        String[] paths = path.split("/");
  
        for(String p : paths)
        {
            // Based on the path, we'll generate a human-readable label
            String label = p;

            if(label!=null && !label.isEmpty())
            {
                //Replace all dashes (-) in paths with spaces
                label = label.replaceAll("-", " ");

                //Uppercase all initial letters
                label = WordUtils.capitalize(label);

                // Append this breadcrumb label & path to our list
                breadcrumbs.add(new BreadCrumb(label, p));
            }
        }

        // Finally, prepend Home to all paths
        breadcrumbs.add(0, new BreadCrumb(applicationName + " Home", "/"));

        return breadcrumbs;
    }

    /**
     * Extract the object identifier out of a given path
     * <P>
     * This may be one of two URL types:
     * /handle/1/1
     * /handle/1/1/extra/info
     *
     * @param path
     */
    private String getObjectIDFromPath(String path)
    {
        String objID = "";

        // Remove our path prefix (e.g. /handle/)
        if(path.startsWith(OBJECT_PATH_PREFIX))
            path = path.substring(OBJECT_PATH_PREFIX.length());

        // Extract the ObjID (e.g. Handle)
        // Format: [obj-prefix]/[obj-suffix]
        int firstSlash = path.indexOf('/');
        int secondSlash = path.indexOf('/', firstSlash + 1);

        if (secondSlash != -1)
        {
            // We have extra path info
            // object ID is everything up to second slash
            objID = path.substring(0, secondSlash);
        }
        else
        {
            // The path is just the Handle
            objID = path;
        }

        return objID;
    }

    /**
     * Adds breadcrumbs for a specific DSpaceObject, by finding parent objects,
     * grandparents, etc.
     *
     * @param breadcrumbs
     * @param dso
     */
    private void addObjectBreadCrumbs(List<BreadCrumb> breadcrumbs, DSpaceObject dso)
    {
        try
        {
            if(dso!=null)
            {
                // Add current object to our breadcrumbs
                BreadCrumb dsoCrumb = new BreadCrumb(dso.getName(), OBJECT_PATH_PREFIX + dso.getHandle());
                breadcrumbs.add(0, dsoCrumb);
            }
            else
            {
                // nothing to add to our breadcrumbs
                return;
            }

            if(dso instanceof Community)
            {
                Community community = (Community) dso;
                List<Community> parents = community.getParentCommunities();

                // Add first parent Community to breadcrumb trail
                if(parents!=null && !parents.isEmpty())
                {
                    Community parent = parents.get(0);
                    //recursively call ourselves on this parent object
                    addObjectBreadCrumbs(breadcrumbs, parent);
                }
            }
            else if(dso instanceof Collection)
            {
                Collection collection = (Collection) dso;
                List<Community> parents = collection.getCommunities();

                // Add first parent Community to breadcrumb trail
                if(parents!=null && !parents.isEmpty())
                {
                    Community parent = parents.get(0);
                    //recursively call ourselves on this parent object
                    addObjectBreadCrumbs(breadcrumbs, parent);
                }
            }
            else if(dso instanceof Item)
            {
                Item item = (Item) dso;
                // An item's parent is its "owning" collection
                Collection parent = item.getOwningCollection();
                if(parent!=null)
                {
                    //recursively call ourselves on this parent object
                    addObjectBreadCrumbs(breadcrumbs, parent);
                }
            }
        }
        catch(SQLException e)
        {
            //do nothing
        }
    }
}
