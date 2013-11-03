package com.rincliu.library.widget.view.waterfall.activity;

import com.rincliu.library.R;
import com.rincliu.library.widget.view.waterfall.WaterfallView;
import com.rincliu.library.widget.view.waterfall.WaterfallView.OnWaterfallRefreshListener;
import com.rincliu.library.widget.view.waterfall.WaterfallView.OnWaterfallScrollListener;
import com.rincliu.library.widget.view.waterfall.WaterfallView.WaterfallItemHandler;

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

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		final WaterfallView wfv=(WaterfallView)findViewById(R.id.wfv);
		setContentView(wfv);
		wfv.createView(getView(200, true));
		wfv.setOnWaterfallScrollListener(new OnWaterfallScrollListener(){
			@Override
			public void onScrollToTop() {
			}
			@Override
			public void onScrollToBottom() {
				wfv.startLoadMore();
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
			public void onItemClick(View view, int position) {
				Toast.makeText(MainActivity.this, ""+position, Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onItemVisible(View view, int position) {
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
				ImageView iv=(ImageView)view;
				iv.setImageDrawable(new ColorDrawable(Color.LTGRAY));
			}
		});
		wfv.setOnWaterfallRefreshListener(new OnWaterfallRefreshListener(){
			@Override
			public void onRefresh() {
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
								wfv.stopRefresh();
							}
						});
					}
				}.start();
			}
		});
		for(int i=200;i<400;i+=5){
			wfv.addItem(getView(i, false));
		}
		wfv.stopRefresh();
	}
	
	private static Handler handler=new Handler();
	
	private View getView(int height, boolean isHeader){
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
}