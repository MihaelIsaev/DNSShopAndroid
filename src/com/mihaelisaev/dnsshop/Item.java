package com.mihaelisaev.dnsshop;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.dnsshop.ViewSwitcher.WHAT_VIEW;
import com.mihaelisaev.helper.CommonHelper;

public class Item {

	private 	View 			result,reviews,comments 			= null;
	private 	ScrollView 		reviews_sv;
    public 		LinearLayout 	reviews_lv;
	private 	Root 			root;
	public 		String 			id									= "";
	public		String			title 								= "";
	private 	String 			tag 								= "Activity ITEM";
	private 	String 			itemJSON 							= "[]";
    private 	boolean 		loadedFromCache, loadedFromNetwork, nowDBloading, loadFromDBAfterServer	= false;
    private 	Bitmap 			bmp;
    public 		WHAT_VIEW 		from 								= WHAT_VIEW.ITEMS;
	
    private ImageView 	itemLogo, itemLogoFull, itemNoimage;
    private ProgressBar itemImageProgress, reviewsLoadMoreProgress;
    private TextView 	itemName, itemCode, itemPrice, itemAvailability, itemDescription, itemDescriptionT;
    private Button 		buttonReviews, buttonComments, fav, buttonLoadMore;
    public  WebView 	itemFeatures;
    
	public Item(Root root){
		this.root = root;
		LayoutInflater li 		= root.getLayoutInflater();
        result 					= li.inflate(R.layout.item, null);
        reviews 				= li.inflate(R.layout.catalog, null);
        comments 				= li.inflate(R.layout.comments, null);
        reviews_sv 				= (ScrollView) 		reviews.findViewById(R.id.scrollView);
        reviews_lv 				= (LinearLayout) 	reviews.findViewById(R.id.listView);
        itemLogo 				= (ImageView) 		result.findViewById(R.id.itemLogo);
    	itemLogoFull 			= (ImageView) 		result.findViewById(R.id.itemLogoFull);
    	itemNoimage 			= (ImageView) 		result.findViewById(R.id.itemNoimage);
    	itemImageProgress		= (ProgressBar) 	result.findViewById(R.id.itemImageProgress);
    	reviewsLoadMoreProgress	= (ProgressBar) 	reviews.findViewById(R.id.reviewsProgress);
    	itemName 				= (TextView) 		result.findViewById(R.id.name);
    	itemCode 				= (TextView) 		result.findViewById(R.id.code);
    	itemPrice 				= (TextView) 		result.findViewById(R.id.price);
    	itemAvailability 		= (TextView) 		result.findViewById(R.id.availability);
    	itemDescription  		= (TextView) 		result.findViewById(R.id.description);
    	itemDescriptionT 		= (TextView) 		result.findViewById(R.id.descriptionTitle);
    	buttonReviews 			= (Button) 			result.findViewById(R.id.reviews);
    	buttonComments 			= (Button) 			result.findViewById(R.id.comments);
    	buttonLoadMore			= (Button) 			reviews.findViewById(R.id.buttonLoadMore);
    	fav 					= (Button) 			result.findViewById(R.id.fav);
    	itemFeatures 			= (WebView) 		result.findViewById(R.id.features);
    	/*if(root.db.getKey(Settings.KEY_SHOW_DESCR_BACK).equals(Settings.KEY_VALUE_ON))
    		itemFeatures.setBackgroundColor(Color.BLACK);
    	else*/
    		itemFeatures.setBackgroundColor(Color.TRANSPARENT);
	}
	
	public View getView(){
		return result;
	}
	
	public View getReviewsView(){
		return reviews;
	}
	
	public View getCommentsView(){
		return comments;
	}
	
