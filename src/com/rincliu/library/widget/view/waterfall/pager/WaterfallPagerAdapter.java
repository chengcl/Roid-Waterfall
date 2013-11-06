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
package com.rincliu.library.widget.view.waterfall.pager;

import java.io.Serializable;
import java.util.ArrayList;

import com.rincliu.library.app.RLAsyncTask;
import com.rincliu.library.app.RLAsyncTask.Status;
import com.rincliu.library.util.RLUiUtil;
import com.rincliu.library.widget.RLScrollView.OnScrollListener;
import com.rincliu.library.widget.view.waterfall.base.WaterfallView.OnWaterfallRefreshListener;

import android.content.Context;

public class WaterfallPagerAdapter{
	private final Object obj=new Object();
	private ArrayList<Serializable> list=new ArrayList<Serializable>();
	private ArrayList<Serializable> tmpList;
	private boolean hasFetchedData=false;
	private boolean isReset=true;
	private boolean hasMore=true;
	private String data=null;
	private int start=0;
	private int tmpStart=0;
	private int currentPage= 0;
	
	private UpdateDataTask task;
	private WaterfallPagerHandler waterfallPagerHandler;
	
	private Context mContext;
	private WaterfallPagerView wfv;
	
	/**
	 * 
	 * @param context
	 * @param waterfallPagerHandler
	 */
	public WaterfallPagerAdapter(Context context, WaterfallPagerHandler waterfallPagerHandler){
		this.mContext=context;
		this.waterfallPagerHandler=waterfallPagerHandler;
	}
	
	void setWaterfallView(WaterfallPagerView waterfall){
		this.wfv=waterfall;
		wfv.setOnWaterfallScrollListener(new OnScrollListener(){
			@Override
			public void onScrollChanged(int x, int y, int oldxX, int oldY) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onScrollStopped() {
				wfv.updateState(false);
			}
			@Override
			public void onScrollStoppedAtTop() {
				// TODO Auto-generated method stub
			}
			@Override
			public void onScrollStoppedAtBottom() {
				if(task.getStatus()==Status.FINISHED){
					if(hasMore){
						isReset=false;
						wfv.startLoadMore();
						executeTask();
					}else{
						waterfallPagerHandler.onAlertNoMore();
						wfv.stopLoadMore();
					}
				}else{
					if(isReset){
						wfv.stopLoadMore();
					}
				}
			}
		});
		wfv.setOnWaterfallRefreshListener(new OnWaterfallRefreshListener(){
			@Override
			public void onRefresh() {
				if(task.getStatus()==Status.FINISHED){
					isReset=true;
					start=0;
					tmpStart=0;
					currentPage=0;
					executeTask();
				}else{
					if(!isReset){
						wfv.stopRefresh(false);
					}
				}
			}
		});
	}
	
	void load(){
		wfv.startLoadMore();
		executeTask();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasMore(){
		return hasMore;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getNextStart(){
		return start;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Serializable> getDataSet(){
		return list;
	}
	
	/**
	 * 
	 * @param list
	 */
	public void setDataSet(ArrayList<Serializable> list){
		this.list=list;
	}
	
	/**
	 * 
	 * @param data
	 */
	public void notifyFetchDataSuccess(String data){
		this.data=data;
		if(data!=null){
			currentPage++;
			tmpList=waterfallPagerHandler.onReadDataSet(data);
		}
		hasFetchedData=true;
		synchronized (obj) {
			obj.notify();
		}
	}
	
	/**
	 * 
	 */
	public void notifyFetchDataFailed(){
		hasFetchedData=true;
		synchronized (obj) {
			obj.notify();
		}
	}
	
	private void executeTask(){
		task=new UpdateDataTask();
		task.executeOnExecutor(RLAsyncTask.DUAL_THREAD_EXECUTOR);
	}
	
	private class UpdateDataTask extends RLAsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(Void... params) {
	   		fetchData();
			return null;
		}
		@Override
		protected void onCancelled(){
			wfv.stopRefresh(false);
			wfv.stopLoadMore();
		}
		@Override
		protected void onCancelled(Void result){
			wfv.stopRefresh(false);
			wfv.stopLoadMore();
		}
		@Override
		protected void onProgressUpdate(Void ...values){
			super.onProgressUpdate(values);
		}
		@Override
		protected void onPostExecute(Void result) {
			dealWithData();
		}
	}
	
	private void fetchData() {
		if(tmpList!=null){
			if(!tmpList.isEmpty()){
				tmpList.clear();
			}
			tmpList=null;
		}
		waterfallPagerHandler.onFetchDataHttp(currentPage,start);
		if(!hasFetchedData){
			synchronized (obj) {
				try {
					obj.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		hasFetchedData=false;
	}
	
	private void dealWithData(){
		if(waterfallPagerHandler.onCheckValid(data)&&tmpList!=null){ 
   			if(isReset){
   				tmpStart=0;
   				if(list!=null){
   					list.clear();
   				}else{
   					list=new ArrayList<Serializable>();
   				}
   				wfv.removeAllItems();
   			}
   			start=waterfallPagerHandler.onReadNextStart(data);
   			if(list==null){
				list=new ArrayList<Serializable>();
			}
			for(int i=0;i<tmpList.size();i++){
				list.add(tmpList.get(i));
				wfv.addItem(wfv.getWaterfallItemHandler().onCreateItemView(list.size()-1));
			}
			hasMore=waterfallPagerHandler.onCheckHasMore(data);
			if(isReset){
				wfv.stopRefresh(true);
			}else{
				wfv.stopLoadMore();
			}
   		}else{
   			if(isReset){
   				start=tmpStart;
				tmpStart=0;
   			}
   			if(waterfallPagerHandler.onCheckValid(data)){
   				String errorStr=waterfallPagerHandler.onReadErrorMessage(data);
   				if(errorStr!=null){
   					RLUiUtil.toast(mContext, errorStr);
   				}
   			}
   			if(isReset){
   				wfv.stopRefresh(false);
   			}else{
   				wfv.stopLoadMore();
   			}
   		}
		isReset=false;
	}
}