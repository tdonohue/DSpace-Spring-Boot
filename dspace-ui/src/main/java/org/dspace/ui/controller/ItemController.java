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
import org.dspace.ui.model.ItemModel;
import org.dspace.ui.model.MetadataEntry;
import org.dspace.ui.utils.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

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
            throw new PageNotFoundException("Item with handle " + handle);
        }
    }

    /**
     * Display Edit item form. This method receives redirect requests from
     * the HandleController, whenever a /handle/././edit path refers to an item
     * @param model
     * @param request
     * @param handle
     * @return
     * @throws SQLException 
     */
    @RequestMapping(value="/edit/item", method=RequestMethod.GET)
    public String editItem(Model model, HttpServletRequest request, @RequestParam String handle)
            throws SQLException
    {
        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        // Now, get the Item with this handle
        DSpaceObject dso = handleService.resolveToObject(context, handle);

        if(dso instanceof Item)
        {
            return displayItemEdit(context, (Item) dso, model, request);
        }
        else
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Item with handle " + handle);
        }
    }
    
    /**
     * Actually save changes to an item. This method receives POST requests
     * via the edit item form.
     * @param model
     * @param request
     * @param itemModel
     * @return
     * @throws SQLException 
     */
    @RequestMapping(value="/edit/item", method=RequestMethod.POST)
    public String saveItem(@ModelAttribute ItemModel itemModel, BindingResult result, Model model, HttpServletRequest request, @RequestParam String handle)
            throws SQLException
    {
        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        // Now, get the Item with this handle
        Item item = (Item) handleService.resolveToObject(context, handle);

        // If we had validation errors, return to item edit page
        if(result.hasErrors()) {
            model.addAttribute("itemModel", itemModel);
            return "item-edit";
            //return displayItemEdit(context, item, model, request);
        }

        for(MetadataEntry entry : itemModel.getAllMetadataEntries())
        {
            log.info("SAVED metadata field: " + entry.getKey() + ", value:" + entry.getValue());
        }
        
        // Return to item.html
        return displayItemHomepage(context, item, model, request);
    }


     /**
     * Display the homepage/splashpage for a single Item
     * @param context
     * @param item
     * @param model
     * @param request
     * @return
     * @throws SQLException
     */
    public String displayItemHomepage(Context context, Item item, Model model, HttpServletRequest request)
            throws SQLException
    {
        // Create a "model" item for display all metadata fields
        ItemModel itemModel = new ItemModel(item, context);
        model.addAttribute("itemModel", itemModel);

        // Get the content bundle
        ItemService itemService = item.getItemService();
        List<Bundle> bundles = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        // There should just be ONE content bundle. Add its bitstreams to our model
        if(bundles.size()>0)
        {
            model.addAttribute("bitstreams", bundles.get(0).getBitstreams());
        }

        // return item.html
        return "item";
    }
    
    /**
     * Display the edit page for single Item
     * @param context
     * @param item
     * @param model
     * @param request
     * @return
     * @throws SQLException
     */
    public String displayItemEdit(Context context, Item item, Model model, HttpServletRequest request)
            throws SQLException
    {
        ItemModel itemModel = new ItemModel(item, context);
        // Add all info the the model that we want to display
        model.addAttribute("itemModel", itemModel);

        // return item-edit.html
        return "item-edit";
    }
}
