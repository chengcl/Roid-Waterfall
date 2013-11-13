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
package com.rincliu.library.widget.waterfall.base;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.rincliu.library.R;
import com.rincliu.library.util.RLSysUtil;
import com.rincliu.library.widget.RLScrollView;
import com.rincliu.library.widget.pulltorefresh.ILoadingLayout;
import com.rincliu.library.widget.pulltorefresh.PullToRefreshBase;
import com.rincliu.library.widget.pulltorefresh.PullToRefreshScrollView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class WaterfallView extends PullToRefreshScrollView{
	private Context context;
	
	private SparseBooleanArray visibleArray=new SparseBooleanArray();
	private SparseIntArray heightArray=new SparseIntArray();
	private boolean enableScrollBar=false;
	private boolean hasCreated=false;
	private boolean isReverse=false;
	
	private static final long DELAY=100;
	
	private int columnCount=3;
	private int offsetPageCount=1, preLoadPageCount=1;
	private int itemCount=0;
	private int currentScroll=0;
	
	private ArrayList<LinearLayout> columnList=new ArrayList<LinearLayout>();
	private RLScrollView sv;
	private LinearLayout footer;
	
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
		if(typedArray.hasValue(R.styleable.Waterfall_offsetPageCount)) {
			this.offsetPageCount=typedArray.getInteger(R.styleable.Waterfall_offsetPageCount, 1);
		}
		if(typedArray.hasValue(R.styleable.Waterfall_preLoadPageCount)) {
			this.preLoadPageCount=typedArray.getInteger(R.styleable.Waterfall_preLoadPageCount, 1);
		}
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
	                updateItemsState(isReverse);
				}else{
					isReverse=currentScroll>sv.getScrollY();
	                currentScroll=sv.getScrollY();
	                postDelayed(scrollCheckTask, DELAY);
	            }
	        }
	    };
	    updateStateTask=new Runnable(){
        	@Override
        	public void run(){
        		if(isReverse){
        			int firstVisible=-1, lastVisible=-1;
        			for(int i=itemCount-1;i>=0;i--){
        				View item=viewArray.get(i);
            			if(item!=null){
            				if(sv.isChildVisible(item)){
            					if(firstVisible==-1){
            						firstVisible=i;
            						lastVisible=i;
            					}else{
            						lastVisible--;
            					}
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
        			if(firstVisible>-1&&lastVisible>-1){
            			int visibleCount=firstVisible-lastVisible+1;
            			for(int i=firstVisible+1;i<=Math.min(firstVisible+offsetPageCount*visibleCount-1, itemCount-1);i++){
            				View item=viewArray.get(i);
                			if(item!=null){
                				if(!visibleArray.get(i)){
            						waterfallItemHandler.onItemVisible(item, i);
            						visibleArray.put(i, true);
            					}
                			}
            			}
            			firstVisible=Math.max(lastVisible-1, 0);
            			lastVisible=Math.max(lastVisible-(offsetPageCount+preLoadPageCount)*visibleCount, 0);
            			for(int i=firstVisible;i>=lastVisible;i--){
            				View item=viewArray.get(i);
                			if(item!=null){
                				if(!visibleArray.get(i)){
            						waterfallItemHandler.onItemVisible(item, i);
            						visibleArray.put(i, true);
            					}
                			}
            			}
            		}
        			isReverse=false;
        		}else{
        			int firstVisible=-1, lastVisible=-1;
            		for(int i=0;i<itemCount;i++){
            			View item=viewArray.get(i);
            			if(item!=null){
            				if(sv.isChildVisible(item)){
            					if(firstVisible==-1){
            						firstVisible=i;
            						lastVisible=i;
            					}else{
            						lastVisible++;
            					}
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
            		if(firstVisible>-1&&lastVisible>-1){
            			int visibleCount=lastVisible-firstVisible+1;
            			for(int i=firstVisible-1;i>Math.max(firstVisible-offsetPageCount*visibleCount+1, 0);i--){
            				View item=viewArray.get(i);
                			if(item!=null){
                				if(!visibleArray.get(i)){
            						waterfallItemHandler.onItemVisible(item, i);
            						visibleArray.put(i, true);
            					}
                			}
            			}
            			firstVisible=Math.min(lastVisible+1, itemCount-1);
            			lastVisible=Math.min(lastVisible+(offsetPageCount+preLoadPageCount)*visibleCount, itemCount-1);
            			for(int i=firstVisible;i<=lastVisible;i++){
            				View item=viewArray.get(i);
                			if(item!=null){
                				if(!visibleArray.get(i)){
            						waterfallItemHandler.onItemVisible(item, i);
            						visibleArray.put(i, true);
            					}
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
		LinearLayout.LayoutParams lp1=new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
		lp1.weight=1;
		container.setLayoutParams(lp1);
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
		footer=new LinearLayout(context);
		footer.setOrientation(LinearLayout.VERTICAL);
		footer.setLayoutParams(lp0);
		ProgressBar pb=new ProgressBar(context);
		int size=RLSysUtil.dip2px(context, 30);
		LinearLayout.LayoutParams lpf=new LinearLayout.LayoutParams(size, size);
		lpf.gravity=Gravity.CENTER;
		pb.setLayoutParams(lpf);
		pb.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progressbar));
		footer.addView(pb);
		footer.setVisibility(View.GONE);
		root.addView(footer);
		sv.addView(root);
		hasCreated=true;
	}
	
	/**
	 * 
	 */
	public void startLoadMore(){
//		super.setMode(Mode.MANUAL_REFRESH_ONLY);
//		super.setRefreshing(true);
		footer.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 
	 */
	public void stopLoadMore(boolean isSuccess){
//		super.setRefreshing(false);
//		super.onRefreshComplete();
//		super.setMode(Mode.PULL_FROM_START);
		footer.setVisibility(View.GONE);
		if(isSuccess){
			updateItemsState(false);
		}
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
			updateItemsState(false);
		}
	}
	
	/**
	 * 
	 */
	public void onActivityResume(){
		if(hasCreated){
			updateItemsState(false);
		}
	}
	
	/**
	 * 
	 */
	public void onActivityPause(){
		if(hasCreated){
			resetItemsState();
		}
	}
	
	/**
	 * 
	 */
	public void onActivityDestroy(){
		if(hasCreated){
			removeAllItems();
			removeAllViews();
		}
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
			if(item instanceof ViewGroup){
				((ViewGroup) item).removeAllViews();
			}
			visibleArray.put(i, false);
		}
		viewArray.clear();
		visibleArray.clear();
		for(int i=0;i<columnList.size();i++){
			LinearLayout column=columnList.get(i);
			column.removeAllViews();
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
	 * @param isReverse
	 */
	public void updateItemsState(boolean isReverse){
		this.isReverse=isReverse;
		if(waterfallItemHandler!=null){
			post(updateStateTask);
		}
	}
	
	/**
	 * 
	 */
	public void resetItemsState(){
		if(waterfallItemHandler!=null){
			post(resetStateTask);
		}
	}
}