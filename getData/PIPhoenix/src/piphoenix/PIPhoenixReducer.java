/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package piphoenix;

import com.osisoft.xml.services.pidataservice.ArrayOfPIArcDataRequest;
import com.osisoft.xml.services.pidataservice.ArrayOfTimeSeries;
import com.osisoft.xml.services.pidataservice.IPITimeSeries;
import com.osisoft.xml.services.pidataservice.PIArcDataRequest;
import com.osisoft.xml.services.pidataservice.PIArcManner;
import com.osisoft.xml.services.pidataservice.PITimeSeriesService;
import com.osisoft.xml.services.pidataservice.TimeRange;
import com.osisoft.xml.services.pidataservice.TimeSeries;
import com.osisoft.xml.services.pidataservice.TimedValue;
import datatosql.DateTimeOffset;
import datatosql.ImporterProperties;
import datatosql.LocationTable;
import datatosql.SQLDriverTable;
import datatosql.SQLManager;
import datatosql.SourceTable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

/**
 *
 * @author sandeep.gainda
 */
public class PIPhoenixReducer
{

    private static final boolean PUSH = true;
    private static final int BATCH_SIZE = 1000;
    private static int DELAY = 1000 * 60; //how many minutes to delay (last number is minutes)

    private static final String LOG_PATH = "C:\\arc\\Logs\\DCSPhoenix\\";

    private static final String ARC_SQL_SERVER = "tf-sql101dag2.greenfieldethanol.com";
    private static final String ARC_SQL_INSTANCE = "MSSQLSERVER";
    private static final String ARC_SQL_USER = "arc";
    private static final String ARC_SQL_PASS = "eryornudie";
    private static final String ARC_SQL_DB = "Gfsa_Arc";
    private static final String ARC_SQL_DRIVER = SQLDriverTable.MSSQL.sqlDriver;

    private static final String PHOENIX_SQL_SERVER = "10.220.60.12";
    private static final String PHOENIX_SQL_DRIVER = SQLDriverTable.PhoenixSQL.sqlDriver;

