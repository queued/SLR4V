package com.github.queued.slr4v.example;

import com.github.queued.slr4v.Config;
import com.github.queued.slr4v.learner.LinearRegression;
import com.github.queued.slr4v.model.Dataset;
import com.github.queued.slr4v.model.Matrix;
import com.github.queued.slr4v.model.entity.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TestLinearRegression
{
    public static int getClosest(LinkedList<Double> sortedList, double key) {
        List<Long> newList = new ArrayList<>();
        for (Double confirmed : sortedList) {
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

    public static void main(String[] args)
    {
        Config.setPath(args.length > 0 ? args[0] : ".");
        Config.getInstance();

        /* Prepare parameter */
        double learningRate = 0.000019;
        int numOfStep = 10000;

        /* Read dataset from inputs.csv file */
        LinkedList<Dataset> inputs = Dataset.fromFile("./dist/datasets/learner/inputs.csv");

        /* Read expected value from outputs.csv */
        LinkedList<Double> outputs = Dataset.expectedValueFromFile("./dist/datasets/learner/outputs.csv");

        /* Initialize coefficient (weight) and random value from 0 to 5 */
        Matrix weight = new Matrix(inputs.getFirst().getCol(), 1);
        weight.random(-0.5, 0.5);

        /* Create Linear Regression object */
        LinearRegression LR = new LinearRegression(inputs, outputs, learningRate, numOfStep, weight);

        /* Train model until it reach number of step */
        LR.train();

        String todayStr = "1," + ((int) inputs.getLast().getData(0, 1) + 1);
        String[] str = todayStr.split(",");
        int today = Integer.parseInt(str[1]) - 1;
        int totalCases = (int) Math.round(outputs.get(outputs.size() - 1));

        Dataset m = new Dataset(1, str.length);
        for( int i = 0 ; i < str.length ; i++) {
            m.setData(0, i, Double.parseDouble(str[i].trim()));
        }
        inputs.addLast(m);
        outputs.addLast((double) totalCases);

        Result prediction = LR.predict(inputs, outputs);
        double predicted = prediction.getOutput() * Config.ISOLATION_RATE / Config.TRANSMISSION_RATE;
        double predicted1 = getClosest(outputs, predicted) * Config.ISOLATION_RATE / Config.TRANSMISSION_RATE;

        System.out.println("\nTotal cases (TODAY): " + totalCases);
        System.out.println("New Cases (TOMORROW): +" + (int) Math.floor(predicted1 - (prediction.getError())));
        System.out.println(String.format("\nPrediction Error: %.02f%%", prediction.getError() * 100));
        System.out.println(String.format("Prediction Accuracy: %.02f%%", prediction.getAccuracy()));
    }
}
