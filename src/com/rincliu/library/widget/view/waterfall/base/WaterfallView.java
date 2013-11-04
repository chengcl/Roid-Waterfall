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
package com.rincliu.library.widget.view.waterfall.base;

import java.util.ArrayList;

import com.rincliu.library.R;
import com.rincliu.library.widget.view.pulltorefresh.PullToRefreshBase;
import com.rincliu.library.widget.view.pulltorefresh.PullToRefreshScrollView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class WaterfallView extends PullToRefreshScrollView{
	private Context context;
	
	private static final long DELAY=300;
	private boolean enableScrollBar=false;
	private int columnCount=3;
//	private int prevItemCount=10;
//	private int nextItemCount=10;
	private int itemCount=0;
	private int currentScroll=0;
	
	private ArrayList<LinearLayout> columnList=new ArrayList<LinearLayout>();
	private Runnable scrollerTask;
	private ScrollView sv;
	
	/**
	 * 
	 * @param context
	 */
	public WaterfallView(Context context) {
		super(context);
		this.context=context;
		init();
	}
	
	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public WaterfallView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Waterfall);
		if(typedArray.hasValue(R.styleable.Waterfall_enableScrollBar)) {
			this.enableScrollBar=typedArray.getBoolean(R.styleable.Waterfall_enableScrollBar, false);
		}
		if(typedArray.hasValue(R.styleable.Waterfall_columnCount)) {
			this.columnCount=typedArray.getInteger(R.styleable.Waterfall_columnCount, 3);
		}
