/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.controller;

import com.google.api.client.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.ui.exception.PageNotFoundException;
import org.dspace.ui.utils.ContextUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UrlPathHelper;

/**
 * Controller for all Bitstream activities.
 *
 * @author Tim Donohue
 */
@Controller
public class BitstreamController extends DSpaceController
{

    /**
     * Download a Bitstream
     * @param model
     * @param request
     * @param response
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     */
    @RequestMapping("/bitstream/handle/*/*/*")
    public void getBitstream(Model model, HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, AuthorizeException
    {
        // Get full path (without context path)
        // This will return something like /handle/1/1
        String path = new UrlPathHelper().getPathWithinApplication(request);

        // Remove the /bitstream prefix from this path
        path = path.substring("/bitstream".length());

        // Now, extract the Object ID and extra info from path
        // objID should be the handle (e.g. 123/456)
        String handle = getObjectIDFromPath(path);
        // extraInfo should be the filename (e.g. file.pdf)
        String bitstreamName = getObjectExtraInfoFromPath(path);

        // Get DSpace context
        Context context = ContextUtil.obtainContext(request);

        // Now, get the Item with this handle
        DSpaceObject dso = handleService.resolveToObject(context, handle);

        if(dso instanceof Item)
        {
            BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();

            // Get our bitstream by its name
            Bitstream bitstream = bitstreamService.getBitstreamByName((Item)dso, Constants.CONTENT_BUNDLE_NAME, bitstreamName);
            if(bitstream==null)
                throw new PageNotFoundException("Bitstream '" + bitstreamName + "' on Item '" + handle + "'");
            else
            {
                // Retrieve Bitstream contents, copying it to the response
                try(InputStream is = bitstreamService.retrieve(context, bitstream))
                {
                    IOUtils.copy(is, response.getOutputStream(), true);
                    response.flushBuffer();
                }
            }
        }
        else
        {
            // Throw a 404 page not found
            throw new PageNotFoundException("Community with handle " + handle);
        }
    }

}
