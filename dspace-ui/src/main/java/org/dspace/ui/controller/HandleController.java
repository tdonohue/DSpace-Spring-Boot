/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.controller;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Context;
import org.dspace.ui.exception.PageNotFoundException;
import org.dspace.ui.utils.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            return displayCommunity((Community) dso, model, request);
        }
        else if(dso instanceof Collection)
        {
            model.addAttribute("collection", dso);
            return "collection";    // collection.html
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
     * @param community
     * @param model
     * @param request
     * @return
     * @throws SQLException
     */
    public String displayCommunity(Community community, Model model, HttpServletRequest request)
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
        model.addAttribute("sidebar", communityService.getMetadata(community, "side_bar_text"));

        // display community.html
        return "community";
    }

}
