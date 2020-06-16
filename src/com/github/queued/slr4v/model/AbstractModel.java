package com.github.queued.slr4v.model;

import com.github.queued.slr4v.model.entity.Result;

import java.util.LinkedList;

public abstract class AbstractModel
{
    protected int samplingRate = 1000; // Sampling Dataset
    protected LinkedList<Dataset> datasets = null; // Patch inputs
    protected LinkedList<Double> ev = null; // Expected Value
    protected double lr = 0.01; // Learning rate
    protected int step = 1000000; // Steps
    protected Matrix w = null; // Weight
    protected Matrix td = null; // Temporal difference

    void initialize() {}

    protected abstract double hypothesis(Dataset dataset, Matrix w);

    protected abstract double costFunc();

    protected abstract Matrix train();

    protected abstract Result predict(LinkedList<Dataset> datasets, LinkedList<Double> expectedValue);

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public LinkedList<Dataset> getInputs() {
        return datasets;
    }

    public void setInputs(LinkedList<Dataset> datasets) {
        this.datasets = datasets;
    }

    public LinkedList<Double> getEv() {
        return ev;
    }

    public void setEv(LinkedList<Double> ev) {
        this.ev = ev;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Matrix getW() {
        return w;
    }

    public void setW(Matrix w) {
        this.w = w;
    }

    public double getLr() {
        return lr;
    }

    public void setLr(double lr) {
        this.lr = lr;
    }

    public Matrix getTd() {
        return td;
    }

    public void setTd(Matrix td) {
        this.td = td;
    }
}
