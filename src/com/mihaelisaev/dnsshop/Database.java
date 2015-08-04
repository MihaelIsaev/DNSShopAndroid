package com.mihaelisaev.dnsshop;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class Database extends SQLiteOpenHelper {

	private String 	tag = "Database.java";
	public Root 	activity;
	public Context 	context;
	public 	Server			server;
	private SQLiteDatabase  db;
	public Parser 			parser;
	
	public static final int 	DB_VERSION 		= 10;
	public static final String 	DB_NAME_PREV 	= "dns9";
	public static final String 	DB_NAME 		= "dns10";
    
    public Database(Root root) {
        super(root.getApplicationContext(), DB_NAME, null, DB_VERSION);
        this.activity 		= root;
        this.context 		= root.getApplicationContext();
        server 				= new Server(context, this);
        db 					= getWritableDatabase();
		parser 				= new Parser(root);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    	sqLiteDatabase.execSQL("create table `Category` 		(Id TEXT UNIQUE, Name TEXT, Parent TEXT);");
    	sqLiteDatabase.execSQL("create table `Items` 			(Code TEXT UNIQUE, Comments TEXT, City TEXT, Name TEXT, Price REAL, Grade TEXT, Preview TEXT, Parent TEXT);");
    	sqLiteDatabase.execSQL("create table `ItemsSearch` 		(Code TEXT UNIQUE, Comments TEXT, City TEXT, Name TEXT, Price REAL, Grade TEXT, Preview TEXT, Parent TEXT);");
    	sqLiteDatabase.execSQL("create table `Item` 			(Code TEXT UNIQUE, City TEXT, Name TEXT, Price REAL, Image TEXT, Availability TEXT, Description TEXT, Features TEXT, Reviews TEXT);");
    	sqLiteDatabase.execSQL("create table `Fav` 				(Code TEXT UNIQUE, City TEXT, Name TEXT, Price REAL, Count REAL, Image TEXT, Availability TEXT, Description TEXT, Features TEXT, Reviews TEXT, UserComment TEXT);");
    	sqLiteDatabase.execSQL("create table `City` 			(Id TEXT UNIQUE, Name TEXT, Phone TEXT, Longitude REAL, Latitude REAL);");
    	//sqLiteDatabase.execSQL("create table `Settings` 		(Key TEXT UNIQUE, Value TEXT);");
    	sqLiteDatabase.execSQL("create table `Filters` 			(Id TEXT UNIQUE, Parent TEXT, Num TEXT, Type TEXT, Key TEXT, Label TEXT);");
    	sqLiteDatabase.execSQL("create table `FilterValues`		(Un TEXT UNIQUE, Id TEXT, Num TEXT, Key TEXT, Label TEXT, FromKey TEXT, ToKey TEXT);");
    	sqLiteDatabase.execSQL("create table `TempFilterValues`	(Id TEXT UNIQUE, Value TEXT);");
    	sqLiteDatabase.execSQL("create table `TempSearchResults`(Code TEXT UNIQUE, Name TEXT, Price TEXT, Num TEXT);");
    	sqLiteDatabase.execSQL("create table `Reviews`(Id TEXT UNIQUE, Code TEXT, Num TEXT, Plus TEXT, Minus TEXT, Comment TEXT, User TEXT, City TEXT, Date TEXT, Grade TEXT);");
    	sqLiteDatabase.execSQL("create table `ReviewsMore`(Id TEXT UNIQUE, Code TEXT, Num TEXT, Plus TEXT, Minus TEXT, Comment TEXT, User TEXT, City TEXT, Date TEXT, Grade TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //Calls if DB is exist
    }
    
    public void destroy(){
    	if(db!=null && db.isOpen())
    		db.close();
    }
    
    public SQLiteDatabase getDB(){
    	return db;
    }
    
    /**
     * Clear cache 
     */
    public void clearCache(){
    	try{
    		db.delete("`Items`", null, null);
    		db.delete("`Item`", null, null);
    		db.delete("`Fav`", null, null);
    		db.delete("`Filters`", null, null);
    		db.delete("`FilterValues`", null, null);
    		db.delete("`TempFilterValues`", null, null);
    		db.delete("`TempSearchResults`", null, null);
    		Toast.makeText(activity, "Љэш очищен", Toast.LENGTH_LONG).show();
    	}catch(Exception e){
    		Loger.d(tag, "clear cache error: "+e.toString());
    	}
    }
    
    /**
     * Add review
     */
    public void addReview(boolean more, String Code, String Num, String Plus, String Minus, String Comment, String User, String City, String Date, String Grade)
    {
    	ContentValues 	values = new ContentValues();
    					values.put("Id", 		Code+Num);				
    					values.put("Code", 		Code);
						values.put("Num",		Num.replace("r", ""));
						values.put("Plus",		Plus);
						values.put("Minus",		Minus);
						values.put("Comment",	Comment);
						values.put("User",		User);
						values.put("City",		City);
						values.put("Date",		Date);
						values.put("Grade",		Grade);
    	if(!more)
    		db.replace("`Reviews`", null, values);
    	else
    		db.replace("`ReviewsMore`", null, values);
    }
    
    /**
     * Clear reviews 
     */
    public void clearReviews(boolean more, String Code){
    	try{
    		if(!more)
    			db.delete("`Reviews`", "Code='" + Code + "'", null);
    		else
    			db.delete("`ReviewsMore`", "Code='" + Code + "'", null);
    	}catch(Exception e){
    		Loger.d(tag, "clear reviews " +
    				"error: "+e.toString());
    	}
    }
    
    /**
     * Get reviews
     */
    public Cursor getReviews(boolean more, String Code){
    	if(!more)
    		return db.rawQuery("SELECT * FROM `Reviews` WHERE Code='"+Code+"' ORDER BY ABS(Num) ASC", null);
    	else
    		return db.rawQuery("SELECT * FROM `ReviewsMore` WHERE Code='"+Code+"' ORDER BY ABS(Num) ASC", null);
    }
    
    /**
     * Add search item
     */
    public void addSearchItem(String Code, String Name, String Price, String Num)
    {
    	ContentValues 	values = new ContentValues();
						values.put("Code", 		Code);
						values.put("Name",		Name);
						values.put("Price",		Price);
						values.put("Num",		Num.replace("t", ""));
    	db.replace("`TempSearchResults`", null, values);
    }
    
    /**
     * Get search results
     */
    public Cursor getSearchResults(){
    	return db.rawQuery("SELECT * FROM `TempSearchResults` ORDER BY ABS(Num) ASC", null);
    }
    
    /**
     * Clear search results 
     */
    public void clearSearchResults(){
    	try{
    		db.delete("`TempSearchResults`", null, null);
    	}catch(Exception e){}
    }
    
    /**
     * Add filter
     */
    public void addFilter(String Id, String Parent, String Num, String Type, String Key, String Label)
    {
    	ContentValues 	values = new ContentValues();
						values.put("Id", 		Id);
						values.put("Parent",	Parent);
						values.put("Num",		Num);
						values.put("Type",  	Type);
						values.put("Key", 		Key);
						values.put("Label", 	Label);
    	db.replace("`Filters`", null, values);
    }
    
    /**
     * Add filter values
     */
    public void addFilterValues(final String Un, final String Id, final String Num, final String Key, final String Label, final String FromKey, final String ToKey)
    {
    	ContentValues 	values = new ContentValues();
    					values.put("Un", 		Un);
    					values.put("Id", 		Id);
						values.put("Num", 		Num);
						values.put("Key",  		Key);
						values.put("Label", 	Label);
						values.put("FromKey", 	FromKey);
						values.put("ToKey", 	ToKey);
    	db.replace("`FilterValues`", null, values);
    }
    
    /**
     * Add tempfilter values
     */
    public void addTempFilterValues(final String Id, final String Value)
    {
    	ContentValues 	values = new ContentValues();
						values.put("Id", 	Id);
						values.put("Value",	Value);
    	db.replace("`TempFilterValues`", null, values);
    }
    
    /**
     * Get filters
     */
    public Cursor getFilters(String parent){
    	return db.rawQuery("SELECT * FROM `Filters` WHERE Parent='"+parent+"' ORDER BY ABS(Num) ASC", null);
    }
    
    /**
     * Get filter values
     */
    public Cursor getFilterValues(String id){
    	return db.rawQuery("SELECT * FROM `FilterValues` WHERE Id='"+id+"' ORDER BY ABS(Num) ASC", null);
    }
    
    /**
     * Get temporary filter value
     */
    public Cursor getTempFilterValue(String id){
    	return db.rawQuery("SELECT * FROM `TempFilterValues` WHERE Id='"+id+"'", null);
    }
    
    /**
     * Get temporary filters
     */
    public Cursor getTempFilters(){
    	return db.rawQuery("SELECT * FROM `TempFilterValues`", null);
    }
    
    /**
     * Clear filter values 
     */
    public void clearFilterValues(){
    	//TODO return boolean
    	try{
    		db.delete("`TempFilterValues`", null, null);
    	}catch(Exception e){}
    }
    
    
    
   
    
    /**
     * Add item
     */
    public void addItem(boolean Search, final String Code, final String Comments, final String Name, final String Price, final String Grade, final String Preview, final String Parent)
    {
    	ContentValues 	values = new ContentValues();
						values.put("Code", 	 	Code);
						values.put("Comments",  Comments);
						values.put("City",   	getKey(Settings.KEY_DEF_CITY));
						values.put("Name",   	Name);
						values.put("Price",  	Price.replace(" ", ""));
						values.put("Grade",  	Grade);
						values.put("Preview",	Preview);
						values.put("Parent", 	Parent);
    	
		String table = "Items";
    	if(Search)
    		table = "ItemsSearch";				
		db.replace("`"+table+"`", null, values);
    }
    
    /**
     * Get items cursor
     */
    public Cursor getItems(String parent, boolean Search){
    	Loger.d("DEBUG", "get items parent = "+parent);
    	String sortBy = "Price";
    	String sortByA = "ASC";
    	if(getKey(Settings.KEY_SORTBY).equals("Price"))
    		sortBy = "Price";
		else if(getKey(Settings.KEY_SORTBY).equals("Name"))
			sortBy = "Name";
		else if(getKey(Settings.KEY_SORTBY).equals("Grade")){
			sortBy = "Grade";
			sortByA = "DESC";
		}
    	
    	String table = "Items";
    	if(Search)
    		table = "ItemsSearch";
    	
    	return db.rawQuery("SELECT * FROM `"+table+"` WHERE Parent='"+parent+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"' ORDER BY ABS("+sortBy+") "+sortByA, null);
    }
    
    /**
     * Get items count
     */
    public Integer getItemsCount(String parent){
    	Cursor cursor = db.rawQuery("SELECT * FROM `Items` WHERE Parent='"+parent+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
    	Integer result = cursor.getCount();
    	cursor.close();
    	return result;
    }
    
    
    /**
     * Get items cursor
     */
    public Cursor getItems(String parent, int LimitFrom, int Limit, boolean Search){
    	Loger.d("DEBUG", "get items parent = "+parent);
    	String sortBy = "Price";
    	String sortByA = "ASC";
    	if(getKey(Settings.KEY_SORTBY).equals("Price"))
    		sortBy = "Price";
		else if(getKey(Settings.KEY_SORTBY).equals("Name"))
			sortBy = "Name";
		else if(getKey(Settings.KEY_SORTBY).equals("Grade")){
			sortBy = "Grade";
			sortByA = "DESC";
		}
    	
    	String table = "Items";
    	if(Search)
    		table = "ItemsSearch";
    	Log.d("SQL", "SELECT * FROM `"+table+"` WHERE Parent='"+parent+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"' ORDER BY ABS("+sortBy+") "+sortByA+" LIMIT "+LimitFrom+","+Limit);
    	return db.rawQuery("SELECT * FROM `"+table+"` WHERE Parent='"+parent+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"' ORDER BY ABS("+sortBy+") "+sortByA+" LIMIT "+LimitFrom+","+Limit, null);
    }
    
    /**
     * Delete items
     */
    public void deleteItems(String parent, boolean Search){
    	String table = "Items";
    	if(Search)
    		table = "ItemsSearch";
    	db.delete("`"+table+"`", "Parent='" + parent + "' AND City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
    }
    
    /**
     * Check key
     */
    public boolean checkKey(String key){
    	SharedPreferences settings = activity.getPreferences(0);
        String res = settings.getString(key,"");
		if(!res.equals(""))
			return true;
		else
			return false;
    }
    
    /**
     * Get key
     * @return
     */
    public String getKey(String key){
    	SharedPreferences settings = activity.getPreferences(0);
        return settings.getString(key,"");
    }    
    
    public String getDBKey(String key){
    	String result = "";
    	Cursor cursor = db.rawQuery("SELECT * FROM `Settings` WHERE `Key` = '"+key+"'", null);
		int indexValue = cursor.getColumnIndex("Value");
		try{
			cursor.moveToFirst();
			if(null!=cursor.getString(indexValue))
				result = cursor.getString(indexValue);
		}catch(Exception e){}
		cursor.close();
    	return result;
    }
    
    /**
     * Set key
     * @return
     */
    public void setKey(String key, String value){
    	SharedPreferences settings = activity.getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    	/*ContentValues 	values = new ContentValues();
						values.put("Key", 	key);
						values.put("Value", value);
    	db.replace("`Settings`", null, values);*/
    }
    
    /**
     * Add item
     */
    public void addItem(final String Image,
    					final String Name,
						final String Code,
						final String Price,
						final String Description,
						final String Availability,
						final String Features,
						final String Reviews){
    	ContentValues 	values = new ContentValues();
						values.put("Code", 			Code);
						values.put("City",  		getKey(Settings.KEY_DEF_CITY));
						values.put("Name",  		Name);
						values.put("Price", 		Price.replace(" ", ""));
						values.put("Image",  		Image);
						values.put("Description",  	Description);
						values.put("Availability",  Availability);
						values.put("Features",  	Features);
						values.put("Reviews",  		Reviews);
    	long res = db.replace("`Item`", null, values);
    	Loger.d("DEBUG", "add item in db Code="+Code+" res = "+res);
    }
    
    /**
     * Get item by code
     */
    public Cursor getItem(String code){
    	return db.rawQuery("SELECT * FROM `Item` WHERE Code='"+code+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
    }
    
    /**
     * Add fav
     */
    public void addFav( final String Image,
    					final String Name,
						final String Code,
						final String Price,
						final String Description,
						final String Availability,
						final String Features,
						final String Reviews){
    	ContentValues 	values = new ContentValues();
						values.put("Code", 			Code);
						values.put("City",  		getKey(Settings.KEY_DEF_CITY));
						values.put("Name",  		Name);
						values.put("Price", 		Price.replace(" ", ""));
						values.put("Count",  		1);
						values.put("Image",  		Image);
						values.put("Description",  	Description);
						values.put("Availability",  Availability);
						values.put("Features",  	Features);
						values.put("Reviews",  		Reviews);
    	db.replace("`Fav`", null, values);
    }
    
    /**
     * Update fav
     */
    public void updateFavCount(String code, Integer count){
    	ContentValues 	values = new ContentValues();
						values.put("Count", 	count);
    	db.update("`Fav`", values, "Code='"+code+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
    }
    
    /**
     * Set fav comment
     */
    public void setFavComment(String code, String text){
    	ContentValues 	values = new ContentValues();
						values.put("UserComment", 	text);
    	db.update("`Fav`", values, "Code='"+code+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
    }
    
    /**
     * Get fav coomment
     */
    public String getFavComment(String code){
    	String result = "";
    	Cursor cursor = db.rawQuery("SELECT * FROM `Fav` WHERE Code='"+code+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
		if(cursor.getCount()>0){
	    	cursor.moveToFirst();
	    	result = cursor.getString(cursor.getColumnIndex("UserComment"));
		}
		cursor.close();
    	return result;
    }
    
    /**
     * Delete fav
     */
    public void deleteFav(String code){
    	db.delete("`Fav`", "Code='" + code + "' AND City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
    }
    
    /**
     * Delete fav
     */
    public void deleteFavs(){
    	db.delete("`Fav`", "City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
    }
    
    /**
     * Get fav summ
     */
    public float getFavSumm(){
    	float result = 0.0f;
    	Cursor cursor = db.rawQuery("SELECT * FROM `Fav` WHERE City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
		int indexCount = cursor.getColumnIndex("Count");
		int indexPrice = cursor.getColumnIndex("Price");
		cursor.moveToFirst();
		for(int i=0;i<cursor.getCount();i++){
			float count = cursor.getFloat(indexCount);
			float price = 0.0f;
			try{
				price = Float.valueOf(cursor.getString(indexPrice).replace(" ", ""));
			}catch(Exception e){}
			result += (count*price);
			cursor.moveToNext();
		}
		cursor.close();
    	return result;
    }
    
    /**
     * Get fav count
     */
    public int getFavCount(String code){
    	int result = 0;
    	Cursor cursor = db.rawQuery("SELECT * FROM `Fav` WHERE Code='"+code+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
		if(cursor.getCount()>0){
	    	int indexCount = cursor.getColumnIndex("Count");
			cursor.moveToFirst();
			result = cursor.getInt(indexCount);
		}
		cursor.close();
    	return result;
    }
    
    /**
     * Is in fav
     */
    public boolean isInFav(String code){
    	boolean result = false;
    	Cursor cursor = db.rawQuery("SELECT * FROM `Fav` WHERE Code='"+code+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
		if(cursor.getCount()>0)
			result = true;
		cursor.close();
    	return result;
    }
    
    /**
     * Get favs
     */
    public Cursor getFavs(){
    	return db.rawQuery("SELECT * FROM `Fav` WHERE City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
    }
    
    /**
     * Get fav by code
     */
    public Cursor getFav(String code){
    	return db.rawQuery("SELECT * FROM `Fav` WHERE Code='"+code+"' AND City='"+getKey(Settings.KEY_DEF_CITY)+"'", null);
    }
    
    
    /**
     * Get cityes cursor
     */
    public Cursor getCityes(){
    	return db.rawQuery("SELECT * FROM `City` ORDER BY `Name`", null);
    }
    
    /**
     * Get cityes cursor
     */
    public Cursor getCityesByKeyword(String keyword){
    	try{
	    	char[] stringArray = keyword.toCharArray();
	    	stringArray[0] = Character.toUpperCase(stringArray[0]);
	    	keyword = new String(stringArray);
    	}catch(Exception e){}
    	return db.rawQuery("SELECT * FROM `City` WHERE lower(Name) LIKE '%"+keyword+"%' ORDER BY `Name`", null);
    }
    
    /**
     * Get cityes cursor
     */
    @SuppressLint("UseValueOf")
	public Cursor getCityesGPS(double longitude, double latitude){
    	Float maxLong = new Float(longitude)+2;
    	Float minLong = new Float(longitude)-2;
    	Float maxLat = new Float(latitude)+2;
    	Float minLat = new Float(latitude)-2;
    	return db.rawQuery("SELECT * FROM `City` WHERE (Longitude>='"+minLong+"' AND Longitude<='"+maxLong+"') AND (Latitude>='"+minLat+"' AND Latitude<='"+maxLat+"') ORDER BY `Name`", null);
    }
    
    /**
     * Get city name and phone
     */
    public Map<String, String> getCityNameAndPhone(String id){
    	Map<String, String> result = new HashMap<String, String>();
    	try{
	    	Cursor cursor = db.rawQuery("SELECT * FROM `City` WHERE `Id`='"+id+"'", null);
	    	cursor.moveToFirst();
	    	
	    	int indexName 	= cursor.getColumnIndex("Name");
	    	int indexPhone 	= cursor.getColumnIndex("Phone");
	    	
	    	result.put("Name", cursor.getString(indexName));
	    	result.put("Phone", cursor.getString(indexPhone));
	    	cursor.close();
    	}catch(Exception e){
    		
    	}
    	return result;
    }
    
    /**
     * Get category cursor
     */
    public Cursor getCategory(String parent){
    	return db.rawQuery("SELECT * FROM `Category` WHERE `Parent`='"+parent+"' ORDER BY `Name`", null);
    }
    
    /**
     * Get category name by Id
     */
    public String getCategoryName(String id){
    	Cursor cursor = db.rawQuery("SELECT * FROM `Category` WHERE `Id`='"+id+"' LIMIT 0,1", null);
    	cursor.moveToFirst();
    	int indexName = cursor.getColumnIndex("Name");
    	String name = cursor.getString(indexName);
    	cursor.close();
    	return name;
    }
    
    /**
     * Get application resource
     * @param rId
     * @return
     */
    public String getResourse(int rId){
		return context.getResources().getString(rId);
	}

	public Cursor rawQuery(String string) {
		return db.rawQuery(string, null);
	}
}
