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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Login via Spring Security. See also WebSecurityConfig
 * @author Tim Donohue
 * @see org.dspace.ui.WebSecurityConfig
 */
@Controller
public class LoginController extends DSpaceController
{
    // This Controller responds to /login path
    @RequestMapping("/login")
    public String login(Model model, HttpServletRequest request)
            throws SQLException
    {
        // use login.html
        return "login"; 
    }
    
}