    /*private static String SQL_SERVER;
     private static String SQL_USER;
     private static String SQL_PASS;
     private static String SQL_DB;
     private static String SQL_DRIVER;
     private static String SQL_INSTANCE;
     */
    /**
     * @param args the command line arguments
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     * @throws java.text.ParseException
     * @throws java.lang.InterruptedException
     * @throws java.io.FileNotFoundException
     * @throws java.net.MalformedURLException
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException, InterruptedException, FileNotFoundException, MalformedURLException
    {

        //////////
        //Set by arguments
        String location = "";
        String mechanism = "";

        String tagRepairFilter = "";
        int bucketLimit = 10;

        //////////
        int sourcePropertyIndex = -1;
        String source = "";
        String rawTable = "";
        String startDateProperty = "";
        String historian = "";

        boolean catchingUp = false;

        boolean setFutures = false;
        boolean importRaw = false;
        boolean importReduced = false;
        boolean updateLastValues = false;
        boolean repair = false;

        String filterTags = "";

        String url = "";
        String tagQuery = "";
        // String dcsTableName = "";
        String analogTableName = "";
        String discreteTableName = "";
        //  SimpleDateFormat dateTimeOffsetFormatSub = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS XXX");
        //   SimpleDateFormat dateTimeOffsetFormatNoSub = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss XXX");

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        int sourceId = 0;
        int locationId = 0;

        if (!(args[0].equalsIgnoreCase("TI") || args[0].equalsIgnoreCase("MF") || args[0].equalsIgnoreCase("CG")))
        {
            System.err.println("argument example for Tiverton Analog Historical: TI Analog TRUE");
            System.exit(1);
        }
        if (args.length == 2)
        {
            location = String.valueOf(args[0]);
            mechanism = String.valueOf(args[1]);

        } else
        {

            System.err.println("Missing Arguments");
            System.exit(1);
        }

        if (mechanism.equalsIgnoreCase("REDUCE_EVEN") || (mechanism.equalsIgnoreCase("REDUCE_ODD")))
        {
            setFutures = true;
            importRaw = false;
            importReduced = true;
            bucketLimit = 10;
            updateLastValues = true;
            DELAY = DELAY * 5; //5 minute delay
            if (mechanism.equalsIgnoreCase("REDUCE_ODD"))
            {
                filterTags = " and Id % 2 = 1";
            } else
            {
                filterTags = " and Id % 2 = 0";
            }

        } else if (mechanism.equalsIgnoreCase("REDUCE_ALL"))
        {
            setFutures = true;
            importRaw = true;
            importReduced = true;
            bucketLimit = 10;
            updateLastValues = true;
            DELAY = DELAY * 7; //7 minute delay
        } else if (mechanism.equalsIgnoreCase("REPAIR"))
        {
            updateLastValues = false;
            setFutures = false;
            importRaw = true;
            importReduced = true;
            bucketLimit = 10;
            repair = true;
            DELAY = DELAY * 30; //30 minute delay

        } else if (mechanism.equalsIgnoreCase("IMPORT_RAW"))
        {
            updateLastValues = false;
            setFutures = false;
            importRaw = true;
            importReduced = true;
            bucketLimit = 10;
            repair = false;
            DELAY = DELAY * 30; //30 minute delay

        } else
        {
            System.out.println("Unknown Mechanism");
            System.exit(0);
        }

        location = location.toUpperCase();
        File log = new File(LOG_PATH + location + "_" + mechanism + "_" + new SimpleDateFormat("yyyMMdd_HHmmssSSS").format(Calendar.getInstance().getTime()) + ".log");
        FileOutputStream fos = new FileOutputStream(log);
        PrintStream ps = new PrintStream(fos);
        System.setErr(ps);
        System.setOut(ps);

        switch (location)
        {
            case "TI":
                url = "http://ti-historian/PIWebServices/PITimeSeries.svc?wsdl";
                historian = "\\\\ti-historian\\";
                sourceId = SourceTable.TivertonDCS.sourceId; // fill in later  
                locationId = LocationTable.Tiverton.locationId;
                tagQuery = "SELECT * FROM dbo.Tag WHERE TagSourceId = '" + sourceId + "' and IsActive=1";
                rawTable = "TI_RAW";
                source = "Tiverton DCS Importer";
                break;
            case "MF":
                url = "http://mf-historian/PIWebServices/PITimeSeries.svc?wsdl";
                historian = "\\\\mf-historian\\";
                sourceId = SourceTable.MtForestDCS.sourceId; // fill in later  
                locationId = LocationTable.MountForest.locationId;
                tagQuery = "SELECT * FROM dbo.Tag WHERE TagSourceId = '" + sourceId + "' and IsActive=1";
                rawTable = "MF_RAW";
                source = "Mount Forest DCS Importer";
                break;
            case "CG":
                url = "http://tse-demo-fthse/PIWebServices/PITimeSeries.svc?wsdl";
                historian = "\\\\tse-demo-fthse\\";
                sourceId = 145; // fill in later  
                locationId = LocationTable.GET.locationId;
                tagQuery = "SELECT * FROM dbo.Tag WHERE TagSourceId = '" + sourceId + "' and IsActive=1";
                rawTable = "CG_RAW";
                source = "GET Bazooka DCS Importer";
                break;
            default:
                System.out.println("Invalid Location ID");
                System.exit(1);
                break;

        }

        ImporterProperties sourceProperties = new ImporterProperties(source);
        for (int z = 0; z < sourceProperties.size(); z++)
        {
            if ((location + "_" + mechanism).equalsIgnoreCase(sourceProperties.getPropertyName(z)))
            {

                if (sourceProperties.getHistoricalPush(z) != null)
                {
                    startDateProperty = sourceProperties.getHistoricalPush(z).toNoOffsetString();

                    // sourceProperties.nullHistoricalPush(z);
                } else
                {

                    System.out.println("Error - no start date");
                    System.exit(1);
                }
                if (mechanism.equalsIgnoreCase("REPAIR"))
                {
                    //If no tag ID present, do all tags.
                    if (!sourceProperties.getProperty3(z).isEmpty())
                    {
                        tagQuery += " and Id = " + Long.parseLong(sourceProperties.getProperty3(z));
                    }

                } else
                {
                    //odd or even tags
                    tagQuery += filterTags;
                }

                sourcePropertyIndex = z;

            }

        }

        System.out.println(location + " " + mechanism + " - Importing Raw: " + importRaw + " , Updating Futures: " + setFutures + " , Updating Last Values: " + updateLastValues + ", Starting from: " + startDateProperty);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = dateFormat.parse(startDateProperty);
        Date endDate = new Date(startDate.getTime() + 1000 * 60 * bucketLimit);
        int numberOfSeconds = 1 + ((int) (endDate.getTime() - startDate.getTime())) / 1000;

        SQLManager arcSQL1 = new SQLManager(ARC_SQL_SERVER, ARC_SQL_INSTANCE, ARC_SQL_USER, ARC_SQL_PASS, ARC_SQL_DB, ARC_SQL_DRIVER);
        SQLManager phoenixSQL1 = new SQLManager(PHOENIX_SQL_SERVER, PHOENIX_SQL_DRIVER);
        SQLManager phoenixSQLu = new SQLManager(PHOENIX_SQL_SERVER, PHOENIX_SQL_DRIVER);

        int countBatch10 = 0;
        int countBatch30 = 0;
        int countBatch60 = 0;
        int countBatch720 = 0;
        int countBatch1440 = 0;

        List<HashMap<String, Object>> tagResults = arcSQL1.queryRaw(tagQuery);
        List<HashMap<String, Object>> testTagResults = arcSQL1.queryRaw(tagQuery);
        System.out.println("Found " + tagResults.size() + " tag(s).");

        HashMap<Long, Reduction> reductions10 = new HashMap();
        HashMap<Long, Reduction> reductions30 = new HashMap();
        HashMap<Long, Reduction> reductions60 = new HashMap();
        HashMap<Long, Reduction> reductions720 = new HashMap();
        HashMap<Long, Reduction> reductions1440 = new HashMap();

        for (HashMap<String, Object> tag : tagResults)
        {
            String tagId = tag.get("Id").toString().trim();
            reductions10.put(Long.parseLong(tagId), new Reduction(location));
            reductions30.put(Long.parseLong(tagId), new Reduction(location));
            reductions60.put(Long.parseLong(tagId), new Reduction(location));
            reductions720.put(Long.parseLong(tagId), new Reduction(location));
            reductions1440.put(Long.parseLong(tagId), new Reduction(location));
        }

        PITimeSeriesService service = new PITimeSeriesService(new URL(url),
                new QName("http://xml.osisoft.com/services/PIDataService", "PITimeSeriesService"));
        IPITimeSeries port = service.getBasicEndpoint();

        if ((endDate.getTime() + (DELAY)) <= System.currentTimeMillis() && (setFutures || updateLastValues))
        {
            System.out.println("Turning off SetFutures/UpdateLastValues as we are trying to catch up first...");
            catchingUp = true;
        }

        //wait one minute before live...
        while ((endDate.getTime() + (DELAY)) > System.currentTimeMillis())
        {
            if ((setFutures || updateLastValues) && catchingUp)
            {
                System.out.println("Turning on SetFutures/UpdateLastValues as we have caught up...");
                catchingUp = false;
            }
            System.out.println("Sleeping... " + ((endDate.getTime() + (DELAY)) - System.currentTimeMillis()) / 1000 + " seconds to go...");
            Thread.sleep(Math.abs((endDate.getTime() + (DELAY)) - System.currentTimeMillis()));

        }

        while (true)
        {
            long start = System.currentTimeMillis();
            long totalDCSQueryTime = 0;

            System.out.println("Start -> " + dateFormat.format(startDate) + " End -> " + dateFormat.format(endDate));

            String prepStmtRaw;
            String prepStmtTagLastValue;
            String prepStmt10;
            String prepStmt30;
            String prepStmt60;
            String prepStmt720;
            String prepStmt1440;

            long totalPhoenixInsert = 0;
            long totalCount = 0;
            int tagCount = tagResults.size();

            Connection conn = null;
            PreparedStatement stmt = null;
            int batchSize = 0;
            // number of rows you want to commit per batch.  
            int commitSize = BATCH_SIZE;
            if (importRaw)
            {

                String insertStatement = "UPSERT INTO " + rawTable + " (DATETIME,TAGID,LASTVALUE) VALUES (?,?,?)";

                try
                {
                    conn = phoenixSQLu.getConnection();
                    conn.setAutoCommit(false);
                    stmt = conn.prepareStatement(insertStatement);
                } catch (SQLException ex)
                {
                    Logger.getLogger(PIPhoenixReducer.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println(ex.getMessage());

                }
            }

            for (HashMap<String, Object> tag : tagResults)
            {
                tagCount -= 1;
                String tagName = tag.get("Name").toString().trim();
                String sourceTagName = tag.get("SourceTagName").toString().trim();
                Long tagId = Long.parseLong(tag.get("Id").toString().trim());
                //Long tagTypeId = Long.parseLong(tag.get("TagTypeId").toString().trim());

                long startOfDCSQuery = System.currentTimeMillis();
                /*
                 if (tagTypeId == TagTypeTable.Analog.tagTypeId) 
                 {
                 dcsTableName = analogTableName;
                 } else if (tagTypeId == TagTypeTable.Discrete.tagTypeId) 
                 {
                 dcsTableName = discreteTableName;
                 }
                 */
                //String dataQuery = "";
                ArrayOfPIArcDataRequest requests = new ArrayOfPIArcDataRequest();
                PIArcDataRequest request = new PIArcDataRequest();
                request.setPath(historian + sourceTagName);
                TimeRange timeRange = new TimeRange();

