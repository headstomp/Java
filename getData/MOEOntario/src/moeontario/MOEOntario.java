/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moeontario;

import datatosql.DateTimeOffset;
import datatosql.PushToSQL;
import datatosql.Utilities;
import datatosql.ValueItem;
import datatosql.ValueTable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author sandeep.gainda
 */
public class MOEOntario
{

    private static boolean PUSH = true;
    private static final String LOCATION = "GFSA";
    private static final String AREA = "Market";
    private static final String DESCRIPTION = "Import of Fuel (Diesel, Gasoline, CNG etc) Prices from Ministry of Energy Ontario";
    private static final String QLIKVIEW = "Daily";
    private static final String SOURCE = "GFSA - Ministry of Energy Ontario";

    /**
     * @param args the command line arguments
     * @throws java.net.MalformedURLException
     */
    public static void main(String[] args) throws MalformedURLException, IOException
    {
        ArrayList<ValueItem> values = null;
        values = new ArrayList();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        String uri = "https://files.ontario.ca/opendata/fueltypesall_51.csv";
        System.out.println(uri);
        URL url = new URL(uri);
        File data = null;
        data = new File("fueltypesall_51.csv");
        data.deleteOnExit();
        System.out.println("Downloading file: " + data.getName());
        FileUtils.copyURLToFile(url, data);
        System.out.println("Parsing data...");

        //Create the CSVFormat object
        CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter(',');

        CSVParser parser = new CSVParser(new FileReader(data.getCanonicalPath()), format);
        FileUtils.deleteQuietly(data);

        for (CSVRecord record : parser)
        {

            boolean toBreak = false;
            String type;

            type = record.get("FuelType").trim();

            for (Map.Entry header : parser.getHeaderMap().entrySet())
            {

                if (record.get(0).trim().isEmpty())
                {
                    break;
                }

                DateTimeOffset date = new DateTimeOffset("MM/dd/yyyy HH:mm", record.get(0) + " 12:00");

                //only do current year and last year
                if (date.getCalendar().get(Calendar.YEAR) < currentYear - 1)
                {
                    continue;
                }

                //System.out.println(date);
                //   System.out.println(record.get(0));
                // System.out.print(headerArray[i - 1]);    
                String name = header.getKey().toString();

                String value = record.get(header.getKey().toString()).trim();

                if (!header.getKey().toString().equalsIgnoreCase("Date") && !header.getKey().toString().equalsIgnoreCase("FuelType"))
                {
                    if (!Utilities.isNumeric(value))
                    {
                        value = null;
                    }
                    values.add(new ValueItem(name, LOCATION, AREA, name + " (¢ per Litre)", null, type + "_" + date.toRawUTCString().substring(0, 8), ValueTable.Decimal.name(), date, value));
                } else if (header.getKey().toString().equalsIgnoreCase("FuelType"))
                {
                    values.add(new ValueItem("Fuel Type", LOCATION, AREA, "Particular fuel type", null, type + "_" + date.toRawUTCString().substring(0, 8), ValueTable.String.name(), date, value));

                }
                //System.out.println(record.get(i));
            }
            if (toBreak)
            {
                break;
            }

        }
        parser.close();

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
        } catch (Exception ex)
        {
            Logger.getLogger(MOEOntario.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("");

        // TODO code application logic here
    }

}
