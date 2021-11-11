package chart;

import chart.dto.ChartDto;
import command.dto.ResultDto;
import org.apache.commons.math3.linear.RealVector;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChartHelper {

    private static final ChartHelper INSTANCE = new ChartHelper();

    private JFrame previousChart;

    private ChartHelper() {
    }

    public static ChartHelper getInstance() {
        return INSTANCE;
    }

    public void showNextChart(ResultDto result, double T) {
        if (previousChart != null) {
            previousChart.dispose();
        }
        XYChart chart = getChart();

        ChartDto optimalDto = getChartDto(result.getOptimalY(), T);
        XYSeries optimalSeries = chart.addSeries("Optimal", optimalDto.getXData(), optimalDto.getYData());
        optimalSeries.setMarker(SeriesMarkers.NONE);
        optimalSeries.setLineColor(Color.GREEN);

        ChartDto nonOptimalDto = getChartDto(result.getNonOptimalY(), T);
        XYSeries nonOptimalSeries = chart.addSeries("Not optimal", nonOptimalDto.getXData(), nonOptimalDto.getYData());
        nonOptimalSeries.setMarker(SeriesMarkers.NONE);
        nonOptimalSeries.setLineColor(Color.RED);

        this.previousChart = new SwingWrapper<>(chart).displayChart();
        this.previousChart.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private XYChart getChart() {
        XYChart chart = new XYChart(1600, 900);
        chart.setTitle("Chart");
        chart.setXAxisTitle("t");
        chart.setYAxisTitle("y(t)");
        return chart;
    }

    private ChartDto getChartDto(List<RealVector> sequenceY, double T) {
        int factor = 1000;
        int step = Math.max(sequenceY.size() / factor, 1);
        List<Double> yData = new ArrayList<>(factor);
        List<Double> xData = new ArrayList<>(factor);
        for (int i = 0; i < sequenceY.size(); i += step) {
            RealVector vector = sequenceY.get(i);
            if (vector.getDimension() == 1) {
                yData.add(vector.getEntry(0));
                xData.add(i * T);
            }
        }
        return new ChartDto(xData, yData);
    }
}