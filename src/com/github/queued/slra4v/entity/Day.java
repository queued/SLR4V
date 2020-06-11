package com.github.queued.slra4v.entity;

public class Day
{
    protected int dayAfterFirst;

    protected double[] reportForThisDay;

    public Day(int dayNo, double[] report)
    {
        dayAfterFirst = dayNo;
        reportForThisDay = report;
    }

    public int getDayNumber()
    {
        return dayAfterFirst;
    }

    public double[] getFullReport()
    {
        return reportForThisDay;
    }

    public int getConfirmedCases()
    {
        return (int) reportForThisDay[0];
    }

    public int getDeaths()
    {
        return (int) reportForThisDay[1];
    }

    public int getActiveCases()
    {
        return (int) reportForThisDay[2];
    }

    @Override
    public String toString()
    {
        return "Day #" + getDayNumber() + ": [" + getConfirmedCases() + ", " + getDeaths() + ", " + getActiveCases() + "]";
    }
}
