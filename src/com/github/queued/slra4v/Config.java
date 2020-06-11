package com.github.queued.slra4v;

import com.github.queued.slra4v.utils.PropertiesParser;

import java.util.Calendar;

public class Config
{
    private static final String VARIABLES_FILE = "./dist/viçosa.properties";

    public static int POPULATION;
    public static double TRANSMISSION_RATE;
    public static double ISOLATION_RATE;
    public static Calendar FIRST_CONFIRMED_CASE_DATE;

    public Config()
    {
        load();
    }

    public void load()
    {
        final PropertiesParser settings = new PropertiesParser(VARIABLES_FILE);

        POPULATION = settings.getInt("Population", 78846);
        TRANSMISSION_RATE = settings.getDouble("TransmissionRate", 1.4);
        ISOLATION_RATE = settings.getDouble("IsolationRate", 0.5);

        final String[] firstReport = settings.getString("FirstConfirmedReport", "23/04/2020").split("/");

        FIRST_CONFIRMED_CASE_DATE = Calendar.getInstance();
        FIRST_CONFIRMED_CASE_DATE.set(Calendar.DATE, Integer.parseInt(firstReport[0]));
        FIRST_CONFIRMED_CASE_DATE.set(Calendar.MONTH, Integer.parseInt(firstReport[1]) - 1);
        FIRST_CONFIRMED_CASE_DATE.set(Calendar.YEAR, Integer.parseInt(firstReport[2]));
    }

    public static Config getInstance()
    {
        return SingletonHolder._instance;
    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder
    {
        protected static final Config _instance = new Config();
    }
}
