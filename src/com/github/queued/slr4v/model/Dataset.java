package com.github.queued.slr4v.model;

import java.io.*;
import java.text.DecimalFormat;
import java.util.LinkedList;

public class Dataset extends Matrix
{
    public Dataset(int row, int col)
    {
        super(row, col);
    }

    public static LinkedList<Dataset> fromFile(String fileName)
    {
        File file = new File(fileName);
        BufferedReader br;
        LinkedList<Dataset> datasets = new LinkedList<Dataset>();
        try {

            br = new BufferedReader(new FileReader(file));
            String header = br.readLine();
            String line;
            // System.out.println("Header: " + header);
            int header_sz = header.split(",").length;

            while ((line = br.readLine()) != null) {
                // System.out.println(line);
                String[] str = line.split(",");
                if( str.length != header_sz ) {
                    br.close();
                    throw new RuntimeException(String.format("Invalid input: size of header is not match header(%s)\\{%d\\} input(%s)\\{%d\\}", header, header_sz, line, str.length));
                }
                Dataset m = new Dataset(1, str.length);
                for( int i = 0 ; i < str.length ; i++) {
                    m.setData(0, i, Double.parseDouble(str[i].trim()));
                }
                datasets.addLast(m);
            }
            // System.out.println("Number of inputs: " + datasets.size());
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return datasets;
    }

    public static Matrix getMatrix(LinkedList<Dataset> datasets)
    {
        int row = datasets.size();
        int col = datasets.get(0).col;
        Matrix m = new Matrix(row, col);
        for (int i = 0 ;i < row ; i++) {
            Matrix mAtRow = (Matrix) datasets.get(i);
            for( int j = 0; j < col; j++) {
                m.data[i][j] = mAtRow.getData(0, j);
            }
        }

        return m;
    }

    @Override
    public void random(double min, double max)
    {
        for (int i = 0; i < this.row; i++) {
            for (int j = 0; j < this.col; j++)
                this.data[i][j] = min + Math.random() * (max - min);
        }
    }

    @Override
    public void Print()
    {
        System.out.println(this.toString());
    }

    public static LinkedList<Double> expectedValueFromFile(String fileName)
    {
        File file = new File(fileName);
        BufferedReader br;
        LinkedList<Double> ev = new LinkedList<Double>();
        try {

            br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            // System.out.println("Header: " + line);
            while ((line = br.readLine()) != null) {
                // System.out.println(line);
                ev.addLast(Double.parseDouble(line.trim()));
            }
            // System.out.println("Number of outputs: " + ev.size());
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ev;
    }

    @Override
    public String toString()
    {
        DecimalFormat df = new DecimalFormat("#.##");
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Matrix : [");
        for (int i = 0; i < this.row; i++) {
            strBuilder.append("[");
            for (int j = 0; j < this.col; j++) {
                strBuilder.append(df.format(this.data[i][j]));
                if ((j + 1) < this.col) {
                    strBuilder.append(",");
                }
            }
            if ((i + 1) < this.row)
                System.out.print("],");
            else
                strBuilder.append("]");
        }
        strBuilder.append("]\n");

        return strBuilder.toString();
    }
}
