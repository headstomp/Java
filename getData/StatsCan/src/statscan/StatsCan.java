/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statscan;

import datatosql.DateTimeOffset;
import datatosql.ImporterProperties;
import datatosql.LocationTable;
import datatosql.PushToSQL;
import datatosql.ValueItem;
import datatosql.ValueTable;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

/**
 *
 * @author sandeep.gainda
 */
public class StatsCan
{

    private static final String AREA = "Finance";
    private static final String DESCRIPTION = "Monthly Imports into Canada (Top Countries)";
    private static final String SOURCE = "GFSA - Statistics Canada Imports";
    private static final String LOCATION = LocationTable.GFSA.name();
    private static final String QLIKVIEW = "Weekly";
    private static final boolean PUSH = true;

    /**
     * @param args the command line arguments
     * @throws java.lang.ClassNotFoundException
     */
    public static void main(String[] args) throws ClassNotFoundException
    {
        ArrayList<ValueItem> values = new ArrayList<>();
        String URL = "";
        String tradeCommodity = "";
        String detail = "";
        String commodityID = "";
        ImporterProperties sourceProperties = new ImporterProperties(SOURCE);
        for (int z = 0; z < sourceProperties.size(); z++)
        {
            tradeCommodity = sourceProperties.getProperty1(z);
            detail = sourceProperties.getProperty2(z);
            commodityID = sourceProperties.getProperty3(z);

            if (sourceProperties.getProperty1(z).isEmpty() || sourceProperties.getProperty2(z).isEmpty() || sourceProperties.getProperty3(z).isEmpty())
            {
                continue;
            }

            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, -2); // always check 2 months back since data is really slow to enter into page
            int month = c.get(Calendar.MONTH) + 1;
            int year = c.get(Calendar.YEAR);

            URL = "http://www5.statcan.gc.ca/cimt-cicm/topNCountries-pays?lang=eng&getSectionId()=0&freq=6&countryId=0&getUsaState()=0&provId=1&retrieve=Retrieve&save=null&country=null&tradeType=3&topNDefault=10&monthStr=null&chapterId=22&arrayId=0&scaleValue=0&scaleQuantity=0&commodityId=" + commodityID + "&dataTransformation=0&refYr=" + year + "&refMonth=" + month;

            System.out.println(URL);
            System.out.println(tradeCommodity);
            System.out.println(detail);
            System.out.println(commodityID);
            values.addAll(parseData(URL, tradeCommodity, detail));
        }
        if (PUSH)
        {
            try
            {
                PushToSQL pusher1 = new PushToSQL(SOURCE, LOCATION, values);
                // pusher1.updateMetaTable(SOURCE, DESCRIPTION, QLIKVIEW, 2);
                pusher1.push();
            } catch (ClassNotFoundException | SQLException | NoSuchAlgorithmException | IOException ex)
            {
                Logger.getLogger(StatsCan.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex)
            {
                Logger.getLogger(StatsCan.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else
        {
            values.stream().forEach((v) ->
            {

                System.out.println(v.toString());

            });

        }

    }

    public static ArrayList<ValueItem> parseData(String URL, String tradeCommodity, String detail)
    {
        ArrayList<ValueItem> values = new ArrayList<>();
        Document doc = getDocument(URL);
        if (doc == null)
        {
            System.out.println("URL FAILED: " + URL);
            return values;
        }

        Elements table = doc.select("table[class=\"CIMT\"]");
        if (table == null || table.first() == null)
        {
            return values;
        }

        Map<String, DateTimeOffset> dates = new HashMap<>();
        dates.put("Date1", new DateTimeOffset("MMMM yyyy HH:mm", table.first().getElementById("Date1").ownText().replaceAll("\\h", " ").trim() + " 12:00"));
        dates.put("Date2", new DateTimeOffset("MMMM yyyy HH:mm", table.first().getElementById("Date2").ownText().replaceAll("\\h", " ").trim() + " 12:00"));
        dates.put("Date3", new DateTimeOffset("MMMM yyyy HH:mm", table.first().getElementById("Date3").ownText().replaceAll("\\h", " ").trim() + " 12:00"));
        dates.put("Date4", new DateTimeOffset("MMMM yyyy HH:mm", table.first().getElementById("Date4").ownText().replaceAll("\\h", " ").trim() + " 12:00"));

        Elements results = table.first().getElementsByClass("ResultRow");
        //System.out.println(table.first().getElementsByClass("ResultRow"));
        for (Element row : results)
        {

            String country = row.getElementsByTag("a").text().trim().replaceAll("\\h", " ");
            if (country.contains(" ("))
            {
                country = country.substring(0, country.indexOf(" ("));
            }
            //  System.out.println(country);
            for (Element rowData : row.getElementsByAttribute("headers"))
            {

                String type = rowData.attributes().get("headers").split(" ")[2].trim();
                if (type.equalsIgnoreCase("Qty"))
                {
                    String key = rowData.attributes().get("headers").split(" ")[1];
                    String value = rowData.getAllElements().text().replaceAll(",", "");
                    String groupId = dates.get(key).toRawUTCString().substring(0, 6);
                    String name = country + " - " + tradeCommodity;

                    if (datatosql.Utilities.isNumeric(value))
                    {
                        values.add(new ValueItem(name, LOCATION, AREA, detail, null, groupId, ValueTable.Decimal.name(), dates.get(key), value));
                    }
                }

            }
            //System.out.println(row.getElementsByClass("headers"));
        }

        return values;
    }

    public static Document getDocument(String URL)
    {
        System.setProperty("phantomjs.binary.path", "lib/phantomjs.exe"); // path to bin file. NOTE: platform dependent
        Document doc = null;

        PhantomJSDriver driver = new PhantomJSDriver();

        driver.get(URL);

        doc = Jsoup.parse(driver.getPageSource());

        driver.quit();

        return doc;
    }

}