                /////////FUCKING DAYLIGHT SAVINGS 
                Calendar calendarStartDate = Calendar.getInstance(); // creates a new calendar instance
                calendarStartDate.setTime(startDate);   // assigns calendar to given date 
                String requestStart = dateFormat.format(startDate);
                String requestEnd = dateFormat.format(endDate);

                //  System.out.println("requestStart="+requestStart);
                //  System.out.println("requestEnd="+requestEnd);
                Calendar calendarEndDate = Calendar.getInstance(); // creates a new calendar instance
                calendarEndDate.setTime(endDate);   // assigns calendar to given date 
                if (calendarStartDate.get(Calendar.HOUR_OF_DAY) == calendarEndDate.get(Calendar.HOUR_OF_DAY))
                {
                    if (calendarStartDate.get(Calendar.MINUTE) > calendarEndDate.get(Calendar.MINUTE))
                    {
                        //If time went backwards, then reset time
                        requestStart = requestEnd;
                    }
                }

                timeRange.setStart(requestStart);
                timeRange.setEnd(requestEnd);

                PIArcManner manner = new PIArcManner();

                request.setTimeRange(timeRange);

                request.setPIArcManner(manner);

                requests.getPIArcDataRequest().add(request);

                ArrayOfTimeSeries result = port.getPIArchiveData(requests);

