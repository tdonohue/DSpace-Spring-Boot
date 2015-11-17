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
import org.dspace.content.Community;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.ui.utils.ContextUtil;
import org.dspace.utils.DSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController extends DSpaceController
{
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    protected CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();

    // This Controller responds to / path
    @RequestMapping("/")
    public String home(Model model, HttpServletRequest request)
            throws SQLException
    {
        // Get application name from application.properties
        model.addAttribute("applicationName", applicationName);
        
        // Get various properties from dspace.cfg file
        ConfigurationService config = new DSpace().getConfigurationService();

        model.addAttribute("path", request.getContextPath());

        // Get list of DSpace Communities
        Context context = ContextUtil.obtainContext(request);
        // Load DSpace Communities & save to "communities" model attribute for View
        List<Community> communities = communityService.findAllTop(context);
        model.addAttribute("communities", communities);
        
        // NOTE: Context cannot be closed before the View loads. But, don't worry
        // as it will be closed automatically by DSpaceRequestContextFilter
        
        // Load home.html view
        return "home";
    }

}
