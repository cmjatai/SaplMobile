package br.leg.interlegis.saplmobile.suport;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask; 
import android.widget.ImageView;


public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	ImageView bmImage;

	public DownloadImageTask(ImageView bmImage) {
		this.bmImage = bmImage;
	}


	@Override
	protected Bitmap doInBackground(String... urls) {
		return download_Image(urls[0]);
	}

	@Override
	protected void onPostExecute(Bitmap result) {

		bmImage.setImageBitmap(result);              // how do I pass a reference to mChart here ?
		
	}


	private Bitmap download_Image(String url) {
		//---------------------------------------------------
		Bitmap bm = null;
		try {
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
		} catch (IOException e) {
			Log.e("Hub","Error getting the image from server : " + e.getMessage().toString());
		} 
		return bm;
		//---------------------------------------------------
	}



}


