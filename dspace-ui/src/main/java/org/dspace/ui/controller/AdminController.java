/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dspace.authorize.AuthorizeException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Sample AdminController which provides tools only accessible to Admin role
 * @author Tim Donohue
 */
@Controller
public class AdminController 
{
    @RequestMapping("/admin")
    public String adminHomepage(Model model, HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, AuthorizeException
    {
        return "admin";
    }
 
}
