package com.github.queued.slr4v.model.entity;

import com.github.queued.slr4v.model.Dataset;

import java.util.LinkedList;

public class Result
{
    protected double _accuracy;
    protected double _error;

    protected LinkedList<Dataset> _input;
    protected LinkedList<Double> _expected;
    protected double _output;

    public Result(LinkedList<Dataset> input, LinkedList<Double> expected, double output, double accuracy, double error)
    {
        _accuracy = accuracy;
        _error = error;
        _input = input;
        _expected = expected;
        _output = output;
    }

    public double getAccuracy()
    {
        return _accuracy;
    }

    public double getError()
    {
        return _error;
    }

    public double getOutput()
    {
        return _output;
    }

    public LinkedList<Dataset> getInputMatrix()
    {
        return _input;
    }

    public LinkedList<Double> getExpectedValueMatrix()
    {
        return _expected;
    }
}
