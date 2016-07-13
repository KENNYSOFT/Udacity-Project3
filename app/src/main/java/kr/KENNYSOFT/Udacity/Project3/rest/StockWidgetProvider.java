package kr.KENNYSOFT.Udacity.Project3.rest;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import kr.KENNYSOFT.Udacity.Project3.R;
import kr.KENNYSOFT.Udacity.Project3.service.StockWidgetService;
import kr.KENNYSOFT.Udacity.Project3.ui.LineGraphActivity;

public class StockWidgetProvider extends AppWidgetProvider
{
	public static final String ACTION_REFRESH="kr.KENNYSOFT.Udacity.Project3.action.REFRESH";

	@Override
	public void onUpdate(Context context,AppWidgetManager appWidgetManager,int[] appWidgetIds)
	{
		update(context,appWidgetManager,appWidgetIds);
		super.onUpdate(context,appWidgetManager,appWidgetIds);
	}

	@Override
	public void onReceive(Context context,Intent intent)
	{
		super.onReceive(context,intent);
		String action=intent.getAction();
		if(AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action))
		{
			AppWidgetManager manager=AppWidgetManager.getInstance(context);
			update(context,manager,manager.getAppWidgetIds(new ComponentName(context,getClass())));
		}
		else if(ACTION_REFRESH.equals(action))
		{
			AppWidgetManager manager=AppWidgetManager.getInstance(context);
			update(context,manager,manager.getAppWidgetIds(new ComponentName(context,getClass())));
		}
	}

	void update(Context context,AppWidgetManager appWidgetManager,int[] appWidgetIds)
	{
		for(int appWidgetId : appWidgetIds)
		{
			Intent intent=new Intent(context,StockWidgetService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			RemoteViews rv=new RemoteViews(context.getPackageName(),R.layout.widget);
			rv.setOnClickPendingIntent(R.id.widget_refresh,PendingIntent.getBroadcast(context,0,new Intent(ACTION_REFRESH),0));
			rv.setRemoteAdapter(R.id.widget_list_view,intent);
			rv.setEmptyView(R.id.widget_list_view,R.id.widget_empty_view);
			rv.setPendingIntentTemplate(R.id.widget_list_view,PendingIntent.getActivity(context,0,new Intent(context,LineGraphActivity.class),0));
			appWidgetManager.updateAppWidget(appWidgetId,rv);
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.widget_list_view);
		}
	}
}