package com.github.queued.slr4v.learner;

import com.github.queued.slr4v.model.AbstractModel;
import com.github.queued.slr4v.model.Dataset;
import com.github.queued.slr4v.model.Matrix;
import com.github.queued.slr4v.model.entity.Result;
import com.github.queued.slr4v.optimization.Optimizer;

import java.util.Iterator;
import java.util.LinkedList;

public class LinearRegression extends AbstractModel
{
    private double accuracy = 0.00;
    private double error = 0.00;

    public LinearRegression(LinkedList<Dataset> datasets, LinkedList<Double> ev, double lr, int step, Matrix w)
    {
        super();
        this.datasets = datasets;
        this.ev = ev;
        this.lr = lr;
        this.step = step;

        if (w == null)
            initialize(datasets.getFirst().getRow(), 1);
        else
            this.w = w;
    }

    private void initialize(int row, int col)
    {
        this.w = new Matrix(row, col);
        this.w.random(0, 100);
    }

    @Override
    public double hypothesis(Dataset dataset, Matrix w)
    {
        return Matrix.multiply(dataset, w);
    }

    /*
     * costFunc : It will calculate cost function, which return sum of different
     * value between input and expected value and even store temporal difference
     * into matrix td
     */
    @Override
    public double costFunc()
    {
        Iterator<Dataset> iterOfInputs = this.datasets.iterator();
        this.td = new Matrix(datasets.size(), 1);
        int numOfInput = datasets.size();
        double evi = 0.0;
        double hThetaX = 0.0;
        double sum = 0.0;
        int i = 0;
        Dataset in = null;

        while (iterOfInputs.hasNext()) {
            evi = ev.get(i);
            in = iterOfInputs.next();
            hThetaX = hypothesis(in, w);

            // Find cost value
            double tdValue = (hThetaX - evi);
            td.setData(i, 0, tdValue);
            sum += tdValue;
            i++;
        }

        return sum / numOfInput;
    }

    @Override
    public Matrix train()
    {
        int s;
        for (s = 0; s < this.step; s++) {
            error = costFunc();
            if (Math.abs(error) < 0.000001) {
                //System.out.print("Training is complete: Cost value is less than " + 0.000001);
                break;
            }

            //System.out.println("\rStep: " + s + " Cost value: " + costVal);
            Optimizer.gradientDescent(this.datasets, this.lr, this.w, this.td);
        }

        /*
        System.out.println("\n========================================================================================");
        System.out.println("Step : " + s);
        System.out.println("Error: " + error);
        System.out.println("========================================================================================\n");
        System.out.println("Out:");
        w.Print();
        */

        return this.w;
    }

    @Override
    public Result predict(LinkedList<Dataset> datasets, LinkedList<Double> expectedValue)
    {
        LinkedList<Dataset> in = (datasets != null) ? datasets : this.datasets;
        LinkedList<Double> ev = (expectedValue != null) ? expectedValue : this.ev;
        Iterator<Dataset> itOfInput = in.iterator();
        Iterator<Double> itOfEv = ev.iterator();

        int currentLine = -1;
        int totalAccuracy = 0;
        double out = 0.00;
        while (itOfInput.hasNext()) {
            currentLine++;
            Dataset dataset = itOfInput.next();
            out = hypothesis(dataset, w);

            int sumDiff = 0;
            for (int i = 0; i < ev.size(); i++) {
                sumDiff += ev.get(currentLine) - out;
            }

            out = out + ((sumDiff + Math.sqrt(error) + this.w.getCol()) / (double) ev.size());

            int successes = 0;
            for (int i = 0; i < in.size(); i++) {
                if (Math.round(out) == Math.round(this.ev.get(currentLine))) {
                    successes++;
                    totalAccuracy++;
                }
            }

            accuracy = ((double) successes * 100) / in.size();
            // System.out.println(String.format("Input: %s -> Output: %d, ev: %d, acc: %.02f", dataset.toString(), Math.round(out), Math.round(itOfEv.next()), accuracy) + "%");
        }

        // System.out.println(String.format("Prediction Success Rate: %d/%d (%d%%)", totalAccuracy / in.size(), in.size(), (totalAccuracy / in.size() * 100 / in.size())));

        double acc = ((double) totalAccuracy / in.size() * 100) / in.size();
        return new Result(in, ev, Math.round(out), acc, error);
    }
}
