package com.timetracker.ui.activities;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.androidplot.xy.*;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.timetracker.R;
import com.timetracker.domain.Task;
import com.timetracker.domain.TaskSwitchEvent;
import com.timetracker.domain.persistance.DatabaseHelper;
import com.timetracker.util.EmptyListAdapter;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;

/**
 * Created by Anton Chernetskij
 */
public class ComparisonReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    private static final int BAR_HEIGHT = 90;
    private final ReportGenerator reportGenerator = new ReportGenerator();

    private List<AggregationPeriod> aggregationPeriods;
    private XYPlot plot;
    private BarFormatter formatter1;
    private BarFormatter formatter2;


    private List<ReportGenerator.AggregatedTaskItem> currentPeriodReport;
    private List<ReportGenerator.AggregatedTaskItem> previousPeriodReport;


    /**
     * The chart view that displays the data.
     */
    private GraphicalView chartView;
    private XYMultipleSeriesDataset dataset;
    private XYMultipleSeriesRenderer seriesRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comparison_report);

        initSlider();
        initPlot();
//        initEnotherPlot();
        updatePlot(aggregationPeriods.get(0));
    }

    private void initSlider() {
        aggregationPeriods = new ArrayList<>();
        String[] labels = getResources().getStringArray(R.array.intervalChooserLabels);
        aggregationPeriods.add(new AggregationPeriod(labels[0], 1));
        aggregationPeriods.add(new AggregationPeriod(labels[1], 2));
        aggregationPeriods.add(new AggregationPeriod(labels[2], 3));
        aggregationPeriods.add(new AggregationPeriod(labels[3], 7));
        aggregationPeriods.add(new AggregationPeriod(labels[4], 30));
        aggregationPeriods.add(new AggregationPeriod(labels[5], 365));

        SeekBar intervalChooser = (SeekBar) findViewById(R.id.reportIntervalChooser);
        intervalChooser.setMax(aggregationPeriods.size() - 1);
        int aggregationIndex = intervalChooser.getProgress();
        updateAggregateLabel(aggregationIndex);
        intervalChooser.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateAggregateLabel(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        intervalChooser.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        ListView tasksList = (ListView) findViewById(R.id.tasks);
//                        tasksList.setAdapter(new EmptyListAdapter());
                        //todo add some kind of progressbar
                        break;
                    case MotionEvent.ACTION_UP:
                        SeekBar intervalChooser = (SeekBar) findViewById(R.id.reportIntervalChooser);
                        int aggregationIndex = intervalChooser.getProgress();
                        updatePlot(aggregationPeriods.get(aggregationIndex));
                        break;
                }
                return false;
            }
        });
    }

    public void updateAggregateLabel(int n) {
        TextView chooserLabel = (TextView) findViewById(R.id.intervalChooserLabel);
        chooserLabel.setText(aggregationPeriods.get(n).label);
    }

    private class AggregationPeriod {

        int days;
        String label;

        private AggregationPeriod(String label, int days) {
            this.label = label;
            this.days = days;
        }

    }

    private void initPlot() {

        formatter1 = new BarFormatter(Color.argb(200, 100, 150, 100), Color.LTGRAY);

        formatter2 = new BarFormatter(Color.argb(200, 100, 100, 150), Color.LTGRAY);

        plot = (XYPlot) findViewById(R.id.comparisionBarChart);

        plot.setTicksPerRangeLabel(1);
        plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
        plot.getGraphWidget().setGridPadding(30, 10, 30, 0);

        plot.setTicksPerDomainLabel(2);


        plot.setDomainValueFormat(new NumberFormat() {
            @Override
            public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {
                if (value >= 0 && value < currentPeriodReport.size()) {
                    return new StringBuffer(currentPeriodReport.get((int) value).task.name);
                } else {
                    return new StringBuffer();
                }
            }

            @Override
            public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Number parse(String string, ParsePosition position) {
                throw new UnsupportedOperationException();
            }
        });

//        plot.setRangeValueFormat(new NumberFormat() {
//            @Override
//            public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {
//                int hours = (int) (value / 60);
//                int minutes = (int) (value % 60);
//                StringBuffer buf = new StringBuffer();
//                buf.append(hours).append(":").append(minutes);
//                return buf;
//            }
//
//            @Override
//            public Number parse(String string, ParsePosition position) {
//                throw new UnsupportedOperationException();
//            }
//        });


    }

    private void updatePlot(AggregationPeriod period) {
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DATE, -(period.days - 1));

        Date timeFrom = from.getTime();
        currentPeriodReport = reportGenerator.generateReport(timeFrom, new Date(), this);

        from.add(Calendar.DATE, -period.days);
        previousPeriodReport = reportGenerator.generateReport(from.getTime(), timeFrom, this);

        long[] previousPeriodTime = getCorrespondingTime(currentPeriodReport, previousPeriodReport);
        displayChart1(currentPeriodReport, previousPeriodTime, period.label);
    }

    private long[] getCorrespondingTime(List<ReportGenerator.AggregatedTaskItem> currentPeriodReport,
                                        List<ReportGenerator.AggregatedTaskItem> previousPeriodReport) {
        long[] result = new long[currentPeriodReport.size()];
        for (int i = 0; i < currentPeriodReport.size(); i++) {
            ReportGenerator.AggregatedTaskItem item = currentPeriodReport.get(i);
            for (ReportGenerator.AggregatedTaskItem correspondingItem : previousPeriodReport) {
                if (item.task.id == correspondingItem.task.id) {
                    result[i] = correspondingItem.duration;
                    break;
                }
            }
        }
        return result;
    }

    private void displayChart(final List<ReportGenerator.AggregatedTaskItem> currentPeriodReport,
                              final long[] previousPeriodDurations, String periodLabel) {
        ListView tasksList = (ListView) findViewById(/*R.id.tasks*/0);
        final long maxDuration = Math.max(currentPeriodReport.get(0).duration, previousPeriodDurations[0]);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        final float width = size.x;

        tasksList.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return currentPeriodReport.size();
            }

            @Override
            public Object getItem(int position) {
                return currentPeriodReport.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Button view = new Button(ComparisonReportActivity.this);
                view.setGravity(Gravity.LEFT);
                view.setHeight(BAR_HEIGHT);
                view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, BAR_HEIGHT));

                ReportGenerator.AggregatedTaskItem currentPeriodItem = currentPeriodReport.get(position);
                view.setText(currentPeriodItem.task.name + " " +
                        buildTime(currentPeriodItem.duration) + "/" +
                        buildTime(previousPeriodDurations[position]));
                BitmapDrawable drawable = getBackgroundBar((int) width, BAR_HEIGHT, (double) currentPeriodItem.duration / maxDuration,
                        (double) previousPeriodDurations[position] / maxDuration, currentPeriodItem.task.color);
                view.setBackground(drawable);

                return view;
            }
        });
    }

    private BitmapDrawable getBackgroundBar(int width, int height, double currentAmount, double previousAmount, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(color);
        p.setAlpha(Task.DEFAULT_COLOR_ALPHA);
        canvas.drawRect(0, 0, (float) (currentAmount * width), height, p);

        float x = (float) (previousAmount * width);
        Paint p2 = new Paint();
        p2.setColor(Color.WHITE);
        p2.setAlpha(Task.DEFAULT_COLOR_ALPHA);
        p2.setStrokeWidth(4);

        canvas.drawLine(x, 0, x, height, p2);

        return new BitmapDrawable(getResources(), bitmap);
    }

    private String buildTime(long time) {
        time /= 1000;
        long s = time % 60;
        time /= 60;
        long m = time % 60;
        time /= 60;
        long h = time;
        return String.format("%d:%02d", h, m);
    }

    private void displayChart1(List<ReportGenerator.AggregatedTaskItem> currentPeriodReport,
                               long[] previousPeriodReport, String periodLabel) {

        for (XYSeries setElement : plot.getSeriesSet()) {
            plot.removeSeries(setElement);
        }

        Number[] values1 = getSeriesData(currentPeriodReport);
        XYSeries series1 = new SimpleXYSeries(Arrays.asList(values1), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "This " + periodLabel);

        Number[] values2 = getSeriesData(previousPeriodReport);
        XYSeries series2 = new SimpleXYSeries(Arrays.asList(values2), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Previous " + periodLabel);

        plot.addSeries(series1, formatter1);
        plot.addSeries(series2, formatter2);

        BarRenderer renderer = (BarRenderer) plot.getRenderer(BarRenderer.class);
        renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.SIDE_BY_SIDE);
        renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.FIXED_WIDTH);
        renderer.setBarWidth(60);
        renderer.setBarGap(10);

        plot.redraw();
    }

    private Number[] getSeriesData(List<ReportGenerator.AggregatedTaskItem> reportItems) {
        Number[] values = new Number[reportItems.size()];
        for (int i = 0; i < reportItems.size(); i++) {
            ReportGenerator.AggregatedTaskItem item = reportItems.get(i);
            values[i] = item.duration;
        }
        return values;
    }

    private Number[] getSeriesData(long[] previousPeriodReport) {
        Number[] result = new Number[previousPeriodReport.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = previousPeriodReport[i];
        }
        return new Number[0];
    }


    //-------------------


    private void initEnotherPlot() {
        seriesRenderer = new XYMultipleSeriesRenderer();

        seriesRenderer.setXTitle("Tasks");
        seriesRenderer.setYTitle("Time");
        seriesRenderer.setAxisTitleTextSize(40);

        seriesRenderer.setChartTitle("Comparision report");
        seriesRenderer.setChartTitleTextSize(60);
        seriesRenderer.setLabelsTextSize(40);
        seriesRenderer.setLegendTextSize(40);

        seriesRenderer.setBarSpacing(1);
        seriesRenderer.setBarWidth(20);

        seriesRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.VERTICAL);

        seriesRenderer.setApplyBackgroundColor(true);
        seriesRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
        seriesRenderer.setGridColor(Color.GRAY);
        seriesRenderer.setShowGrid(false);

        seriesRenderer.setMargins(new int[]{80, 80, 300, 30});
        seriesRenderer.setYLabelsAlign(Paint.Align.LEFT);