//		if(typedArray.hasValue(R.styleable.Waterfall_prevItemCount)) {
//			this.prevItemCount=typedArray.getInteger(R.styleable.Waterfall_prevItemCount, 10);
//		}
//		if(typedArray.hasValue(R.styleable.Waterfall_nextItemCount)) {
//			this.nextItemCount=typedArray.getInteger(R.styleable.Waterfall_nextItemCount, 10);
//		}
		typedArray.recycle();
		init();
	}
	
	private void init(){
		super.setMode(Mode.PULL_FROM_START);
		super.setShowViewWhileRefreshing(true);
		super.setOnRefreshListener(new OnRefreshListener<ScrollView>(){
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
				if(onWaterfallRefreshListener!=null){
					setRefreshing(true);
					onWaterfallRefreshListener.onRefresh();
				}
			}
			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
			}
		});
		sv=super.getRefreshableView();
		sv.setVerticalScrollBarEnabled(enableScrollBar);
		scrollerTask = new Runnable() {
			@Override
	        public void run() {
	            int newScroll = sv.getScrollY();
	            if(currentScroll==newScroll){
	                if(onWaterfallScrollListener!=null){
	                    onWaterfallScrollListener.onScrollStop();
	                    if(isScrollViewTop(sv)){
	                    	onWaterfallScrollListener.onScrollToTop();
	            		}
	                    if(isScrollViewBottom(sv)){
	            	    	onWaterfallScrollListener.onScrollToBottom();
	            	    }
	                }
	                updateState();
	            }else{
	                currentScroll=sv.getScrollY();
	                postDelayed(scrollerTask, DELAY);
	            }
	        }
	    };
	    sv.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
	            if (event.getAction() == MotionEvent.ACTION_UP) {
	            	currentScroll = sv.getScrollY();
	                postDelayed(scrollerTask, DELAY);
	            }
	            return false;
	        }
	    });
	}
	
	private boolean hasCreated=false;
	
	/**
	 * 
	 * @param header
	 */
	public void createView(View header){
		LinearLayout.LayoutParams lp0=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout root=new LinearLayout(context);
		root.setOrientation(LinearLayout.VERTICAL);
		if(header!=null){
			root.addView(header);
		}
		root.setLayoutParams(lp0);
		LinearLayout container=new LinearLayout(context);
		container.setOrientation(LinearLayout.HORIZONTAL);
		container.setLayoutParams(lp0);
		for(int i=0;i<columnCount;i++){
			LinearLayout column=new LinearLayout(context);
			column.setOrientation(LinearLayout.VERTICAL);
			LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
			lp.weight=1;
			column.setLayoutParams(lp);
			columnList.add(column);
			container.addView(column);
		}
		root.addView(container);
		sv.addView(root);
		hasCreated=true;
	}
	
	/**
	 * 
	 */
	public void startLoadMore(){
		super.setMode(Mode.MANUAL_REFRESH_ONLY);
		super.setRefreshing(true);
	}
	
	/**
	 * 
	 */
	public void stopLoadMore(){
		super.setRefreshing(false);
		super.onRefreshComplete();
		super.setMode(Mode.PULL_FROM_START);
		updateState();
	}
	
	/**
	 * 
	 */
	public void stopRefresh(){
		super.setRefreshing(false);
		super.onRefreshComplete();
		super.setMode(Mode.PULL_FROM_START);
		updateState();
	}
	
	/**
	 * 
	 * @param view
	 * @param isAppend
	 */
	public void addItem(View view){
		if(!hasCreated){
			throw new IllegalStateException("The method createView() should be called first");
		}
		view.setTag(itemCount);
		view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if(waterfallItemHandler!=null){
					waterfallItemHandler.onItemClick(view, (Integer)view.getTag());
				}
			}
		});
		int x=itemCount%columnCount;
		columnList.get(x).addView(view);
		itemCount++;
	}
	
	/**
	 * 
	 */
	public void removeAllItems(){
		if(!hasCreated){
			throw new IllegalStateException("The method createView() should be called first");
		}
		for(int i=0;i<itemCount;i++){
			View item=findViewWithTag(i);
			if(waterfallItemHandler!=null){
				waterfallItemHandler.onItemInvisible(item, i);
			}
		}
		for(int i=0;i<columnList.size();i++){
			LinearLayout column=columnList.get(i);
			column.removeAllViewsInLayout();
		}
		itemCount=0;
	}
	
	/**
	 * 
	 */
	public void scrollToTop(){
		sv.fullScroll(View.FOCUS_UP);
	}
	
	/**
	 * 
	 */
	public void scrollToBottom(){
		sv.fullScroll(View.FOCUS_DOWN);
	}
	
	private OnWaterfallRefreshListener onWaterfallRefreshListener;
	
	public interface OnWaterfallRefreshListener{
		public void onRefresh();
	}
	
	/**
	 * 
	 * @param onWaterfallRefreshListener
	 */
	public void setOnWaterfallRefreshListener(OnWaterfallRefreshListener onWaterfallRefreshListener){
		this.onWaterfallRefreshListener=onWaterfallRefreshListener;
	}
	
	private OnWaterfallScrollListener onWaterfallScrollListener;
	
	public interface OnWaterfallScrollListener {
		public void onScrollToTop();
		public void onScrollToBottom();
		public void onScrollStop();
	}
	
	/**
	 * 
	 * @param onWaterfallScrollListener
	 */
	public void setOnWaterfallScrollListener(OnWaterfallScrollListener onWaterfallScrollListener){
		this.onWaterfallScrollListener=onWaterfallScrollListener;
	}
	
	private WaterfallItemHandler waterfallItemHandler; 
	
	public interface WaterfallItemHandler {
		public void onItemClick(View view, int position);
		public void onItemVisible(View view, int position);
		public void onItemInvisible(View view, int position);
	}
	
	/**
	 * 
	 * @param waterfallItemHandler
	 */
	public void setWaterfallItemHandler(WaterfallItemHandler waterfallItemHandler){
		this.waterfallItemHandler=waterfallItemHandler;
	}
	
	private void updateState(){
		if(waterfallItemHandler!=null){
			post(new Runnable(){
            	@Override
            	public void run(){
            		for(int i=0;i<itemCount;i++){
            			View item=findViewWithTag(i);
        				if(isScrollViewItemVisible(sv, i)){
        					waterfallItemHandler.onItemVisible(item, i);
        				}else{
        					waterfallItemHandler.onItemInvisible(item, i);
        				}
        			}
            	}
            });
		}
	}
	
	
	
	private boolean isScrollViewItemVisible(ScrollView sv, int position){
		View item=findViewWithTag(position);
		Rect scrollBounds = new Rect();
		sv.getHitRect(scrollBounds);
		return item.getLocalVisibleRect(scrollBounds);
	}
	
	private boolean isScrollViewTop(ScrollView sv){
		return sv.getScrollY()<=0;
	}
	
	private boolean isScrollViewBottom(ScrollView sv){
		return sv.getChildAt(sv.getChildCount()-1).getBottom()+sv.getPaddingBottom()
				==sv.getHeight()+sv.getScrollY();
	}
}