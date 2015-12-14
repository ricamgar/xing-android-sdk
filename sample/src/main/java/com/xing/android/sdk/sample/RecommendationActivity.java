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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.squareup.okhttp.logging.HttpLoggingInterceptor.Level;
import com.xing.android.sdk.Callback;
import com.xing.android.sdk.Response;
import com.xing.android.sdk.XingApi;
import com.xing.android.sdk.model.Recommendation;
import com.xing.android.sdk.resources.RecommendationsResource;
import com.xing.android.sdk.sample.adapters.RecommendationsRecyclerAdapter;
import com.xing.android.sdk.sample.prefs.Prefs;

@SuppressWarnings("ConstantConditions")
public class RecommendationActivity extends AppCompatActivity {
    private XingApi api;
    private RecommendationsRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recommendations_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        //Setting the adapter passing the contact list of a user
        // and the default on which we contacts will be rendered
        adapter = new RecommendationsRecyclerAdapter(this, R.layout.visitor_view);

        recyclerView.setAdapter(adapter);

        api = new XingApi.Builder()
              .apiEndpoint("https://api.xing.com")
              .consumerKey(BuildConfig.OAUTH_CONSUMER_KEY)
              .consumerSecret(BuildConfig.OAUTH_CONSUMER_SECRET)
              .accessToken(Prefs.getInstance(this).getOauthToken())
              .accessSecret(Prefs.getInstance(this).getOauthSecret())
              .logLevel(Level.BODY)
              .build();

        RecommendationsResource recommendationsResource = api.resource(RecommendationsResource.class);
        recommendationsResource.getRecommendations(Prefs.getInstance(this).getUserId())
              .queryParam("user_fields", "id,display_name,photo_urls")
              .enqueue(
                    new Callback<Recommendation, Object>() {
                        @Override
                        public void onResponse(Response<Recommendation, Object> response) {
                            adapter.addItems(response.body());
                            Log.i("RecoResponseID", String.valueOf(response.body().getTotal()));
                            Log.i("RecoResponseID",
                                  String.valueOf(response.body().getRecommendedUsers().get(0).getDisplayName()));
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.e("Reco Error", t.getMessage(), t);
                        }
                    });
    }
}