                TimeSeries serie = result.getTimeSeries().get(0);

                // System.out.println("Waiting for Query from DCS:");
                //   List<HashMap<String, Object>> tagDataResults = dcsSQL1.queryRaw(dataQuery);
                totalDCSQueryTime += System.currentTimeMillis() - startOfDCSQuery;
                // System.out.println("Done waiting...");
                Double lastValue = 0.0;
                Double lastMin = null;
                Double lastMax = null;
                Double sum = 0.0;
                Double weightedAverage = 0.0;
                double sumWeightedAverage = 0;
                List<Double> allValues = new ArrayList<>();
                Double average = 0.0;
                String lastDate = "";
                int count = 0;

                boolean hasData = false;
                Double[] weightedAverageValues = new Double[numberOfSeconds];
                long startOfPhoenixRawInsert = 0;

                System.out.print("[" + tagCount + "]: " + tagId + "-" + tagName);
                for (TimedValue tagData : serie.getTimedValues().getTimedValue())
                {

                    if (!tagData.getValue().isEmpty())
                    {
                        hasData = true;
                        try
                        {
                            lastValue = Double.parseDouble(tagData.getValue());
                        } catch (NumberFormatException e)
                        {
                            if (tagData.getValue().equalsIgnoreCase("Active"))
                            {
                                lastValue = 1.0;
                            } else if (tagData.getValue().equalsIgnoreCase("Inactive"))
                            {
                                lastValue = 0.0;
                            } else
                            {
                                System.out.println(tagData.getValue());
                                throw new NumberFormatException();
                            }

                        }
                        dateTimeFormat.setTimeZone(TimeZone.getDefault());
                        lastDate = dateTimeFormat.format(tagData.getTime().toGregorianCalendar().getTime());

                        Date currentDate = dateFormat.parse(lastDate);
                        //System.out.println(currentDate);
                        //System.out.println(startDate);
                        //System.out.println(lastDate);
                        // System.out.print(lastDate+" ");
                        //  System.out.print(lastValue+" ");
                        //  System.out.println(currentDate+" ");
                        int timeKey = (int) (currentDate.getTime() - startDate.getTime()) / 1000;

                        //take care of daylight savings 
                        //System.out.println(timeKey);
                        if (timeKey >= 3600 && timeKey <= 7800)
                        {

                            hasData = false;
                            continue;
                            //timeKey-=3600;
                        }
                        if (timeKey < 0)
                        {
                            hasData = false;
                            continue;
                            // timeKey=0;

                        }

                        if (importRaw)
                        {
                            startOfPhoenixRawInsert = System.currentTimeMillis();

                            try
                            {
                                stmt.setString(1, lastDate);
                                stmt.setLong(2, tagId);
                                stmt.setDouble(3, lastValue);
                                stmt.executeUpdate();
                                //System.out.println(value.get("Date") + " " + Integer.parseInt(value.get("TagId")) + " "+ Double.parseDouble(value.get("Value")));
                                batchSize++;
                                if (batchSize % commitSize == 0)
                                {
                                    System.out.println("\n\nUpserting Raw Values batch#" + batchSize + "...");
                                    System.out.println("\n");
                                    // Do all upserts here
                                    conn.commit();
                                }

                            } catch (SQLException ex)
                            {
                                Logger.getLogger(PIPhoenixReducer.class.getName()).log(Level.SEVERE, null, ex);
                                System.out.println(ex.getMessage());
                            }

                            totalPhoenixInsert += System.currentTimeMillis() - startOfPhoenixRawInsert;

                            //prepStmtRaw = "UPSERT INTO " + rawTable + "(DATETIME, TAGID, LASTVALUE) VALUES ('" + lastDate + "'" + ", " + tagId + ", " + lastValue + ")";
                            //  System.out.println(prepStmtRaw);
                            //startOfPhoenixRawInsert = System.currentTimeMillis();
                            //phoenixSQL1.updateRaw(prepStmtRaw);
                            //totalPhoenixInsert += System.currentTimeMillis() - startOfPhoenixRawInsert;
                        }

                        weightedAverageValues[timeKey] = lastValue;

                        if (lastMin == null)
                        {

                            lastMin = lastValue;
                        }
                        if (lastMax == null)
                        {
                            lastMax = lastValue;
                        }

                        if (lastValue < lastMin)
                        {

                            lastMin = lastValue;
                        }
                        if (lastValue > lastMax)
                        {
                            lastMax = lastValue;
                        }
                        count += 1;
                        sum += lastValue;
                        allValues.add(lastValue);
                    }

                }
                System.out.print("(" + count + ")\t");
                if (hasData)
                {
                    if (!catchingUp && updateLastValues)
                    {
                        prepStmtTagLastValue = "Update Tag set LastDataAt='" + lastDate + "', LastValue='" + lastValue + "' Where Id=" + tagId;
                        // System.out.println("Executing: "+prepStmtTagLastValue);
                        arcSQL1.updateRaw(prepStmtTagLastValue);
                    }
                    //getting variance means going over the values one more time

                    int numberWeightedAverageBuckets = numberOfSeconds;

                    for (int i = 0; i < numberOfSeconds; i++)
                    {
                        if (weightedAverageValues[i] == null && i > 0)
                        {
                            weightedAverageValues[i] = weightedAverageValues[i - 1];
                        }
                        if (weightedAverageValues[i] != null)
                        {
                            sumWeightedAverage += weightedAverageValues[i];
                        } else
                        {
                            numberWeightedAverageBuckets -= 1;
                        }
                        // System.out.println(i+"->"+weightedAverageValues[i]);

                    }

                    weightedAverage = sumWeightedAverage / numberWeightedAverageBuckets;
                    //System.out.println("Weighted average: "+weightedAverage+" vs average: "+(sum/count));

                    reductions10.get(tagId).setValues(tagId, endDate, count, lastValue, lastMin, lastMax, sum, weightedAverage, allValues);
                    reductions30.get(tagId).setValues(tagId, endDate, count, lastValue, lastMin, lastMax, sum, weightedAverage, allValues);
                    reductions60.get(tagId).setValues(tagId, endDate, count, lastValue, lastMin, lastMax, sum, weightedAverage, allValues);
                    reductions720.get(tagId).setValues(tagId, endDate, count, lastValue, lastMin, lastMax, sum, weightedAverage, allValues);
                    reductions1440.get(tagId).setValues(tagId, endDate, count, lastValue, lastMin, lastMax, sum, weightedAverage, allValues);

                    prepStmt10 = reductions10.get(tagId).getStatement(10, true, null);
                    startOfPhoenixRawInsert = System.currentTimeMillis();
                    if (importReduced)
                    {
                        if (prepStmt10 != null)
                        {
                            System.out.print("(10min)...");
                            phoenixSQL1.updateRaw(prepStmt10);
                        }
                    }
                    if (!catchingUp && setFutures)
                    {
                        prepStmt30 = reductions30.get(tagId).getStatement(30, false, getRoundedDate(30, endDate));
                        prepStmt60 = reductions60.get(tagId).getStatement(60, false, getRoundedDate(60, endDate));
                        prepStmt720 = reductions720.get(tagId).getStatement(720, false, getRoundedDate(720, endDate));
                        prepStmt1440 = reductions1440.get(tagId).getStatement(1440, false, getRoundedDate(1440, endDate));

                        if (importReduced)
                        {
                            if (prepStmt30 != null)
                            {
                                System.out.print("F(30min)...");
                                phoenixSQL1.updateRaw(prepStmt30);
                            }
                            if (prepStmt60 != null)
                            {
                                System.out.print("F(60min)...");
                                phoenixSQL1.updateRaw(prepStmt60);
                            }
                            if (prepStmt720 != null)
                            {
                                System.out.print("F(12hour)...");
                                phoenixSQL1.updateRaw(prepStmt720);
                            }
                            if (prepStmt1440 != null)
                            {
                                System.out.print("F(24hour)...");
                                phoenixSQL1.updateRaw(prepStmt1440);
                            }
                        }
                    }

                    totalPhoenixInsert += System.currentTimeMillis() - startOfPhoenixRawInsert;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);

