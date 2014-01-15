package org.alfresco.mobile.android.application.session;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.model.impl.FolderImpl;
import org.alfresco.mobile.android.api.model.impl.onpremise.OnPremiseRepositoryInfoImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.session.authentication.AuthenticationProvider;
import org.alfresco.mobile.android.api.session.authentication.impl.PassthruAuthenticationProviderImpl;
import org.alfresco.mobile.android.api.session.impl.RepositorySessionImpl;
import org.alfresco.mobile.android.api.utils.OnPremiseUrlRegistry;
import org.alfresco.mobile.android.api.utils.messages.Messagesl18n;
import org.alfresco.mobile.android.application.accounts.Account;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;

public class OdsRepositorySession extends RepositorySessionImpl
{
    public static final String PROTO_TYPE = "org.opendataspace.android.app.session.proto";

    public static final String BINDING_JSON = "/cmis/browser";

    private OdsRepositorySession shared;
    private OdsRepositorySession global;

    private OdsRepositorySession()
    {
        super();
    }

    private OdsRepositorySession(String url, String username, String password, Map<String, Serializable> settings)
    {
        super(url, username, password, settings);
    }

    public static RepositorySession connect(String url, String username, String password,
            Map<String, Serializable> parameters)
    {
        if (url == null || url.isEmpty()) { throw new IllegalArgumentException(String.format(
                Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "url")); }

        if (username == null || username.isEmpty()) { throw new IllegalArgumentException(String.format(
                Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "username")); }

        return new OdsRepositorySession(url, username, password, parameters);
    }

    @Override
    protected Session createSession(SessionFactory sessionFactory,
            AuthenticationProvider authenticator, Map<String, String> param)
    {
        try
        {
            if (param.get(SessionParameter.REPOSITORY_ID) != null)
            {
                return super.createSession(sessionFactory, authenticator, param);
            }
            else
            {
                Session ses = findSession(((SessionFactoryImpl) sessionFactory)
                        .getRepositories(param, null, new PassthruAuthenticationProviderImpl(authenticator), null));

                return ses != null ? ses : super.createSession(sessionFactory, authenticator, param);
            }
        }
        catch (CmisPermissionDeniedException e)
        {
            throw new AlfrescoSessionException(ErrorCodeRegistry.SESSION_UNAUTHORIZED, e);
        }
        catch (Exception e)
        {
            throw new AlfrescoSessionException(ErrorCodeRegistry.SESSION_GENERIC, e);
        }
    }

    @Override
    protected Session createSession(SessionFactory sessionFactory, Map<String, String> param)
    {
        try
        {
            if (param.get(SessionParameter.REPOSITORY_ID) != null)
            {
                return super.createSession(sessionFactory, param);
            }
            else
            {
                Session ses = findSession(sessionFactory.getRepositories(param));
                return ses != null ? ses : super.createSession(sessionFactory, param);
            }
        }
        catch (CmisPermissionDeniedException e)
        {
            throw new AlfrescoSessionException(ErrorCodeRegistry.SESSION_UNAUTHORIZED, e);
        }
        catch (Exception e)
        {
            throw new AlfrescoSessionException(ErrorCodeRegistry.SESSION_GENERIC, e);
        }
    }

    private Session findSession(List<Repository> ls)
    {
        Session ses = null;

        for (Repository cur : ls)
        {
            String name = cur.getName();

            if (name.equals("my"))
            {
                ses = cur.createSession();
            }
            else if (name.equals("shared"))
            {
                shared = create(cur.createSession());
            }
            else if (name.equals("global"))
            {
                global = create(cur.createSession());
            }
        }

        return ses;
    }

    public OdsRepositorySession getShared()
    {
        return shared;
    }

    public OdsRepositorySession getGlobal()
    {
        return global;
    }

    private OdsRepositorySession create(Session ses)
    {
        OdsRepositorySession rep = new OdsRepositorySession();
        rep.initSettings(baseUrl, userIdentifier, password, new HashMap<String, Serializable> (userParameters));
        rep.cmisSession = ses;
        rep.rootNode = new FolderImpl(rep.cmisSession.getRootFolder());
        rep.repositoryInfo = new OnPremiseRepositoryInfoImpl(rep.cmisSession.getRepositoryInfo());
        rep.create();
        return rep;
    }

    @Override
    protected void createCmisSettings()
    {
        super.createCmisSettings();

        if (isJsonProto(userParameters))
        {
            sessionParameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
            sessionParameters.put(SessionParameter.BROWSER_URL, getBaseUrl());
        }

        sessionParameters.put(SessionParameter.COOKIES, "true");
    }

    private boolean isJsonProto(Map<String, Serializable> params) {
        return params != null && Account.ProtocolType.JSON.equals((Account.ProtocolType) params.get(PROTO_TYPE));
    }

    @Override
    protected void initSettings(String url, String username, String password, Map<String, Serializable> settings)
    {
        try {
            URL u = new URL(url);

            if ("".equals(u.getPath()))
            {
                url += isJsonProto(settings) ? OdsRepositorySession.BINDING_JSON : OnPremiseUrlRegistry.BINDING_CMIS;
            }
        } catch (Exception ex) {
            // nothing
        }

        Map<String, Serializable> tmpSettings = new HashMap<String, Serializable>();

        if (settings != null)
        {
            tmpSettings.putAll(settings);
        }

        tmpSettings.put(AlfrescoSession.HTTP_CHUNK_TRANSFERT, "true");
        super.initSettings(url, username, password, tmpSettings);
    }
}
