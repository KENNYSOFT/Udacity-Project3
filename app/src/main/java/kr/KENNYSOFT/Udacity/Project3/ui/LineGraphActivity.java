package kr.KENNYSOFT.Udacity.Project3.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.KENNYSOFT.Udacity.Project3.R;

public class LineGraphActivity extends AppCompatActivity
{
	String symbol;
	CandleStickChart mChart;
	GraphTask graphTask;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_line_graph);

		symbol=getIntent().getStringExtra("symbol").toUpperCase();
		getSupportActionBar().setTitle(symbol);

		mChart=(CandleStickChart)findViewById(R.id.chart);
		mChart.setMarkerView(new StockMarkerView(this,R.layout.marker));
		mChart.setBackgroundColor(Color.WHITE);
		mChart.setDescription("");
		mChart.setMaxVisibleValueCount(60);
		mChart.setPinchZoom(false);
		mChart.setDrawGridBackground(false);
		XAxis xAxis=mChart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setLabelRotationAngle(90);
		xAxis.setDrawGridLines(true);
		xAxis.setValueFormatter(new AxisValueFormatter()
		{
			@Override
			public String getFormattedValue(float value,AxisBase axis)
			{
				try
				{
					return (String)mChart.getCandleData().getDataSetByIndex(0).getEntryForIndex((int)value).getData();
				}
				catch(Exception e)
				{
					return "";
				}
			}

			@Override
			public int getDecimalDigits()
			{
				return -1;
			}
		});
		YAxis leftAxis=mChart.getAxisLeft();
		leftAxis.setLabelCount(7,false);
		leftAxis.setDrawGridLines(true);
		leftAxis.setDrawAxisLine(false);
		YAxis rightAxis=mChart.getAxisRight();
		rightAxis.setEnabled(false);
		mChart.getLegend().setEnabled(false);

		graphTask=new GraphTask(this,mChart);
		graphTask.execute(symbol);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(graphTask!=null)graphTask.cancel(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}
}

class GraphTask extends AsyncTask<String,Void,Void>
{
	Context context;
	CandleStickChart chart;
	CandleDataSet dataSet;

	GraphTask(Context context,CandleStickChart chart)
	{
		this.context=context;
		this.chart=chart;
	}

	@Override
	protected void onPreExecute()
	{
		dataSet=new CandleDataSet(new ArrayList<CandleEntry>(),"dataSet");
		dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		dataSet.setShadowColor(Color.DKGRAY);
		dataSet.setShadowWidth(0.7f);
		dataSet.setDecreasingColor(Color.RED);
		dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
		dataSet.setIncreasingColor(Color.BLUE);
		dataSet.setIncreasingPaintStyle(Paint.Style.FILL);
		dataSet.setNeutralColor(Color.GREEN);
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(String... symbols)
	{
		try
		{
			int cnt=0;
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
			JSONObject stock=new JSONObject(URLToString("http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.stocks%20where%20symbol%20IN%20(%22"+symbols[0]+"%22)&format=json&env=http://datatables.org/alltables.env")).getJSONObject("query").getJSONObject("results").getJSONObject("stock");
			Date end=format.parse(stock.getString("end")),startDate=format.parse(stock.getString("start")),endDate;
			while(true)
			{
				if(isCancelled())break;
				Calendar calendar=Calendar.getInstance();
				calendar.setTime(startDate);
				calendar.add(Calendar.YEAR,1);
				calendar.add(Calendar.DAY_OF_YEAR,-1);
				endDate=calendar.getTime();
				JSONArray array=new JSONObject(URLToString("http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20IN%20(%22"+symbols[0]+"%22)%20and%20startDate%20=%20%22"+format.format(startDate)+"%22%20and%20endDate%20=%20%22"+format.format(endDate)+"%22&format=json&env=http://datatables.org/alltables.env")).getJSONObject("query").getJSONObject("results").getJSONArray("quote");
				for(int i=array.length()-1;i>=0;--i)
				{
					JSONObject info=array.getJSONObject(i);
					dataSet.addEntry(new CandleEntry(++cnt,Float.parseFloat(info.getString("High")),Float.parseFloat(info.getString("Low")),Float.parseFloat(info.getString("Open")),Float.parseFloat(info.getString("Close")),info.getString("Date")));
				}
				((Activity)context).runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{

						chart.setData(new CandleData(dataSet));
						chart.invalidate();
					}
				});
				calendar.add(Calendar.DAY_OF_YEAR,1);
				startDate=calendar.getTime();
				if(startDate.after(end))break;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	String URLToString(String url)
	{
		String html="";
		try
		{
			URLConnection connection=new URL(url).openConnection();
			InputStream is=connection.getInputStream();
			BufferedReader in=new BufferedReader(new InputStreamReader(is));
			String line;
			while((line=in.readLine())!=null)html=html+line+"\n";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return html;
	}
}

class StockMarkerView extends MarkerView
{
	Context context;
	TextView tvContent;

	StockMarkerView(Context context,int layoutResource)
	{
		super(context,layoutResource);
		this.context=context;
		tvContent=(TextView)findViewById(R.id.tvContent);
	}

	@Override
	public void refreshContent(Entry e,Highlight highlight)
	{
		CandleEntry entry=(CandleEntry)e;
		tvContent.setText(String.format(Locale.getDefault(),context.getString(R.string.marker),entry.getData(),entry.getHigh(),entry.getLow(),entry.getOpen(),entry.getClose()));
	}

	@Override
	public int getXOffset(float xpos)
	{
		return -getWidth()/2;
	}

	@Override
	public int getYOffset(float ypos)
	{
		return 0;
	}
}