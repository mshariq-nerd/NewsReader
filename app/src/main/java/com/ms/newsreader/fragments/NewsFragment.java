package com.ms.newsreader.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ms.newsreader.R;
import com.ms.newsreader.adapter.DividerItemDecoration;
import com.ms.newsreader.adapter.GoogleFeed;
import com.ms.newsreader.adapter.NewsFeedAdapter;
import com.ms.newsreader.util.Constant;
import com.ms.newsreader.util.GoogleNewsXmlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Mohd. Shariq on 22/03/17.
 */

public class NewsFragment extends Fragment {
    ProgressBar loading;
    WebView errorMsgWebView;
    private List<GoogleFeed> newsFeedList = new ArrayList<>();
    private NewsFeedAdapter adapter = null;
    RecyclerView recyclerView;

    private AdView mAdView;

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;

    public static NewsFragment newInstance(String language) {
        NewsFragment newsFragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString("language", language);
        newsFragment.setArguments(args);
        return newsFragment;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        if (rootView != null) {
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            // 2. set layoutManger
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            loading = (ProgressBar) rootView.findViewById(R.id.loading);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new DividerItemDecoration(rootView.getContext(), LinearLayoutManager.VERTICAL));
            recyclerView.setVisibility(View.GONE);
            // The specified network connection is not available. Displays error message in webview.
            errorMsgWebView = (WebView) rootView.findViewById(R.id.webview);
            errorMsgWebView.setVisibility(View.GONE);
            mAdView = (AdView) rootView.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            String strtext = getArguments().getString("language", "");
            fetchGoogleNewsFeed(strtext);
        }
        return rootView;
    }

    private void fetchGoogleNewsFeed(String newsType) {
        final GoogleNewsXmlParser googleNewsXmlParser = new GoogleNewsXmlParser();
        AndroidNetworking.get(String.format("%s%s", Constant.URL, newsType))
                .setPriority(Priority.LOW)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            InputStream stream = new ByteArrayInputStream(response.getBytes("UTF-8"));

                            // get list of news feeds
                            newsFeedList = googleNewsXmlParser.parse(stream);

                            adapter = new NewsFeedAdapter(getActivity(), newsFeedList);
                            // News Feed Header
                            if (newsFeedList.size() != 0)
                                //Apply adapter to the RecyclerView
                                recyclerView.setAdapter(adapter);

                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                                recyclerView.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.GONE);
                            }
                        } catch (IOException | XmlPullParserException | ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Timber.d(String.format("%s %s", "Error: ", anError.toString()));
                    }
                });
    }


//    private void loadPage(String newsType) {
//        if (((networkPreference.equals(ANY)) && (wifiConnected || mobileConnected))
//                || ((networkPreference.equals(WIFI)) && (wifiConnected))) {
//            // call to load google news feed
//            fetchGoogleNewsFeed(newsType);
//
//            // close navigation drawer
//            loading.setVisibility(View.VISIBLE);
//        } else {
//            showErrorPage();
//        }
//    }

    // Displays an error if the app is unable to load content.
    private void showErrorPage() {
        errorMsgWebView.setVisibility(View.VISIBLE);
        errorMsgWebView.loadData(getResources().getString(R.string.connection_error),
                "text/html", null);
        loading.setVisibility(View.GONE);
    }

}