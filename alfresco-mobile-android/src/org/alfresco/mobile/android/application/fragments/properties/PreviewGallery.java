package org.alfresco.mobile.android.application.fragments.properties;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.opendataspace.android.app.R;

import java.util.ArrayList;
import java.util.List;

public class PreviewGallery extends BaseFragment implements RefreshFragment
{

    public static final String TAG = PreviewGallery.class.getName();

    public static final String ARGUMENT_NODE = "node";

    private final List<Node> nodes = new ArrayList<Node>();

    private ChildrenBrowserFragment frag;

    private ViewPager viewPager;

    // //////////////////////////////////////////////////////////////////////
    // COSNTRUCTORS
    // //////////////////////////////////////////////////////////////////////
    public PreviewGallery()
    {
        super();
    }

    public static PreviewGallery newInstance(Node node)
    {
        PreviewGallery pg = new PreviewGallery();
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        pg.setArguments(args);
        return pg;
    }

    public static BaseFragment newInstance()
    {
        return new PreviewGallery();
    }

    // //////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // //////////////////////////////////////////////////////////////////////
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        View v = inflater.inflate(R.layout.app_pager, container, false);

        // Retrieve nodes
        frag = (ChildrenBrowserFragment) ((getActivity()).getFragmentManager()
                .findFragmentByTag(ChildrenBrowserFragment.TAG));
        viewPager = (ViewPager) v.findViewById(R.id.view_pager);
        refresh();

        Node node;
        if (getArguments() != null && getArguments().containsKey(ARGUMENT_NODE))
        {
            node = (Node) getArguments().get(ARGUMENT_NODE);
        }
        else
        {
            node = frag.getSelectedNodes();
        }

        if (node != null)
        {
            viewPager.setCurrentItem(nodes.indexOf(node));
        }
        else if (nodes.size() > 0)
        {
            frag.unselect();
            frag.highLight(nodes.get(0));
        }

        viewPager.setOnPageChangeListener(new OnPageChangeListener()
        {

            @Override
            public void onPageSelected(int location)
            {
                frag.unselect();
                frag.highLight(nodes.get(location));
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2)
            {
                // Nothing special
            }

            @Override
            public void onPageScrollStateChanged(int arg0)
            {
                // Nothing special
            }
        });

        viewPager.setPageTransformer(true, new DepthPageTransformer());

        PagerTabStrip pagerTabStrip = (PagerTabStrip) v.findViewById(R.id.pager_header);
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.blue_light));

        return v;
    }

    @Override
    public void refresh()
    {
        if (frag.getActivity() != null)
        {
            frag.refresh();
        }

        nodes.clear();

        if (frag != null && nodes.isEmpty())
        {
            List<Node> tmpNodes = frag.getNodes();

            if (tmpNodes != null)
            {
                for (Node node : tmpNodes)
                {
                    if (node.isDocument())
                    {
                        nodes.add(node);
                    }
                }
            }
        }

        ScreenSlidePagerAdapter adapter = new ScreenSlidePagerAdapter(getActivity().getFragmentManager(), nodes);
        viewPager.setAdapter(adapter);
    }
}

class DepthPageTransformer implements ViewPager.PageTransformer
{
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position)
    {
        int pageWidth = view.getWidth();

        if (position < -1)
        { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        }
        else if (position <= 0)
        { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        }
        else if (position <= 1)
        { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        }
        else
        { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}

class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
{
    private List<Node> nodes = new ArrayList<Node>();

    public ScreenSlidePagerAdapter(FragmentManager fm, List<Node> nodes)
    {
        super(fm);
        this.nodes = nodes;
    }

    @Override
    public Fragment getItem(int position)
    {
        return PreviewFragment.newInstance(nodes.get(position));
    }

    @Override
    public int getCount()
    {
        return nodes.size();
    }

    public CharSequence getPageTitle(int position)
    {
        return nodes.get(position).getName();
    }
}