	/**
	 * Function for copy item information to clipboard
	 */
	@SuppressWarnings("deprecation")
	public void copy(){
		final TextView itemName 			= (TextView) 	result.findViewById(R.id.name);
    	final TextView itemCode 			= (TextView) 	result.findViewById(R.id.code);
    	final TextView itemPrice 			= (TextView) 	result.findViewById(R.id.price);
    	final TextView itemAvailability 	= (TextView) 	result.findViewById(R.id.availability);
		root.clipboard.setText(new StringBuffer().append(itemCode.getText()).append(" ").append(itemName.getText()).append(" ").append(itemPrice.getText()).append(" ").append(itemAvailability.getText()).toString());
		Toast.makeText(root.getApplicationContext(), root.db.getResourse(R.string.clipboard_pass_message), Toast.LENGTH_SHORT).show(); 
	}
	
	/**
	 * Function for link item information to clipboard
	 */
	@SuppressWarnings("deprecation")
	public void link(){
		StringBuffer url = new StringBuffer().append("http://").append(root.db.getKey(Settings.KEY_DEF_CITY)).append(".dns-shop.ru/catalog/i").append(id).append("/");
		root.clipboard.setText(url.toString());
		Toast.makeText(root.getApplicationContext(), root.db.getResourse(R.string.clipboard_link_message), Toast.LENGTH_SHORT).show(); 
	}
	
