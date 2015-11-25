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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    // Parameter which, when specified, refers to a specific object
    protected final String OBJECT_ID_PARAMETER = "handle";

    protected final String THEME_SETTING = "dspace.theme";

    // Autowire our Environment, so that we can directly read settings from application.properties
    @Autowired
    Environment env;

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
     * Add several key attributes to our model at once. These attributes
     * help to drive our theme / layout (e.g. breadcrumbs, theme, etc)
     * @param model
     * @param request 
     */
    @ModelAttribute
    public void setAttributes(Model model, HttpServletRequest request)
    {
        // Get our breadcrumbs, based on our current request location
        List<BreadCrumb> breadcrumbs = getBreadCrumbs(request);
        model.addAttribute(breadcrumbs);

        String default_theme = env.getProperty(THEME_SETTING);
        String theme = null;
        // Now, based on our breadcrumbs, determine our theme!
        for(BreadCrumb crumb: breadcrumbs)
        {
            // Get the path in the breadcrumbs, and see if it
            // matches with a theme configuration
            // For example, this theme will be applied to anything under /handle/1234/5678
            //    dspace.theme.handle.1234.5678 = mytheme
            String objPath = crumb.getPath();
            // Transform paths like /handle/1234/5678 to ".handle.1234.5678"
            objPath = objPath.replace("/", ".");
            // Check for a theme configuration specific to this path
            theme = env.getProperty(THEME_SETTING + objPath);
            // First match wins! (This lets Community/Collection themes be inherited by Collections/Items, etc)
            if(theme!=null && !theme.isEmpty())
                break;
        }

        // If theme is still null, check the configured default theme.
        // Otherwise, set to a theme actually named "default"
        if(theme==null)
            theme = default_theme != null ? default_theme : "default";

        // Add theme name to our model
        model.addAttribute("theme", theme);
    }
    
    /**
     * Add list of breadcrumbs, based on our path within the application.
     *
     * @param request HttpServletRequest (automatically provided)
     * @return List of BreadCrumbs for display in theme
     */
    public List<BreadCrumb> getBreadCrumbs(HttpServletRequest request)
    {
        // First, check if a parameter was passed on the request referring
        // to a specific object by its ID
        String objectIDParam = request.getParameter(OBJECT_ID_PARAMETER);

        // Get servlet path (without application's context path)
        // If you are accessing a specific object in the system,
        // this will return something like /handle/1/1
        String path = new UrlPathHelper().getOriginatingServletPath(request);

        // Double check that the context path is not at the beginning
        String contextPath = new UrlPathHelper().getOriginatingContextPath(request);
        if(path.startsWith(contextPath))
            path = path.replace(contextPath, "");

        // List of breadcrumbs
        List<BreadCrumb> breadcrumbs = new LinkedList<>();

        // If an objectID parameter was found, use it.
        // Otherwise, determine it from our request path
        String objID = null;
        if(objectIDParam!=null && !objectIDParam.isEmpty())
            objID = objectIDParam;
        else
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

        // If our path referenced an object, remove that part of the path,
        // in order to see if we have any extra information
        if(path.startsWith(OBJECT_PATH_PREFIX) && objID!=null && !objID.isEmpty())
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
     * /handle/123/456
     * /handle/123/456/extra/info
     *
     * @param path
     * @return objectID (e.g. 123/456
     */
    public String getObjectIDFromPath(String path)
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
     * Extract the Object extra info out of a given path
     * <P>
     * This may be one of two URL types:
     * /handle/1/1            -> Returns null
     * /handle/1/1/extra/info -> Returns "extra/info"
     *
     * @param path
     */
    public String getObjectExtraInfoFromPath(String path)
    {
        // Remove our path prefix (e.g. /handle/)
        if(path.startsWith(OBJECT_PATH_PREFIX))
            path = path.substring(OBJECT_PATH_PREFIX.length());

        // Then extract any Object ID from path
        String objectID = getObjectIDFromPath(path);

        //Remove the Object ID as well
        if(objectID!=null && path.startsWith(objectID))
            path = path.substring(objectID.length());

        //Remove any preceding slashes
        if(path.startsWith("/"))
            path = path.substring(1);

        // What remains is the "extra info"
        return path;
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
