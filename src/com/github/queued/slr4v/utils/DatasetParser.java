package com.github.queued.slr4v.utils;

import com.github.queued.slr4v.entity.Day;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DatasetParser
{
    public static List<Day> getArrayListFromCSV(String fileName) throws IOException {
        List<Day> days = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(line -> {
                String[] parts = line.split(","); // First column is the day. The other 3, are the report.
                Day day = new Day(Integer.parseInt(parts[0]), new double[] { Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]) });
                days.add(day);
            });
        }

        return days;
    }
}
