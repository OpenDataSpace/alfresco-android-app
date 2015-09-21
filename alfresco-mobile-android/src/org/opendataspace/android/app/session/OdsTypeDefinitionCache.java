package org.opendataspace.android.app.session;

import org.apache.chemistry.opencmis.client.bindings.impl.TypeDefinitionCacheImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.SecondaryTypeDefinitionImpl;

public class OdsTypeDefinitionCache extends TypeDefinitionCacheImpl
{
    public static String LINK_TYPE_ID = "gds:link";
    public static String LINK_TYPE_UPLOAD = "gds:uploadLink";
    public static String LINK_TYPE_DOWNLAOD = "gds:downloadLink";

    public static String SUBJECT_PROP_ID = "gds:subject";
    public static String MESSAGE_PROP_ID = "gds:message";
    public static String EMAIL_PROP_ID = "gds:emailAddress";
    public static String URL_PROP_ID = "gds:url";
    public static String LTYPE_PROP_ID = "gds:linkType";

    private final AbstractTypeDefinition link;

    public OdsTypeDefinitionCache()
    {
        super();

        PropertyStringDefinitionImpl subject = new PropertyStringDefinitionImpl();
        subject.setId(SUBJECT_PROP_ID);

        PropertyStringDefinitionImpl message = new PropertyStringDefinitionImpl();
        message.setId(MESSAGE_PROP_ID);

        PropertyStringDefinitionImpl email = new PropertyStringDefinitionImpl();
        email.setId(EMAIL_PROP_ID);

        PropertyStringDefinitionImpl url = new PropertyStringDefinitionImpl();
        url.setId(URL_PROP_ID);

        PropertyDateTimeDefinitionImpl expire = new PropertyDateTimeDefinitionImpl();
        expire.setId(PropertyIds.EXPIRATION_DATE);

        PropertyStringDefinitionImpl type = new PropertyStringDefinitionImpl();
        type.setId(LTYPE_PROP_ID);

        link = new SecondaryTypeDefinitionImpl();
        link.setId(LINK_TYPE_ID);
        link.setBaseTypeId(BaseTypeId.CMIS_ITEM);
        link.addPropertyDefinition(subject);
        link.addPropertyDefinition(message);
        link.addPropertyDefinition(email);
        link.addPropertyDefinition(type);
        link.addPropertyDefinition(expire);
        link.addPropertyDefinition(type);
    }

    @Override
    public TypeDefinition get(String repositoryId, String typeId)
    {
        TypeDefinition def = super.get(repositoryId, typeId);

        if (def != null)
        {
            return def;
        }

        if (LINK_TYPE_ID.equals(typeId))
        {
            return link;
        }

        return null;
    }
}
