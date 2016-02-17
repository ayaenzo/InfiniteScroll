/*
 * Copyright (C) 2016 Piotr Wittchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pwittchen.infinitescroll.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener;
import java.util.LinkedList;
import java.util.List;
import pwittchen.github.com.infinitescroll.R;

public class MainActivity extends AppCompatActivity {
  private static final int MAX_ITEMS_PER_REQUEST = 20;
  private static final int NUMBER_OF_ITEMS = 100;
  private static final int SIMULATED_LOADING_TIME_IN_MS = 1500;

  public Toolbar toolbar;
  public RecyclerView recyclerView;
  public ProgressBar progressBar;

  private LinearLayoutManager layoutManager;
  private List<String> items;
  private int page = 0;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.items = createItems();
    initViews();
    initRecyclerView();
    setSupportActionBar(toolbar);
  }

  private List<String> createItems() {
    List<String> items = new LinkedList<>();
    for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
      String prefix = i < 10 ? "0" : "";
      items.add("Item #".concat(prefix).concat(String.valueOf(i)));
    }
    return items;
  }

  private void initViews() {
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    progressBar = (ProgressBar) findViewById(R.id.progress_bar);
  }

  private void initRecyclerView() {
    recyclerView.setHasFixedSize(true);
    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(new MyAdapter(items.subList(page, MAX_ITEMS_PER_REQUEST)));
    recyclerView.addOnScrollListener(createInfiniteScrollListener());
  }

  @NonNull private InfiniteScrollListener createInfiniteScrollListener() {
    return new InfiniteScrollListener(MAX_ITEMS_PER_REQUEST, layoutManager) {
      @Override public void onScrolledToEnd(final int firstVisibleItemPosition) {
        simulateLoading();
        int start = ++page * MAX_ITEMS_PER_REQUEST;
        final boolean allItemsLoaded = start >= items.size();
        if (allItemsLoaded) {
          progressBar.setVisibility(View.GONE);
        } else {
          int end = start + MAX_ITEMS_PER_REQUEST;
          final List<String> items = getItemsToBeLoaded(start, end);
          refreshView(recyclerView, new MyAdapter(items), firstVisibleItemPosition);
        }
      }
    };
  }

  @NonNull private List<String> getItemsToBeLoaded(int start, int end) {
    List<String> newItems = items.subList(start, end);
    final List<String> oldItems = ((MyAdapter) recyclerView.getAdapter()).getItems();
    final List<String> items = new LinkedList<>();
    items.addAll(oldItems);
    items.addAll(newItems);
    return items;
  }

  /**
   * WARNING! This method is only for demo purposes!
   * Don't do anything like that in your regular project!
   */
  private void simulateLoading() {
    new AsyncTask<Void, Void, Void>() {
      @Override protected void onPreExecute() {
        progressBar.setVisibility(View.VISIBLE);
      }

      @Override protected Void doInBackground(Void... params) {
        try {
          Thread.sleep(SIMULATED_LOADING_TIME_IN_MS);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return null;
      }

      @Override protected void onPostExecute(Void param) {
        progressBar.setVisibility(View.GONE);
      }
    }.execute();
  }
}
