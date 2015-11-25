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
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.ui.exception.PageNotFoundException;
import org.dspace.ui.utils.ContextUtil;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for all Collection activities.
 * <P>
 * Note: The HandleController forwards requests here if the object is a Collection.
 *
 * @author Tim Donohue
 */
@Controller
public class CollectionController extends DSpaceController
{

    // This Controller receives forwards (from HandleController) via the /collection path
    @RequestMapping("/collection")
    public String collection(Model model, HttpServletRequest request, @RequestParam String handle)
            throws SQLException
    {
        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        // Now, get the Collection with this handle
        DSpaceObject dso = handleService.resolveToObject(context, handle);

        if(dso instanceof Collection)
        {
            return displayCollectionHomepage(context, (Collection) dso, model, request);
        }
        else
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Collection with handle " + handle);
        }
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
    public String displayCollectionHomepage(Context context, Collection collection, Model model, HttpServletRequest request)
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
        //List<Item> recentSubmissions = HandleController.getRecentSubmissions(context, collection);
        //model.addAttribute("recentSubmissions", recentSubmissions);

        ItemService itemService = ContentServiceFactory.getInstance().getItemService();

        // TODO: Not sure this is very scalable. Loads entire list into memory
        // at once for pagination using Spring's PagedListHolder
        // Solr queries would likely be better here
        Iterator<Item> itemIterator = itemService.findByCollection(context, collection);
        List<Item> itemList = IteratorUtils.toList(itemIterator);

        PagedListHolder<Item> items = new PagedListHolder<>(itemList);
        items.setPageSize(10);  // 10 items per page
        if(request.getParameter("page")!=null)
        {
            int page = Integer.parseInt(request.getParameter("page"));
            items.setPage(page);  // set current page
        }
        model.addAttribute("items", items);

        // display collection.html
        return "collection";
    }

}