	private void setImage(final Bitmap bmp, final ImageView itemLogo, final ImageView itemLogoFull, final ProgressBar itemImageProgress, final ImageView itemNoimage){
		root.runOnUiThread(new Runnable(){
			public void run(){
				if (null != bmp){
			    	itemLogo.setImageBitmap(bmp);
			    	itemLogoFull.setImageBitmap(bmp);
			    	itemLogoFull.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							CommonHelper.gone(root, itemLogoFull);
						}
					});
			    	itemLogo.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							CommonHelper.visible(root, itemLogoFull);
						}
					});
			    	CommonHelper.visible(root, itemLogo);
			    	CommonHelper.gone(root, itemNoimage);
			    	CommonHelper.gone(root, itemImageProgress);			    	
			    }else{
			    	CommonHelper.visible(root, itemNoimage);
			    	CommonHelper.gone(root, itemImageProgress);
			    	System.out.println("The Bitmap is NULL");
			    }
			}
		});
	}
	
	private void loadComments(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				final WebView 		wv 			= (WebView) 	comments.findViewById(R.id.comments);
				final ProgressBar 	progress 	= (ProgressBar) comments.findViewById(R.id.commentsProgress);
				final TextView 		message 	= (TextView) 	comments.findViewById(R.id.commentsMessage);
				CommonHelper.gone(root, wv);
				CommonHelper.visible(root, progress);
				CommonHelper.visible(root, message);
				CommonHelper.setText(root, message, root.db.getResourse(R.string.comments_start_loading));
				StringBuffer url 	= new StringBuffer().append(root.db.getResourse(R.string.domain_prefix)).append(root.db.getKey(Settings.KEY_DEF_CITY)).append(".dns-shop.ru/engine/scripts/ajax.php?action=comment_load&module_id=1&object_id=").append(id).append("&page=0&id=false&loadAll=1");
				String comments 	= root.db.server.executeHTTPRequest(url.toString(), new ArrayList<NameValuePair>(2));
				CommonHelper.setText(root, message, root.db.getResourse(R.string.comments_start_parsing));
				final StringBuffer 	buffer = new StringBuffer();
									buffer.append(CommonHelper.readRawTextFile(root.getApplicationContext(), R.raw.header));
				try{
					if(!comments.equals("null"))
						buffer.append(comments);
					else
						buffer.append(root.db.getResourse(R.string.comments_no_comments));
				}catch(Exception e){buffer.append(root.db.getResourse(R.string.comments_not_loaded));}
				buffer.append(CommonHelper.readRawTextFile(root.getApplicationContext(), R.raw.footer));
				CommonHelper.setTransparent(root, wv);
				wv.setWebChromeClient(new WebChromeClient() {
	                public void onProgressChanged(WebView view, int prg){
	                	if(prg == 100) {
	                		CommonHelper.visible(root, wv);
	                		CommonHelper.gone(root, message);
	                		CommonHelper.gone(root, progress);
	                		CommonHelper.clearText(root, message);
	                   }
	                }
	            });
				wv.loadDataWithBaseURL("file:///android_asset/features.html", buffer.toString(), "text/html", "UTF-8", null);
			}
		}).start();
	}
	
	private void getFromDB(){
		new Thread(new Runnable(){
			public void run(){
				nowDBloading = true;
				Cursor cursor = root.db.getItem(id);
		    	if(cursor.getCount()>0){
					cursor.moveToFirst();
					final String Image 			= cursor.getString(cursor.getColumnIndex("Image"));
					final String Name 			= cursor.getString(cursor.getColumnIndex("Name"));
					final String Code 			= cursor.getString(cursor.getColumnIndex("Code"));
					final String Price 			= cursor.getString(cursor.getColumnIndex("Price"));
					final String Description 	= cursor.getString(cursor.getColumnIndex("Description"));
					final String Availability 	= cursor.getString(cursor.getColumnIndex("Availability"));
					final String Features 		= cursor.getString(cursor.getColumnIndex("Features"));
					final String Reviews 		= cursor.getString(cursor.getColumnIndex("Reviews"));
					cursor.close();
					root.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							CommonHelper.setText(root, itemName, Name);
							CommonHelper.setText(root, itemCode, "Код: "+Code);
							CommonHelper.setText(root, itemAvailability, Availability);
							CommonHelper.setText(root, itemDescription, Description);
							if(Description!="" && !Description.equals("null")){
								if(!itemDescription.getText().toString().equals("null") && !itemDescription.getText().toString().equals("")){
									CommonHelper.visible(root, itemDescriptionT);
									CommonHelper.visible(root, itemDescription);
								}
							}
							try{
								StringBuffer 	buffer 		= new StringBuffer();
								buffer.append(CommonHelper.readRawTextFile(root.getApplicationContext(), R.raw.header));
								buffer.append(Features);
								buffer.append(CommonHelper.readRawTextFile(root.getApplicationContext(), R.raw.footer));
								itemFeatures.loadDataWithBaseURL("file:///android_asset/features.html", buffer.toString(), "text/html", "UTF-8", null);
								CommonHelper.visible(root, itemFeatures);
								CommonHelper.setTransparent(root, itemFeatures);
								itemFeatures.setBackgroundColor(Color.TRANSPARENT);
							}catch(Exception e){Loger.d(tag, e.toString());}
						}
					});
					CommonHelper.checkValueForTextView(root, itemPrice, Price, "null", "нет в наличии", " руб.");
					buttonReviews.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							root.viewSwitcher.switchTo(WHAT_VIEW.REVIEWS);
							root.item.loadReviews(false);
						}
					});
					buttonComments.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							root.viewSwitcher.switchTo(WHAT_VIEW.COMMENTS);
						}
					});
					buttonLoadMore.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							root.item.loadReviews(true);
						}
					});
					CommonHelper.visible(root, buttonReviews);
					CommonHelper.visible(root, buttonComments);
					CommonHelper.visible(root, fav);
			    	TextView	mess 	= (TextView) 	result.findViewById(R.id.message);
					Button 		retry 	= (Button) 		result.findViewById(R.id.retry);
					Button 		back 	= (Button) 		result.findViewById(R.id.back);
					CommonHelper.gone(root, retry);
					CommonHelper.gone(root, back);
					CommonHelper.gone(root, mess);
					bindFav(Image, Name, Code, Price, Description, Availability, Features, Reviews);
					root.db.parser.reviews(Reviews, false, id);
					getMoreReviews();
					loadImageFromCache();
					loadedFromCache = true;
				}
		    	if(!cursor.isClosed())
		    		cursor.close();
		    	nowDBloading = false;
		    	if(loadFromDBAfterServer)
		    		getFromDB();
			}
		}).start();
	}
	
	private void getFromServer(){
		new Thread(new Runnable(){
			public void run(){
				List<NameValuePair> 	params = new ArrayList<NameValuePair>(2);
										params.add(new BasicNameValuePair("what", 	"item"));
										params.add(new BasicNameValuePair("city", 	root.db.getKey(Settings.KEY_DEF_CITY)));
										params.add(new BasicNameValuePair("id",     id));
				itemJSON = root.db.server.executeHTTPRequest(root.db.server.buildURL(), params);
				try{
					final JSONObject jItem = new JSONObject(itemJSON);
					if(!jItem.getString("Name").equals(""))
						loadedFromNetwork = true;
					bindFav(jItem.getString("Image"), jItem.getString("Name"), jItem.getString("Code"), jItem.getString("Price"), jItem.getString("Description"), jItem.getString("Availability"), jItem.getString("Features"), jItem.getString("Reviews"));
					root.db.addItem(jItem.getString("Image"), jItem.getString("Name"), jItem.getString("Code").replace("Код: ", ""), jItem.getString("Price").replace(" ", ""), jItem.getString("Description"), jItem.getString("Availability"), jItem.getString("Features"), jItem.getString("Reviews"));
					loadImageFromServer(jItem.getString("Image"));
					if(!nowDBloading)
						getFromDB();
					else
						loadFromDBAfterServer = true;
				} catch(Exception e) {  
					Loger.d(tag, e.toString());
					loadedFromNetwork = false;
				} finally {
					root.runOnUiThread(new Runnable(){
						public void run(){
							if(!loadedFromCache && !loadedFromNetwork)
								empty();
							else
								notEmpty(fav);
							CommonHelper.hide(root, root.pb);
							CommonHelper.gone(root, itemImageProgress);
						}
					});
				}
			}
		}).start();
	}
	
	private void getMoreReviews(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				CommonHelper.visible(root, reviewsLoadMoreProgress);
				try{
					final List<NameValuePair> 	params = new ArrayList<NameValuePair>(2);
												params.add(new BasicNameValuePair("what", 	"itemMoreReviews"));
												params.add(new BasicNameValuePair("city", 	root.db.getKey(Settings.KEY_DEF_CITY)));
												params.add(new BasicNameValuePair("id",     id));
					root.db.parser.reviews(root.db.server.executeHTTPRequest(root.db.server.buildURL(), params), true, id);
				}catch(Exception e){
					Loger.d(tag, "error get more reviews: "+e.toString());
				}finally{
					CommonHelper.gone(root, reviewsLoadMoreProgress);
				}
			}
		}).start();
	}
	
	public void load(){
		nowDBloading = false;
		loadFromDBAfterServer = false;
		loadComments();
		if(null!=bmp)
			bmp.recycle();
		CommonHelper.visible(root, root.pb);
		CommonHelper.hide(root, itemNoimage);
    	CommonHelper.hide(root, itemLogoFull);
    	CommonHelper.hide(root, itemLogo);
    	CommonHelper.hide(root, buttonReviews);
    	CommonHelper.hide(root, buttonComments);
    	CommonHelper.hide(root, fav);
    	CommonHelper.gone(root, itemDescriptionT);
    	CommonHelper.gone(root, itemDescription);    	
    	CommonHelper.gone(root, itemFeatures);
    	CommonHelper.gone(root, reviewsLoadMoreProgress);
    	CommonHelper.gone(root, buttonLoadMore);
    	CommonHelper.visible(root, itemImageProgress);
    	CommonHelper.clearText(root, itemName);
    	CommonHelper.clearText(root, itemCode);
    	CommonHelper.clearText(root, itemPrice);
    	CommonHelper.clearText(root, itemAvailability);
    	CommonHelper.clearText(root, itemDescription);
    	CommonHelper.setTransparent(root, itemFeatures);
    	CommonHelper.setTransparent(root, itemLogo);
    	getFromDB();
    	getFromServer();
	}
	
	private void bindFav(final String Image, final String Name, final String Code, final String Price, final String Description, final String Availability, final String Features, final String Reviews){
		root.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Button fav = (Button) result.findViewById(R.id.fav);
				if(root.db.isInFav(id)){
					fav.setBackgroundResource(R.drawable.button_red);
					CommonHelper.setText(root, fav, root.db.getResourse(R.string.item_from_cart));
					fav.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							root.db.deleteFav(id);
							bindFav(Image, Name, Code, Price, Description, Availability, Features, Reviews);
						}
					});
				}else{
					fav.setBackgroundResource(R.drawable.button_blue_selector);
					CommonHelper.setText(root, fav, root.db.getResourse(R.string.item_to_cart));
					fav.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							root.db.addFav(Image, Name, Code.replace("Код: ", ""), Price.replace(" ", ""), Description, Availability, Features, Reviews);
							bindFav(Image, Name, Code, Price, Description, Availability, Features, Reviews);
						}
					});
				}
			}
		});
	}
	
	private void empty(){
		TextView mess = (TextView) result.findViewById(R.id.message);
		Button retry = (Button) result.findViewById(R.id.retry);
		retry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CommonHelper.gone(root, v);
				load();
			}
		});
		CommonHelper.setText(root, mess, root.db.getResourse(R.string.item_loading_fail));
		CommonHelper.gone(root, retry);
		CommonHelper.visible(root, mess);
	}
	
	private void notEmpty(Button fav){
		TextView mess 	= (TextView) 	result.findViewById(R.id.message);
		Button retry 	= (Button) 		result.findViewById(R.id.retry);
		Button back 	= (Button) 		result.findViewById(R.id.back);
		CommonHelper.visible(root, reviews);
		CommonHelper.visible(root, comments);
    	CommonHelper.visible(root, fav);
    	CommonHelper.gone(root, retry);
		CommonHelper.gone(root, back);
		CommonHelper.gone(root, mess);
	}
	
	public void loadReviews(final boolean more){
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(more)
					setLoadingLoadMoreButton();		
				
				clearReviews(more);
				
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				LinearLayout ll = new LinearLayout(root.getApplicationContext());
				ll.setOrientation(LinearLayout.VERTICAL);
				ll.setBackgroundColor(Color.TRANSPARENT);
				ll.setLayoutParams(lp);				
				
				Cursor 			cursor 		= root.db.getReviews(more, id);
				LayoutInflater	li 			= root.getLayoutInflater();
				TextView 		mess 		= (TextView) reviews_sv.findViewById(R.id.message);
			    Button 			back 		= (Button) reviews_sv.findViewById(R.id.back);
			    if(cursor.getCount()>0){
					if(!more){
						CommonHelper.gone(root, back);
					    CommonHelper.visible(root, mess);
					    CommonHelper.setText(root, mess, "Загружаем отзывы");
					}
					cursor.moveToFirst();
					int k=0;
					for(int i=0;i<cursor.getCount();i++){
						k++;
						View		reviewRow 	= li.inflate(R.layout.review_row, null);
						if(k==1)
							reviewRow.setBackgroundResource(R.drawable.textlines);
						else if(k==2)
							reviewRow.setBackgroundResource(R.drawable.textlines_light);
						RatingBar 	ratingView 	= (RatingBar) reviewRow.findViewById(R.id.rating);
						CommonHelper.checkValueForTextView(root, reviewRow.findViewById(R.id.plus), 		cursor.getString(cursor.getColumnIndex("Plus")), 		"null", "-");
						CommonHelper.checkValueForTextView(root, reviewRow.findViewById(R.id.minus), 		cursor.getString(cursor.getColumnIndex("Minus")), 		"null", "-");
						CommonHelper.checkValueForTextView(root, reviewRow.findViewById(R.id.comment), 		cursor.getString(cursor.getColumnIndex("Comment")), 	"null", "-");
						CommonHelper.checkValueForTextView(root, reviewRow.findViewById(R.id.user), 		cursor.getString(cursor.getColumnIndex("User")), 		"null", "-");
						CommonHelper.checkValueForTextView(root, reviewRow.findViewById(R.id.city), 		cursor.getString(cursor.getColumnIndex("City")), 		"null", "-");
						CommonHelper.checkValueForTextView(root, reviewRow.findViewById(R.id.date), 		cursor.getString(cursor.getColumnIndex("Date")), 		"null", "-");
						CommonHelper.setRaiting(root, ratingView, cursor.getString(cursor.getColumnIndex("Grade")));
						ll.addView(reviewRow, lp);
						cursor.moveToNext();
						if(k==2) k=0;
					}
					if(!more){
						CommonHelper.gone(root, mess);
						checkMoreReviews();
					}
					addReview(ll);
				}else
					if(!more){
						emptyMessage(mess, back);
					}
			    cursor.close();
			    if(more)
					hideLoadMoreButton();
			}
		}).start();
	}
	
	public void showLoadMoreButton(){
		CommonHelper.visible(root, buttonLoadMore);
		CommonHelper.setButtonText(root, buttonLoadMore, "Показать еще отзывы");
	}
	
	public void setLoadingLoadMoreButton(){
		CommonHelper.setButtonText(root, buttonLoadMore, "загружаем, подождите");
	}
	
	public void hideLoadMoreButton(){
		CommonHelper.gone(root, buttonLoadMore);
		CommonHelper.setButtonText(root, buttonLoadMore, "Показать еще отзывы");
	}
	
	public void checkMoreReviews(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				Cursor cursor = root.db.getReviews(true, id);
				if(cursor.getCount()>0)
					showLoadMoreButton();
				else
					hideLoadMoreButton();
				cursor.close();
			}
		}).start();	}
	
	private void addReview(final View reviewRow){
		root.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try{
					reviews_lv.addView(reviewRow);
				}catch(Exception e){
					Loger.d(tag, "error addView reviewRow: "+e.toString());
				}
			}
		});
	}
	
	private void clearReviews(final boolean more){
		root.runOnUiThread(new Runnable(){
			public void run(){
			    if(!more)
					reviews_lv.removeAllViews();
			}
	    });
	}
	
	private void emptyMessage(TextView mess, Button back){
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				root.viewSwitcher.switchTo(root.getHistoryPrev());
				CommonHelper.gone(root, v);
			}
		});
		CommonHelper.setText(root, mess, root.db.getResourse(R.string.reviews_no_reviews));
		CommonHelper.visible(root, back);
		CommonHelper.visible(root, mess);
	}
	
	private void loadImageFromServer(final String url){
		File imageFile = new File(CommonHelper.getImageFileName(id));
		if(!imageFile.exists())
			new Thread(new Runnable(){
				public void run(){
					try{
						URL urln = new URL(url);
					    HttpURLConnection con = (HttpURLConnection)urln.openConnection();
					    InputStream is = con.getInputStream();
					    final Bitmap bmp = BitmapFactory.decodeStream(is);
					    
					    if(null != bmp)
						    try{
						    	File outputDir = new File(CommonHelper.getCachePath());
						    	outputDir.mkdirs();
																	    	
						    	int quality = 85;
								BitmapFactory.Options options=new BitmapFactory.Options();
								options.inSampleSize = 5;
								
								File outputFile = new File(CommonHelper.getImageFileName(id));
								
								FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
								BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
								bmp.compress(CompressFormat.JPEG, quality, bos);
								bos.flush();
								bos.close();
						    }catch(Exception e){Loger.d(tag, "Write file error: "+e.toString()); }
					    setImage(bmp, itemLogo, itemLogoFull, itemImageProgress, itemNoimage);
				    }catch(Exception e){
				    	root.runOnUiThread(new Runnable(){
							public void run(){
								CommonHelper.visible(root, itemNoimage);
								CommonHelper.visible(root, itemImageProgress);
							}
				    	});
				    	String fff = e.toString();
				    	Loger.d(tag, fff);
				    }
				}
			}).start();
	}
	
	private void loadImageFromCache(){
		File imageFile = new File(CommonHelper.getImageFileName(id));
		final ImageView itemLogo 			= (ImageView) 	result.findViewById(R.id.itemLogo);
    	final ImageView itemLogoFull 		= (ImageView) 	result.findViewById(R.id.itemLogoFull);
    	final ImageView itemNoimage 		= (ImageView) 	result.findViewById(R.id.itemNoimage);
		if(imageFile.exists()){
			bmp = BitmapFactory.decodeFile(CommonHelper.getImageFileName(id));
			setImage(bmp, itemLogo, itemLogoFull, itemImageProgress, itemNoimage);
		}else{
			CommonHelper.visible(root, itemNoimage);
	    	CommonHelper.gone(root, itemImageProgress);
		}
	}
}
