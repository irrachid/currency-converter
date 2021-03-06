package com.techytec.currencyconverter.data;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public enum Image_BitmapManager {  
	INSTANCE;  

	private final Map<String, SoftReference<Bitmap>> cache;  
	public final ExecutorService pool;  
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());  
	private Bitmap placeholder;  

	Image_BitmapManager() {  
		cache = new HashMap<String, SoftReference<Bitmap>>();  
		pool = Executors.newFixedThreadPool(5);  
	}  

	public void setPlaceholder(Bitmap bmp) {  
		placeholder = bmp;  
	}  

	public Bitmap getBitmapFromCache(String url) {  
		if (cache.containsKey(url)) {  
			return cache.get(url).get();  
		}  

		return null;  
	}  

	public void queueJob(final String url, final ImageView imageView,  
			final int width, final int height) {  
		/* Create handler in UI thread. */  

		final Handler handler = new Handler() {  
			@Override  
			public void handleMessage(Message msg) {  
				String tag = imageViews.get(imageView);  

				if (tag != null && tag.equals(url)) {  
					if (msg.obj != null) {  
						imageView.setImageBitmap((Bitmap) msg.obj);  
					} else {  
						imageView.setImageBitmap(placeholder);  
						Log.d(null, "fail " + url);  
					}  
				}  
			}  
		};  

		pool.submit(new Runnable() {  

			public void run() {  
				final Bitmap bmp = downloadBitmap(url, width, height);  
				Message message = Message.obtain();  
				message.obj = bmp;  
				Log.e(null, "Item downloaded: " + url);  

				handler.sendMessage(message);  
			}  
		});  
	}  

	public void loadBitmap(final String url, final ImageView imageView,  
			final int width, final int height) {  
		imageViews.put(imageView, url);  
		Bitmap bitmap = getBitmapFromCache(url);  

		// check in UI thread, so no concurrency issues  
		if (bitmap != null) {  
			Log.e(null, "Item loaded from cache: " + url);  
			imageView.setImageBitmap(bitmap);  
		} else {  
			imageView.setImageBitmap(placeholder);  
			queueJob(url, imageView, width, height);  
		}  
	}  

	private Bitmap downloadBitmap(String url, int width, int height) {  
		
		try { 
			
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());


			Log.e("Download image", "Download");
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			
//			String strfilename =url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("."));
//			Log.e("strimgurl", strfilename);
//			
//			File mypath=new File(Big_Amritsar_Splash.direct_thumb,strfilename+".jpg");
//			
//						
//			Log.e("load mypath", mypath.toString());
//			FileOutputStream fOut = new FileOutputStream(mypath);
//			BufferedOutputStream bos = new BufferedOutputStream(fOut);
//			ByteArrayOutputStream bas = new ByteArrayOutputStream();
//			bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , bas);
//			byte[] fileBytes = bas.toByteArray();
//			bos.write(fileBytes);
//			bos.flush();
//			bos.close();
			
			//bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);  
			cache.put(url, new SoftReference<Bitmap>(bitmap));  
			return bitmap;  
		} catch (MalformedURLException e) {  
			e.printStackTrace();  
		} catch (IOException e) {  
			e.printStackTrace();  
			Log.e("IOException", e.toString());
		}  

		return null;  
	}  
}  