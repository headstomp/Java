/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package environmentcanada;

import datatosql.DateTimeOffset;
import datatosql.LocationTable;
import datatosql.PushToSQL;
import datatosql.ImporterProperties;
import datatosql.Utilities;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import datatosql.ValueItem;
import datatosql.ValueTable;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.commons.lang3.text.WordUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sandeep.gainda
 */
public class EnvironmentCanada
{

    private static final String LOCATION = LocationTable.GFSA.name();
    private static final String SOURCE = "GFSA - Environment Canada";
    private static final String DESCRIPTION = "Historical import from climate.weather.gc.ca (Environment Canada)";
    private static final String QLIKVIEW = "Daily";
    private static final boolean PUSH = true;

    /**
     * @param args the command line arguments
     * @throws java.net.MalformedURLException
     * @throws javax.xml.xpath.XPathExpressionException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public static void main(String[] args) throws MalformedURLException, IOException, XPathExpressionException, ClassNotFoundException, SQLException
    {
        ArrayList<ValueItem> values = new ArrayList();
        boolean historicalPush = false;
        boolean doneHistoricalPush = true;

        int year = 2015;
        int month = 1;
        int day = 0;
        boolean daily = false;
        DateTimeOffset historicalDate = null;
        Calendar historicalCal = null;
        System.out.println(LOCATION);
        System.out.println(SOURCE);

        if (args.length > 1 && args.length < 3)
        {
            try
            {
                year = Integer.valueOf(args[0]);
                month = Integer.valueOf(args[1]);
                if (args.length == 4)
                {
                    day = Integer.valueOf(args[2]);
                }
            } catch (NumberFormatException e)
            {
                System.err.println("Arguments must be valid integers.");
                System.exit(1);
            }

        } else if (args.length == 0)
        {
            historicalPush = true;

        }
        else
        {

            System.err.println("Invalid Arguments");
            System.exit(1);
        }

        ImporterProperties sourceProperties = new ImporterProperties(SOURCE);

        for (int z = 0; z < sourceProperties.size(); z++)
        {
            if (historicalPush)
            {
                doneHistoricalPush = false;
            }
            String tagLocation = sourceProperties.getPropertyName(z);
            String stationId = sourceProperties.getProperty1(z);
            String granularity = sourceProperties.getProperty2(z);

            String timeFrame = "";
            switch (granularity.toUpperCase())
            {
                case "HOURLY":
                    daily = false;
                    granularity = "Hourly";
                    timeFrame = "1";
                    break;
                case "DAILY":
                    daily = true;
                    granularity = "Daily";
                    timeFrame = "2";
                    break;
                default:
                    System.out.println("Invalid Granularity: " + granularity);
                    System.exit(1);
                    break;
            }

            tagLocation = LocationTable.getLocation(tagLocation).name();

            if (historicalPush)
            {

                if (sourceProperties.getHistoricalPush(z) != null)
                {
                    //Start Historical Date to date value
                    historicalDate = sourceProperties.getHistoricalPush(z);
                    historicalCal = historicalDate.getCalendar();
                    year = historicalCal.get(Calendar.YEAR);
                    month = historicalCal.get(Calendar.MONTH) + 1;
                    day = 0;
                } else
                {
                    //Only doing sites that have a historical push value
                    continue;
                }

            }

            do
            {

                String uri = "http://climate.weather.gc.ca/climate_data/bulk_data_e.html?&stationID=" + stationId + "&Month=" + month + "&timeframe=" + timeFrame + "&Year=" + year + "&format=xml";
                
                System.out.println(uri);
                
                URL url = new URL(uri);
                Document document = Utilities.getDocument(uri);
                
            
            
                XPath xPath = XPathFactory.newInstance().newXPath();

                String stationNameExpression = "//climatedata/stationinformation/name";
                Node node = (Node) xPath.compile(stationNameExpression).evaluate(document, XPathConstants.NODE);
                String stationName = node.getFirstChild().getNodeValue();
                //System.out.println(stationName);

                //If location is of GFSA, then use station name as tag name
                if (tagLocation.equals(LocationTable.GFSA.name()))
                {
                    tagLocation = LocationTable.GFSA.name() + " - Station: " + stationName;
                }

                //Grabbing Compounds
                String stationDataExpression = "//climatedata/stationdata";
                NodeList stationDataNodes = (NodeList) xPath.compile(stationDataExpression).evaluate(document, XPathConstants.NODESET);

                for (int i = 0; i < stationDataNodes.getLength(); i++)
                {

                    Integer valueYear = Integer.parseInt(stationDataNodes.item(i).getAttributes().getNamedItem("year").getNodeValue());
                    Integer valueMonth = Integer.parseInt(stationDataNodes.item(i).getAttributes().getNamedItem("month").getNodeValue());
                    Integer valueDay = Integer.parseInt(stationDataNodes.item(i).getAttributes().getNamedItem("day").getNodeValue());
                    Integer valueHour = 0;
                    Integer valueMinute = 0;
                    if (!daily)
                    {
                        valueHour = Integer.parseInt(stationDataNodes.item(i).getAttributes().getNamedItem("hour").getNodeValue());
                        valueMinute = Integer.parseInt(stationDataNodes.item(i).getAttributes().getNamedItem("minute").getNodeValue());
                    }
                    //Only import the specific day if required (also keeps specific month)
                    if ((day != 0 && day != valueDay) || month != valueMonth)
                    {
                        continue;
                    }

                    DateTimeOffset valueDateTimeOffset = new DateTimeOffset(TimeZone.getTimeZone("EST5EDT"), false, valueYear, valueMonth, valueDay, valueHour, valueMinute, 0, 0);
                    String groupId = valueDateTimeOffset.toRawUTCString();
                    NodeList aspectNodes = stationDataNodes.item(i).getChildNodes();

                    for (int j = 0; j < aspectNodes.getLength(); j++)
                    {

                        String tagName = WordUtils.capitalizeFully(tagLocation + " - " + granularity + " " + aspectNodes.item(j).getAttributes().getNamedItem("description").getNodeValue());
                        String tagDescription;
                        String units;
                        if (aspectNodes.item(j).getFirstChild() != null)
                        {
                            if (aspectNodes.item(j).getAttributes().getNamedItem("units") != null)
                            {
                                units = aspectNodes.item(j).getAttributes().getNamedItem("units").getNodeValue();
                            } else
                            {
                                units = "no units";
                            }

                            tagDescription = WordUtils.capitalizeFully(stationName + " Climate Station - " + aspectNodes.item(j).getAttributes().getNamedItem("description").getNodeValue()) + " (" + units + ")";
                            String tagValue = aspectNodes.item(j).getFirstChild().getNodeValue();
                            System.out.println(valueDateTimeOffset.toString() + ": " + tagName + " (" + tagDescription + ") -> " + tagValue);
                            String tagType = aspectNodes.item(j).getAttributes().getNamedItem("description").getNodeValue().toUpperCase();

                            if (tagType.contains("HUMIDITY") || tagType.contains("WIND DIRECTION") || tagType.contains("WIND"))
                            {
                                try
                                {
                                    Integer.parseInt(tagValue);

                                } catch (NumberFormatException e)
                                {
                                    System.out.println(tagType + " -> " + tagValue + " NOT A VALID INTEGER OR VALUE NOT AVAILABLE");
                                    continue;
                                }

                                values.add(new ValueItem(tagName, tagLocation, null, tagDescription, null, groupId, ValueTable.Integer.name(), valueDateTimeOffset, tagValue));

                            } else if (tagType.contains("WEATHER"))
                            {
                                values.add(new ValueItem(tagName, tagLocation, null, tagDescription, null, groupId, ValueTable.String.name(), valueDateTimeOffset, tagValue));
                            } else
                            {
                                try
                                {
                                    Double.parseDouble(tagValue);

                                } catch (NumberFormatException e)
                                {
                                    System.out.println(tagType + " -> " + tagValue + " NOT A VALID DECIMAL OR VALUE NOT AVAILABLE");
                                    continue;
                                }
                                values.add(new ValueItem(tagName, tagLocation, null, tagDescription, null, groupId, ValueTable.Decimal.name(), valueDateTimeOffset, tagValue));
                            }

                        }

                    }

                }


                if (historicalPush)
                {
                    historicalCal.add(Calendar.MONTH, 1);
                    year = historicalCal.get(Calendar.YEAR);
                    month = historicalCal.get(Calendar.MONTH) + 1;

                    if (historicalCal.after(Calendar.getInstance(TimeZone.getTimeZone("UTC"))))
                    {
                        doneHistoricalPush = true;
                        sourceProperties.nullHistoricalPush(z);
                    }
                }
            } while (!doneHistoricalPush);

            //Only do 1 site if doing historical
            if (historicalPush && doneHistoricalPush)
            {
                break;
            }

        }

        //////////////////////
        try
        {

            if (PUSH)
            {
                PushToSQL pusher1 = new PushToSQL(SOURCE, LOCATION, values);
                pusher1.updateMetaTable(DESCRIPTION, QLIKVIEW);
                pusher1.push();

            } else
            {
                System.out.println("\nNot pushing...");

                values.stream().forEach((v) ->
                {
                    System.out.println(v);
                });
            }
        } catch (Exception ex)
        {
            Logger.getLogger(EnvironmentCanada.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
