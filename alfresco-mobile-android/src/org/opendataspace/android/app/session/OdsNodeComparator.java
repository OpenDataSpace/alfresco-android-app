package org.opendataspace.android.app.session;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.utils.NodeComparator;

public class OdsNodeComparator extends NodeComparator
{
    private static final long serialVersionUID = 1L;

    public static final String SORT_FOLDERS = ":folders";

    public OdsNodeComparator(boolean asc, String propertySorting)
    {
        super(asc, propertySorting);
    }

    @Override
    public int compare(Node nodeA, Node nodeB)
    {
        if (nodeA != null && nodeB != null && SORT_FOLDERS.equals(propertySorting))
        {
            boolean fa = nodeA.isFolder();
            boolean fb = nodeB.isFolder();
            int b = 0;

            if (fa != fb)
            {
                b = Boolean.valueOf(fb).compareTo(fa);
            }
            else
            {
                b = nodeA.getName().compareToIgnoreCase(nodeB.getName());
            }

            return asc ? b : -b;
        }

        return super.compare(nodeA, nodeB);
    }
}
