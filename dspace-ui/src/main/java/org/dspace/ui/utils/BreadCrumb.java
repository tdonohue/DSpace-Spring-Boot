/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.utils;

/**
 * A single breadcrumb in a UI breadcrumb list.
 * Used to generate the page breadcrumbs in the layout
 *
 * @author Tim Donohue
 */
public class BreadCrumb
{
    // The path the breadcrumb links to
    String path;
    // The label (pretty name)
    String label;

    public BreadCrumb(String label, String path)
    {
        this.label = label;
        this.path = path;
    }

    public BreadCrumb()
    {
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return this.label;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return this.path;
    }


}
