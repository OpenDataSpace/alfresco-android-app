package org.opendataspace.android.app.manager;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class CookieStoreImpl implements CookieStore
{
    private HashMap<String, ArrayList<HttpCookie>> mCookie = new HashMap<String, ArrayList<HttpCookie>>();

    @Override
    public void add(URI uri, HttpCookie cookie)
    {
        if (uri == null || cookie == null)
        {
            return;
        }

        String url = uri.toString();
        ArrayList<HttpCookie> c = new ArrayList<HttpCookie>();

        if (!mCookie.containsKey(url))
        {
            c.add(cookie);
            mCookie.put(url, c);
        }
        else
        {
            c = mCookie.get(url);
            c.add(cookie);
            mCookie.remove(url);
            mCookie.put(url, c);
        }
    }

    @Override
    public List<HttpCookie> get(URI uri)
    {
        ArrayList<HttpCookie> list = new ArrayList<HttpCookie>();
        if (uri == null)
        {
            return list;
        }

        String key = uri.toString();
        if (!mCookie.containsKey(key))
        {
            return list;
        }

        List<HttpCookie> cook = mCookie.get(key);
        if (cook == null || cook.size() == 0)
        {
            return list;
        }

        return cook;
    }

    @Override
    public List<HttpCookie> getCookies()
    {
        ArrayList<HttpCookie> list = new ArrayList<HttpCookie>();

        for (Entry<String, ArrayList<HttpCookie>> cooklist : mCookie.entrySet())
        {
            if (cooklist != null && cooklist.getValue() != null)
            {
                for (HttpCookie cook : cooklist.getValue())
                {
                    if (cook != null)
                    {
                        list.add(cook);
                    }
                }
            }

        }

        return list;
    }

    @Override
    public List<URI> getURIs()
    {
        ArrayList<URI> list = new ArrayList<URI>();
        for (Entry<String, ArrayList<HttpCookie>> cooklist : mCookie.entrySet())
        {
            if (cooklist != null && cooklist.getKey() != null)
            {
                try
                {
                    list.add(new URI(cooklist.getKey()));
                } catch (URISyntaxException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return list;
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie)
    {
        Boolean res = false;

        if (uri != null)
        {
            if (mCookie.containsKey(uri.toString()))
            {
                mCookie.remove(uri.toString());
                return true;

            }
        }

        for (Entry<String, ArrayList<HttpCookie>> cooklist : mCookie.entrySet())
        {
            if (cooklist != null && cooklist.getValue() != null)
            {
                for (HttpCookie cook : cooklist.getValue())
                {
                    if (cook != null)
                    {
                        if (cookie.equals(cook))
                        {
                            res = cooklist.getValue().remove(cook);
                        }
                    }
                }
            }
        }

        return res;
    }

    @Override
    public boolean removeAll()
    {
        mCookie.clear();
        return true;
    }
}
