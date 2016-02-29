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
import org.dspace.content.Bitstream;
import org.dspace.content.Community;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Context;
import org.dspace.ui.exception.PageNotFoundException;
import org.dspace.ui.utils.ContextUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for all Community activities.
 * <P>
 * Note: The HandleController forwards requests here if the object is a Community.
 *
 * @author Tim Donohue
 */
@Controller
@RequestMapping("/communities")
public class CommunityController extends DSpaceController
{
    protected CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
    
    // This Controller receives forwards (from HandleController) via the /communities path
    @RequestMapping("/{id}")
    public String community(@PathVariable UUID id, Model model, HttpServletRequest request)
            throws SQLException
    {
        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        Community community = communityService.find(context, id);

        if(community!=null)
        {
            return displayCommunityHomepage(context, community, model, request);
        }
        else
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Community with ID=" + id);
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
