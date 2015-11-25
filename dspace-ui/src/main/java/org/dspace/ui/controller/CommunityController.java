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
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Context;
import org.dspace.ui.exception.PageNotFoundException;
import org.dspace.ui.utils.ContextUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for all Community activities.
 * <P>
 * Note: The HandleController forwards requests here if the object is a Community.
 *
 * @author Tim Donohue
 */
@Controller
public class CommunityController extends DSpaceController
{

    // This Controller receives forwards (from HandleController) via the /community path
    @RequestMapping("/community")
    public String community(Model model, HttpServletRequest request, @RequestParam String handle)
            throws SQLException
    {
        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        // Now, get the Collection with this handle
        DSpaceObject dso = handleService.resolveToObject(context, handle);

        if(dso instanceof Community)
        {
            return displayCommunityHomepage(context, (Community) dso, model, request);
        }
        else
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Community with handle " + handle);
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
    public String displayCommunityHomepage(Context context, Community community, Model model, HttpServletRequest request)
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
        //List<Item> recentSubmissions = HandleController.getRecentSubmissions(context, community);
        //model.addAttribute("recentSubmissions", recentSubmissions);

        // display community.html
        return "community";
    }
}
