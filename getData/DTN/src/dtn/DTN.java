/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dtn;

import datatosql.DateTimeOffset;
import datatosql.ImporterProperties;
import datatosql.LocationTable;
import java.util.logging.Level;
import java.util.logging.Logger;
import datatosql.PushToSQL;
import datatosql.Utilities;
import datatosql.ValueItem;
import datatosql.ValueTable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author sandeep.gainda
 */
public class DTN
{

    private static final String LOCATION = LocationTable.GFSA.name();
    private static final String DESCRIPTION = "DTN Webservice Importer";
    private static final String SOURCE = "GFSA - DTN Market Data";
    private static final String QLIKVIEW = "Daily";
    private static final String AREA = "Market";
    private static final int INTERVAL = 1;
    private static final boolean PUSH = true;

    /**
     * @param args the command line arguments
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.IOException
     * @throws javax.xml.xpath.XPathExpressionException
     * @throws java.sql.SQLException
     */
    public static void main(String[] args) throws ClassNotFoundException, IOException, XPathExpressionException, SQLException
    {

        ArrayList<ValueItem> values = new ArrayList();
        boolean historicalPush;
        ImporterProperties sourceProperties = new ImporterProperties(SOURCE);
        int limit;

        for (int z = 0; z < sourceProperties.size(); z++)
        {
            if (sourceProperties.getHistoricalPush(z) == null)
            {
                //do last 7 days
                historicalPush = false;
                limit = 7;
            } else
            {
                historicalPush = true;
                limit = Math.abs(sourceProperties.getHistoricalPush(z).daysBetweenNow());
            }
            String requestedSymbol = sourceProperties.getPropertyName(z);
            String market = sourceProperties.getProperty1(z);
            String vendor = sourceProperties.getProperty2(z);
            String description = sourceProperties.getPropertyDescription(z);
            String units = sourceProperties.getProperty3(z);

            String uri = "http://ws2.dtn.com/COMALC/fiminetws2.asmx/GetInterdayHistoryV?UserID=COMALC&Password=&ID=&Symbol=" + requestedSymbol + "&Market=" + market + "&Vendor=" + vendor + "&Interval=" + INTERVAL + "&Limit=" + limit;
            System.out.println(uri);
            Document document = Utilities.getDocument(uri);
            

            XPath xPath = XPathFactory.newInstance().newXPath();

            String dtnDataExpression = "//History/Day";
            NodeList dtnDataNodes = (NodeList) xPath.compile(dtnDataExpression).evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < dtnDataNodes.getLength(); i++)
            {

                String date = dtnDataNodes.item(i).getAttributes().getNamedItem("Date").getNodeValue().trim();
                DateTimeOffset valueDateTimeOffset = new DateTimeOffset("yyyy-MM-dd HH:mm:ss", date+" 16:00:00");
                String groupId = valueDateTimeOffset.toRawUTCString();

                String openValue, closeValue, highValue, lowValue;
                openValue = checkDouble(dtnDataNodes.item(i).getAttributes().getNamedItem("Open").getNodeValue().trim());
                closeValue = checkDouble(dtnDataNodes.item(i).getAttributes().getNamedItem("Close").getNodeValue().trim());
                highValue = checkDouble(dtnDataNodes.item(i).getAttributes().getNamedItem("High").getNodeValue().trim());
                lowValue = checkDouble(dtnDataNodes.item(i).getAttributes().getNamedItem("Low").getNodeValue().trim());

                values.add(new ValueItem(requestedSymbol + " - Open", LOCATION, AREA, description + " - Opening Price (" + units + ")", null, groupId, ValueTable.Decimal.name(), valueDateTimeOffset, openValue));
                values.add(new ValueItem(requestedSymbol + " - Close", LOCATION, AREA, description + " - Closing Price (" + units + ")", null, groupId, ValueTable.Decimal.name(), valueDateTimeOffset, closeValue));
                values.add(new ValueItem(requestedSymbol + " - High", LOCATION, AREA, description + " - High Price (" + units + ")", null, groupId, ValueTable.Decimal.name(), valueDateTimeOffset, highValue));
                values.add(new ValueItem(requestedSymbol + " - Low", LOCATION, AREA, description + " - Low Price (" + units + ")", null, groupId, ValueTable.Decimal.name(), valueDateTimeOffset, lowValue));

            }

            if (historicalPush)
            {
                //We are done now, so nullify the history push
                sourceProperties.nullHistoricalPush(z);
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
                values.stream().forEach((v) ->
                {
                    System.out.println(v);
                });
            }
        } catch (ClassNotFoundException ex)
        {
            Logger.getLogger(DTN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex)
        {
            Logger.getLogger(DTN.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("");

    }

    private static String checkDouble(String value)
    {
        Double testValue;
        try
        {

            testValue = Double.parseDouble(value);
            return testValue.toString();

        } catch (NumberFormatException e)
        {
            System.out.println("Double" + " -> " + value + " NOT A VALID DOUBLE OR VALUE NOT AVAILABLE");
            return null;
        }
    }

}
