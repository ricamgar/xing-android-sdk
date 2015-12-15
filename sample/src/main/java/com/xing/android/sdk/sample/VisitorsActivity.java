/*
 * Copyright (c) 2015 XING AG (http://xing.com/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.xing.android.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.okhttp.logging.HttpLoggingInterceptor.Level;
import com.xing.android.sdk.Callback;
import com.xing.android.sdk.Response;
import com.xing.android.sdk.XingApi;
import com.xing.android.sdk.model.user.ProfileVisit;
import com.xing.android.sdk.resources.ProfileVisitsResource;
import com.xing.android.sdk.sample.adapters.VisitorsRecyclerAdapter;
import com.xing.android.sdk.sample.prefs.Prefs;
import com.xing.android.sdk.sample.utils.RecyclerItemClickListener;
import com.xing.android.sdk.sample.utils.RecyclerItemClickListener.OnItemClickListener;
import com.xing.android.sdk.task.profile_visits.VisitsTask;

import java.util.List;

public class VisitorsActivity extends BaseActivity implements OnItemClickListener,
      Callback<List<ProfileVisit>, Object> {

    //The amount of contacts that should be loaded at a time
    private static final int VISITS_BATCH_SIZE = 10;
    private static final String ME = "me";
    private VisitorsRecyclerAdapter adapter;
    //Boolean to see if the load more functionality should be triggered
    private boolean shouldLoadMore = true;
    private VisitsTask mVisitsTask;

    private XingApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitors);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        api = new XingApi.Builder()
              .consumerKey(BuildConfig.OAUTH_CONSUMER_KEY)
              .consumerSecret(BuildConfig.OAUTH_CONSUMER_SECRET)
              .accessToken(Prefs.getInstance(this).getOauthToken())
              .accessSecret(Prefs.getInstance(this).getOauthSecret())
              .logLevel(Level.BODY)
              .build();

        ProfileVisitsResource profileVisitsResource = api.resource(ProfileVisitsResource.class);
        profileVisitsResource.getProfileVisits(Prefs.getInstance(this).getUserId()).enqueue(this);

        //Initialize the RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.visitors_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        //Setting the adapter passing the contact list of a user
        // and the default on which we contacts will be rendered
        adapter = new VisitorsRecyclerAdapter(this, R.layout.visitor_view);

        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_visitors, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        String userId = adapter.getItem(position).getUserId();
        if (userId != null && !TextUtils.isEmpty(userId)) {
            showToast("Item clicked with id " + userId);
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_USER_ID, userId);
            startActivity(intent);
        }
    }

    @Override
    public void onResponse(Response<List<ProfileVisit>, Object> response) {
        if (response.body() != null) {
            //After the first contacts are loaded successfully we set them to the adapter
            adapter.addItems(response.body());
            if (response.body().size() < VISITS_BATCH_SIZE) {
                //If the amount of received contacts is smaller than the batch size we know
                //that we've reached the end of the list so the next time load more should not be triggered
                shouldLoadMore = false;
            }
        } else {
            shouldLoadMore = false;
        }
    }

    @Override
    public void onFailure(Throwable t) {
        Log.d("Visitor Error", t.getMessage(), t);
    }
}


