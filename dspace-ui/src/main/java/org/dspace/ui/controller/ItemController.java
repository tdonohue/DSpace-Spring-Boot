/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.controller;

import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.ui.exception.PageNotFoundException;
import org.dspace.ui.model.ItemModel;
import org.dspace.ui.model.MetadataEntry;
import org.dspace.ui.utils.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller for all Item activities.
 * <P>
 * Note: The HandleController forwards requests here if the object is an Item.
 *
 * @author Tim Donohue
 */
@Controller
@RequestMapping("/items")
public class ItemController extends DSpaceController
{
    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    
    /**
     * This Method produces our JSON or XML responses and performs the "business
     * logic" which makes up our REST API.
     * <P>
     * NOTE: This returns an ItemModel object, which just contains basic info
     * about the Item itself, useful in Views or REST calls
     * @param id UUID which is referenced on the path
     * @param request current request
     * @return an ItemModel object (which contains info about our Item)
     * @throws SQLException 
     */
    @RequestMapping(path="/{id}", produces={XML_RESPONSE, JSON_RESPONSE})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ItemModel getItem(@PathVariable UUID id, HttpServletRequest request)
            throws SQLException
    {
        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        Item item = itemService.find(context, id);
        
        if(item!=null)
        {
            return new ItemModel(item, context);
        }
        else
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Item with ID=" + id);
        }
    }
    
    /**
     * This Method is called for all HTML requests. It uses the above REST method
     * to generate the ItemModel, and then passes it to the View (to generate HTML).
     * <P>
     * NOTE: This method also receives redirects from the HandleController, whenever
     * it encounters a Handle that refers to an Item object.
     * @param id UUID passed in on path
     * @param model Current Model object for view
     * @param request Current request
     * @return path to view
     * @throws SQLException 
     */
    @RequestMapping(path="/{id}")
    public String item(@PathVariable UUID id, Model model, HttpServletRequest request)
            throws SQLException
    {
        // Call getItem() above, which handles REST requests
        ItemModel itemModel = getItem(id, request);
        
        // Create a "model" item for display all metadata fields
        //ItemModel itemModel = new ItemModel(item, context);
        model.addAttribute("itemModel", itemModel);

         // Display our Item homepage (item.html)
        return "item";
    }

    /**
     * Display Edit item form. This method receives redirect requests from
     * the HandleController, whenever a /handle/././edit path refers to an item
     * @param id UUID passed in on path
     * @param model
     * @param request
     * @return
     * @throws SQLException 
     */
    @RequestMapping(path="/{id}/edit", method=RequestMethod.GET)
    public String editItem(@PathVariable UUID id, Model model, HttpServletRequest request)
            throws SQLException
    {
        // Call getItem() above, which handles REST requests
        ItemModel itemModel = getItem(id,request);

        //ItemModel itemModel = new ItemModel(item, context);
        // Add all info the the model that we want to display
        model.addAttribute("itemModel", itemModel);

        // return item-edit.html
        return "item-edit";
    }
    
    /**
     * Actually save changes to an item. This method receives POST requests
     * via the edit item form.
     * @param id The ID of the Item
     * @param model
     * @param result
     * @param itemModel
     * @param request
     * @return
     * @throws SQLException 
     */
    @RequestMapping(path="/{id}/edit", method=RequestMethod.POST)
    public String saveItem(@PathVariable UUID id, @ModelAttribute ItemModel itemModel, BindingResult result, Model model, HttpServletRequest request, @RequestParam String handle)
            throws SQLException
    {
        // Create a "model" item for display all metadata fields
        //ItemModel itemModel = new ItemModel(item, context);
        model.addAttribute("itemModel", itemModel);
        
        // If we had validation errors, return to item edit page
        if(result.hasErrors()) {
            log.error( "There are form errors! {}", result );
            // return item-edit.html
            return "item-edit";
        }

        for(MetadataEntry entry : itemModel.getAllMetadataEntries())
        {
            log.info("SAVED metadata field: " + entry.getKey() + ", value:" + entry.getValue());
        }
        
        // Display our Item homepage (item.html)
        return "item";
    }
}
