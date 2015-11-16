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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TestController {
    
    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    protected CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();

    // This reads the value of "spring.application.name" from application.properties
    // and assigns it to "appName"
    @Value("${spring.application.name}")
    private String appName;
    
    /**
     * Sample Controller. Responds to the "/test" path
     * @param name - This is an example, optional parameter which may be passed into the request (e.g. ?name=Tim on querystring)
     * @param model - Spring Model, used to pass data to the View
     * @param request - The current request object (provided automatically by Spring whenever added as a param)
     * @return name of the view to load
     * @throws SQLException if DSpace content cannot be loaded
     */
    // This Controller responds to /test path
    @RequestMapping("/test")
    public String test(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model, HttpServletRequest request)
            throws SQLException
    {
        // Get value of "name" passed on query string (if any)
        model.addAttribute("name", name);
        
        // Get application name from application.properties
        model.addAttribute("appName", appName);
        
        // Get various properties from dspace.cfg file
        ConfigurationService config = new DSpace().getConfigurationService();
        String dir = config.getProperty("dspace.dir");
        model.addAttribute("dir", dir);
        
        String host = config.getProperty("dspace.hostname");
        model.addAttribute("host", host);
        
        String url = config.getProperty("dspace.url");
        model.addAttribute("url", url);

        // Get list of DSpace Communities
        Context context = ContextUtil.obtainContext(request);
        // Load DSpace Communities & save to "communities" model attribute for View
        List<Community> communities = communityService.findAllTop(context);
        model.addAttribute("communities", communities);
        
        // NOTE: Context cannot be closed before the View loads. But, don't worry
        // as it will be closed automatically by DSpaceRequestContextFilter
        
        // Load test.html view
        return "test";
    }

}
