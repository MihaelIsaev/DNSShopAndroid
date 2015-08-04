package com.mihaelisaev.dnsshop;

import java.security.KeyStore;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import com.mihaelisaev.helper.CommonHelper;

import com.mihaelisaev.dnsshop.R;

import android.content.Context;

public class Server {
	
	public static final int SYNC_CARD_IS_ANNULED 		= -4;
	public static final int SYNC_CARD_OUT_OF_DATE 		= -3;
	public static final int SYNC_WRONG_PASSWORD 		= -2;
	public static final int SYNC_CARDNUMBER_NOT_FOUND	= -1;
	public static final int SYNC_CARD_NOT_ACTIVATED		= 0;
	public static final int SYNC_NEED_REGISTER_DEVICE 	= 1;
	public static final int SYNC_SERVER_OFFLINE 		= 2;
	public static final int SYNC_ERROR_CONNECTING 		= 3;
	public static final int SYNC_OK 					= 4;
	
	private String TAG = "Database.java";
	private Context context;
	@SuppressWarnings("unused")
	private Database db;
	
	public Server(Context context, Database db){
		this.context = context;
		this.db = db;
	}
	
	/**
	 * Build URL from constants 
	 * @return String URL
	 */
	public String buildURL(){
		StringBuffer urlBuffer = new StringBuffer();
		urlBuffer.append(context.getString(R.string.domain_prefix))
				 .append(context.getString(R.string.domain_name))
				 .append(context.getString(R.string.domain_script));
		return urlBuffer.toString();
	}
	
	/**
	 * Executor for HTTPPost
	 * @param String URL 
	 * @param List<NameValuePair> list with post data
	 * @return Map<String, String> with server answer
	 */
	public String executeHTTPRequest(String url, List<NameValuePair> params){
		Loger.d("Server.java", "Params: "+params.toString());
		String result = "";
		try{
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
			HttpConnectionParams.setSoTimeout(httpParameters, 15000);
			HttpClient hc = getNewHttpClient();
			HttpPost request;
			HttpResponse response = null;
			request = new HttpPost(url);
			request.addHeader("Accept-Encoding", 	"gzip");
			//signRequest(request);
			addPostData(request, params);
			try {
				response = hc.execute(request);
			} catch (Exception e){
				Loger.e(TAG, "Exception httpRequest: "+e.toString());
			}
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
				result = CommonHelper.httpResponseHTML(response);
			else
				Loger.e(TAG, "Error in HTTP request: "+response.getStatusLine().getStatusCode());
		}
		catch(Exception e){Loger.e(TAG, "Error get json 4 data "+e.toString());}
		return result;
	}
	
	
	public HttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
	
	/**
	 * Post data adder to HTTPPost
	 * @param HTTPPost request
	 * @param List<NameValuePair> list with post data
	 * @return HttpPost request
	 */
	private HttpPost addPostData(HttpPost request, List<NameValuePair> params) {
		try{
			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		}catch(Exception e){
			Loger.d(TAG, "Error when try add params");
		}
		return request;
	}

	/**
	 * Sign request
	 * Add UID in Header of request
	 * Add SW Version in Header of request
	 * @param HttpPost request
	 * @return HttpPost request
	 *//*
	private HttpPost signRequest(HttpPost request){
		String encodedUdid = UDID.getUDIDInSha1(context);
		String ver = Settings.SWVersion;
		request.addHeader(db.getResourse(R.string.http_uid), encodedUdid);
		request.addHeader(db.getResourse(R.string.http_ver), ver);
		return request;
	}*/
	
}
