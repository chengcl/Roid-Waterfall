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
import com.rincliu.library.widget.view.waterfall.base.OnWaterfallScrollListener;
import com.rincliu.library.widget.view.waterfall.base.WaterfallView.OnWaterfallRefreshListener;

import android.content.Context;

public abstract class WaterfallPagerAdapter{
	private final Object obj=new Object();
	private ArrayList<Serializable> list=new ArrayList<Serializable>();
	private ArrayList<Serializable> tmpList;
	private boolean hasFetchedData=false;
	private boolean isReset=false;
	private String data=null;
	private int start=0;
	private int tmpStart=0;
	private int currentPage=0;
	private UpdateDataTask task;
	
	boolean hasMore=true;
	
	private Context mContext;
	private WaterfallPagerView wfv;
	
	/**
	 * 
	 * @param context
	 */
	public WaterfallPagerAdapter(Context context){
		this.mContext=context;
	}
	
	void setWaterfallView(WaterfallPagerView waterfall){
		this.wfv=waterfall;
		wfv.setOnWaterfallScrollListener(new OnWaterfallScrollListener(){
			@Override
			public void onScrollToTop() {
			}
			@Override
			public void onScrollToBottom() {
				if(task.getStatus()==Status.FINISHED){
					if(hasMore){
						isReset=false;
						wfv.startLoadMore();
						executeTask();
					}else{
						onAlertNoMore();
						wfv.stopLoadMore();
					}
				}else{
					if(isReset){
						wfv.stopLoadMore();
					}
				}
			}
			@Override
			public void onScrollStop() {
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
						wfv.stopRefresh();
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
			tmpList=onReadDataSet(data);
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
	
	public abstract void onFetchDataHttp(int currentPage,int currentStart);
	
	public abstract boolean onCheckValid(String data);
	
	public abstract String onReadErrorMessage(String data);
	
	public abstract boolean onCheckHasMore(String data);
	
	public abstract int onReadNextStart(String data);
	
	public abstract ArrayList<Serializable> onReadDataSet(String data);
	
	public abstract void onAlertNoMore();
	
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
			wfv.stopRefresh();
			wfv.stopLoadMore();
		}
		@Override
		protected void onCancelled(Void result){
			wfv.stopRefresh();
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
		onFetchDataHttp(currentPage,start);
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
		if(onCheckValid(data)&&tmpList!=null){ 
   			if(isReset){
   				tmpStart=0;
   				if(list!=null){
   					list.clear();
   				}else{
   					list=new ArrayList<Serializable>();
   				}
   				wfv.removeAllItems();
   			}
   			start=onReadNextStart(data);
   			if(list==null){
				list=new ArrayList<Serializable>();
			}
			for(int i=0;i<tmpList.size();i++){
				list.add(tmpList.get(i));
				wfv.addItem(wfv.getWaterfallItemHandler().onCreateItemView(list.size()-1));
			}
			hasMore=onCheckHasMore(data);
   		}else{
   			if(isReset){
   				start=tmpStart;
				tmpStart=0;
   			}
   			if(onCheckValid(data)){
   				String errorStr=onReadErrorMessage(data);
   				if(errorStr!=null){
   					RLUiUtil.toast(mContext, errorStr);
   				}
   			}
   		}
		wfv.stopRefresh();
		wfv.stopLoadMore();
		isReset=false;
	}
}