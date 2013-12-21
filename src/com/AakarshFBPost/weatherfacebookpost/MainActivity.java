package com.AakarshFBPost.weatherfacebookpost;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.*;

import com.facebook.*;
import com.facebook.model.*;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class MainActivity extends Activity {

	EditText loc;
	TextView city;
	TextView region;
	TextView condition;
	TextView temp;
	TextView noWeather;
	RelativeLayout resultLayout;
	RelativeLayout noWeatherLayout;
	TableLayout forecastTab;
	ImageView iv;
	Bitmap image ;
	RadioGroup radioGroup;
	String locType = null;
	
	TextView weatherLink;
	TextView forecastLink;
	JSONObject jsonWeather;
	int postId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		super.onCreate(savedInstanceState);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.AakarshFBPost.weatherfacebookpost", 
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
        } catch (NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

		setContentView(R.layout.activity_main);

		noWeather = (TextView) findViewById(R.id.noweather);
		loc = (EditText) findViewById(R.id.location);
		city = (TextView) findViewById(R.id.city);
		region = (TextView) findViewById(R.id.region);
		condition = (TextView) findViewById(R.id.condition);
		temp = (TextView) findViewById(R.id.temp);
		resultLayout = (RelativeLayout) findViewById(R.id.resultLayout);
		noWeatherLayout = (RelativeLayout) findViewById(R.id.noWeatherLayout);
		forecastTab = (TableLayout) findViewById(R.id.forecast);
		iv = (ImageView) findViewById(R.id.image);
		radioGroup = (RadioGroup) findViewById(R.id.tempgrp);
		resultLayout.setVisibility(View.INVISIBLE);
		noWeatherLayout.setVisibility(View.INVISIBLE);
		weatherLink = (TextView) findViewById(R.id.weatherLink);
		forecastLink = (TextView) findViewById(R.id.forecastLink);
		
		forecastTab.setColumnShrinkable(0, true);
		forecastTab.setColumnStretchable(1, true);
		forecastTab.setColumnShrinkable(2, true);
		forecastTab.setColumnShrinkable(3, true);
		
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() 
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	            // checkedId is the RadioButton selected
	        	//weatherSearch(loc);
	        }
	    });
		weatherLink.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//postToFacebook(v);
				showDialog(1);
			}
		});

		forecastLink.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//postToFacebook(v);
				showDialog(2);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Post To Facebook");
		//builder.setMessage(R.string.dialog_message);
		builder.setIcon(android.R.drawable.btn_star);
		if(id == 1) {
			postId = 1;
			builder.setPositiveButton("Post Current Weather",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
						postToFacebook(1);
						//return;
					}
				});
		} else if(id == 2) {
			postId = 2;
			builder.setPositiveButton("Post Weather Forecast",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							
							postToFacebook(2);
							//return;
						}
					});
		}
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(), "Clicked Cancelled!",
								Toast.LENGTH_SHORT).show();
						return;
					}
				});
		return builder.create();

	}

	public void postToFacebook(int id) {

		// start Facebook Login
		Session.openActiveSession(this, true, new Session.StatusCallback() {

			// callback when session changes state
			@SuppressWarnings("deprecation")
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {

					// make request to the /me API
					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

								// callback after Graph API response with user
								// object
								@Override
								public void onCompleted(GraphUser user,	Response response) {
									// TODO Auto-generated method stub
									if (user != null) {
										publishFeedDialog();
									}
								}
							});
				}
			}
		});

	}
	
	private void publishFeedDialog() {
	    Bundle params = new Bundle();
	    
	    String title = "";
	    
	    if(jsonWeather.getJSONObject("location").getString("region").equals("")) {
	    	title = jsonWeather.getJSONObject("location").getString("city")+", "+jsonWeather.getJSONObject("location").getString("country");
	    } else {
	    	title = jsonWeather.getJSONObject("location").getString("city")+", "+jsonWeather.getJSONObject("location").getString("region")+", "+jsonWeather.getJSONObject("location").getString("country");
	    }
	    
	    final JSONObject propIn = new JSONObject() {
	    	{
	    	try { 
	    		put("text", "here"); 
	    		put("href", jsonWeather.getString("link")); 
	    	} catch(Exception e ) { 
	    		e.printStackTrace(); 
	    	}
	    	}
	    };
	    JSONObject prop = new JSONObject(){
	        {
	            try {
	                put("Look at details", propIn );
	            } catch(Exception e){
	                e.printStackTrace();
	            }
	        }
	    };
	    
	    if(postId == 1 ) {
		    params.putString("name", title);
		    params.putString("caption", "The current condition for "+jsonWeather.getJSONObject("location").getString("city")+" is "+jsonWeather.getJSONObject("condition").getString("text"));
		    params.putString("description", "Temperature is "+jsonWeather.getJSONObject("condition").getString("temp")+(char) 0x00B0 + jsonWeather.getJSONObject("units").getString("temperature"));
		    params.putString("link", URLDecoder.decode(jsonWeather.getString("feed")));
		    params.putString("picture", jsonWeather.getString("img"));
		    params.putString("properties", prop.toString());
	    } else if(postId == 2) {
	    	String desc = "";
	    	String tempUnit = jsonWeather.getJSONObject("units").getString("temperature");
	    	JSONArray forecastArray = jsonWeather.getJSONArray("forecast");
	    	for (int i = 0; i < forecastArray.length(); i++) {
				JSONObject row = forecastArray.getJSONObject(i);				
				desc += row.getString("day")+": "+row.getString("text")+", "+row.getString("high")+ "/" + row.getString("low")+ (char) 0x00B0 + tempUnit + "; ";
			}
	    	params.putString("name", title);
		    params.putString("caption", "Weather Forecast for "+jsonWeather.getJSONObject("location").getString("city"));
		    params.putString("description", desc);
		    params.putString("link", URLDecoder.decode(jsonWeather.getString("feed")));
		    params.putString("picture", "http://www-scf.usc.edu/~csci571/2013Fall/hw8/weather.jpg");
		    params.putString("properties", prop.toString());
	    }

	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(this,
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {

	            @Override
	            public void onComplete(Bundle values,
	                FacebookException error) {
	            	if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(getApplicationContext(),
	                            "Posted successfully, id: "+postId,
	                            Toast.LENGTH_LONG).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(getApplicationContext(), 
	                            "Post cancelled", 
	                            Toast.LENGTH_LONG).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(getApplicationContext(), 
	                        "Post cancelled", 
	                        Toast.LENGTH_LONG).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(getApplicationContext(), 
	                        "Error in posting", 
	                        Toast.LENGTH_LONG).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
	}

	public void weatherSearch(View view) {

		if (!validateQuery(loc)) {
			return;
		}

		
		int radioId = radioGroup.getCheckedRadioButtonId();
		RadioButton b = (RadioButton) findViewById(radioId);
		String t = null;
		if(b.getId() == R.id.ftemp) {
			t = "f";
		} else {
			t = "c";
		}
		String l = loc.getText().toString();
		
		l = l.replace("'", " ");

		// Async call to get JSON
		new GetJSONFromServlet().execute(l,locType,t);
		// Process JSON and display results accordingly
		/*
		 * city.setText("Los Angeles"); region.setText("CA, United States");
		 * condition.setText("Fair"); temp.setText("16" + "\u2103");
		 */

	}

	private boolean validateQuery(EditText loc) {
		Context context = getApplicationContext();
		Toast toast;
		if (loc.getText().length() == 0) {
			// display error
			toast = Toast.makeText(context,
					"Please enter Location or ZIP Code!", Toast.LENGTH_LONG);
			toast.show();
			return false;
		}

		Pattern num = Pattern.compile("^\\s*[0-9]+\\s*$");
		boolean flagn = num.matcher(loc.getText()).matches();

		Pattern num5 = Pattern.compile("^\\s*[0-9]{5}\\s*$");
		boolean flagz = num5.matcher(loc.getText()).matches();

		if (flagn && !flagz) {
			// display error
			toast = Toast.makeText(context,
					"Invalid zipcode: must be five digits\nExample: 90007",
					Toast.LENGTH_LONG);
			toast.show();
			return false;
		}

		Pattern locP = Pattern.compile("^.{1,},.{1,}[,]{0,1}.{1,}$");
		boolean flagl = locP.matcher(loc.getText()).matches();

		if (!flagz && !flagl) {
			// display error
			toast = Toast
					.makeText(
							context,
							"Invalid location: must include state or country seperated by comma\nExample: Los Angeles, CA",
							Toast.LENGTH_LONG);
			toast.show();
			return false;
		}
		
		if(flagz) {
			locType = "zip";
		} else if(flagl) {
			locType = "city";
		}
		return true;
	}

	// -----------------Get JSON Async Task Inner Class-------------------------
	private class GetJSONFromServlet extends AsyncTask<String, Void, String> {

		/*
		 * private GetJSONFromServlet(Activity act) { this.Activity = activity }
		 */
		@Override
		protected String doInBackground(String... params) {
			String jsonString = "";
			
			if( params[0] == null || params[1] == null || params[2] == null ) {
				return null;
			}
			
			
			String url = "";
			try {
				url = "http://cs-server.usc.edu:22715/examples/servlet/SearchServlet?location="+URLEncoder.encode(params[0], "UTF-8")+"&loctype="+params[1]+"&temperature="+params[2];
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
/*			HttpGet getRequest = new HttpGet(URLEncoder.encode(url));
			ResponseHandler<String> handler = new BasicResponseHandler();
			DefaultHttpClient httpclient = new DefaultHttpClient();
			try {
				jsonString = httpclient.execute(getRequest, handler);
			} */
			try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                jsonString = EntityUtils.toString(httpEntity);
            } catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return jsonString;
		}

		@Override
		protected void onPostExecute(String result) {
			// You will get the JSON string here
			/*
			 * TextView city = (TextView) findViewById(R.id.city); TextView
			 * region = (TextView) findViewById(R.id.region); TextView condition
			 * = (TextView) findViewById(R.id.condition); TextView temp =
			 * (TextView) findViewById(R.id.temp);
			 */
			if(result == null || result == "") {
				return;
			}

			JSONObject jsonObj;
			// Convert string into JSON object
			try {
				 jsonObj = new JSONObject(result);
			} catch(JSONException e) {
				resultLayout.setVisibility(View.INVISIBLE);
				 noWeatherLayout.setVisibility(View.VISIBLE);
				 noWeather.setText("Something went wrong!!");
				 return;
			}
			/*
			 * if( jsonObj.NULL.equals(null) ) { //no json returned
			 * city.setText("Weather Information cannot be found!");
			 * region.setVisibility(0); condition.setVisibility(0);
			 * temp.setVisibility(0); 
			 * } else */if(jsonObj.has("error")) { 
				 resultLayout.setVisibility(View.INVISIBLE);
				 noWeatherLayout.setVisibility(View.VISIBLE);
				 noWeather.setText(jsonObj.getJSONObject("error").getString("description")); 
			 } else if (jsonObj.has("weather")) {
				resultLayout.setVisibility(View.VISIBLE);
				noWeatherLayout.setVisibility(View.INVISIBLE);
				jsonWeather = jsonObj.getJSONObject("weather");
				String c = jsonWeather.getJSONObject("location").getString(
						"city");
				city.setText(c);
				String region_temp = "N/A";
				if(!jsonWeather.getJSONObject("location").getString("region").equals("")) {
					region_temp = jsonWeather.getJSONObject("location").getString("region");
				}
				region.setText(region_temp
						+ ", "
						+ jsonWeather.getJSONObject("location").getString(
								"country"));
				condition.setText(jsonWeather.getJSONObject("condition")
						.getString("text"));
				temp.setText(jsonWeather.getJSONObject("condition").getString(
						"temp") + (char) 0x00B0 + jsonWeather.getJSONObject("units").getString("temperature"));
				fillTable(jsonWeather.getJSONArray("forecast"), jsonWeather.getJSONObject("units").getString("temperature"));
				
				new GetImageTask().execute(jsonWeather.getString("img"));

			}
		}

		private void fillTable(JSONArray forecastArray, String tempUnit) {
			TableRow tabRow;
			TextView textV;
			for (int i = 0; i < forecastArray.length(); i++) {
				JSONObject row = forecastArray.getJSONObject(i);
				tabRow = (TableRow) forecastTab.getChildAt(i + 1);
				textV = (TextView) tabRow.getChildAt(0);
				textV.setText(row.getString("day"));
				textV = (TextView) tabRow.getChildAt(1);
				textV.setText(row.getString("text"));
				textV = (TextView) tabRow.getChildAt(2);
				textV.setText(row.getString("high")+ (char) 0x00B0 + tempUnit);
				textV = (TextView) tabRow.getChildAt(3);
				textV.setText(row.getString("low")+ (char) 0x00B0 + tempUnit);
			}
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	// --------------------------------------------------------------------

	// -----------------Get Image Async Task Inner Class-------------------
	private class GetImageTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try
	        {
	        image = downloadBitmap(params[0]);
	        }
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
	        return null;
		}
		
		private Bitmap downloadBitmap(String url) {
		     // initilize the default HTTP client object
		     final DefaultHttpClient client = new DefaultHttpClient();

		     //forming a HttoGet request 
		     final HttpGet getRequest = new HttpGet(url);
		     try {

		         HttpResponse response = client.execute(getRequest);

		         //check 200 OK for success
		         final int statusCode = response.getStatusLine().getStatusCode();

		         if (statusCode != HttpStatus.SC_OK) {
		             Log.w("ImageDownloader", "Error " + statusCode + 
		                     " while retrieving bitmap from " + url);
		             return null;

		         }

		         final HttpEntity entity = response.getEntity();
		         if (entity != null) {
		             InputStream inputStream = null;
		             try {
		                 // getting contents from the stream 
		                 inputStream = entity.getContent();

		                 // decoding stream data back into image Bitmap that android understands
		                 image = BitmapFactory.decodeStream(inputStream);


		             } finally {
		                 if (inputStream != null) {
		                     inputStream.close();
		                 }
		                 entity.consumeContent();
		             }
		         }
		     } catch (Exception e) {
		         // You Could provide a more explicit error message for IOException
		         getRequest.abort();
		         Log.e("ImageDownloader", "Something went wrong while" +
		                 " retrieving bitmap from " + url + e.toString());
		     } 

		     return image;
		 }

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
	        //pd.dismiss();
	        if(image!=null)
	        {
	            iv.setImageBitmap(image);
	        }
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
	// -----------------------------------------------------------------------------
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}
}