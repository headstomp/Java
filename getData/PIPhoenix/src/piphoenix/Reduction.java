/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package piphoenix;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author sgainda-admin
 */
public class Reduction
{

    private long tagId;
    private Double lastValue;
    private Double lastMin;
    private Double lastMax;
    private Double sum;
    private Double weightedAverage;
    private Double weightedAverageSum;
    private Double weightedAverageSumTotal;
    private Double variance;
    private final List<Double> allValues;
    private int count;
    private Date endDate;
    private int internalCounter;
    private int internalCounterTotal;
    private String site;

    Reduction(String site)
    {
        this.tagId = 0;
        this.count = 0;
        this.lastValue = 0.0;
        this.internalCounter = 0;
        this.internalCounterTotal = 0;
        this.lastMin = null;
        this.lastMax = null;
        this.sum = 0.0;
        this.weightedAverageSum = 0.0;
        this.weightedAverageSumTotal = 0.0;
        this.allValues = new ArrayList<>();
        this.endDate = null;
        this.site = site;
    }

    public void setValues(long tagId, Date endDate, int count, Double lastValue, Double lastMin, Double lastMax, Double sum, Double weightedAverage, List<Double> allValues)
    {

        this.internalCounter += 1;
        this.internalCounterTotal += 1;
        this.tagId = tagId;
        this.count += count;
        this.lastValue = lastValue;

        if (this.lastMin == null)
        {
            this.lastMin = lastMin;
        }
        if (this.lastMax == null)
        {
            this.lastMax = lastMax;
        }

        if (lastMin < this.lastMin)
        {
            this.lastMin = lastMin;
        }

        if (lastMax > this.lastMax)
        {
            this.lastMax = lastMax;
        }

        this.sum += sum;
        this.weightedAverageSum += weightedAverage;
        this.weightedAverageSumTotal += weightedAverage;
        this.allValues.addAll(allValues);
        this.endDate = endDate;

    }

    public String getStatement(int bucket, boolean toClear, Date endDateTemp) throws SQLException
    {

        if (internalCounter == 0)
        {

            return null;
        }

        weightedAverage = weightedAverageSum / internalCounter;
        double average = sum / count;
        double sumVariance = 0.0;
        for (Double value : allValues)
        {
            sumVariance += Math.pow(value - average, 2);
        }

        variance = sumVariance / allValues.size();
        String statement = "";
        if (!toClear)
        {
            statement = "UPSERT INTO " + getTable(bucket) + "(DATETIME, TAGID, LASTVALUE, MINIMUM, MAXIMUM, TOTAL, WEIGHTEDAVERAGE, COUNT, VARIANCE, BUCKETCOUNT, WEIGHTEDAVERAGESUM) VALUES ('" + new java.sql.Timestamp(endDateTemp.getTime()).toString() + "'" + ", " + tagId + ", " + lastValue + ", " + lastMin + ", " + lastMax + ", " + sum + ", " + weightedAverage + ", " + count + ", " + variance + ", " + internalCounterTotal + ", " + weightedAverageSumTotal + ")";
        } else
        {
            statement = "UPSERT INTO " + getTable(bucket) + "(DATETIME, TAGID, LASTVALUE, MINIMUM, MAXIMUM, TOTAL, WEIGHTEDAVERAGE, COUNT, VARIANCE, BUCKETCOUNT, WEIGHTEDAVERAGESUM) VALUES ('" + new java.sql.Timestamp(endDate.getTime()).toString() + "'" + ", " + tagId + ", " + lastValue + ", " + lastMin + ", " + lastMax + ", " + sum + ", " + weightedAverage + ", " + count + ", " + variance + ", " + internalCounterTotal + ", " + weightedAverageSumTotal + ")";
        }
        //System.out.println(tagId + " LastValue = " + lastValue + " Last Date = " + endDate.toString() + " Sum = " + sum + " Average =" + weightedAverage + " Min = " + lastMin + " Max = " + lastMax + " Total = " + count);

        if (toClear)
        {
            clear();
           
        }
        return statement;
    }

    public void roundEndDate60()
    {
        if (internalCounter > 0)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.endDate);
            int minutes = calendar.get(Calendar.MINUTE);
            int timeToAdd = 0;
            if (minutes > 0)
            {
                timeToAdd = (60 - minutes) * 60 * 1000;
            }
            this.endDate = new Date(this.endDate.getTime() + timeToAdd);
        }
    }

    public void roundEndDate30()
    {
        if (internalCounter > 0)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.endDate);
            int minutes = calendar.get(Calendar.MINUTE);
            int timeToAdd = 0;
            if (minutes > 0)
            {
                if (minutes > 30)
                {
                    timeToAdd = (60 - minutes) * 60 * 1000;
                } else if (minutes < 30)
                {
                    timeToAdd = (30 - minutes) * 60 * 1000;
                }
            }
            this.endDate = new Date(this.endDate.getTime() + timeToAdd);
        }
    }

    public void roundEndDate720()
    {

        if (internalCounter > 0)
        {
            Date tempDate;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.endDate);
            int minutes = calendar.get(Calendar.MINUTE);
            int timeToAdd = 0;

            if (minutes > 0)
            {
                timeToAdd = (60 - minutes) * 60 * 1000;
            }

            tempDate = new Date(this.endDate.getTime() + timeToAdd);
            timeToAdd = 0;
            calendar.setTime(tempDate);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            if (hours > 12)
            {
                timeToAdd = (24 - hours) * 60 * 60 * 1000;
            } else if (hours < 12 && hours > 0)
            {
                timeToAdd = (12 - hours) * 60 * 60 * 1000;
            }

            this.endDate = new Date(tempDate.getTime() + timeToAdd);

        }
    }

    public void roundEndDate1440()
    {

        if (internalCounter > 0)
        {
            Date tempDate;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.endDate);
            int minutes = calendar.get(Calendar.MINUTE);
            int timeToAdd = 0;
            if (minutes > 0)
            {
                timeToAdd = (60 - minutes) * 60 * 1000;
            }

            tempDate = new Date(this.endDate.getTime() + timeToAdd);
            calendar.setTime(tempDate);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            timeToAdd = 0;
            if (hours > 0)
            {
                timeToAdd = (24 - hours) * 60 * 60 * 1000;
            }

            this.endDate = new Date(tempDate.getTime() + timeToAdd);

        }
    }

    private void clear()
    {
        this.tagId = 0;
        this.count = 0;
        this.internalCounter = 0;
        this.lastMin = null;
        this.lastMax = null;
        this.sum = 0.0;
        this.weightedAverageSum = 0.0;
        this.endDate = null;
        this.allValues.clear();
        //Do not clear internalCounterTotal and WeightedAverageSumTotal
    }

    public void resetTotalizer()
    {
        this.internalCounterTotal = 0;
        this.weightedAverageSumTotal = 0.0;
    }

    private String getTable(int bucket)
    {
        String table;

        switch (bucket)
        {
            case 10:
                table = site + "_REDUCTIONS10";
                break;
            case 30:
                table = site + "_REDUCTIONS30";
                break;
            case 60:
                table = site + "_REDUCTIONS60";
                break;
            case 720:
                table = site + "_REDUCTIONS720";
                break;
            case 1440:
                table = site + "_REDUCTIONS1440";
                break;
            default:
                table = "";
                break;

        }
        return table;
    }

}
