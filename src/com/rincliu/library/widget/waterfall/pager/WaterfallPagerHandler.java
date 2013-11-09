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

import java.io.Serializable;
import java.util.ArrayList;

public interface WaterfallPagerHandler {
	/**
	 * 
	 * @param currentPage
	 * @param currentStart
	 */
	public abstract void onFetchDataHttp(int currentPage,int currentStart);
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	public abstract boolean onCheckValid(String data);
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	public abstract String onReadErrorMessage(String data);
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	public abstract boolean onCheckHasMore(String data);
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	public abstract int onReadNextStart(String data);
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	public abstract ArrayList<Serializable> onReadDataSet(String data);
	
	/**
	 * 
	 */
	public abstract void onAlertNoMore();
}