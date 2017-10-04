package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockPlotActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mSymbol;
    private static final int STOCK_HISTORY_LOADER = 42;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    private LineDataSet mDataSet;
    private LineData mLineData;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.stock_price_chart)
    LineChart stock_price_chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_plot);
        ButterKnife.bind(this);

        Intent newIntent = getIntent();
        if (newIntent != null && newIntent.hasExtra(MainActivity.SYMBOL_PASSED_INTENT))
            mSymbol = newIntent.getStringExtra(MainActivity.SYMBOL_PASSED_INTENT);

        initializeChart();
        getSupportLoaderManager().initLoader(STOCK_HISTORY_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(mSymbol),
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mLineData.removeDataSet(mDataSet);
        mDataSet.clear();
        stock_price_chart.clear();

        if (data == null || data.getCount() == 0) {
            mLineData.addDataSet(null);
            return;
        }

        data.moveToFirst();
        String[] history = data.getString(Contract.Quote.POSITION_HISTORY).split("\n");

        for (int i = history.length - 1; i > 0; i--) {
            String[] historyData = history[i].split(",");
            float date = Float.valueOf(historyData[0]);
            float price = Float.valueOf(historyData[1]);

            mDataSet.addEntry(new Entry(date, price));
        }

        mLineData.addDataSet(mDataSet);
        stock_price_chart.setData(mLineData);
        stock_price_chart.invalidate();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private void initializeChart() {
        mDataSet = new LineDataSet(null, mSymbol);
        mDataSet.setHighlightEnabled(true);
        mDataSet.setDrawHighlightIndicators(true);
        mDataSet.setHighLightColor(ContextCompat.getColor(this, R.color.chart_highlight));
        mDataSet.setColor(ContextCompat.getColor(this, R.color.chart_line));
        mDataSet.setCircleColor(ContextCompat.getColor(this, R.color.chart_circle));

        mLineData = new LineData(mDataSet);
        mLineData.setValueTextColor(ContextCompat.getColor(this, R.color.chart_line_values));
        mLineData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return Float.toString(value) + "$";
            }
        });

        stock_price_chart.setKeepPositionOnRotation(true);
        stock_price_chart.setNoDataText(getString(R.string.error_no_chart_data));
        stock_price_chart.setNoDataTextColor(ContextCompat.getColor(this, R.color.chart_no_data));
        stock_price_chart.getLegend().setTextColor(ContextCompat.getColor(this, R.color.chart_label));

        XAxis xAxis = stock_price_chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(DAY_IN_MILLIS);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.chart_x_axis_values));

        TypedValue xAxisSizeValue = new TypedValue();
        getResources().getValue(R.dimen.chart_x_axis_text_size, xAxisSizeValue, true);
        xAxis.setTextSize(xAxisSizeValue.getFloat());

        TypedValue rotationValue = new TypedValue();
        getResources().getValue(R.dimen.chart_x_axis_label_rotation, rotationValue, true);
        xAxis.setLabelRotationAngle(rotationValue.getFloat());

        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                Date resultdate = new Date((long) value);
                return dateFormat.format(resultdate);
            }
        });

        YAxis yAxisRight = stock_price_chart.getAxisRight();
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawAxisLine(false);

        YAxis yAxisLeft = stock_price_chart.getAxisLeft();

        TypedValue yAxisSizeValue = new TypedValue();
        getResources().getValue(R.dimen.chart_y_axis_text_size, yAxisSizeValue, true);
        yAxisLeft.setTextSize(xAxisSizeValue.getFloat());

        yAxisLeft.setTextColor(ContextCompat.getColor(this, R.color.chart_x_axis_values));
        yAxisLeft.setDrawAxisLine(true);
    }
}
