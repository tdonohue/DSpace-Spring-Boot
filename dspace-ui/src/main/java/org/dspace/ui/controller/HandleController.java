/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.controller;

import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.dspace.browse.BrowseEngine;
import org.dspace.browse.BrowseException;
import org.dspace.browse.BrowseIndex;
import org.dspace.browse.BrowseInfo;
import org.dspace.browse.BrowserScope;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.sort.SortException;
import org.dspace.sort.SortOption;
import org.dspace.ui.exception.PageNotFoundException;
import org.dspace.ui.utils.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for /handle/** paths.
 * <P>
 * This Controller basically just determines what object we are dealing with
 * and then forwards the request on to the appropriate object's controller.
 * <P>
 * For example, Item requests are forwarded to the ItemController.
 * 
 * @author Tim Donohue
 */
@Controller
@RequestMapping("/handle")
public class HandleController extends DSpaceController
{
    private static final Logger log = LoggerFactory.getLogger(HandleController.class);

    // Path that this Controller responds to
    public static final String PATH = "/handle";

    /**
     * This method responds to /handle/{prefix}/{suffix}. Its job is to redirect
     * to the appropriate controller.
     * @param prefix
     * @param suffix
     * @param model
     * @param request
     * @param redirectAttributes
     * @return
     * @throws SQLException 
     */
    @RequestMapping("/{prefix}/{suffix}")
    public String redirectToObject(@PathVariable String prefix, @PathVariable String suffix, HttpServletRequest request)
            throws SQLException
    {
        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        // Get the object with this handle
        DSpaceObject dso = handleService.resolveToObject(context, prefix + "/" + suffix);

        // Send to correct view based on type of object
        if(dso == null)
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Object with Handle=" + prefix + "/" + suffix);
        }
        else if(dso instanceof Community)
        {
            // Forward request to the CommunityController, passing it the internal id
            return "forward:/communities/" + dso.getID();
        }
        else if(dso instanceof Collection)
        {
            // Forward request to the CollectionController, passing it the internal id
            return "forward:/collections/" + dso.getID();
        }
        else if(dso instanceof Item)
        {
            // Forward request to the ItemController, passing it the internal id
            return "forward:/items/" + dso.getID();
        }
        else
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Object with Handle=" + prefix + "/" + suffix + " is not a valid DSpace object.");
        }
    }
    
    
    // This method responds to /handle/*/*/edit path
    @RequestMapping("/{prefix}/{suffix}/edit")
    public String editObject(@PathVariable String prefix, @PathVariable String suffix, Model model, HttpServletRequest request)
            throws SQLException
    {
        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        // Get the object with this handle
        DSpaceObject dso = handleService.resolveToObject(context, prefix + "/" + suffix);
        
        // Send to correct view based on type of object
        if(dso == null)
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Object with Handle=" + prefix + "/" + suffix);
        }
        else if(dso instanceof Community)
        {
            // Forward request to the CommunityController, passing it the id
            return "forward:/community/" + dso.getID() + "/edit";
        }
        else if(dso instanceof Collection)
        {
            // Forward request to the CollectionController, passing it the id
            return "forward:/collection/" + dso.getID() + "/edit";
        }
        else if(dso instanceof Item)
        {
            // Forward request to the ItemController, passing it the id
            return "forward:/item/" + dso.getID() + "/edit";
        }
        else
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Object with Handle=" + prefix + "/" + suffix + " is not a valid DSpace object.");
        }
    }

    /**
     * Obtain the recent submissions from the given container object.  This
     * method uses the configuration to determine which field and how many
     * items to retrieve from the DSpace Object.
     *
     * If the object you pass in is not a Community or Collection (e.g. an Item
     * is a DSpaceObject which cannot be used here), an exception will be thrown
     *
     * @param context
     * @param dso	DSpaceObject: Community, Collection or null for SITE
     * @return		The recently submitted items as a list
     */
    public List<Item> getRecentSubmissions(Context context, DSpaceObject dso)
    {
        try
        {
            // get our configuration
            String source = configurationService.getProperty("recent.submissions.sort-option");
            String count = configurationService.getProperty("recent.submissions.count");

            // prep our engine and scope
            BrowseEngine be = new BrowseEngine(context);
            BrowserScope bs = new BrowserScope(context);
            BrowseIndex bi = BrowseIndex.getItemBrowseIndex();

            // fill in the scope with the relevant gubbins
            bs.setBrowseIndex(bi);
            bs.setOrder(SortOption.DESCENDING);
            bs.setResultsPerPage(Integer.parseInt(count));
            if (dso != null)
            {
                bs.setBrowseContainer(dso);
            }
            for (SortOption so : SortOption.getSortOptions())
            {
                if (so.getName().equals(source))
                {
                    bs.setSortBy(so.getNumber());
                }
            }

            BrowseInfo results = be.browseMini(bs);

            return results.getResults();
        }
        catch (SortException | BrowseException e)
        {
            // TODO: Is there a way to catch issues when Solr is unavailable?
            log.error("Error generating Recent Submissions listing. Check your access to Solr! ", e);

            // Return null so that page still displays,
            // even if recent submissions cannot be populated.
            return null;
        }
    }

}
