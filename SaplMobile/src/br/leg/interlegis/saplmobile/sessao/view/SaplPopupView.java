package br.leg.interlegis.saplmobile.sessao.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet; 
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import br.leg.interlegis.saplmobile.R;
import br.leg.interlegis.saplmobile.SaplActivity;
import br.leg.interlegis.saplmobile.suport.Log;

public class SaplPopupView extends LinearLayout {

	PopupWindow popupWindow = null;

	public SaplPopupView(Context context, AttributeSet attrs) {

		super(context, attrs); 

	} 


	public void show(View layout) {  
		popupWindow = new PopupWindow(
				this, 
				LayoutParams.MATCH_PARENT,  
				LayoutParams.MATCH_PARENT);

		popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);   
		popupWindow.showAtLocation(SaplActivity.saplActivity.findViewById(R.id.lm),Gravity.CENTER, 0, 0); 
		
		final LinearLayout popup_container = (LinearLayout)this.findViewById(R.id.popup_container);

		popup_container.addView(layout); 
		
		final View llD = (View)this.findViewById(R.id.popupDismiss);
		final View d1 = (View)this.findViewById(R.id.dismiss1);
		final View d2 = (View)this.findViewById(R.id.dismiss2);
		final View d3 = (View)this.findViewById(R.id.dismiss3);

		View.OnClickListener vOnClick = new View.OnClickListener(){
			@Override
			public void onClick(final View v) {

				if (v == d1 || v == d2 || v == llD)
					llD.setBackgroundColor(Color.LTGRAY);
				else 
					d3.setBackgroundColor(Color.LTGRAY);

				new Thread(new Runnable() {

					@Override
					public void run() {


						SaplActivity.saplActivity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub

								try {
									Thread.sleep(100);

									if (v == d1 || v == d2 || v == llD)
										llD.setBackgroundColor(Color.TRANSPARENT);
									else 
										d3.setBackgroundColor(Color.TRANSPARENT);



									new Thread(new Runnable() {

										@Override
										public void run() {
											SaplActivity.saplActivity.runOnUiThread(new Runnable() {

												@Override
												public void run() {
													// TODO Auto-generated method stub
													try {
														Thread.sleep(300);

													} catch (InterruptedException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}

													popupWindow.dismiss();
												}
											});
										}
									}).start();

								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
					}
				}).start();
			}
		};
		d1.setOnClickListener(vOnClick);
		d2.setOnClickListener(vOnClick);
		d3.setOnClickListener(vOnClick); 

		this.setOnTouchListener(new View.OnTouchListener() {


			private float x0 = 0;
			private float y0 = 0;
			private long time = 0; 

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				//printSamples(event);

				if (x0 == 0 || y0 == 0) {
					x0 = event.getX();
					y0 = event.getY();
					time = event.getEventTime();
				} 

				float x = 0;
				float y = 0;
				x = event.getX();
				y = event.getY();

				Log.v("touch", "x0:"+x0+"..."+"y0:"+y0+"....x:"+x+"..."+"y:"+y+"..d:"+Math.abs(y0 - event.getY())+"..v.h: "+v.getHeight()+"...evt:"+(Math.abs(y0 - event.getY())/(event.getEventTime()-time)) );


				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (Math.abs(y0 - event.getY()) > v.getHeight()*0.5) {

						if (Math.abs(y0 - event.getY())/(event.getEventTime()-time) > 1.5)

							popupWindow.dismiss();
					}
					x0 = 0;
					y0 = 0;
					time = 0;
				}

				return false;
			}
/*
			private void printSamples(MotionEvent ev) {
				final int historySize = ev.getHistorySize();
				final int pointerCount = ev.getPointerCount();
				for (int h = 0; h < historySize; h++) {
					Log.v("touch", "At time1 :"+ev.getHistoricalEventTime(h));
					for (int p = 0; p < pointerCount; p++) {
						Log.v("touch", "  pointer "+ev.getPointerId(p)+": ("+ev.getHistoricalX(p, h)+","+ev.getHistoricalY(p, h)+")");
					}
				}
				Log.v("touch", "At time2 :"+ev.getEventTime());
				for (int p = 0; p < pointerCount; p++) {
					Log.v("touch", "  pointer "+ev.getPointerId(p)+": ("+ev.getX(p)+","+ev.getY(p)+")"); 
				}
			}*/
		});



	} 



}
