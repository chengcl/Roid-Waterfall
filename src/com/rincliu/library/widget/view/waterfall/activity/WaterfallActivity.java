/**
 * Copyright (c) 2013-2014, Rinc Liu (http://rincliu.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rincliu.library.widget.view.waterfall.activity;

import com.rincliu.library.R;
import com.rincliu.library.widget.view.waterfall.base.OnWaterfallScrollListener;
import com.rincliu.library.widget.view.waterfall.base.WaterfallItemHandler;
import com.rincliu.library.widget.view.waterfall.base.WaterfallView;
import com.rincliu.library.widget.view.waterfall.base.WaterfallView.ItemOrder;
import com.rincliu.library.widget.view.waterfall.base.WaterfallView.OnWaterfallItemClickListener;
import com.rincliu.library.widget.view.waterfall.base.WaterfallView.OnWaterfallRefreshListener;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class WaterfallActivity extends Activity {
	private WaterfallView wfv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_waterfall);
		wfv=(WaterfallView)findViewById(R.id.wfv);
		setContentView(wfv);
		wfv.createView(getView(200, true));//TODO: Simulating the process of creating header view
		wfv.setItemOrder(ItemOrder.SHORTEST_COLUMN_FIRST);
		wfv.setOnWaterfallScrollListener(new OnWaterfallScrollListener(){
			@Override
			public void onScrollToTop() {
			}
			@Override
			public void onScrollToBottom() {
				wfv.startLoadMore();
				//TODO: Simulating the process of loading more data
				new Thread(){
					public void run(){
						try {
							Thread.sleep(3456);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						handler.post(new Runnable(){
							public void run(){
								for(int i=300;i<500;i+=5){
									wfv.addItem(getView(i, false));
								}
								wfv.stopLoadMore();
							}
						});
					}
				}.start();
			}
			@Override
			public void onScrollStop() {
			}
		});
		wfv.setWaterfallItemHandler(new WaterfallItemHandler(){
			@Override
			public View onCreateItemView(int position) {
				//TODO: Simulating the process of creating item view
				return getView(300+position, false);
			}
			@Override
			public void onItemVisible(View view, int position) {
				//TODO: Simulating the process of image loading
				final ImageView iv=(ImageView)view;
				wfv.postDelayed(new Runnable(){
					@Override
					public void run() {
						iv.setImageDrawable(getWallpaper());
					}
				}, 300);
			}
			@Override
			public void onItemInvisible(View view, int position) {
				//TODO: Simulating the process of image recycle
				ImageView iv=(ImageView)view;
				iv.setImageDrawable(new ColorDrawable(Color.LTGRAY));
			}
		});
		wfv.setOnWaterfallItemClickListener(new OnWaterfallItemClickListener(){
			@Override
			public void onItemClick(View view, int position) {
				Toast.makeText(WaterfallActivity.this, ""+position, Toast.LENGTH_SHORT).show();
			}
		});
		wfv.setOnWaterfallRefreshListener(new OnWaterfallRefreshListener(){
			@Override
			public void onRefresh() {
				//TODO: Simulating the process of data refreshing
				new Thread(){
					public void run(){
						try {
							Thread.sleep(3456);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						handler.post(new Runnable(){
							public void run(){
								wfv.removeAllItems();
								for(int i=200;i<400;i+=5){
									wfv.addItem(getView(i, false));
								}
								wfv.stopRefresh(true);
							}
						});
					}
				}.start();
			}
		});
		//TODO: Simulating the process of first data loading
		for(int i=200;i<400;i+=5){
			wfv.addItem(getView(i, false));
		}
		wfv.stopRefresh(true);
	}
	
	private static Handler handler=new Handler();
	
	private View getView(int height, boolean isHeader){
		//TODO: Simulating the process of creating view
		ImageView iv=new ImageView(this);
		iv.setScaleType(ScaleType.FIT_XY);
		if(isHeader){
			iv.setImageDrawable(getWallpaper());
		}
		LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
		iv.setLayoutParams(lp);
		iv.setPadding(6, 6, 6, 6);
		return iv;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		wfv.onActivityResume();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		wfv.onActivityPause();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		wfv.onActivityDestroy();
	}
}