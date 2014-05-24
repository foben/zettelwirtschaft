package de.mathrandom.zw.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by benedict on 24.05.14.
 */
@EBean
public class NetworkManager {

	Context context;

	public NetworkManager(Context context) {
		this.context = context;
	}

	@Background
	public void uploadImage(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
		byte[] byte_arr = stream.toByteArray();
		String image_str = Base64.encodeToString(byte_arr, Base64.DEFAULT);
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		nameValuePairs.add(new BasicNameValuePair("image", image_str));
		System.out.println("Gogo");
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://ec2-54-76-99-61.eu-west-1.compute.amazonaws" +
													 ".com:5000/shoppinglist");
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println("success----------------------------");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
