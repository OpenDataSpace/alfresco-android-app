package org.opendataspace.android.app.session;

import android.os.Parcel;
import android.os.Parcelable;

import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.model.impl.FolderImpl;
import org.alfresco.mobile.android.api.model.impl.onpremise.OnPremiseRepositoryInfoImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.session.authentication.AuthenticationProvider;
import org.alfresco.mobile.android.api.session.authentication.impl.PassthruAuthenticationProviderImpl;
import org.alfresco.mobile.android.api.session.impl.RepositorySessionImpl;
import org.alfresco.mobile.android.api.utils.messages.Messagesl18n;
import org.alfresco.mobile.android.application.accounts.Account;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.opendataspace.android.ui.logging.OdsLog;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdsRepositorySession extends RepositorySessionImpl
{
    public static final String PROTO_TYPE = "org.opendataspace.android.app.session.proto";

    public static final String BINDING_JSON = "/cmis/browser";
    public static final String BINDING_ATOM = "/cmis/atom11";

    public enum LinkCapablilty
    {
        UNKNOWN, ITEM, COMBINED
    }

    private OdsRepositorySession shared;
    private OdsRepositorySession global;
    private OdsRepositorySession current;
    private OdsRepositorySession projects;
    private OdsRepositorySession config;
    private WeakReference<OdsRepositorySession> parent;
    private List<Repository> repos;
    private LinkCapablilty lcap = LinkCapablilty.UNKNOWN;
    private final OdsRepoType repoType;

    private OdsRepositorySession(OdsRepoType repoType)
    {
        super();
        this.repoType = repoType;
    }

    private OdsRepositorySession(Parcel p)
    {
        super(p);
        this.repoType = OdsRepoType.DEFAULT;
    }

    private OdsRepositorySession(String url, String username, String password, Map<String, Serializable> settings)
    {
        super(url, username, password, settings);
        this.repoType = OdsRepoType.DEFAULT;
        getLinkCapablilty();
    }

    public static RepositorySession connect(String url, String username, String password,
                                            Map<String, Serializable> parameters)
    {
        if (url == null || url.isEmpty())
        {
            throw new IllegalArgumentException(
                    String.format(Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "url"));
        }

        if (username == null || username.isEmpty())
        {
            throw new IllegalArgumentException(
                    String.format(Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "username"));
        }

        return new OdsRepositorySession(url, username, password, parameters);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Session createSession(SessionFactory sessionFactory, AuthenticationProvider authenticator,
                                    Map<String, String> param)
    {
        try
        {
            if (param.get(SessionParameter.REPOSITORY_ID) != null)
            {
                return super.createSession(sessionFactory, authenticator, param);
            }
            else
            {
                repos = ((SessionFactoryImpl) sessionFactory)
                        .getRepositories(param, null, new PassthruAuthenticationProviderImpl(authenticator), null);
                Session ses = findSession();
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
                repos = sessionFactory.getRepositories(param);
                Session ses = findSession();
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

    private Session findSession()
    {
        Session ses = null;

        for (Repository cur : repos)
        {
            String name = cur.getName();

            if (name.equals("my"))
            {
                ses = cur.createSession();
            }
            else if (name.equals("shared"))
            {
                shared = create(cur.createSession(), this, OdsRepoType.SHARED);
            }
            else if (name.equals("global"))
            {
                global = create(cur.createSession(), this, OdsRepoType.GLOBAL);
            }
            else if (name.equals("projects"))
            {
                projects = create(cur.createSession(), this, OdsRepoType.PROJECTS);
            }
            else if (name.equals("config"))
            {
                config = create(cur.createSession(), this, OdsRepoType.CONFIG);
            }
        }

        return ses;
    }

    private OdsRepositorySession create(Session ses, OdsRepositorySession parent, OdsRepoType repoType)
    {
        OdsRepositorySession rep = new OdsRepositorySession(repoType);
        rep.initSettings(baseUrl, userIdentifier, password, new HashMap<String, Serializable>(userParameters));
        rep.cmisSession = ses;
        rep.rootNode = new FolderImpl(rep.cmisSession.getRootFolder());
        rep.repositoryInfo = new OnPremiseRepositoryInfoImpl(rep.cmisSession.getRepositoryInfo());
        rep.initServices();
        rep.parent = new WeakReference<OdsRepositorySession>(parent);
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

    private boolean isJsonProto(Map<String, Serializable> params)
    {
        return params != null && Account.ProtocolType.JSON.equals(params.get(PROTO_TYPE));
    }

    @Override
    protected void initSettings(String url, String username, String password, Map<String, Serializable> settings)
    {
        try
        {
            URL u = new URL(url);

            if ("".equals(u.getPath()))
            {
                url += isJsonProto(settings) ? OdsRepositorySession.BINDING_JSON : OdsRepositorySession.BINDING_ATOM;
            }
        }
        catch (Exception ex)
        {
            // nothing
        }

        Map<String, Serializable> tmpSettings = new HashMap<String, Serializable>();

        if (settings != null)
        {
            tmpSettings.putAll(settings);
        }

        tmpSettings.put(SessionParameter.CONNECT_TIMEOUT, "20000");
        tmpSettings.put(SessionParameter.READ_TIMEOUT, "300000");
        tmpSettings.put(AlfrescoSession.HTTP_CHUNK_TRANSFERT, "true");
        tmpSettings.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, OdsAuthProviderImpl.class.getCanonicalName());
        tmpSettings.put(AlfrescoSession.ONPREMISE_SERVICES_CLASSNAME, OdsServiceRegistry.class.getCanonicalName());
        tmpSettings.put(SessionParameter.OBJECT_FACTORY_CLASS, OdsObjectFactoryImpl.class.getCanonicalName());

        super.initSettings(url, username, password, tmpSettings);
    }

    @Override
    protected Map<String, String> retrieveSessionParameters()
    {
        Map<String, String> res = super.retrieveSessionParameters();
        res.put(SessionParameter.OBJECT_FACTORY_CLASS, OdsObjectFactoryImpl.class.getCanonicalName());
        return res;
    }

    public OdsRepositorySession getCurrent()
    {
        return current;
    }

    public void setCurrent(OdsRepositorySession current)
    {
        this.current = current;
    }

    public OdsRepositorySession getParent()
    {
        return parent != null ? parent.get() : null;
    }

    public LinkCapablilty getLinkCapablilty()
    {
        OdsRepositorySession ses = getParent();

        if (ses != null)
        {
            return ses.getLinkCapablilty();
        }

        if (lcap != null && lcap != LinkCapablilty.UNKNOWN)
        {
            return lcap;
        }

        try
        {
            ObjectType type = getCmisSession().getTypeDefinition(OdsTypeDefinition.LINK_TYPE_ID);

            if (type != null)
            {
                lcap = LinkCapablilty.COMBINED;
                return lcap;
            }
        }
        catch (Exception ex)
        {
            OdsLog.ex("getLinkCapablilty", ex);
        }

        lcap = LinkCapablilty.ITEM;
        return lcap;
    }

    public OdsRepoType getRepoType()
    {
        return repoType;
    }

    public OdsRepositorySession getByType(OdsRepoType type)
    {
        OdsRepositorySession root = parent != null ? parent.get() : null;

        if (root == null)
        {
            root = this;
        }

        switch (type)
        {
        case DEFAULT:
            return root;

        case SHARED:
            return root.shared;

        case GLOBAL:
            return root.global;

        case CONFIG:
            return root.config;

        case PROJECTS:
            return root.projects;

        default:
            return null;
        }
    }

    public static final Parcelable.Creator<OdsRepositorySession> CREATOR =
            new Parcelable.Creator<OdsRepositorySession>()
            {
                public OdsRepositorySession createFromParcel(Parcel in)
                {
                    return new OdsRepositorySession(in);
                }

                public OdsRepositorySession[] newArray(int size)
                {
                    return new OdsRepositorySession[size];
                }
            };
}
