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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.rincliu.library.R;
import com.rincliu.library.widget.RLScrollView;
import com.rincliu.library.widget.view.pulltorefresh.ILoadingLayout;
import com.rincliu.library.widget.view.pulltorefresh.PullToRefreshBase;
import com.rincliu.library.widget.view.pulltorefresh.PullToRefreshScrollView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class WaterfallView extends PullToRefreshScrollView{
	private Context context;
	
	private SparseBooleanArray visibleArray=new SparseBooleanArray();
	private SparseIntArray heightArray=new SparseIntArray();
	private boolean enableScrollBar=false;
	private boolean hasCreated=false;
	
	private static final long DELAY=100;
	
	private int columnCount=3;
//	private int prevItemCount=10;
//	private int nextItemCount=10;
	private int itemCount=0;
	private int currentScroll=0;
	
	private ArrayList<LinearLayout> columnList=new ArrayList<LinearLayout>();
	private RLScrollView sv;
	
	private OnWaterfallItemClickListener onWaterfallItemClickListener;
	private OnWaterfallRefreshListener onWaterfallRefreshListener;
	private OnWaterfallScrollListener onWaterfallScrollListener;
	protected WaterfallItemHandler waterfallItemHandler;
	
	private ItemOrder itemOrder=ItemOrder.DEFAULT;
	
	private Runnable scrollCheckTask, updateStateTask, resetStateTask;
	
	private SparseArray<View> viewArray=new SparseArray<View>();
	
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
	
	public enum ItemOrder{
		DEFAULT, SHORTEST_COLUMN_FIRST
	}
	
	/**
	 * 
	 * @param itemOrder
	 */
	public void setItemOrder(ItemOrder itemOrder){
		this.itemOrder=itemOrder;
	}
	
	private void init(){
		super.setMode(Mode.PULL_FROM_START);
		super.setShowViewWhileRefreshing(true);
		super.setOnRefreshListener(new OnRefreshListener<RLScrollView>(){
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<RLScrollView> refreshView) {
				if(onWaterfallRefreshListener!=null){
					setRefreshing(true);
					onWaterfallRefreshListener.onRefresh();
				}
			}
			@Override
			public void onPullUpToRefresh(PullToRefreshBase<RLScrollView> refreshView) {
			}
		});
		sv=super.getRefreshableView();
		sv.setVerticalScrollBarEnabled(enableScrollBar);
		scrollCheckTask = new Runnable() {
			@Override
	        public void run() {
	            int newScroll = sv.getScrollY();
	            if(currentScroll==newScroll){
					if(onWaterfallScrollListener!=null){
	                    onWaterfallScrollListener.onScrollStopped();
	                    if(sv.isAtTop()){
	                    	onWaterfallScrollListener.onScrollStoppedAtTop();
	            		}
	                    if(sv.isAtBottom()){
	            	    	onWaterfallScrollListener.onScrollStoppedAtBottom();
	            	    }
	                }
	                updateState(false);
				}else{
	                currentScroll=sv.getScrollY();
	                postDelayed(scrollCheckTask, DELAY);
	            }
	        }
	    };
	    updateStateTask=new Runnable(){
	    	@Override
	    	public void run(){
	    		for(int i=0;i<itemCount;i++){
        			View item=viewArray.get(i);
        			if(item!=null){
        				if(sv.isChildVisible(item)){
        					if(!visibleArray.get(i)){
        						waterfallItemHandler.onItemVisible(item, i);
        						visibleArray.put(i, true);
        					}
        				}else{
        					if(visibleArray.get(i)){
        						waterfallItemHandler.onItemInvisible(item, i);
        						visibleArray.put(i, false);
        					}
        				}
        			}
    			}
	    	}
	    };
	    resetStateTask=new Runnable(){
			@Override
			public void run() {
				for(int i=0;i<itemCount;i++){
        			View item=viewArray.get(i);
        			if(item!=null){
        				if(visibleArray.get(i)){
    						waterfallItemHandler.onItemInvisible(item, i);
    						visibleArray.put(i, false);
    					}
        			}
				}
			}
	    };
	    sv.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
	            if (event.getAction() == MotionEvent.ACTION_UP) {
	            	currentScroll = sv.getScrollY();
	                postDelayed(scrollCheckTask, DELAY);
	            }
	            return false;
	        }
	    });
	}
	
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
			heightArray.put(i, 0);
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
	}
	
	/**
	 * 
	 * @param isSuccess
	 */
	public void stopRefresh(boolean isSuccess){
		super.setRefreshing(false);
		super.onRefreshComplete();
		super.setMode(Mode.PULL_FROM_START);
		if(isSuccess){
			ILoadingLayout layout=getLoadingLayoutProxy(true, false);
			if(layout!=null){
				layout.setLastUpdatedLabel(context.getString(R.string.ptr_updated_at)
						+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA)
						.format(new Date(System.currentTimeMillis())));
			}
			updateState(false);
		}
	}
	
	/**
	 * 
	 */
	public void onActivityResume(){
		updateState(false);
	}
	
	/**
	 * 
	 */
	public void onActivityPause(){
		updateState(true);
	}
	
	/**
	 * 
	 */
	public void onActivityDestroy(){
		removeAllItems();
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
		visibleArray.put(itemCount, false); 
		view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if(onWaterfallItemClickListener!=null){
					onWaterfallItemClickListener.onItemClick(view, (Integer)view.getTag());
				}
			}
		});
		int index=itemCount%columnCount;
		if(itemOrder==ItemOrder.SHORTEST_COLUMN_FIRST){
			index=getShortestColumn();
		}
		LinearLayout column=columnList.get(index);
		column.addView(view);
		viewArray.put(itemCount, view);
		if(itemOrder==ItemOrder.SHORTEST_COLUMN_FIRST){
			if(view.getLayoutParams()==null||view.getLayoutParams().height<=0){
				throw new IllegalStateException(
						"You should set layout params to the item view, especially the height.");
			}
			heightArray.put(index, heightArray.get(index)+view.getLayoutParams().height);
		}
		itemCount++;
	}
	
	private int getShortestColumn(){
		int index=0;
		int minHeight=heightArray.get(0);
		for(int i=1;i<heightArray.size();i++){
			int tmp=heightArray.get(i);
			if(tmp<minHeight){
				minHeight=tmp;
				index=i;
			}
		}
		return index;
	}
	
	/**
	 * 
	 */
	public void removeAllItems(){
		if(!hasCreated){
			throw new IllegalStateException("The method createView() should be called first");
		}
		for(int i=0;i<itemCount;i++){
			View item=viewArray.get(i);
			if(waterfallItemHandler!=null){
				waterfallItemHandler.onItemInvisible(item, i);
			}
			visibleArray.put(i, false);
		}
		viewArray.clear();
		visibleArray.clear();
		for(int i=0;i<columnList.size();i++){
			LinearLayout column=columnList.get(i);
			column.removeAllViewsInLayout();
			heightArray.put(i, 0);
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
	
	public interface OnWaterfallItemClickListener{
		public void onItemClick(View view, int position);
	}
	
	/**
	 * 
	 * @param onWaterfallItemClickListener
	 */
	public void setOnWaterfallItemClickListener(OnWaterfallItemClickListener onWaterfallItemClickListener) {
		this.onWaterfallItemClickListener = onWaterfallItemClickListener;
	}
	
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
	
	/**
	 * 
	 * @param onWaterfallScrollListener
	 */
	public void setOnWaterfallScrollListener(OnWaterfallScrollListener onWaterfallScrollListener){
		this.onWaterfallScrollListener=onWaterfallScrollListener;
	}
	
	/**
	 * 
	 * @param waterfallItemHandler
	 */
	public void setWaterfallItemHandler(WaterfallItemHandler waterfallItemHandler){
		this.waterfallItemHandler=waterfallItemHandler;
	}
	
	/**
	 * 
	 * @param isReset
	 */
	public void updateState(boolean isReset){
		if(waterfallItemHandler!=null){
			post(isReset?resetStateTask:updateStateTask);
		}
	}
}