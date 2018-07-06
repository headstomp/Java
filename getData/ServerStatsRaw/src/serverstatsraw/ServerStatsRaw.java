/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverstatsraw;

import datatosql.ImporterProperties;
import datatosql.LocationTable;
import datatosql.SQLDriverTable;
import datatosql.SQLManager;
import datatosql.SourceTable;
import datatosql.Utilities;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ServerStatsRaw
{

    private static final String ARC_SQL_SERVER = "tf-sql101dag2.greenfieldethanol.com";
    private static final String ARC_SQL_INSTANCE = "MSSQLSERVER";
    private static final String ARC_SQL_USER = "arc";
    private static final String ARC_SQL_PASS = "eryornudie";
    private static final String ARC_SQL_DB = "Gfsa_Arc";
    private static final String ARC_SQL_DRIVER = SQLDriverTable.MSSQL.sqlDriver;

    private static final String PHOENIX_SQL_SERVER = "10.220.60.12";
    private static final String PHOENIX_SQL_DRIVER = SQLDriverTable.PhoenixSQL.sqlDriver;

    private static final int SLEEP = 2;

    private static final String LOG_PATH = "C:\\arc\\Logs\\ServerStats\\";

    private static final int SOURCE_ID = SourceTable.SYSStats.sourceId;
    private static final int LOCATION_ID = LocationTable.getLocationId(LocationTable.GFSA.name());
    private static final String LOCATION = LocationTable.GFSA.name();
    private static final String RAW_TABLE = "SYS_RAW";
    private static final String REDUCTION_TABLE = "SYS_REDUCTIONS";
    private static final String SOURCE = "GFSA - Server Statistics";
    private static final String AREA = "IT Operations";

    /**
     *
     * @param args
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     * @throws java.lang.InterruptedException
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException, FileNotFoundException
    {

        File log = new File(LOG_PATH + LOCATION + "_RAW_" + new SimpleDateFormat("yyyMMdd_HHmmssSSS").format(Calendar.getInstance().getTime()) + ".log");
        FileOutputStream fos = new FileOutputStream(log);
        PrintStream ps = new PrintStream(fos);
        System.setErr(ps);
        System.setOut(ps);

        String tagQuery = "SELECT * FROM dbo.Tag WHERE TagSourceId = '" + SOURCE_ID + "' and IsActive=1";

        List<HashMap<String, String>> hosts = new ArrayList<>();
        ImporterProperties sourceProperties = new ImporterProperties(SOURCE);
        for (int z = 0; z < sourceProperties.size(); z++)
        {
            if (sourceProperties.getPropertyName(z).equalsIgnoreCase("HOST"))
            {
                HashMap<String, String> tempMap = new HashMap<>();
                tempMap.put("name", sourceProperties.getProperty1(z).toLowerCase());
                hosts.add(tempMap);

            }
        }
        int initial = hosts.size();
        SQLManager arcSQL1 = new SQLManager(ARC_SQL_SERVER, ARC_SQL_INSTANCE, ARC_SQL_USER, ARC_SQL_PASS, ARC_SQL_DB, ARC_SQL_DRIVER);
        SQLManager phoenixSQL1 = new SQLManager(PHOENIX_SQL_SERVER, PHOENIX_SQL_DRIVER);

        //Find out what tags are in the system....
        List<HashMap<String, Object>> tagResults = arcSQL1.queryRaw(tagQuery);
        for (HashMap<String, Object> list : tagResults)
        {
            String sourceTagName = list.get("SourceTagName").toString();
            String tagName = list.get("Name").toString();
            Long tagId = Long.parseLong(list.get("Id").toString());

            for (HashMap host : hosts)
            {
                String hostName = host.get("name").toString();

                if (hostName.equalsIgnoreCase(sourceTagName))
                {
                    for (Stats stat : Stats.values())
                    {
                        if (tagName.equalsIgnoreCase(sourceTagName + stat.aspect))
                        {
                            host.put(stat.aspect, tagId);

                        }
                    }
                }
            }

        }

        //if the tag id is null, create tags
        for (HashMap host : hosts)
        {
            String hostName = host.get("name").toString();
            for (Stats stat : Stats.values())
            {
                if (host.get(stat.aspect) == null)
                {
                    host.put(stat.aspect, createTag(host, stat.aspect, stat.description));
                }
            }

        }

        String prepStmtRaw = "";
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        //At this point all tags should be created
        while (true)
        {
            for (HashMap host : hosts)
            {

                String hostName = host.get("name").toString();

                System.out.println(hostName);

                for (Stats stat : Stats.values())
                {
                    Double value = null;
                    Long tagId = null;
                    Date date = null;
                    if (host.get(stat.aspect) != null)
                    {
                        tagId = Long.parseLong(host.get(stat.aspect).toString());
                    }

                    if (stat.equals(Stats.CPU_UTILIZATION))
                    {
                        date = new Date();
                        value = getCPUAvg(hostName);

                    }
                    if (stat.equals(Stats.RAM_UTILIZATION))
                    {
                        date = new Date();
                        value = getRAMpct(hostName);
                    }
                    if (stat.equals(Stats.RAM_USED))
                    {
                        date = new Date();
                        value = getRAMusage(hostName);
                    }
                    if (stat.equals(Stats.CDISK_UTILIZATION))
                    {
                        date = new Date();
                        value = getDISKpct(hostName);
                    }
                    if (stat.equals(Stats.CDISK_USED))
                    {
                        date = new Date();
                        value = getDISKusage(hostName);
                    }
                    if (stat.equals(Stats.NET_SENT))
                    {
                        date = new Date();
                        value = getETHsent(hostName);
                    }
                    if (stat.equals(Stats.NET_REC))
                    {
                        date = new Date();
                        value = getETHrec(hostName);
                    }
                    if (stat.equals(Stats.UP_TIME))
                    {
                        date = new Date();
                        value = getUPtime(hostName);
                    }

                    if (value != null && tagId != null)
                    {

                        prepStmtRaw = "UPSERT INTO " + RAW_TABLE + "(DATETIME, TAGID, LASTVALUE) VALUES ('" + dateTimeFormat.format(date) + "'" + ", " + tagId + ", " + value + ")";
                        phoenixSQL1.updateRaw(prepStmtRaw);
                        System.out.println(stat.aspect + " - " + prepStmtRaw);
                    }

                }

                System.out.println();

            }

            int newCount = 0;
            sourceProperties = new ImporterProperties(SOURCE);
            for (int z = 0; z < sourceProperties.size(); z++)
            {
                if (sourceProperties.getPropertyName(z).equalsIgnoreCase("HOST"))
                {
                    newCount += 1;
                }
            }
            if (newCount > initial)
            {
                System.out.println("New hosts added -- Exiting...");
                System.exit(0);
            }
            System.out.println("Sleeping for " + SLEEP + " mins...");
            Thread.sleep(1000 * 60 * SLEEP);
        }
    }

    public static String createTag(HashMap host, String tagAspect, String description) throws SQLException, ClassNotFoundException
    {
        SQLManager arcSQL1 = new SQLManager(ARC_SQL_SERVER, ARC_SQL_INSTANCE, ARC_SQL_USER, ARC_SQL_PASS, ARC_SQL_DB, ARC_SQL_DRIVER);
        String statement = "Insert into Tag (TagSourceId, DataLocationId, LocationId, TagTypeId, Name, SourceTagName, Description, Area, RawTableName, ReducedTableName) select top 1 ?,?,?,?,?,?,?,?,?,? from Tag where not exists (select Name, TagSourceId from Tag where Tag.Name = ? and Tag.TagSourceId = ?) ";

        String tagName = host.get("name") + tagAspect;
        String sourceTagName = host.get("name").toString();
        int dataLocationId = 4;
        int tagTypeId = 1;
        PreparedStatement prepStmt1;
        prepStmt1 = arcSQL1.createPrepared(statement);

        prepStmt1.setLong(1, SOURCE_ID);
        prepStmt1.setLong(2, dataLocationId);
        prepStmt1.setLong(3, LOCATION_ID);
        prepStmt1.setLong(4, tagTypeId);
        prepStmt1.setString(5, tagName);
        prepStmt1.setString(6, sourceTagName);
        prepStmt1.setString(7, description);
        prepStmt1.setString(8, AREA);
        prepStmt1.setString(9, RAW_TABLE);
        prepStmt1.setString(10, REDUCTION_TABLE);

        prepStmt1.setString(11, tagName);
        prepStmt1.setLong(12, SOURCE_ID);

        Long newTag = arcSQL1.execUpdatePrepared(prepStmt1, false, true);
        System.out.println("Inserted: Name: " + tagName + " SourceName: " + sourceTagName + " Description: " + description + " Area: " + AREA + " LocationID: " + LOCATION_ID + " SourceId: " + SOURCE_ID + " DataLocationId: " + dataLocationId + " RawTable: " + RAW_TABLE + " ReductionTable: " + REDUCTION_TABLE + " TagType: " + tagTypeId + " New Tag: " + newTag);

        return newTag.toString();
    }

    //returns average of CPU of all cores
    public static Double getCPUAvg(String hostName)
    {
        String uri = "https://" + hostName + Stats.CPU_UTILIZATION.api;

        JSONObject obj = Utilities.getJSONObj(uri);
        JSONArray elem = null;
        //System.out.println(obj.get("percent"));
        if (obj != null)
        {
            elem = (JSONArray) obj.get("percent");
        }
        int count = 0;
        double sum = 0;
        if (elem != null)
        {
            for (Object elem1 : elem)
            {
                JSONArray element = null;
                try
                {
                    element = (JSONArray) elem1;
                } catch (Exception e)
                {

                }

                if (element != null)
                {
                    for (Object elem2 : element)
                    {
                        if (Utilities.isNumeric(elem2.toString()))
                        {
                            sum += Double.parseDouble(elem2.toString());
                            count += 1;
                        }
                    }
                }

            }
        }
        if (count == 0)
        {
            return null;
        } else
        {
            return sum / count;
        }

    }

    //returns RAM percentage
    public static Double getRAMpct(String hostName)
    {
        String uri = "https://" + hostName + Stats.RAM_UTILIZATION.api;

        JSONObject obj = Utilities.getJSONObj(uri);
        JSONArray elem = null;
        //System.out.println(obj.get("percent"));
        if (obj != null)
        {
            elem = (JSONArray) obj.get("percent");
        }
        Double pct = null;
        if (elem != null)
        {
            for (Object elem1 : elem)
            {
                if (elem1 != null)
                {

                    if (Utilities.isNumeric(elem1.toString()))
                    {
                        pct = Double.parseDouble(elem1.toString());
                    }
                }

            }
        }
        return pct;
    }

    //Returns RAM usage in MB
    public static Double getRAMusage(String hostName)
    {
        String uri = "https://" + hostName + Stats.RAM_USED.api;
        JSONObject obj = Utilities.getJSONObj(uri);
        JSONArray elem = null;
        //System.out.println(obj.get("percent"));
        if (obj != null)
        {
            elem = (JSONArray) obj.get("used");
        }
        Double used = null;
        if (elem != null)
        {
            for (Object elem1 : elem)
            {
                if (elem1 != null)
                {

                    if (Utilities.isNumeric(elem1.toString()))
                    {
                        used = Double.parseDouble(elem1.toString());
                        used = used / 1024 / 1024;
                    }
                }

            }
        }
        return used;
    }

    //returns C DISK percentage
    public static Double getDISKpct(String hostName)
    {
        String uri = "https://" + hostName + Stats.CDISK_UTILIZATION.api;
        JSONObject obj = Utilities.getJSONObj(uri);
        JSONArray elem = null;
        //System.out.println(obj.get("percent"));
        if (obj != null)
        {
            elem = (JSONArray) obj.get("used_percent");
        }
        Double pct = null;
        if (elem != null)
        {
            for (Object elem1 : elem)
            {
                if (elem1 != null)
                {

                    if (Utilities.isNumeric(elem1.toString()))
                    {
                        pct = Double.parseDouble(elem1.toString());
                    }
                }

            }
        }
        return pct;
    }

    //Returns CDisk usage in MB
    public static Double getDISKusage(String hostName)
    {
        String uri = "https://" + hostName + Stats.CDISK_USED.api;

        JSONObject obj = Utilities.getJSONObj(uri);
        JSONArray elem = null;
        //System.out.println(obj.get("percent"));
        if (obj != null)
        {
            elem = (JSONArray) obj.get("used");
        }
        Double used = null;
        if (elem != null)
        {
            for (Object elem1 : elem)
            {
                if (elem1 != null)
                {

                    if (Utilities.isNumeric(elem1.toString()))
                    {
                        used = Double.parseDouble(elem1.toString());
                        used = used / 1024 / 1024;
                    }
                }

            }
        }

        return used;
    }

    //Returns uptime in Days
    public static Double getUPtime(String hostName)
    {
        String uri = "https://" + hostName + Stats.UP_TIME.api;
        JSONObject obj = Utilities.getJSONObj(uri);
        JSONArray elem = null;
        //System.out.println(obj.get("percent"));
        if (obj != null)
        {
            elem = (JSONArray) obj.get("uptime");
        }
        Double time = null;
        if (elem != null)
        {
            for (Object elem1 : elem)
            {
                if (elem1 != null)
                {

                    if (Utilities.isNumeric(elem1.toString()))
                    {
                        time = Double.parseDouble(elem1.toString());
                        time = time / 60 / 60 / 24;
                    }
                }

            }
        }
        return time;
    }

    //Returns network mb sent
    public static Double getETHsent(String hostName)
    {
        String uri = "https://" + hostName + Stats.NET_SENT.api;
        JSONObject obj = Utilities.getJSONObj(uri);
        JSONArray elem = null;
        //System.out.println(obj.get("percent"));
        if (obj != null)
        {
            elem = (JSONArray) obj.get("bytes_sent");
        }
        Double sent = null;
        if (elem != null)
        {
            for (Object elem1 : elem)
            {
                if (elem1 != null)
                {

                    if (Utilities.isNumeric(elem1.toString()))
                    {
                        sent = Double.parseDouble(elem1.toString());
                        sent = sent / 1024 / 1024;
                    }
                }

            }
        }

        return sent;
    }

    //Returns network mb received
    public static Double getETHrec(String hostName)
    {
        String uri = "https://" + hostName + Stats.NET_REC.api;
        JSONObject obj = Utilities.getJSONObj(uri);
        JSONArray elem = null;
        //System.out.println(obj.get("percent"));
        if (obj != null)
        {
            elem = (JSONArray) obj.get("bytes_recv");
        }
        Double received = null;
        if (elem != null)
        {
            for (Object elem1 : elem)
            {
                if (elem1 != null)
                {

                    if (Utilities.isNumeric(elem1.toString()))
                    {
                        received = Double.parseDouble(elem1.toString());
                        received = received / 1024 / 1024;
                    }
                }

            }
        }
        return received;
    }

}
