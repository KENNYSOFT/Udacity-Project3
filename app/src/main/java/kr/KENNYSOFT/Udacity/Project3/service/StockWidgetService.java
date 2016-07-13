package kr.KENNYSOFT.Udacity.Project3.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import kr.KENNYSOFT.Udacity.Project3.R;
import kr.KENNYSOFT.Udacity.Project3.data.QuoteColumns;
import kr.KENNYSOFT.Udacity.Project3.data.QuoteProvider;
import kr.KENNYSOFT.Udacity.Project3.rest.Utils;

public class StockWidgetService extends RemoteViewsService
{
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		return new StockRemoteViewsFactory(this.getApplicationContext(),intent);
	}
}

class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory, Loader.OnLoadCompleteListener<Cursor>
{
	static final int CURSOR_LOADER_ID=0;
	Context mContext;
	int mAppWidgetId;
	CursorLoader mCursorLoader;
	Cursor mCursor;
	boolean dataIsValid;
	int rowIdColumn;

	StockRemoteViewsFactory(Context context,Intent intent)
	{
		mContext=context;
		mAppWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	@Override
	public void onCreate()
	{
		mCursorLoader=new CursorLoader(mContext,QuoteProvider.Quotes.CONTENT_URI,new String[]{QuoteColumns._ID,QuoteColumns.SYMBOL,QuoteColumns.BIDPRICE,QuoteColumns.PERCENT_CHANGE,QuoteColumns.CHANGE,QuoteColumns.ISUP},QuoteColumns.ISCURRENT+"=?",new String[]{"1"},null);
		mCursorLoader.registerListener(CURSOR_LOADER_ID,this);
		mCursorLoader.startLoading();
	}

	@Override
	public void onDestroy()
	{
		if(mCursorLoader!=null)
		{
			mCursorLoader.unregisterListener(this);
			mCursorLoader.cancelLoad();
			mCursorLoader.stopLoading();
		}
		if(mCursor!=null)mCursor.close();
	}

	@Override
	public int getCount()
	{
		if(dataIsValid&&mCursor!=null)return mCursor.getCount();
		else return 0;
	}

	@Override
	public RemoteViews getViewAt(int position)
	{
		RemoteViews rv=new RemoteViews(mContext.getPackageName(),R.layout.list_item_quote);
		rv.setContentDescription(R.id.list_item_quote,String.format(mContext.getString(R.string.desc_select),mCursor.getString(mCursor.getColumnIndex("symbol"))));
		mCursor.moveToPosition(position);
		rv.setTextViewText(R.id.stock_symbol,mCursor.getString(mCursor.getColumnIndex("symbol")));
		rv.setTextViewText(R.id.bid_price,mCursor.getString(mCursor.getColumnIndex("bid_price")));
		if(mCursor.getInt(mCursor.getColumnIndex("is_up"))==1)rv.setInt(R.id.change,"setBackgroundResource",R.drawable.percent_change_pill_green);
		else rv.setInt(R.id.change,"setBackgroundResource",R.drawable.percent_change_pill_red);
		if(Utils.showPercent)rv.setTextViewText(R.id.change,mCursor.getString(mCursor.getColumnIndex("percent_change")));
		else rv.setTextViewText(R.id.change,mCursor.getString(mCursor.getColumnIndex("change")));
		Bundle extras=new Bundle();
		extras.putString("symbol",mCursor.getString(mCursor.getColumnIndex("symbol")));
		Intent fillInIntent=new Intent();
		fillInIntent.putExtras(extras);
		rv.setOnClickFillInIntent(R.id.list_item_quote,fillInIntent);
		return rv;
	}

	@Override
	public RemoteViews getLoadingView()
	{
		return null;
	}

	@Override
	public int getViewTypeCount()
	{
		return 1;
	}

	@Override
	public long getItemId(int position)
	{
		if(dataIsValid&&mCursor!=null&&mCursor.moveToPosition(position))return mCursor.getLong(rowIdColumn);
		return 0;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@Override
	public void onDataSetChanged()
	{
	}

	@Override
	public void onLoadComplete(Loader<Cursor> loader,Cursor data)
	{
		mCursor=data;
		dataIsValid=mCursor!=null;
		rowIdColumn=dataIsValid?mCursor.getColumnIndex("_id"):-1;
	}
}