package de.mathrandom.zw.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@EActivity(R.layout.main_activity)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity {

	public static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int SELECT_PICTURE = 2;

	@Bean
	NetworkManager networkManager;

	WebView webView;


	@AfterViews
	public void setWebView() {
		//LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//View newTagView = inflater.inflate(R.layout.main_activity, null);
		webView = (WebView) findViewById(R.id.webView);
		//setContentView(webView);
		//webView.setWebViewClient(new WebViewClient() {
		//	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		//	}
		//});
		webView.setWebViewClient(new MyBrowser());
		webView.getSettings().setLoadsImagesAutomatically(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("http://ec2-54-76-99-61.eu-west-1.compute.amazonaws.com/zettelwirtschaft/index.html");


	}

	private class MyBrowser extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	@OptionsItem(R.id.action_camera)
	public void onClickCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = new File(android.os.Environment
								  .getExternalStorageDirectory(), "temp.jpg");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	@OptionsItem(R.id.action_picture_from_gallery)
	public void onClickOnGallery() {
		//TODO get picture from gallery
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent,
													"Select Picture"), SELECT_PICTURE);

	}

	@OnActivityResult(SELECT_PICTURE)
	public void onGalleryResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			InputStream stream = null;
			try {
				stream = getContentResolver().openInputStream(
						data.getData());

				Bitmap bitmap = BitmapFactory.decodeStream(stream);
				stream.close();
				networkManager.uploadImage(bitmap);
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@OnActivityResult(REQUEST_IMAGE_CAPTURE)
	public void onResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			File f = new File(Environment.getExternalStorageDirectory()
										 .toString());
			for (File temp : f.listFiles()) {
				if (temp.getName().equals("temp.jpg")) {
					f = temp;
					break;
				}
			}
			try {
				Bitmap bm;
				BitmapFactory.Options btmapOptions = new BitmapFactory.Options();

				bm = BitmapFactory.decodeFile(f.getAbsolutePath(),
											  btmapOptions);
				networkManager.uploadImage(bm);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

}
