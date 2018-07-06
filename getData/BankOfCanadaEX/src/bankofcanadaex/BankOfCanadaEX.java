/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bankofcanadaex;

import datatosql.DateTimeOffset;
import datatosql.LocationTable;
import datatosql.PushToSQL;
import datatosql.Utilities;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import datatosql.ValueItem;
import datatosql.ValueTable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.text.WordUtils;
import org.w3c.dom.NodeList;

/**
 *
 * @author sandeep.gainda
 */
public class BankOfCanadaEX
{

    private static final String LOCATION = LocationTable.GFSA.name();
    private static final String SOURCE = "GFSA - Bank of Canada Exchange Rates";
    private static final String DESCRIPTION = "Daily FX rates and legacy noon rates";
    private static final String QLIKVIEW = "Daily";
    private static final boolean PUSH = true;

    /**
     * @param args the command line arguments
     * @throws java.net.MalformedURLException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    public static void main(String[] args) throws IOException, MalformedURLException, XPathExpressionException
    {

        System.out.println(LOCATION);
        System.out.println(SOURCE);
        ArrayList<ValueItem> values = new ArrayList();

        Calendar c = Calendar.getInstance();
        int previousYear = c.get(Calendar.YEAR) - 1;
        //   values = getNoon(Utilities.getDocument("http://www.bankofcanada.ca/stats/assets/xml/noon-five-day.xml"), values);
        //  values = getCloseUSD(Utilities.getDocument("http://www.bankofcanada.ca/stats/results//p_xml?rangeType=range&rangeValue=5&lP=lookup_daily_exchange_rates.php&se=_0102"), values);
        //values = getCloseUSD(Utilities.getDocument("http://www.bankofcanada.ca/stats/results//p_xml?rangeType=dates&lP=lookup_daily_exchange_rates.php&sR=2006-12-12&se=_0102&dF=2006-12-12&dT=2016-12-12"), values);
        values = getClose(Utilities.getDocument("https://www.bankofcanada.ca/valet/observations/group/FX_RATES_DAILY/xml?start_date=" + previousYear + "-12-01"), values);
        //////////////////////
        try
        {

            if (PUSH)
            {
                PushToSQL pusher1 = new PushToSQL(SOURCE, LOCATION, values);
                // pusher1.updateMetaTable(DESCRIPTION, QLIKVIEW);
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
            Logger.getLogger(BankOfCanadaEX.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static ArrayList<ValueItem> getClose(Document document, ArrayList<ValueItem> values) throws XPathExpressionException
    {

        //Grab details first
        XPath xPath = XPathFactory.newInstance().newXPath();
        String seriesExpression = "//data/seriesDetail";
        NodeList seriesDataNodes = (NodeList) xPath.compile(seriesExpression).evaluate(document, XPathConstants.NODESET);

        Map<String, Currency> currencies = new HashMap<>();

        for (int i = 0; i < seriesDataNodes.item(0).getChildNodes().getLength(); i++)
        {
            Currency currency = null;
            if (seriesDataNodes.item(0).getChildNodes().item(i).getAttributes() != null)
            {
                currency = new Currency(seriesDataNodes.item(0).getChildNodes().item(i).getAttributes().getNamedItem("id").getNodeValue().trim().toUpperCase());
                //System.out.print(seriesDataNodes.item(0).getChildNodes().item(i).getAttributes().getNamedItem("id").getNodeValue() + " ");
                for (int j = 0; j < seriesDataNodes.item(0).getChildNodes().item(i).getChildNodes().getLength(); j++)
                {
                    if (seriesDataNodes.item(0).getChildNodes().item(i).getChildNodes().item(j).getNodeName().equalsIgnoreCase("label"))
                    {
                        currency.setShortDescirption(seriesDataNodes.item(0).getChildNodes().item(i).getChildNodes().item(j).getFirstChild().getNodeValue().trim().toUpperCase());
                        //System.out.print(seriesDataNodes.item(0).getChildNodes().item(i).getChildNodes().item(j).getFirstChild().getNodeValue() + " ");

                    }
                    if (seriesDataNodes.item(0).getChildNodes().item(i).getChildNodes().item(j).getNodeName().equalsIgnoreCase("description"))
                    {
                        currency.setLongDescription(seriesDataNodes.item(0).getChildNodes().item(i).getChildNodes().item(j).getFirstChild().getNodeValue().trim());
                        //System.out.println(seriesDataNodes.item(0).getChildNodes().item(i).getChildNodes().item(j).getFirstChild().getNodeValue());

                    }
                }
                /*
                 System.out.print(currency.getId() + " ");
                 System.out.print(currency.getShortDescription() + " ");
                 System.out.println(currency.getLongDescription());*/
            }
            if (currency != null)
            {
                currencies.put(currency.getId(), currency);
            }

        }

        String observationExpression = "//data/observations/o";
        NodeList observationDataNodes = (NodeList) xPath.compile(observationExpression).evaluate(document, XPathConstants.NODESET);
        DateTimeOffset date = null;
        String groupId = null;

        for (int i = 0; i < observationDataNodes.getLength(); i++)
        {

            date = new DateTimeOffset("yyyy-MM-dd HH:mm:ss", observationDataNodes.item(i).getAttributes().getNamedItem("d").getNodeValue().trim() + " 16:30:00");
            groupId = observationDataNodes.item(i).getAttributes().getNamedItem("d").getNodeValue().replace("-", "").trim();
            System.out.print(observationDataNodes.item(i).getAttributes().getNamedItem("d").getNodeValue() + " ");
            for (int j = 0; j < observationDataNodes.item(i).getChildNodes().getLength(); j++)
            {
                String name = null;
                String value = null;
                String description = null;

                if (observationDataNodes.item(i).getChildNodes().item(j).getFirstChild() != null)
                {
                    name = observationDataNodes.item(i).getChildNodes().item(j).getAttributes().getNamedItem("s").getNodeValue().trim().toUpperCase();
                    value = observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue().trim();
                    description = currencies.get(name).getShortDescription() + " " + currencies.get(name).getLongDescription() + ".";
                    System.out.print(name + " ");
                    System.out.print(value + " ");

                    if (Utilities.isNumeric(value))
                    {
                        values.add(new ValueItem(name, LOCATION, "Finance", description, null, groupId, ValueTable.Decimal.name(), date, value));
                    }
                }
            }
            System.out.println();

        }

        return values;
    }

    public static ArrayList<ValueItem> getCloseUSD(Document document, ArrayList<ValueItem> values) throws MalformedURLException, IOException, XPathExpressionException
    {
        XPath xPath = XPathFactory.newInstance().newXPath();
        String observationExpression = "//Currency/Observation";
        NodeList observationDataNodes = (NodeList) xPath.compile(observationExpression).evaluate(document, XPathConstants.NODESET);

        String groupId = null;
        for (int i = 0; i < observationDataNodes.getLength(); i++)
        {

            String description = null;
            String name = null;
            DateTimeOffset date = null;
            String value = null;

            for (int j = 0; j < observationDataNodes.item(i).getChildNodes().getLength(); j++)
            {
                if (observationDataNodes.item(i).getChildNodes().item(j).getFirstChild() != null)
                {

                    if ("Currency_name".equals(observationDataNodes.item(i).getChildNodes().item(j).getNodeName()))
                    {

                        description = observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue();
                        description = WordUtils.capitalize(description.trim());
                        System.out.println("Name: USD_CLOSE");
                        System.out.println("Description: " + description);
                    }

                    if ("Observation_date".equals(observationDataNodes.item(i).getChildNodes().item(j).getChildNodes().item(1).getNodeName()))
                    {
                        date = new DateTimeOffset("yyyy-MM-dd HH:mm:ss", observationDataNodes.item(i).getChildNodes().item(j).getChildNodes().item(1).getFirstChild().getNodeValue() + " 16:30:00");
                        groupId = observationDataNodes.item(i).getChildNodes().item(j).getChildNodes().item(1).getFirstChild().getNodeValue().replace("-", "").trim();
                        System.out.println("Date " + date);

                    }

                    if ("Observation_data".equals(observationDataNodes.item(i).getChildNodes().item(j).getChildNodes().item(3).getNodeName()))
                    {

                        value = observationDataNodes.item(i).getChildNodes().item(j).getChildNodes().item(3).getFirstChild().getNodeValue();
                        if (!Utilities.isNumeric(value))
                        {
                            value = null;
                        }
                        System.out.println("Value " + value);
                    }

                }

            }

            name = "USD_CLOSE";
            values.add(new ValueItem(name, LOCATION, "Finance", description, null, groupId, ValueTable.Decimal.name(), date, value));

        }
        return values;

    }

    public static ArrayList<ValueItem> getNoon(Document document, ArrayList<ValueItem> values) throws MalformedURLException, IOException, XPathExpressionException
    {
        XPath xPath = XPathFactory.newInstance().newXPath();
        String observationExpression = "//Currency/Observation";
        NodeList observationDataNodes = (NodeList) xPath.compile(observationExpression).evaluate(document, XPathConstants.NODESET);

        String groupId = null;
        for (int i = 0; i < observationDataNodes.getLength(); i++)
        {

            String description = null;
            String name = null;
            DateTimeOffset date = null;
            String value = null;

            for (int j = 0; j < observationDataNodes.item(i).getChildNodes().getLength(); j++)
            {
                if (observationDataNodes.item(i).getChildNodes().item(j).getFirstChild() != null)
                {

                    if ("Currency_name".equals(observationDataNodes.item(i).getChildNodes().item(j).getNodeName()))
                    {
                        description = observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue();
                        System.out.println("Name " + observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue());
                    }
                    if ("Observation_ISO4217".equals(observationDataNodes.item(i).getChildNodes().item(j).getNodeName()))
                    {
                        name = observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue();
                        System.out.println("Symbol " + observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue());
                    }
                    if ("Observation_date".equals(observationDataNodes.item(i).getChildNodes().item(j).getNodeName()))
                    {
                        date = new DateTimeOffset("yyyy-MM-dd HH:mm:ss", observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue() + " 12:00:00");
                        groupId = observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue().replace("-", "").trim();
                        System.out.println("Date " + observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue());

                    }
                    if ("Observation_data".equals(observationDataNodes.item(i).getChildNodes().item(j).getNodeName()))
                    {
                        value = observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue();
                        if (!Utilities.isNumeric(value))
                        {
                            value = null;
                        }
                        System.out.println("Value " + observationDataNodes.item(i).getChildNodes().item(j).getFirstChild().getNodeValue());
                    }

                }

            }

            values.add(new ValueItem(name, LOCATION, "Finance", description, null, groupId, ValueTable.Decimal.name(), date, value));

        }
        return values;

    }

}
