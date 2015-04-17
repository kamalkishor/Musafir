package com.example.musafir;

import org.json.JSONArray;
import org.json.JSONObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;


public class MainActivity extends ActionBarActivity implements android.widget.AdapterView.OnItemSelectedListener, OnClickListener {

	private Spinner spinner;
	String distance_string=null;
	int radius=0;
	View mapViewer, search;
	LatLng latlng, new_latlng, current_lat, current_lng;
	public static GoogleMap googleMap;
	private String mFilterType="0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		mapViewer = (FrameLayout)findViewById(R.id.map_frame_layout);
		spinner = (Spinner) findViewById(R.id.spinner);


		search=(View)findViewById(R.id.search);

		spinner.setOnItemSelectedListener(MainActivity.this);
		search.setOnClickListener( this);


		// Getting LocationManager object from System Service LOCATION_SERVICE
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


		// Creating a criteria object to retrieve provider
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// Getting the name of the best provider
		String provider = locationManager.getBestProvider(criteria, true);

		// Getting Current Location
		Location location = null;
		try{
			location = locationManager.getLastKnownLocation(provider);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		if(location!=null)
		{

			latlng = new LatLng(location.getLatitude(),location.getLongitude());;
		}
		mapSetter(true,latlng);
	}


	public void mapSetter(boolean flag, LatLng latlong){
		try{
			android.support.v4.app.FragmentManager fmanager = getSupportFragmentManager();
			android.support.v4.app.Fragment fragment = fmanager.findFragmentById(R.id.dialog_map);

			SupportMapFragment supportmapfragment = (SupportMapFragment)fragment;
			googleMap = supportmapfragment.getMap();
			googleMap.getUiSettings().setMyLocationButtonEnabled(true);
			//	googleMap.getUiSettings().setScrollGesturesEnabled(false);
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 16)); 

			googleMap.addMarker(new MarkerOptions()
			.title("Current Location")
			.snippet("Explore New Places.")
			.position(latlong));

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}



	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

		switch (position) {
		case 0:
			mFilterType = "0";
			break;
		case 1:
			mFilterType = "1";
			break;
		case 2:
			mFilterType = "2";
			break;
		case 3:
			mFilterType = "3";
			break;
		case 4:
			mFilterType = "4";
			break;
		case 5:
			mFilterType = "5";
			break;
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if(id == search.getId())
		{
			SearchAsyncTask Task = new SearchAsyncTask(latlng,this);
			Task.execute();
		}


	}

	public class SearchAsyncTask extends AsyncTask<Void,String, String> {

		private Activity appContext = null;
		private LatLng LatLng;
		private static final String TAG_LAT = "lat";
		private static final String TAG_TYPE = "type";
		private static final String TAG_RATING = "rating";
		private static final String TAG_LANG = "lang";
		private static final String TAG_NAME = "name";

		boolean success=false;
		private ProgressDialog pd;
		private double mLng;
		private double mLat;

		public SearchAsyncTask(LatLng latlong, Activity context) {
			// TODO Auto-generated constructor stub
			this.appContext=context;
			this.LatLng = latlong;

			mLat = LatLng.latitude;
			mLng = LatLng.longitude;

		}

		@Override
		protected  String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			//String response ="";
			String serverResponse = "";
			// Creating service handler class instance
			
			ServiceHandler sh = new ServiceHandler();
			String url= "https://stayzilla.herokuapp.com/?latlong="+mLat+","+mLng+"&filter="+mFilterType+"&full_data=True";
			// Making a request to url and getting response
			serverResponse = sh.makeServiceCall(url, ServiceHandler.GET);
			
			return serverResponse;
		}


		public boolean isNullOrEmpty(String value) {
			if (null == value) {
				return true;
			}

			value = value.trim();

			if ("null".equalsIgnoreCase(value) || value.length() == 0) {
				return true;
			}

			return false;
		}


		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			pd.dismiss();

			try {

				if (!isNullOrEmpty(result)) {
					JSONArray array = new JSONArray(result);
					JSONObject obj;

					int len = array.length();
					if (len != 0) {
						for (int i = 0; i < len-1; i++) {
							try{
								obj = array.getJSONObject(i);
								String Lat = obj.getString(TAG_LAT);
								String type = obj.getString(TAG_TYPE);
								String rating = obj.getString(TAG_RATING);
								String lang = obj.getString(TAG_LANG);
								String name = obj.getString(TAG_NAME);


								double mLat = Double.parseDouble(Lat);
								double mLong = Double.parseDouble(lang);

								new_latlng = new LatLng(mLat,mLong);


								Marker melbourne = googleMap.addMarker(new MarkerOptions()
								.position(new_latlng)
								.title(name)
								.snippet("Type: "+type+" Rating: "+rating)
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.mark_blue)));

							}   catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}}
			}
			catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(appContext, null, "Searching . . .");
			googleMap.clear();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

}
