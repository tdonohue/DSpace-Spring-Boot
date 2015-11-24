/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.controller;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.IteratorUtils;
import org.dspace.browse.BrowseEngine;
import org.dspace.browse.BrowseException;
import org.dspace.browse.BrowseIndex;
import org.dspace.browse.BrowseInfo;
import org.dspace.browse.BrowserScope;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.sort.SortException;
import org.dspace.sort.SortOption;
import org.dspace.ui.exception.PageNotFoundException;
import org.dspace.ui.utils.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UrlPathHelper;

/**
 * Controller for /handle paths
 * @author Tim Donohue
 */
@Controller
public class HandleController extends DSpaceController
{
    private static final Logger log = LoggerFactory.getLogger(HandleController.class);

    // Path that this Controller responds to
    private static final String PATH = "/handle";

    // This Controller responds to /handle/** path
    @RequestMapping(PATH + "/*/*")
    public String handle(Model model, HttpServletRequest request)
            throws SQLException
    {
        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        // Get full path (without context path)
        // This will return something like /handle/1/1
        String path = new UrlPathHelper().getPathWithinApplication(request);

        // Extract the handle prefix suffix out of our path
        String handle = path.substring(PATH.length()+1);

        // Now, get the object with this handle
        DSpaceObject dso = handleService.resolveToObject(context, handle);

        // Send to correct view based on type of object
        if(dso == null)
        {
            // Throw a 404 page not found
            throw new PageNotFoundException(path);
        }
        else if(dso instanceof Community)
        {
            return displayCommunity(context, (Community) dso, model, request);
        }
        else if(dso instanceof Collection)
        {
            return displayCollection(context, (Collection) dso, model, request);
        }
        else if(dso instanceof Item)
        {
            model.addAttribute("item", dso);
            return "item";          // item.html
        }
        else
        {
            // Throw a 404 page not found
            throw new PageNotFoundException(path);
        }
    }

    /**
     * Display the homepage for a single Community
     * @param context
     * @param community
     * @param model
     * @param request
     * @return
     * @throws SQLException
     */
    public String displayCommunity(Context context, Community community, Model model, HttpServletRequest request)
            throws SQLException
    {
        // Add all info the the model that we want to display
        model.addAttribute("community", community);

        // Get path to logo
        Bitstream logo = community.getLogo();
        String logoPath = null;
        if(logo!=null)
            logoPath = request.getContextPath() + "/retrieve/" + logo.getID();
        model.addAttribute("logo", logoPath);

        // Get other Community info for display
        CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
        model.addAttribute("name", community.getName());
        model.addAttribute("intro", communityService.getMetadata(community, "introductory_text"));
        model.addAttribute("copyright", communityService.getMetadata(community, "copyright_text"));
        model.addAttribute("news", communityService.getMetadata(community, "side_bar_text"));

        // Get recent submissions to this Community
        // TODO: Won't work without hooking up Solr
        // MAY NEED TO JUST RUN THIS AS A WAR alongside Solr, instead of standalone
        // http://www.petrikainulainen.net/spring-data-solr-tutorial/
        List<Item> recentSubmissions = getRecentSubmissions(context, community);
        model.addAttribute("recentSubmissions", recentSubmissions);

        // display community.html
        return "community";
    }

    /**
     * Display the homepage for a single Collection
     * @param context
     * @param collection
     * @param model
     * @param request
     * @return
     * @throws SQLException
     */
    public String displayCollection(Context context, Collection collection, Model model, HttpServletRequest request)
            throws SQLException
    {
        // Add all info the the model that we want to display
        model.addAttribute("collection", collection);

        // Get path to logo
        Bitstream logo = collection.getLogo();
        String logoPath = null;
        if(logo!=null)
            logoPath = request.getContextPath() + "/retrieve/" + logo.getID();
        model.addAttribute("logo", logoPath);

        // Get other Collection info for display
        CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
        model.addAttribute("name", collection.getName());
        model.addAttribute("intro", collectionService.getMetadata(collection, "introductory_text"));
        model.addAttribute("copyright", collectionService.getMetadata(collection, "copyright_text"));
        model.addAttribute("news", collectionService.getMetadata(collection, "side_bar_text"));

        // Get recent submissions to this Collection
        // TODO: Won't work without hooking up Solr
        //List<Item> recentSubmissions = getRecentSubmissions(context, collection);
        //model.addAttribute("recentSubmissions", recentSubmissions);

        ItemService itemService = ContentServiceFactory.getInstance().getItemService();

        // TODO: Not sure this is very scalable. Loads entire list into memory
        // at once for pagination using Spring's PagedListHolder
        // Solr queries would likely be better here
        Iterator<Item> itemIterator = itemService.findByCollection(context, collection);
        List<Item> itemList = IteratorUtils.toList(itemIterator);

        PagedListHolder<Item> items = new PagedListHolder<>(itemList);
        items.setPageSize(2);  // 10 items per page
        if(request.getParameter("page")!=null)
        {
            int page = Integer.parseInt(request.getParameter("page"));
            items.setPage(page);  // set current page
        }
        model.addAttribute("items", items);

        // display collection.html
        return "collection";
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
