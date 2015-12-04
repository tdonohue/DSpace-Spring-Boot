/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.model;

import org.dspace.content.DSpaceObject;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.ui.controller.HandleController;

/**
 * A DSpaceObjectModel is essentially just a utility class that makes it easier to work
 * with DSpaceObject via Spring Boot. It wraps all our calls to the underlying API layer
 * into a Model object which is easy to use from our Views.
 * @author Tim Donohue
 */
public class DSpaceObjectModel 
{
    private String uuid;
    private String name;
    private String handle;
    private String type;
    
    /**
     * Public Constructor
     */
    public DSpaceObjectModel() {
    }

    /**
     * Constructor given an existing DSpaceObject
     * @param dso 
     */
    public DSpaceObjectModel(DSpaceObject dso) 
    {
        setUUID(dso.getID().toString());
        setName(dso.getName());
        setHandle(dso.getHandle());
        DSpaceObjectService dspaceObjectService = ContentServiceFactory.getInstance().getDSpaceObjectService(dso);
        setType(dspaceObjectService.getTypeText(dso).toLowerCase());
    }

    
    public String getName(){
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Get Link / URL Path to the object
     * @return path
     */
    public String getLink() {
        return HandleController.PATH + "/" + getHandle();
    }
    
    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
    
}