                startOfPhoenixRawInsert = System.currentTimeMillis();
                if (minutes == 0)
                {
                    reductions30.get(tagId).roundEndDate60();
                    reductions60.get(tagId).roundEndDate60();
                    prepStmt30 = reductions30.get(tagId).getStatement(30, true, null);
                    prepStmt60 = reductions60.get(tagId).getStatement(60, true, null);

                    if (importReduced)
                    {
                        if (prepStmt30 != null)
                        {
                            System.out.print("(30min)...");
                            phoenixSQL1.updateRaw(prepStmt30);
                        }
                        if (prepStmt60 != null)
                        {
                            System.out.print("(60min)...");
                            phoenixSQL1.updateRaw(prepStmt60);
                        }
                    }
                }

                if (minutes == 30)
                {
                    reductions30.get(tagId).roundEndDate30();
                    prepStmt30 = reductions30.get(tagId).getStatement(30, true, null);
                    if (importReduced)
                    {
                        if (prepStmt30 != null)
                        {
                            System.out.print("(30min)...");
                            phoenixSQL1.updateRaw(prepStmt30);
                        }
                    }

                }
                if (hours == 12 && minutes == 0)
                {
                    reductions720.get(tagId).roundEndDate720();
                    prepStmt720 = reductions720.get(tagId).getStatement(720, true, null);
                    if (importReduced)
                    {
                        if (prepStmt720 != null)
                        {
                            System.out.print("(12hour)...");
                            phoenixSQL1.updateRaw(prepStmt720);
                        }
                    }

                }
                if (hours == 0 && minutes == 0)
                {
                    reductions720.get(tagId).roundEndDate720();
                    reductions1440.get(tagId).roundEndDate1440();
                    prepStmt720 = reductions720.get(tagId).getStatement(720, true, null);
                    prepStmt1440 = reductions1440.get(tagId).getStatement(1440, true, null);

                    if (importReduced)
                    {
                        if (prepStmt720 != null)
                        {
                            System.out.print("(12hour)...");
                            phoenixSQL1.updateRaw(prepStmt720);
                        }
                        if (prepStmt1440 != null)
                        {
                            System.out.print("(24hour)...");
                            phoenixSQL1.updateRaw(prepStmt1440);

                        }
                    }

                }
                totalPhoenixInsert += System.currentTimeMillis() - startOfPhoenixRawInsert;

