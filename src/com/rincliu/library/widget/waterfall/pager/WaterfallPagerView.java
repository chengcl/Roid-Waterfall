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
package com.rincliu.library.widget.waterfall.pager;

import com.rincliu.library.widget.waterfall.base.WaterfallItemHandler;
import com.rincliu.library.widget.waterfall.base.WaterfallView;

import android.content.Context;
import android.util.AttributeSet;

public class WaterfallPagerView extends WaterfallView{
	/**
	 * 
	 * @param context
	 */
	public WaterfallPagerView(Context context) {
		super(context);
	}
	
	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public WaterfallPagerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private WaterfallPagerAdapter waterfallPagerAdapter;
	
	/**
	 * 
	 * @param waterfallPagerAdapter
	 */
	public void setWaterfallPagerAdapter(WaterfallPagerAdapter waterfallPagerAdapter){
		this.waterfallPagerAdapter=waterfallPagerAdapter;
		waterfallPagerAdapter.setWaterfallView(this);
	}
	
	/**
	 * 
	 * @return
	 */
	public WaterfallPagerAdapter getWaterfallAdapter(){
		return waterfallPagerAdapter;
	}
	
	@Override
	public void startLoadMore(){
		if(waterfallPagerAdapter.getDataSet().size()>0){
			super.setMode(Mode.MANUAL_REFRESH_ONLY);
		}
		super.setRefreshing(true);
	}
	
	/**
	 * 
	 */
	public void load(){
		if(this.waterfallPagerAdapter==null){
			throw new IllegalStateException("The WaterfallPagerView has no WaterfallPagerAdapter.");
		}
		this.waterfallPagerAdapter.load();
	}
	
	WaterfallItemHandler getWaterfallItemHandler(){
		return super.waterfallItemHandler;
	}
}