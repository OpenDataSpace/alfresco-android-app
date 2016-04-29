package org.opendataspace.android.app.session;

import org.alfresco.mobile.android.api.session.authentication.impl.PassthruAuthenticationProviderImpl;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.cookies.CmisCookieManager;
import org.apache.chemistry.opencmis.commons.SessionParameter;

import java.util.List;
import java.util.Map;

public class OdsAuthProviderImpl extends PassthruAuthenticationProviderImpl
{
    private static final long serialVersionUID = 1L;

    private CmisCookieManager cookieManager;

    @Override
    public Map<String, List<String>> getHTTPHeaders(String url)
    {
        Map<String, List<String>> result = super.getHTTPHeaders(url);

        // cookies
        if (cookieManager != null)
        {
            Map<String, List<String>> cookies = cookieManager.get(url, result);
            if (!cookies.isEmpty())
            {
                result.putAll(cookies);
            }
        }

        return result;
    }

    @Override
    public void putResponseHeaders(String url, int statusCode, Map<String, List<String>> headers)
    {
        super.putResponseHeaders(url, statusCode, headers);

        if (cookieManager != null)
        {
            cookieManager.put(url, headers);
        }
    }

    @Override
    public void setSession(BindingSession session)
    {
        super.setSession(session);

        if (isTrue(SessionParameter.COOKIES))
        {
            cookieManager = new CmisCookieManager(session.getSessionId());
        }
    }

    protected boolean isTrue(String parameterName)
    {
        Object value = getSession().get(parameterName);

        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }

        return value instanceof String && Boolean.parseBoolean((String) value);

    }
}
