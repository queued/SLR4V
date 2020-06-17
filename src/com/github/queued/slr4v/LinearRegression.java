package com.github.queued.slr4v;

import com.github.queued.slr4v.model.entity.Day;
import com.github.queued.slr4v.utils.DatasetParser;
import com.github.queued.slr4v.utils.Plot;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LinearRegression {
    public static List<Integer> x = new ArrayList<>();
    public static List<Integer> y = new ArrayList<>();
    public static List<Day> _days = new ArrayList<>();

    public static int predictedDiff;
    public static int predictedValue;
    public static double newPossibleInfections;

    private static Double predictForValue(int predictForDependentVariable) {
        if (x.size() != y.size())
            throw new IllegalStateException("Must have equal X and Y data points");

        int numberOfDataValues = x.size();

        List<Double> xSquared = x
                .stream()
                .map(position -> Math.pow(position, 2))
                .collect(Collectors.toList());

        List<Integer> xMultipliedByY = IntStream.range(0, numberOfDataValues)
                .map(i -> x.get(i) * y.get(i))
                .boxed()
                .collect(Collectors.toList());

        int xSummed = x
                .stream()
                .reduce(Integer::sum)
                .get();

        int ySummed = y
                .stream()
                .reduce(Integer::sum)
                .get();

        double sumOfXSquared = xSquared
                .stream()
                .reduce(Double::sum)
                .get();

        int sumOfXMultipliedByY = xMultipliedByY
                .stream()
                .reduce(Integer::sum)
                .get();

        int slopeNominator = numberOfDataValues * sumOfXMultipliedByY - ySummed * xSummed;
        double slopeDenominator = numberOfDataValues * sumOfXSquared - Math.pow(xSummed, 2);
        double slope = slopeNominator / slopeDenominator;

        double interceptNominator = ySummed - slope * xSummed;
        double intercept = interceptNominator / (double) numberOfDataValues;

        return (slope * predictForDependentVariable) + intercept;
    }

    private static void bootstrap(String cfgPath) throws IOException {
        Config.setPath(cfgPath);
        Config.getInstance();
        _days = DatasetParser.getArrayListFromCSV(cfgPath + "/datasets/viçosa.csv");

        for (Day day : _days) {
            x.add(day.getDayNumber());
            y.add(day.getConfirmedCases());
        }
    }

    public static int getTotalCases(int newCases)
    {
        return newCases + y.get(y.size() - 1);
    }

    public static int getActiveCases()
    {
        return _days.get(_days.size() - 1).getActiveCases();
    }

    public static int getClosest(List<Integer> sortedList, int key) {
        List<Long> newList = new ArrayList<>();
        for (Integer confirmed : sortedList) {
            newList.add(confirmed.longValue());
        }

        int index = Collections.binarySearch(newList, (long) key);
        Long closest;
        if (index >= 0) {
            closest = newList.get(index);
        } else {
            index = -index - 1;
            if (index == 0){
                closest = newList.get(index);
            } else if (index == sortedList.size()){
                closest = newList.get(index - 1);
            } else {
                Long prev = newList.get(index - 1);
                Long next = newList.get(index);
                closest = ((key - prev) < (next - key)) ? prev : next;
            }
        }

        return (int) Math.floor(closest);
    }

    private static List<Double> getPlotDayList()
    {
        ArrayList<Double> list = new ArrayList<>();

        for (Day day : _days) {
            list.add((double) day.getDayNumber());
        }

        return list;
    }

    private static List<Double> getPlotActiveCases()
    {
        ArrayList<Double> list = new ArrayList<>();

        for (Day day : _days) {
            list.add((double) day.getActiveCases());
        }

        return list;
    }

    private static List<Double> getPlotDayNewCases()
    {
        ArrayList<Double> list = new ArrayList<>();

        int lastNewCase = 0;
        for (int i = 0; i < _days.size(); i++) {
            Day day = _days.get(i);
            if (i >= 1) {
                lastNewCase = day.getConfirmedCases() - _days.get(i - 1).getConfirmedCases();
            }
            list.add((double) lastNewCase);
        }

        return list;
    }

    private static List<Double> getPlotDayTotalCases()
    {
        ArrayList<Double> list = new ArrayList<>();

        int lastNewCase = 0;
        for (Day day : _days) {
            lastNewCase = day.getConfirmedCases();
            list.add((double) lastNewCase);
        }

        return list;
    }

    public static void main(String[] args) throws IOException {
        bootstrap(args.length > 0 ? args[0] : ".");

        final int dayToPredict = x.size() + 1; // only works for "tomorrow" predictions
        predictedValue = predictForValue(dayToPredict).intValue();
        predictedDiff = y.get(y.size() - 1) - getClosest(y, predictedValue);
        newPossibleInfections = (predictedDiff - (predictedDiff * Config.ISOLATION_RATE)) / Config.TRANSMISSION_RATE;
        final int totalCases = getTotalCases(0);
        final int newTotalCases = getTotalCases((int) newPossibleInfections);
        BigDecimal diffDecimal = BigDecimal.valueOf(newPossibleInfections - Math.floor(newPossibleInfections));
        diffDecimal = diffDecimal.setScale(2, RoundingMode.HALF_UP);

        Calendar cal = Config.FIRST_CONFIRMED_CASE_DATE;
        cal.add(Calendar.DATE, dayToPredict - 4);
        Date date = cal.getTime();

        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = format1.format(date);

        double maxInfections = Math.min(
                // A
                Config.POPULATION,
                // B
                (Config.POPULATION - (Config.POPULATION * Config.ISOLATION_RATE)) / ((totalCases - getActiveCases()) * Config.TRANSMISSION_RATE)
        );
        double maxInfectionsPercentage = (maxInfections * 100) / Config.POPULATION;

        System.out.println("\nLinear Regression / Viçosa - MG");
        System.out.println("------------------------------------------");
        System.out.println("Total Population: " + Config.POPULATION);
        System.out.println("Max. Infections (" + (int) (Config.ISOLATION_RATE * 100) + "% Isolation Rate): " + (int) Math.floor(maxInfections)
                + " (" + String.format("%.02f", maxInfectionsPercentage) + "% of the total population)"
        );
        System.out.println("------------------------------------------");
        System.out.println(String.format("Error Difference: ~%d", (int) Math.exp(diffDecimal.intValue())));
        System.out.println("New Cases (on " + dateStr + "): +" + (int) newPossibleInfections);
        System.out.println("Total Cases (on " + dateStr + "): ~" + newTotalCases + " (" + String.format("%.02f", (double) newTotalCases * 100 / Config.POPULATION) + "% of the total population)");
        System.out.println("Total Cases (TODAY): " + totalCases + " (" + String.format("%.02f", (double) totalCases * 100 / Config.POPULATION) + "% of the total population)");
        System.out.println("Active Cases (TODAY): " + getActiveCases() + " (" + String.format("%.02f", (double) getActiveCases() * 100 / Config.POPULATION) + "% of the total population)");
        System.out.println("------------------------------------------");
        System.out.println("Formula: (Total Cases[" + totalCases + "] - Predicted[" + predictedValue
                + "]) * Isolation Rate[" + Config.ISOLATION_RATE + "] / Transmission Rate["
                + Config.TRANSMISSION_RATE + "]"
        );
        System.out.println("------------------------------------------");
        System.out.println("Generating graph...");

        /*
        // configuring everything by default
        Plot plot = Plot.plot(null);
        // setting data
        plot.series(null, Plot.data().xy(1, 2).xy(3, 4), null);

        // saving sample_minimal.png
        plot.save("sample_minimal", "png");
        */

        Plot plot = Plot.plot(Plot.plotOpts().
                title("COVID-19 Data for Viçosa (MG) - Prediction For " + dateStr).
                titleFont(new Font("Arial", Font.BOLD, 16)).
                width(1024).
                height(768).
                padding(50).
                labelFont(new Font("Arial", Font.PLAIN, 14)).
                labelPadding(20).
                legend(Plot.LegendFormat.BOTTOM)).
                xAxis("Day", Plot.axisOpts().
                        range(0, dayToPredict + 1). // 2 days ahead
                        format(Plot.AxisFormat.NUMBER_INT)).
                yAxis("Cases", Plot.axisOpts().
                        range(0, getTotalCases(5)).// 5 cases padding
                        format(Plot.AxisFormat.NUMBER_INT)
                );

        plot.series("Active Cases", Plot.data().
                        xy(0, 0).
                        xy(getPlotDayList(), getPlotActiveCases()).
                        xy(dayToPredict, getActiveCases() + (int) newPossibleInfections),
                Plot.seriesOpts().
                        line(Plot.Line.NONE).
                        lineWidth(3).
                        color(Color.ORANGE).
                        marker(Plot.Marker.CIRCLE).
                        markerColor(Color.ORANGE).
                        markerSize(12)
        );

        plot.series("New Cases", Plot.data().
                        xy(0, 0).
                        xy(getPlotDayList(), getPlotDayNewCases()).
                        xy(dayToPredict, (int) newPossibleInfections), // Adds the predicted new cases
                Plot.seriesOpts().
                        line(Plot.Line.NONE).
                        lineWidth(3).
                        color(Color.RED).
                        marker(Plot.Marker.CIRCLE).
                        markerColor(Color.RED).
                        markerSize(12)
        );

        plot.series("Total Cases", Plot.data().
                        xy(0, 0).
                        xy(getPlotDayList(), getPlotDayTotalCases()).
                        // Sums the new possible infections with the total cases
                        xy(dayToPredict, getTotalCases((int) newPossibleInfections)),
                Plot.seriesOpts().
                        line(Plot.Line.SOLID).
                        lineWidth(3).
                        color(Color.BLUE).
                        marker(Plot.Marker.NONE)
        );

        String currentPath = args.length > 0 ? args[0] : ".";
        String graphFile = currentPath + "/graphs/graph_" + dateStr.replace("/", "-");
        plot.save(graphFile, "png");
        System.out.println("Graph plotted and saved at: " + graphFile + ".png\n");
    }
}
