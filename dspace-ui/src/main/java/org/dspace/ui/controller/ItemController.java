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
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.ui.exception.PageNotFoundException;
import org.dspace.ui.utils.ContextUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for all Item activities.
 * <P>
 * Note: The HandleController forwards requests here if the object is an Item.
 *
 * @author Tim Donohue
 */
@Controller
public class ItemController extends DSpaceController
{

    // This Controller receives forwards (from HandleController) via the /item path
    @RequestMapping("/item")
    public String item(Model model, HttpServletRequest request, @RequestParam String handle)
            throws SQLException
    {
        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        // Now, get the Item with this handle
        DSpaceObject dso = handleService.resolveToObject(context, handle);

        if(dso instanceof Item)
        {
            return displayItemHomepage(context, (Item) dso, model, request);
        }
        else
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Community with handle " + handle);
        }
    }


     /**
     * Display the homepage/splashpage for a single Item
     * @param context
     * @param community
     * @param model
     * @param request
     * @return
     * @throws SQLException
     */
    public String displayItemHomepage(Context context, Item item, Model model, HttpServletRequest request)
            throws SQLException
    {
        // Add all info the the model that we want to display
        model.addAttribute("item", item);
        model.addAttribute("title", item.getName());

        // Get itemService, which we will use to display specific metadata fields
        ItemService itemService = item.getItemService();
        model.addAttribute("itemService", itemService);

        // Get the content bundle
        List<Bundle> bundles = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        // There should just be ONE content bundle. Add its bitstreams to our model
        if(bundles.size()>0)
        {
            model.addAttribute("bitstreams", bundles.get(0).getBitstreams());
        }

        // return item.html
        return "item";
    }


}
