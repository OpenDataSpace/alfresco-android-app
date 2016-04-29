package org.opendataspace.android.app.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

public class OdsStringUtils
{
    public static String join(Collection var0, String var1)
    {
        StringBuilder var2 = new StringBuilder();

        for (Iterator var3 = var0.iterator(); var3.hasNext(); var2.append((String) var3.next()))
        {
            if (var2.length() != 0)
            {
                var2.append(var1);
            }
        }

        return var2.toString();
    }

    public static String[] splitString(String var0, String var1)
    {
        StringTokenizer var2 = new StringTokenizer(var0, var1);
        String[] var3 = new String[var2.countTokens()];

        for (int var4 = 0; var4 < var3.length; ++var4)
        {
            var3[var4] = var2.nextToken();
        }

        return var3;
    }
}
