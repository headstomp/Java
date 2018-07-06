/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opis;

import datatosql.DateTimeOffset;
import datatosql.Utilities;
import datatosql.ValueItem;
import datatosql.ValueItemWithFile;
import datatosql.ValueTable;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sandeep.gainda
 */
public class OPISFile
{

    private static final String AREA = "Market";
    private static final String LOCATION = "GFSA";
    private String regHeader;
    private String regPrices;
    private String preName;
    private String closeTime;
    private int columns;
    private int rowStart;
    private FileInputStream fstream;
    private File file;

    public OPISFile(String regHeader, String regPrices, int columns, int rowStart, FileInputStream fstream, File file, String preName, String closeTime)
    {
        this.regHeader = regHeader;
        this.regPrices = regPrices;
        this.preName = preName;
        this.columns = columns;
        this.closeTime = closeTime;
        this.rowStart = rowStart;
        this.fstream = fstream;
        this.file = file;

    }

    /**
     *
     * @param values
     * @return
     */
    public ArrayList<ValueItem> getValues(ArrayList<ValueItem> values)
    {

        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine = null;
        String[] sufName = new String[columns];
        DateTimeOffset date = null;
        String groupId = null;
        for (int z = 0; z < 4; z++)
        {
            try
            {
                if(rowStart > 0 && z ==0)
                {
                    for (int r = 0; r < rowStart; r++)
                    {
                        strLine = br.readLine();
                    }
                }
                strLine = br.readLine();
            } catch (IOException ex)
            {
                Logger.getLogger(OPISFile.class.getName()).log(Level.SEVERE, null, ex);
            }
           System.out.println(strLine); 
            if (Utilities.filterList(strLine.trim(), regPrices) != null)
            {
                date = new DateTimeOffset("MMMM dd',' yyyy HH:mm", Utilities.filterList(strLine.trim(), regPrices).group(1).trim() + " " + closeTime);
                groupId = date.toRawUTCString();
                System.out.println(date);
                System.out.println(groupId);
            }

            String value = null;

            for (int y = 1; y <= columns; y++)
            {
                if (Utilities.filterList(strLine, regHeader) != null && Utilities.filterList(strLine.trim(), regPrices) == null)
                {
                    sufName[y - 1] = Utilities.filterList(strLine, regHeader).group(y).trim();
                    //System.out.println(sufName[y - 1]);
                }
                if (Utilities.filterList(strLine.trim(), regPrices) != null)
                {
                    value = Utilities.filterList(strLine.trim(), regPrices).group(y + 1).trim();
                    System.out.println(value);
                }
                if (sufName[y - 1] != null && value != null)
                {
                    values.add(new ValueItem(preName + " " + sufName[y - 1], LOCATION, AREA, null, null, groupId, ValueTable.Decimal.name(), date, value));
                }
            }

        }

        System.out.println(file);
        values.add(new ValueItemWithFile(preName + " File", LOCATION, AREA, "Emailed text file from source", null, groupId, date, file));
        return values;
    }
}
