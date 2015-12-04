/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.model;

import java.util.regex.Pattern;

/**
 * A single metadata field key, value and language.
 * <P>
 * This was stolen/borrowed directly from org.dspace.rest.common.MetadataEntry
 * @author Tim Donohue
 */
public class MetadataEntry 
{
    String key;

    String value;

    String language;
    
    // TODO: This should NOT be hardcoded
    public final static String DEFAULT_LANGUAGE="en";

    public MetadataEntry()
    {
    }

    public MetadataEntry(String key, String value, String language)
    {
        this.key = key;
        this.value = value;
        this.language = language;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getSchema() {
        String[] fieldPieces = key.split(Pattern.quote("."));
        return fieldPieces[0];
    }

    public String getElement() {
        String[] fieldPieces = key.split(Pattern.quote("."));
        return fieldPieces[1];
    }

    public String getQualifier() {
        String[] fieldPieces = key.split(Pattern.quote("."));
        if(fieldPieces.length == 3) {
            return fieldPieces[2];
        } else {
            return null;
        }
    }
    
}
