package de.mathrandom.zw.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@EActivity(R.layout.main_activity)
public class MainActivity extends Activity implements OnFinishedUploading {

	public static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int SELECT_PICTURE = 2;
	private String[] mNavigationTitles;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	@Bean
	NetworkManager networkManager;
	WebView webView;

	@AfterViews
	public void setWebView() {
		mNavigationTitles = getResources().getStringArray(R.array.nav_drawer_items);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
														R.layout.drawer_list_item, mNavigationTitles));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		webView = (WebView) findViewById(R.id.webView);
		webView.setWebViewClient(new MyBrowser());
		webView.getSettings().setLoadsImagesAutomatically(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("http://ec2-54-76-99-61.eu-west-1.compute.amazonaws.com/zettelwirtschaft/index.html");
	}

	@Override
	@UiThread
	public void onFinishedUploading() {
		Toast.makeText(this, "Bild hochgeladen", Toast.LENGTH_LONG).show();
	}

	private class MyBrowser extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	public void onClickCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = new File(android.os.Environment
								  .getExternalStorageDirectory(), "temp.jpg");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	public void onClickOnGallery() {
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
				networkManager.uploadImage(bitmap, this);
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
				networkManager.uploadImage(bm, this);
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

	private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
			selectItem(i);
		}
	}

	private void selectItem(int i) {
		if ( i == 0) {
			onClickCamera();
		} else if ( i == 1) {
			onClickOnGallery();
		} else if ( i == 2) {
			webView.reload();
		}
		mDrawerList.setItemChecked(i, false);
		mDrawerLayout.closeDrawer(mDrawerList);
	}
}
