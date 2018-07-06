/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skoop;

/**
 *
 * @author sandeep.gainda
 */
import datatosql.DateTimeOffset;
import datatosql.PushToSQL;
import datatosql.ValueItem;
import datatosql.ValueItemWithComment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class SkoopCsv extends Application
{

    private static boolean push = false;
    private static final String LOCATION = "GFSA";
    private static final String SOURCE = "Skoop";

    private static final TextArea text = new TextArea();
    private static final TextField dateFormatTextField = new TextField();
    private static String dateFormat;
    //private static String LOG_PATH = "C:\\install\\Logs\\";

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("Skoop");
        RadioButton testButton = new RadioButton("Test");
        RadioButton liveButton = new RadioButton("Live");
        Button skoopButton = new Button("Skoop File");
        FileChooser fileChooser = new FileChooser();
        skoopButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(final ActionEvent e)
            {
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null)
                {
                    try
                    {
                        importCsv(file);
                    } catch (Exception ex)
                    {
                        Logger.getLogger(SkoopCsv.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        final ToggleGroup group = new ToggleGroup();
        liveButton.setToggleGroup(group);
        testButton.setToggleGroup(group);
        testButton.selectedProperty().setValue(true);
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
        {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle)
            {
                if (group.getSelectedToggle() != null)
                {
                    RadioButton r1 = (RadioButton) group.getSelectedToggle();
                    push = !"Test".equals(r1.getText());

                }

            }
        });

        Label dateFormatLabel = new Label("Date Format: ");
        dateFormatTextField.setText("yyyy-MM-dd HH:mm");
        dateFormat = dateFormatTextField.getText();

        //import CSV
        //  text.append("Importing: " + file.getCanonicalPath() + "\n");
        // importCsv(file);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(skoopButton, 0, 1);
        grid.add(testButton, 1, 1);
        grid.add(liveButton, 2, 1);
        grid.add(dateFormatLabel, 3, 1);
        grid.add(dateFormatTextField, 4, 1);

        Scene scene = new Scene(grid, 500, 100);

        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
    {
      //  String logFileNamePath = LOG_PATH + "GFSA_SKOOP_" + new SimpleDateFormat("yyyMMdd_HHmmssSSS").format(Calendar.getInstance().getTime()) + ".log";
        //  File log = new File(logFileNamePath);
        //  FileOutputStream fos = new FileOutputStream(log);
        //PrintStream ps = new PrintStream(fos);
        //    System.setErr(ps);
        // System.setOut(ps);

        launch(args);

    }

    private static void importCsv(File file) throws IOException, ClassNotFoundException, SQLException, NoSuchAlgorithmException, Exception
    {

        //Create the CSVFormat object
        CSVFormat format = CSVFormat.EXCEL.withDelimiter(',');
        ArrayList<ValueItem> values = null;
        try (CSVParser parser = new CSVParser(new FileReader(file.getCanonicalPath()), format))
        {

            values = new ArrayList();

            Integer[] tagIdArray = null;

            for (CSVRecord record : parser)
            {
                try
                {
                    Integer tagId = null;
                    String groupId = null;
                    DateTimeOffset date = null;

                    if (record.getRecordNumber() == 2)
                    {
                        tagIdArray = new Integer[record.size() - 2];
                        for (int i = 2; i < record.size(); i++)
                        {
                            if (record.get(i).isEmpty())
                            {
                                tagIdArray[i - 2] = null;

                            } else
                            {
                                tagIdArray[i - 2] = Integer.parseInt(record.get(i));

                            }

                        }
                    }

                    //Grab the TagId's
                    //Grab the groupId and Date for each record
                    if (record.getRecordNumber() > 1)
                    {

                        if (record.get(1).isEmpty())
                        {
                            continue;
                        }

                        groupId = record.get(0);
                        dateFormat = dateFormatTextField.getText();
                        date = new DateTimeOffset(dateFormat, record.get(1));
                        if (groupId.isEmpty())
                        {
                            groupId = date.toRawUTCString();
                        }

                       // System.out.println(groupId);
                        // System.out.println(date);
                        for (int i = 2; i < record.size(); i++)
                        {

                            tagId = tagIdArray[i - 2];
                            if (tagId == null)
                            {
                                continue;
                            }

                            String value = record.get(i);
                            if (value.isEmpty())
                            {
                                value = null;
                            }

                            if (tagId == 15569)
                            {
                                values.add(new ValueItemWithComment(tagId, groupId, date, value, "arc", 0));
                            } else
                            {
                                values.add(new ValueItem(tagId, groupId, date, value));
                            }

                        }
                    }

                } catch (IllegalArgumentException e)
                {
                    Logger.getLogger(SkoopCsv.class.getName()).log(Level.SEVERE, null, e);
                    System.out.println("Failed\n");

                }

            }
        } catch (Exception e)
        {
            Logger.getLogger(SkoopCsv.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Failed\n");
        }
        if (push)
        {

            PushToSQL pusher1 = new PushToSQL(SOURCE, LOCATION, values);
            pusher1.push();
            System.out.println("Successfully Imported");

        } else
        {
            values.stream().forEach((v) ->
            {

                System.out.println(v.toString());

            });

        }

        //close the parser
    }

}
