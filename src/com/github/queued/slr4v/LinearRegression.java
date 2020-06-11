package com.github.queued.slr4v;

import com.github.queued.slr4v.entity.Day;
import com.github.queued.slr4v.utils.DatasetParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LinearRegression {
    public static String CURRENT_PATH;

    public static List<Integer> x = new ArrayList<>();
    public static List<Integer> y = new ArrayList<>();

    public static int predictedDiff;
    public static int predictedValue;
    public static int newPossibleInfections;

    private static Double predictForValue(int predictForDependentVariable) {
        if (x.size() != y.size())
            throw new IllegalStateException("Must have equal X and Y data points");

        Integer numberOfDataValues = x.size();

        List<Double> xSquared = x
                .stream()
                .map(position -> Math.pow(position, 2))
                .collect(Collectors.toList());

        List<Integer> xMultipliedByY = IntStream.range(0, numberOfDataValues)
                .map(i -> x.get(i) * y.get(i))
                .boxed()
                .collect(Collectors.toList());

        Integer xSummed = x
                .stream()
                .reduce((prev, next) -> prev + next)
                .get();

        Integer ySummed = y
                .stream()
                .reduce((prev, next) -> prev + next)
                .get();

        Double sumOfXSquared = xSquared
                .stream()
                .reduce((prev, next) -> prev + next)
                .get();

        Integer sumOfXMultipliedByY = xMultipliedByY
                .stream()
                .reduce((prev, next) -> prev + next)
                .get();

        int slopeNominator = numberOfDataValues * sumOfXMultipliedByY - ySummed * xSummed;
        Double slopeDenominator = numberOfDataValues * sumOfXSquared - Math.pow(xSummed, 2);
        Double slope = slopeNominator / slopeDenominator;

        double interceptNominator = ySummed - slope * xSummed;
        double interceptDenominator = numberOfDataValues;
        Double intercept = interceptNominator / interceptDenominator;

        return (slope * predictForDependentVariable) + intercept;
    }

    private static void bootstrap() throws IOException {
        Config.getInstance();
        List<Day> days = DatasetParser.getArrayListFromCSV(CURRENT_PATH + "/datasets/viçosa.csv");

        for (Day day : days) {
            x.add(day.getDayNumber());
            y.add(day.getConfirmedCases());
        }
    }

    public static int getTotalCases(int newCases)
    {
        return newCases + y.get(y.size() - 1);
    }

    public static void main(String[] args) throws IOException {
        CURRENT_PATH = args[0].isEmpty() ? "." : args[0];
        bootstrap();

        final int dayToPredict = x.get(x.size() - 1) + 1; // only works for "tomorrow" predictions
        predictedValue = (int) predictForValue(dayToPredict).longValue();
        predictedDiff = y.get(y.size() - 1) - y.get(y.indexOf(predictedValue));
        newPossibleInfections = (int) (predictedDiff * Config.ISOLATION_RATE / Config.TRANSMISSION_RATE);
        final int totalCases = getTotalCases(0);
        final int newTotalCases = getTotalCases(newPossibleInfections);

        Calendar cal = Config.FIRST_CONFIRMED_CASE_DATE;
        cal.add(Calendar.DATE, dayToPredict - 4);
        Date date = cal.getTime();

        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = format1.format(date);

        double maxInfections = Config.POPULATION * Config.ISOLATION_RATE / Config.TRANSMISSION_RATE;
        double maxInfectionsPercentage = (maxInfections * 100) / Config.POPULATION;

        System.out.println("\nLinear Regression / Viçosa - MG");
        System.out.println("------------------------------------------");
        System.out.println("Total Population: " + Config.POPULATION);
        System.out.println("Maximum Infections: " + (int) Math.floor(maxInfections)
                + " (" + String.format("%.02f", maxInfectionsPercentage) + "% of the total population)"
        );
        System.out.println("------------------------------------------");
        System.out.println("New cases (on " + dateStr + "): +" + newPossibleInfections);
        System.out.println("Total cases (on " + dateStr + "): ~" + newTotalCases + " (" + String.format("%.02f", (double) newTotalCases * 100 / Config.POPULATION) + "% of the total population)");
        System.out.println("Total cases (TODAY): " + y.get(y.size() - 1) + " (" + String.format("%.02f", (double) totalCases * 100 / Config.POPULATION) + "% of the total population)");
        System.out.println("------------------------------------------");
        System.out.println("Formula: Predicted[" + predictedValue + "] * Isolation Rate[" + Config.ISOLATION_RATE + "] / Transmission Rate[" + Config.TRANSMISSION_RATE + "]\n");
    }
}
