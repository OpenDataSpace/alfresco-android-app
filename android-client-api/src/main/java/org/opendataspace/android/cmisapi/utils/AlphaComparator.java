/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.opendataspace.android.cmisapi.utils;

import java.io.Serializable;
import java.util.Comparator;

import org.opendataspace.android.cmisapi.model.Site;
import org.opendataspace.android.cmisapi.services.SiteService;

public class AlphaComparator implements Serializable, Comparator<Site>
{

    private static final long serialVersionUID = 1L;

    private boolean asc;

    private String propertySorting;

    public AlphaComparator(boolean asc, String propertySorting)
    {
        super();
        this.asc = asc;
        this.propertySorting = propertySorting;
    }

    public int compare(Site siteA, Site siteB)
    {
        int b = 0;
        if (SiteService.SORT_PROPERTY_SHORTNAME.equals(propertySorting))
        {
            b = siteA.getShortName().compareToIgnoreCase(siteB.getShortName());
        }
        else
        {
            b = siteA.getTitle().compareToIgnoreCase(siteB.getTitle());
        }
        if (asc)
        {
            return b;
        }
        else
        {
            return -b;
        }
    }
}