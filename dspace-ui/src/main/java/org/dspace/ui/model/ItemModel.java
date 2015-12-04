/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ui.model;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang.time.DateFormatUtils;
import org.dspace.app.util.factory.UtilServiceFactory;
import org.dspace.app.util.service.MetadataExposureService;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * An ItemModel is essentially just a utility class (of getters/setters) that 
 * makes it easier to work with Items via Spring Boot views / Thymeleaf. See also
 * http://www.thymeleaf.org/doc/tutorials/2.1/thymeleafspring.html
 * <P>
 * It is used by both the Item View screen and the Item Edit screen.
 * <P>
 * This model does NOT actually save any object changes, that would be performed in the 
 * ItemController itself (but it's not yet complete)
 * <P>
 * WARNING: SETTERS ARE UNFINISHED / UNTESETED
 * <P>
 * TODO: Ideally, this would be reworked to just be getters/setters and to PULL
 * its list of fields to display/set from configuration. For example:
 *   * getAllMetadataEntries() would determine the list of metadata fields & their ordering from configuration.
 *   * allMetadataEntries would then be used in the item form on 'item-edit.html' to display all fields
 *     (in order) for editing. item-edit.html would also pull in labels via I18N.
 *   * possibly defining our own custom Bean Validation Provider, to validate fields based on 
 *     their *type* (e.g. dc.title treated as String, but dc.date.issued as a Date, etc). See:
 *     http://docs.spring.io/spring/docs/current/spring-framework-reference/html/validation.html#validation-beanvalidation-spring
 *     http://stackoverflow.com/a/28704025/3750035
 * <P>
 * Some of these concepts were borrowed from the REST API's
 * org.dspace.rest.common.Item class
 * 
 * @author Tim Donohue
 */
public class ItemModel extends DSpaceObjectModel
{
    
    private static final Logger log = LoggerFactory.getLogger(ItemModel.class);

    // Get access to the various services we need to construct our ItemModel
    protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    protected MetadataExposureService metadataExposureService = UtilServiceFactory.getInstance().getMetadataExposureService();
    protected AuthorizeService authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();

    private MultiValuedMap<String,MetadataEntry> metadata;
    private String isArchived;
    private String isWithdrawn;
    private String lastModified;
    
    private List<MetadataEntry> allMetadataEntries;
    
    private static final String DATE_FORMAT="yyyy-MM-dd";
    
    
    /**
     * Example of form validation using our issuedDate field, which must be a 
     * valid date of a specific format.
     */
    @DateTimeFormat(pattern=DATE_FORMAT)
    @NotNull
    private Date dateIssued;
    
    
    private ItemModel(){}

    /**
     * Constructor given an existing Item object
     * @param item
     * @param context
     * @throws SQLException 
     */
    public ItemModel(Item item, Context context) throws SQLException
    {
        super(item);
        setup(item, context);
    }
    
    private void setup(Item item, Context context) throws SQLException
    {
        // Initialize our MetadataEntry display list
        metadata = new ArrayListValuedHashMap<String,MetadataEntry>();
        
        // Get all item metadata fields
        List<MetadataValue> allMetadataValues = itemService.getMetadata(item, Item.ANY, Item.ANY, Item.ANY, Item.ANY);

        // Now we have to loop through each and see which fields are NOT hidden
        // ItemService should really offer a way to do this!
        for (MetadataValue metadataValue : allMetadataValues) {
            MetadataField metadataField = metadataValue.getMetadataField();
            // If it is NOT hidden, add to our display list
            if (!metadataExposureService.isHidden(context, metadataField.getMetadataSchema().getName(), metadataField.getElement(), metadataField.getQualifier())) {
                // Key is the metadata field name (e.g. 'dc.identifier.uri')
                String metadataKey = metadataField.toString('.');
                metadata.put(metadataKey, new MetadataEntry(metadataKey, metadataValue.getValue(), metadataValue.getLanguage()));
            }
        }

        // Set other object properties
        this.setArchived(Boolean.toString(item.isArchived()));
        this.setWithdrawn(Boolean.toString(item.isWithdrawn()));
        this.setLastModified(item.getLastModified().toString());
        
        // Get our dateIssued (from metadata fields loaded above)
        this.dateIssued = getDateIssued();
    }
    
    /**
     * Get a List of all MetadataEntry objects, which is every metadata
     * field on this item object.
     * @return 
     */
    public List<MetadataEntry> getAllMetadataEntries() 
    {
        if(allMetadataEntries==null || allMetadataEntries.isEmpty())
        {
            allMetadataEntries = new ArrayList(this.metadata.values());
        }
        return this.allMetadataEntries;
    }
    
    /**
     * Set List of all MetadataEntry objects, which is every metadata
     * field on this item object.
     */
    public void setAllMetadataEntries(List<MetadataEntry> metadataEntries) 
    {
        this.metadata.clear();
        for(MetadataEntry entry : metadataEntries)
        {
            this.metadata.put(entry.getKey(), entry);
        }
        this.allMetadataEntries = metadataEntries;
    }

    /**
     * Add a new metadata field to this model object.
     * @param metadataKey The full metadata field name (e.g. 'dc.identifier.uri')
     * @param metadataEntry The associated MetadataEntry object
     */
    public void addMetadataValue(String metadataKey, MetadataEntry metadataEntry) 
    {
        this.metadata.put(metadataKey, metadataEntry);
    }
    
    /**
     * Get values for a particular metadata field (from its key)
     * @param metadataKey metadata field key (e.g. dc.identifier.uri)
     * @return 
     */
    public List<String> getMetadataValues(String metadataKey) 
    {
        List<String> values = new ArrayList<>();
        Collection<MetadataEntry> entries = this.metadata.get(metadataKey);
        for(MetadataEntry entry: entries)
        {
           values.add(entry.getValue());
        }
        return values;
    }
    
    /**
     * Get first value of a metadata field
     * @param metadataKey
     * @return 
     */
    public String getMetadataFirstValue(String metadataKey) 
    {
        Collection<MetadataEntry> entries = this.metadata.get(metadataKey);
        if(entries!=null && !entries.isEmpty())
            return entries.iterator().next().getValue();
        else
            return null;
    }
    
    /**
     * Set values for a particular metadata field
     * @param metadataKey
     */
    public void setMetadataValues(String metadataKey, List<String> values) 
    {
        // Remove all existing values associated with this key
        this.metadata.remove(metadataKey);
        
        for(String value : values)
        {
            this.metadata.put(metadataKey, new MetadataEntry(metadataKey, value, MetadataEntry.DEFAULT_LANGUAGE));
        }
    }

    public String getArchived() {
        return this.isArchived;
    }

    public void setArchived(String archived) {
        this.isArchived = archived;
    }

    public String getWithdrawn() {
        return this.isWithdrawn;
    }

    public void setWithdrawn(String withdrawn) {
        this.isWithdrawn = withdrawn;
    }

    public String getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    
    /**
     * Special setter for IssuedDate, so that we can perform date validation
     * on this field
     * @param date 
     */
    public void setDateIssued(Date date) 
    {
        // Performs form validation (see definition of this field above)
        this.dateIssued = date;
        
        // Remove existing issue date
        this.metadata.remove("dc.date.issued");
        
        // Add new one
        this.metadata.put("dc.date.issued", new MetadataEntry("dc.date.issued", DateFormatUtils.format(date, DATE_FORMAT), null));
    }
    
    /**
     * Special getter for DateIssued, so that we can perform date validation
     * on this field
     * @return date
     */
    public Date getDateIssued() 
    {
        Collection<MetadataEntry> dates = this.metadata.get("dc.date.issued");
        Date date = null;
        
        if(dates!=null && !dates.isEmpty())
        {
            // Get the first one, as there's just one issued date
            MetadataEntry entry = dates.iterator().next();
            
            try
            {
                DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                date = df.parse(entry.getValue());
            }
            catch(ParseException e)
            {
                //otherwise, use our old DCDate method
                date = new DCDate(entry.getValue()).toDate();
            }
            
            this.dateIssued = date;
            return this.dateIssued;
        }
        else
            return null;
    }
    
    
}
