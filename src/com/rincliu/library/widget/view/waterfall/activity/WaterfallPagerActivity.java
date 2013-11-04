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

import java.io.Serializable;
import java.util.ArrayList;

import com.rincliu.library.R;
import com.rincliu.library.util.RLUiUtil;
import com.rincliu.library.widget.view.waterfall.pager.WaterfallPagerAdapter;
import com.rincliu.library.widget.view.waterfall.pager.WaterfallPagerView;
import com.rincliu.library.widget.view.waterfall.pager.WaterfallPagerView.OnWaterfallItemClickListener;
import com.rincliu.library.widget.view.waterfall.pager.WaterfallPagerView.WaterfallItemHandler;

import android.os.Bundle;
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

public class WaterfallPagerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_waterfall_pager);
		final WaterfallPagerView wfv=(WaterfallPagerView)findViewById(R.id.wfv);
		setContentView(wfv);
		wfv.createView(getView(200), new WaterfallItemHandler(){
			@Override
			public View onCreateItemView(int position) {
				return getView(300+position*3);
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
				Toast.makeText(WaterfallPagerActivity.this, ""+position, Toast.LENGTH_SHORT).show();
			}
		});
		wfv.autoLoad(new WaterfallPagerAdapter<WaterfallBean>(this){
			private int i=0;
			@Override
			public void onFetchDataHttp(int currentPage, int currentStart) {
				//TODO: Simulating the process of HTTP request
				try {
                    Thread.sleep(3456);
				} catch (InterruptedException e) {
                    e.printStackTrace();
				}
				super.notifyFetchDataSuccess("");
				//If the request is failed you should call super.notifyDataFailed();
				i++;
			}
			@Override
			public boolean onCheckValid(String data) {
				//TODO: Simulating the process of checking data
				return true;
			}
			@Override
			public String onReadErrorMessage(String data) {
				//TODO: Simulating the process of parsing error message
				return null;
			}
			@Override
			public boolean onCheckHasMore(String data) {
				//TODO: Simulating the process of pager
				boolean res=i<=3;
				i=res?i:0;
				return res;
			}
			@Override
			public int onReadNextStart(String data) {
				//TODO: Simulating the process of parsing next start
				return 0;
			}
			@Override
			public ArrayList<WaterfallBean> onReadDataSet(String data) {
				//TODO: Simulating the process of data parsing
				ArrayList<WaterfallBean> list=new ArrayList<WaterfallBean>(); 
				for(int i=0;i<30;i++){
					list.add(new WaterfallBean());
				}
				return list;
			}
			@Override
			public void onAlertNoMore() {
				RLUiUtil.toast(WaterfallPagerActivity.this, "没有更多数据");
			}
		});
	}
	
	private View getView(int height){
		//TODO: Simulating the process of creating view
		ImageView iv=new ImageView(this);
		iv.setScaleType(ScaleType.FIT_XY);
		iv.setImageDrawable(getWallpaper());
		LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
		iv.setLayoutParams(lp);
		iv.setPadding(6, 6, 6, 6);
		return iv;
	}
	
	public class WaterfallBean implements Serializable{
		private static final long serialVersionUID = -2016014533623788774L;
		//TODO
	}
}