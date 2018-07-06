/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opis;

import datatosql.PushToSQL;
import datatosql.ValueItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sandeep.gainda
 */
public class OPIS
{

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    private static final String REG_PRICES4 = "(.*)[ ]{3,}(\\d+\\.\\d*)[ ]*+(\\d+\\.\\d*)[ ]*+(\\d+\\.\\d*)[ ]*(\\d+\\.\\d*)";
    private static final String REG_HEADER4 = "[ ]{3,}([a-zA-Z]*)[ ]*([a-zA-Z]*)[ ]*([a-zA-Z]*)[ ]*([a-zA-Z]*)";
    private static final String REG_PRICES3 = "(.*)[ ]{3,}(\\d+\\.\\d*)[ ]*+(\\d+\\.\\d*)[ ]*(\\d+\\.\\d*)";
    private static final String REG_HEADER3 = "[ ]{3,}([a-zA-Z]*)[ ]*([a-zA-Z]*)[ ]*([a-zA-Z]*)";

    private static final boolean PUSH = true;
    private static final String DESCRIPTION = "Import of OPIS prices from TXT email attachments";
    private static final String QLIKVIEW = "Daily";
    private static final String LOCATION = "GFSA";
    private static final String SOURCE = "GFSA - Oil Price Information Service";

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, NoSuchAlgorithmException, Exception
    {

        String filePath = "";
        String preName = "";
        String closeTime = "";
        String regHeader = "";
        String regPrices = "";
        int columns = 0;
        int rowStart = 0;

        if (args.length == 1)
        {
            filePath = String.valueOf(args[0]);

        } else
        {

            System.err.println("Missing Arguments");
            System.exit(1);
        }
        ArrayList<ValueItem> values = new ArrayList();

        try
        {

            FileInputStream fstream = new FileInputStream(filePath);
            File file = new File(filePath);
            String fileName = file.getName().toUpperCase();
            if (fileName.contains("HISTORICALSPOT.TXT") || fileName.contains("CHICAGOETHANOL.TXT"))
            {
                preName = "OPIS Chicago Ethanol";
                closeTime = "17:15";
                columns = 4;
                regHeader = REG_HEADER4;
                regPrices = REG_PRICES4;
                rowStart = 0;
            } else if (file.getName().toUpperCase().contains("HISTORICALLP.TXT"))
            {

                preName = "OPIS LP Prices Mont Belvieu Non-TET N. Gasoline";
                closeTime = "17:15";
                columns = 3;
                regHeader = REG_HEADER3;
                regPrices = REG_PRICES3;
                rowStart = 8;

            } else
            {

                System.err.println("Unknown File");
                System.exit(1);
            }

            OPISFile opis1 = new OPISFile(regHeader, regPrices, columns, rowStart, fstream, file, preName, closeTime);
            values = opis1.getValues(values);
        } catch (FileNotFoundException ex)
        {
            Logger.getLogger(OPISFile.class.getName()).log(Level.SEVERE, null, ex);
        }

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
            Logger.getLogger(OPIS.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
