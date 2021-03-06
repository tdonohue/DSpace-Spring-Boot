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
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Context;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.ui.utils.BreadCrumb;
import org.dspace.ui.utils.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

    // Constants for response types
    protected final String HTML_RESPONSE = "text/html";
    protected final String JSON_RESPONSE = "application/json";
    protected final String XML_RESPONSE = "application/xml";
    
    // Path which corresponds to an object homepage
    protected final String OBJECT_PATH_PREFIX = "/handle/";

    protected final String THEME_SETTING = "dspace.theme";

    protected final static String BREADCRUMBS_ATTR = "breadCrumbList";
    protected final static String THEME_ATTR = "theme";
    
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
    protected ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();

    @ModelAttribute(BREADCRUMBS_ATTR)
    public List<BreadCrumb> addBreadCrumbs(@PathVariable Optional<UUID> id, @ModelAttribute(GlobalControllerAdvice.APPLICATION_NAME_ATTR) String applicationName, Model model, HttpServletRequest request)
    {
        DSpaceObject dso = null;
        if(id.isPresent() && id.get()!=null)
        {
            try
            {
                // Get DSpace context
                Context context = ContextUtil.obtainContext(request);

                // If we have a UUID, determine which kind of Controller we are dealing with,
                // and get acess to the corresponding DSpace Object
                if(this.getClass().equals(ItemController.class))
                {
                    dso = ContentServiceFactory.getInstance().getItemService().find(context, id.get());
                }
                else if(this.getClass().equals(CollectionController.class))
                {
                    dso = ContentServiceFactory.getInstance().getCollectionService().find(context, id.get());
                }
                else if(this.getClass().equals(CommunityController.class))
                {
                    dso = ContentServiceFactory.getInstance().getCommunityService().find(context, id.get());
                }
            }
            catch(SQLException e)
            {
                // ignore for now
            }
        }

        // get our list of breadcrumbs and return them
        return getBreadCrumbs(request, dso, applicationName);
    }

    @ModelAttribute(THEME_ATTR)
    public String addTheme(@ModelAttribute(BREADCRUMBS_ATTR) List<BreadCrumb> breadcrumbs)
    {
        //return "default";
        // Look for default theme configuration in DSpace configuration first
        // Look in DSpace config first
        String default_theme = configurationService.getProperty(THEME_SETTING);

        // If not there, default to one in application.properties (Spring Boot config)
        if(StringUtils.isBlank(default_theme))
            default_theme = env.getProperty(THEME_SETTING);

        String theme = null;
        // Now, based on our breadcrumbs, determine our theme!
        for(BreadCrumb crumb: breadcrumbs)
        {
            // Get the path in the breadcrumbs, and see if it
            // matches with a theme configuration
            // For example, this theme will be applied to anything under /handle/1234/5678
            //    dspace.theme.handle.1234.5678 = mytheme
            String objPath = crumb.getPath();

            // We'll ignore the root object path (/) and any empty ones (which shouldn't exist anyways)
            if (StringUtils.isNotBlank(objPath) && !objPath.equals("/"))
            {
                // Transform paths like /handle/1234/5678 to ".handle.1234.5678"
                objPath = objPath.replace("/", ".");
                // Check for a theme configuration specific to this path
                String theme_config = configurationService.getProperty(THEME_SETTING + objPath);
                // If a configuration specific to this path is found, it's our theme
                // NOTE: this loop also ensures that themes are inherited from the parent object (Community/Collection)
                // unless explicitly overridden by the child object.
                if(theme_config!=null && !theme_config.isEmpty())
                    theme = theme_config;
            }
        }

        // If theme is still null, check the configured default theme.
        // Otherwise, set to a theme actually named "default"
        if(StringUtils.isBlank(theme))
            theme = default_theme != null ? default_theme : "default";

        return theme;
    }

    /**
     * Add list of breadcrumbs, based on our path within the application.
     *
     * @param request HttpServletRequest (automatically provided)
     * @param dso DSpaceObject we are viewing (or null)
     * @return List of BreadCrumbs for display in theme
     */
    public List<BreadCrumb> getBreadCrumbs(HttpServletRequest request, DSpaceObject dso, String applicationName)
    {
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

        if(dso!=null)
        {
            // Obtain the breadcrumbs for this object (which includes referencing all parent objects)
            addObjectBreadCrumbs(breadcrumbs, dso);
        
            String uuid = dso.getID()!=null ? dso.getID().toString() : "";

            // If our path referenced an object (by ID or handle), remove everything up to, and including
            // the identifier from the path, to see if we have anything else on our path.
            if(StringUtils.isNotBlank(uuid) && path.contains(uuid))
                path = path.substring(path.indexOf(uuid) + uuid.length());
            if(StringUtils.isNotBlank(dso.getHandle()) && path.contains(dso.getHandle()))
                path = path.substring(path.indexOf(dso.getHandle()) + dso.getHandle().length());
        }
        
        // At this point we may still have a non-empty path, if it didn't refer
        // to a specific object, or if there was extra info provided after the
        // handle on the path
        
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
        else // if no object path prefix, this is not an object path
            return objID;

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
        // Then extract any Object ID from path
        String objectID = getObjectIDFromPath(path);

        // Remove our path prefix (e.g. /handle/)
        if(path.startsWith(OBJECT_PATH_PREFIX))
            path = path.substring(OBJECT_PATH_PREFIX.length());

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
            if(dso!=null && (dso instanceof Community || dso instanceof Collection || dso instanceof Item))
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