                totalCount += count;
                //System.out.println(tagName + " LastValue = " + lastValue + " Last Date = " + lastDate + " Sum = " + sum + " Average =" + weightedAverage + " Min = " + lastMin + " Max = " + lastMax + " Total = " + count);
                System.out.println();
            }

            if (importRaw)
            {
                long startOfPhoenixRawInsert = System.currentTimeMillis();
                try
                {
                    System.out.println("\n\nUpserting Remaining Raw Values (" + (batchSize % commitSize) + ")...");
                    System.out.println("\n");

                    conn.commit(); // commit the last batch of records 
                } catch (SQLException ex)
                {
                    Logger.getLogger(PIPhoenixReducer.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println(ex.getMessage());

                }

                if (stmt != null)
                {
                    try
                    {
                        stmt.close();
                    } catch (Exception e)
                    {
                        Logger.getLogger(PIPhoenixReducer.class.getName()).log(Level.SEVERE, null, e);
                        System.out.println(e.getMessage());
                    }
                }
                if (conn != null)
                {
                    try
                    {
                        conn.close();
                    } catch (Exception e)
                    {

                        Logger.getLogger(PIPhoenixReducer.class.getName()).log(Level.SEVERE, null, e);
                        System.out.println(e.getMessage());

                    }
                }
                totalPhoenixInsert += System.currentTimeMillis() - startOfPhoenixRawInsert;
            }
            System.out.println();
            String totalPhoenix = "Total Phoenix Insert Time (s): " + (totalPhoenixInsert / 1000);
            String totalTime = "Total Time (s): " + (System.currentTimeMillis() - start) / 1000;
            String totalQueryTime = "Total Query Time (s): " + totalDCSQueryTime / 1000;
            String totalProcessingTime = "Total Processing (s): " + ((((System.currentTimeMillis() - start) / 1000) - totalDCSQueryTime / 1000) - (totalPhoenixInsert / 1000));
            String totalEfficiency = "Total Leeway Left (s): " + String.valueOf(((bucketLimit * 60 * 1000) - (System.currentTimeMillis() - start)) / 1000);
            String totalRecordCount = "Total Records Processed: " + totalCount;

            System.out.println(totalProcessingTime);
            System.out.println(totalQueryTime);
            System.out.println(totalTime);
            System.out.println(totalEfficiency);
            System.out.println(totalPhoenix);
            System.out.println(totalRecordCount);

            sourceProperties.setPropertyValue1(sourcePropertyIndex, dateFormat.format(endDate));
            sourceProperties.setPropertyValue2(sourcePropertyIndex, totalTime + ", " + totalEfficiency + ", " + totalQueryTime + ", " + totalProcessingTime + ", " + totalPhoenix + ", " + totalRecordCount);

            startDate = new Date(startDate.getTime() + 1000 * 60 * bucketLimit);
            endDate = new Date(endDate.getTime() + 1000 * 60 * bucketLimit);

            SimpleDateFormat dateNoTimeFormat = new SimpleDateFormat("yyyy-MM-dd");
            String historicalPushDate = dateNoTimeFormat.format(dateNoTimeFormat.parse(dateFormat.format(startDate)));

            sourceProperties.setHistoricalPush(sourcePropertyIndex, historicalPushDate);

            Calendar calendarTestEndOfDay = Calendar.getInstance();
            calendarTestEndOfDay.setTime(startDate);
            int hoursEndOfDay = calendarTestEndOfDay.get(Calendar.HOUR_OF_DAY);
            int minutesEndOfDay = calendarTestEndOfDay.get(Calendar.MINUTE);
            if (hoursEndOfDay == 0 && minutesEndOfDay == 0)
            {
                if (repair)
                {

                    System.out.println("New startdate: " + startDate);
                    System.out.println("Day has reached end");
                    sourceProperties.nullHistoricalPush(sourcePropertyIndex);
                    System.exit(0);
                } else
                {
                    for (HashMap<String, Object> tag : tagResults)
                    {
                        String tagId = tag.get("Id").toString().trim();
                        reductions10.get(Long.parseLong(tagId)).resetTotalizer();
                        reductions30.get(Long.parseLong(tagId)).resetTotalizer();
                        reductions60.get(Long.parseLong(tagId)).resetTotalizer();
                        reductions720.get(Long.parseLong(tagId)).resetTotalizer();
                        reductions1440.get(Long.parseLong(tagId)).resetTotalizer();
                    }
                    System.out.println("Reset potential totalizers buckets for " + tagResults.size() + " tag(s).");

                }
            }

            if (!repair)
            {
                //Update tagsource table
                String finishedUpdateStatement = "Update TagSource set LastData=?, LastRun=? where Id=?";
                PreparedStatement prepStmt2 = arcSQL1.createPrepared(finishedUpdateStatement);
                DateTimeOffset dateNow = new DateTimeOffset();
                DateTimeOffset lastDate = new DateTimeOffset("yyyy-MM-dd HH:mm:ss", dateFormat.format(endDate));
                prepStmt2.setString(1, lastDate.toString());
                prepStmt2.setString(2, dateNow.toString());
                prepStmt2.setLong(3, sourceId);
                arcSQL1.execUpdatePrepared(prepStmt2, false, true);

            }

            while ((endDate.getTime()
                    + (DELAY)) > System.currentTimeMillis())
            {
                long secondsLeft = ((endDate.getTime() + (DELAY)) - System.currentTimeMillis()) / 1000;
                System.out.println("Sleeping... " + secondsLeft + " seconds to go...");

                if ((setFutures || updateLastValues) && catchingUp)
                {
                    System.out.println("Turning on SetFutures/UpdateLastValues as we have caught up...");
                    catchingUp = false;
                }
                //If more than 30 seconds left, test tag size count to see if any new tags came up
                if (secondsLeft > 30)
                {
                    testTagResults = arcSQL1.queryRaw(tagQuery);
                    int newTags = testTagResults.size() - tagResults.size();
                    if (newTags != 0)
                    {
                        System.out.println("(" + newTags + ") new tags found in system -- Exiting");
                        System.exit(0);
                    }
                }
                Thread.sleep(Math.abs((endDate.getTime() + (DELAY)) - System.currentTimeMillis()));

            }

        }
    }

    private static Date getRoundedDate(int bucket, Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int minutes = calendar.get(Calendar.MINUTE);
        int hours;
        Date tempDate;

        if (bucket == 10)
        {
            return date;
        }

        if (bucket == 30)
        {
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
            return new Date(date.getTime() + timeToAdd);
        }
        if (bucket == 60)
        {
            int timeToAdd = 0;
            if (minutes > 0)
            {
                timeToAdd = (60 - minutes) * 60 * 1000;
            }
            return new Date(date.getTime() + timeToAdd);
        }

        if (bucket == 720)
        {
            int timeToAdd = 0;
            if (minutes > 0)
            {
                timeToAdd = (60 - minutes) * 60 * 1000;
            }
            tempDate = new Date(date.getTime() + timeToAdd);
            timeToAdd = 0;
            calendar.setTime(tempDate);

            hours = calendar.get(Calendar.HOUR_OF_DAY);
            if (hours > 12)
            {
                timeToAdd = (24 - hours) * 60 * 60 * 1000;
            } else if (hours < 12 && hours > 0)
            {
                timeToAdd = (12 - hours) * 60 * 60 * 1000;
            }
            return new Date(tempDate.getTime() + timeToAdd);

        }
        if (bucket == 1440)
        {
            int timeToAdd = 0;
            if (minutes > 0)
            {
                timeToAdd = (60 - minutes) * 60 * 1000;
            }
            tempDate = new Date(date.getTime() + timeToAdd);
            calendar.setTime(tempDate);

            hours = calendar.get(Calendar.HOUR_OF_DAY);
            timeToAdd = 0;
            if (hours > 0)
            {
                timeToAdd = (24 - hours) * 60 * 60 * 1000;
            }

            return new Date(tempDate.getTime() + timeToAdd);

        }

        return date;

    }

}