//        seriesRenderer.setXLabels(0);
//        seriesRenderer.setXAxisMin(0);
//        seriesRenderer.setYAxisMin(0);
        seriesRenderer.setZoomEnabled(false, true);
        seriesRenderer.setPanEnabled(true, true);
//        seriesRenderer.setXAxisMax(20);

        LinearLayout layout = (LinearLayout) findViewById(/*R.id.chart*/0);
        dataset = new XYMultipleSeriesDataset();
        chartView = ChartFactory.getBarChartView(this, dataset, seriesRenderer, BarChart.Type.DEFAULT);
        layout.addView(chartView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void displayChart2(List<ReportGenerator.AggregatedTaskItem> currentPeriodReport,
                               List<ReportGenerator.AggregatedTaskItem> previousPeriodReport, String periodLabel) {

        int count = dataset.getSeriesCount();
        for (int i = 0; i < count; i++) {
            dataset.removeSeries(0);
        }
        seriesRenderer.removeAllRenderers();

        seriesRenderer.setXAxisMax(currentPeriodReport.size());
        long duration = Math.max(currentPeriodReport.get(0).duration, previousPeriodReport.get(0).duration);
        seriesRenderer.setYAxisMax(convertDuration(duration) * 1.05);
        seriesRenderer.clearXTextLabels();
        seriesRenderer.addXTextLabel(0, "");
        for (int i = 0; i < currentPeriodReport.size(); i++) {
            ReportGenerator.AggregatedTaskItem item = currentPeriodReport.get(i);
            seriesRenderer.addXTextLabel(i + 1, item.task.name);
        }
        CategorySeries series1 = new CategorySeries("Current " + periodLabel);

        CategorySeries series2 = new CategorySeries("Previous " + periodLabel);

        for (int i = 0; i < 2; i++) {
            ReportGenerator.AggregatedTaskItem item1 = currentPeriodReport.get(i);
            series1.add(convertDuration(item1.duration));
            ReportGenerator.AggregatedTaskItem item2 = previousPeriodReport.get(i);
            series2.add(convertDuration(item2.duration));
        }

        dataset.addSeries(series1.toXYSeries());
        SimpleSeriesRenderer renderer1 = getSeriesRenderer();
        renderer1.setColor(Color.argb(200, 100, 150, 100));
        seriesRenderer.addSeriesRenderer(renderer1);

        dataset.addSeries(series2.toXYSeries());
        SimpleSeriesRenderer renderer2 = getSeriesRenderer();
        renderer2.setColor(Color.argb(200, 100, 100, 150));
        seriesRenderer.addSeriesRenderer(renderer2);

        chartView.repaint();
    }

    private double convertDuration(long duration) {
        return ((double) duration) / 1000 / 60;
    }

    private SimpleSeriesRenderer getSeriesRenderer() {
        SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
//        renderer.setStroke(BasicStroke.SOLID);
//        renderer.set
        return renderer;
    }
}
