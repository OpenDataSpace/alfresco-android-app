package org.opendataspace.android.app.session;

import org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyBooleanDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDateTimeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDecimalDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyHtmlDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIdDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIntegerDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyStringDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyUriDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OdsObjectFactoryImpl extends AlfrescoObjectFactoryImpl
{
    private static final long serialVersionUID = 1L;

    private Session session;

    public void initialize(Session session, Map<String, String> parameters)
    {
        super.initialize(session, parameters);
        this.session = session;
    }

    @SuppressWarnings("unchecked")
    public Properties convertProperties(Map<String, ?> properties, ObjectType type,
                                        Collection<SecondaryType> secondaryTypes, Set<Updatability> updatabilityFilter)
    {
        // check input
        if (properties == null)
        {
            return null;
        }

        // get the type
        if (type == null)
        {
            Object typeId = properties.get(PropertyIds.OBJECT_TYPE_ID);
            if (!(typeId instanceof String))
            {
                throw new IllegalArgumentException("Type or type property must be set!");
            }

            if (typeId != "cmis:item")
            {
                return super.convertProperties(properties, null, secondaryTypes, updatabilityFilter);
            }

            type = session.getTypeDefinition(typeId.toString());
        }

        // get secondary types
        Collection<SecondaryType> allSecondaryTypes = null;
        Object secondaryTypeIds = properties.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
        if (secondaryTypeIds instanceof List)
        {
            allSecondaryTypes = new ArrayList<SecondaryType>();

            for (Object secondaryTypeId : (List<?>) secondaryTypeIds)
            {
                if (!(secondaryTypeId instanceof String))
                {
                    throw new IllegalArgumentException(
                            "Secondary types property contains an invalid entry: " + secondaryTypeId);
                }

                ObjectType secondaryType = session.getTypeDefinition(secondaryTypeId.toString());
                if (!(secondaryType instanceof SecondaryType))
                {
                    throw new IllegalArgumentException(
                            "Secondary types property contains a type that is not a secondary type: " +
                                    secondaryTypeId);
                }

                allSecondaryTypes.add((SecondaryType) secondaryType);
            }
        }

        if (secondaryTypes != null && allSecondaryTypes == null)
        {
            allSecondaryTypes = secondaryTypes;
        }

        boolean found = false;

        if (allSecondaryTypes != null)
        {
            for (SecondaryType tp : allSecondaryTypes)
            {
                final String id = tp.getId();

                if (id.equals(OdsTypeDefinitionCache.LINK_TYPE_DOWNLAOD) ||
                        id.equals(OdsTypeDefinitionCache.LINK_TYPE_UPLOAD) ||
                        id.equals(OdsTypeDefinitionCache.LINK_TYPE_ID))
                {
                    found = true;
                    break;
                }
            }
        }

        if (!found)
        {
            return super.convertProperties(properties, type, secondaryTypes, updatabilityFilter);
        }

        // some preparation
        BindingsObjectFactory bof = getBindingsObjectFactory();
        List<PropertyData<?>> propertyList = new ArrayList<PropertyData<?>>();

        // the big loop
        for (Map.Entry<String, ?> property : properties.entrySet())
        {
            if ((property == null) || (property.getKey() == null))
            {
                continue;
            }

            String id = property.getKey();
            Object value = property.getValue();

            if (value instanceof Property<?>)
            {
                Property<?> p = (Property<?>) value;
                if (!id.equals(p.getId()))
                {
                    throw new IllegalArgumentException("Property id mismatch: '" + id + "' != '" + p.getId() + "'!");
                }
                value = (p.getDefinition().getCardinality() == Cardinality.SINGLE ? p.getFirstValue() : p.getValues());
            }

            // get the property definition
            PropertyDefinition<?> definition = type.getPropertyDefinitions().get(id);

            if (definition == null)
            {
                for (SecondaryType secondaryType : allSecondaryTypes)
                {
                    if (secondaryType != null && secondaryType.getPropertyDefinitions() != null)
                    {
                        definition = secondaryType.getPropertyDefinitions().get(id);
                        if (definition != null)
                        {
                            break;
                        }
                    }
                }
            }

            if (definition == null)
            {
                throw new IllegalArgumentException(
                        "Property '" + id + "' is not valid for this type or one of the secondary types!");
            }

            // check updatability
            if (updatabilityFilter != null)
            {
                if (!updatabilityFilter.contains(definition.getUpdatability()))
                {
                    continue;
                }
            }

            // single and multi value check
            List<?> values;
            if (value == null)
            {
                values = null;
            }
            else if (value instanceof List<?>)
            {
                if (definition.getCardinality() != Cardinality.MULTI)
                {
                    throw new IllegalArgumentException("Property '" + id + "' is not a multi value property!");
                }
                values = (List<?>) value;

                // check if the list is homogeneous and does not contain null
                // values
                Class<?> valueClazz = null;
                for (Object o : values)
                {
                    if (o == null)
                    {
                        throw new IllegalArgumentException("Property '" + id + "' contains null values!");
                    }
                    if (valueClazz == null)
                    {
                        valueClazz = o.getClass();
                    }
                    else
                    {
                        if (!valueClazz.isInstance(o))
                        {
                            throw new IllegalArgumentException("Property '" + id + "' is inhomogeneous!");
                        }
                    }
                }
            }
            else
            {
                if (definition.getCardinality() != Cardinality.SINGLE)
                {
                    throw new IllegalArgumentException("Property '" + id + "' is not a single value property!");
                }
                values = Collections.singletonList(value);
            }

            // assemble property
            PropertyData<?> propertyData = null;
            Object firstValue = (values == null || values.isEmpty() ? null : values.get(0));

            if (definition instanceof PropertyStringDefinition)
            {
                if (firstValue == null)
                {
                    propertyData = bof.createPropertyStringData(id, (List<String>) null);
                }
                else if (firstValue instanceof String)
                {
                    propertyData = bof.createPropertyStringData(id, (List<String>) values);
                }
                else
                {
                    throwWrongTypeError(firstValue, "string", String.class, id);
                }
            }
            else if (definition instanceof PropertyIdDefinition)
            {
                if (firstValue == null)
                {
                    propertyData = bof.createPropertyIdData(id, (List<String>) null);
                }
                else if (firstValue instanceof String)
                {
                    propertyData = bof.createPropertyIdData(id, (List<String>) values);
                }
                else
                {
                    throwWrongTypeError(firstValue, "string", String.class, id);
                }
            }
            else if (definition instanceof PropertyHtmlDefinition)
            {
                if (firstValue == null)
                {
                    propertyData = bof.createPropertyHtmlData(id, (List<String>) values);
                }
                else if (firstValue instanceof String)
                {
                    propertyData = bof.createPropertyHtmlData(id, (List<String>) values);
                }
                else
                {
                    throwWrongTypeError(firstValue, "html", String.class, id);
                }
            }
            else if (definition instanceof PropertyUriDefinition)
            {
                if (firstValue == null)
                {
                    propertyData = bof.createPropertyUriData(id, (List<String>) null);
                }
                else if (firstValue instanceof String)
                {
                    propertyData = bof.createPropertyUriData(id, (List<String>) values);
                }
                else
                {
                    throwWrongTypeError(firstValue, "uri", String.class, id);
                }
            }
            else if (definition instanceof PropertyIntegerDefinition)
            {
                if (firstValue == null)
                {
                    propertyData = bof.createPropertyIntegerData(id, (List<BigInteger>) null);
                }
                else if (firstValue instanceof BigInteger)
                {
                    propertyData = bof.createPropertyIntegerData(id, (List<BigInteger>) values);
                }
                else if ((firstValue instanceof Byte) || (firstValue instanceof Short) ||
                        (firstValue instanceof Integer) || (firstValue instanceof Long))
                {
                    // we accept all kinds of integers
                    List<BigInteger> list = new ArrayList<BigInteger>(values.size());
                    for (Object v : values)
                    {
                        list.add(BigInteger.valueOf(((Number) v).longValue()));
                    }

                    propertyData = bof.createPropertyIntegerData(id, list);
                }
                else
                {
                    throwWrongTypeError(firstValue, "integer", BigInteger.class, id);
                }
            }
            else if (definition instanceof PropertyBooleanDefinition)
            {
                if (firstValue == null)
                {
                    propertyData = bof.createPropertyBooleanData(id, (List<Boolean>) null);
                }
                else if (firstValue instanceof Boolean)
                {
                    propertyData = bof.createPropertyBooleanData(id, (List<Boolean>) values);
                }
                else
                {
                    throwWrongTypeError(firstValue, "boolean", Boolean.class, id);
                }
            }
            else if (definition instanceof PropertyDecimalDefinition)
            {
                if (firstValue == null)
                {
                    propertyData = bof.createPropertyDecimalData(id, (List<BigDecimal>) null);
                }
                else if (firstValue instanceof BigDecimal)
                {
                    propertyData = bof.createPropertyDecimalData(id, (List<BigDecimal>) values);
                }
                else if ((firstValue instanceof Float) || (firstValue instanceof Double) ||
                        (firstValue instanceof Byte) || (firstValue instanceof Short) ||
                        (firstValue instanceof Integer) || (firstValue instanceof Long))
                {
                    // we accept all kinds of integers
                    // as well as floats and doubles
                    List<BigDecimal> list = new ArrayList<BigDecimal>(values.size());
                    for (Object v : values)
                    {
                        list.add(new BigDecimal(v.toString()));
                    }

                    propertyData = bof.createPropertyDecimalData(id, list);
                }
                else
                {
                    throwWrongTypeError(firstValue, "decimal", BigDecimal.class, id);
                }
            }
            else if (definition instanceof PropertyDateTimeDefinition)
            {
                if (firstValue == null)
                {
                    propertyData = bof.createPropertyDateTimeData(id, (List<GregorianCalendar>) null);
                }
                else if (firstValue instanceof GregorianCalendar)
                {
                    propertyData = bof.createPropertyDateTimeData(id, (List<GregorianCalendar>) values);
                }
                else if (firstValue instanceof Date)
                {
                    List<GregorianCalendar> list = new ArrayList<GregorianCalendar>(values.size());
                    for (Object d : values)
                    {
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTime((Date) d);
                        list.add(cal);
                    }
                    propertyData = bof.createPropertyDateTimeData(id, list);
                }
                else
                {
                    throwWrongTypeError(firstValue, "datetime", GregorianCalendar.class, id);
                }
            }

            // do we have something?
            if (propertyData == null)
            {
                throw new IllegalArgumentException("Property '" + id + "' doesn't match the property defintion!");
            }

            propertyList.add(propertyData);
        }

        return bof.createPropertiesData(propertyList);
    }

    private void throwWrongTypeError(Object obj, String type, Class<?> clazz, String id)
    {
        String expectedTypes;
        if (clazz.equals(BigInteger.class))
        {
            expectedTypes = "<BigInteger, Byte, Short, Integer, Long>";
        }
        else if (clazz.equals(BigDecimal.class))
        {
            expectedTypes = "<BigDecimal, Double, Float, Byte, Short, Integer, Long>";
        }
        else if (clazz.equals(GregorianCalendar.class))
        {
            expectedTypes = "<java.util.GregorianCalendar, java.util.Date>";
        }
        else
        {
            expectedTypes = clazz.getName();
        }

        String message = "Property '" + id + "' is a " + type + " property. Expected type '" + expectedTypes +
                "' but received a '" + obj.getClass().getName() + "' property.";

        throw new IllegalArgumentException(message);
    }
}
