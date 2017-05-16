/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cameronjharvey;

/**
 *
 * @author Cameron
 */
import com.googlecode.jcsv.annotations.internal.ValueProcessorProvider;
import com.googlecode.jcsv.reader.CSVEntryParser;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.AnnotationEntryParser;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TraQCErDataInjector {

    private static final Logger Log = Logger.getLogger(TraQCErDataInjector.class.getName());
    private static MainFrame mainFrame;

    /*
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        try {

            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String d = sdf.format(date);
            String fn = String.format("C:/Logs/TraQCErDataInjector-%s.log", d);
            FileHandler logFile = new FileHandler(fn);
            Log.addHandler(logFile);
            logFile.setFormatter(new SimpleFormatter());

        } catch (IOException | SecurityException ex) {
            LogData(ex.getMessage(), Level.SEVERE);
        }

        mainFrame = new MainFrame();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
         LogData("TraQCEr Data Injector has started ", Level.FINE);
    }
    public static void ImportEnrolmentData(){
        String dbPath = "";
        String filePath = "";
        try {
            JFileChooser dbChooser = new JFileChooser();
            dbChooser.setDialogTitle("Select TraQCEr database");
            dbChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int dbcRetVal = dbChooser.showOpenDialog(mainFrame);
            if (dbcRetVal == JFileChooser.APPROVE_OPTION) {
                dbPath = dbChooser.getSelectedFile().getPath();
                LogData("Selected TraQCEr database path " + dbPath, Level.FINE);
            } else {
                throw new Exception("No TraQCEr database selected");
            }

            JFileChooser dfChooser = new JFileChooser();
            dfChooser.setDialogTitle("Select Enrolment data file");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text (Semicolon delimited)", "txt");
            dfChooser.setFileFilter(filter);
            int dfCRetVal = dfChooser.showOpenDialog(mainFrame);
            if (dfCRetVal == JFileChooser.APPROVE_OPTION) {
                filePath = dfChooser.getSelectedFile().getAbsolutePath();
                LogData("Selected Enrolment data file " + filePath, Level.FINE);
            } else {
                throw new Exception("No Enrolment data file selected");
            }

            //read results data file
            LogData("Reading Enrolment data file to inject", Level.FINE);
            //aXcelerate/InSchool/other data file
            Reader datafile = new FileReader(filePath);

            ValueProcessorProvider provider = new ValueProcessorProvider();
            CSVEntryParser<StudentDataObject> entryParser = new AnnotationEntryParser<>(StudentDataObject.class, provider);
            CSVReader<StudentDataObject> csvPersonReader = new CSVReaderBuilder<StudentDataObject>(datafile).entryParser(entryParser).build();

            List<StudentDataObject> students = csvPersonReader.readAll();
            LogData("InSchool data read", Level.FINE);

            //open TraQCEr DB connection
            LogData("Opening TraQCEr database", Level.FINE);
            Connection derbyCon = DriverManager.getConnection("jdbc:derby:" + dbPath);
            LogData("TraQCEr database cennection open", Level.FINE);

            //Update the TraQCEr db
            for (StudentDataObject s : students) {
                try{
                PreparedStatement psUpdate = derbyCon.prepareStatement("update ENROLMENTS set LOCAL_PARTICIPATION=?, LOCAL_RESULT=? "
                        + "where LUI=? and LEARNING_ID=?");

                int subLenght = s.getSubject().length();

                if (subLenght == 8) {
                    psUpdate.setString(2, null);
                } else {
                    psUpdate.setString(2, s.getResult());
                }
                psUpdate.setInt(1, Math.round(s.getParticipation()));
                psUpdate.setLong(3, s.getLUI());
                psUpdate.setString(4, s.getSubject());
                int result = psUpdate.executeUpdate();
                if (result == 0) {
                    PreparedStatement psInsert = derbyCon.prepareStatement("insert into ENROLMENTS (LOCAL_PARTICIPATION,LOCAL_RESULT,LUI,LEARNING_ID,LEARNING_TYPE,PROVIDER,ENROLMENT_DATE)  "+
                            "values (?,?,?,?,?,?,?) ");
                    if (subLenght == 8) {
                        psInsert.setString(2, null);
                        psInsert.setString(5, "VET");
                           psInsert.setString(6, "616");
                    } else {
                        psInsert.setString(2, s.getResult());
                        psInsert.setString(5, "SS");
                           psInsert.setString(6, "555");
                    }

                    psInsert.setInt(1, Math.round(s.getParticipation()));
                    psInsert.setLong(3, s.getLUI());
                    psInsert.setString(4, s.getSubject());

                    java.util.Date tmpDate = new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-27");
                    psInsert.setDate(7, new java.sql.Date(tmpDate.getTime()));

                    psInsert.execute();
                    derbyCon.commit();
                    LogData("Added Student (" + s.getLUI() + ") for " + s.getSubject(), Level.WARNING);
                } else {
                    derbyCon.commit();
                    LogData("Updated Student (" + s.getLUI() + ") for " + s.getSubject(), Level.FINE);
                }
                } catch (Exception ex) {
                    //Exception
                    LogData(ex.getMessage(), Level.SEVERE);
                }
            }

            LogData("Commited data to the TraQCEr database", Level.FINE);

            try {
                // the shutdown=true attribute shuts down Derby
                DriverManager.getConnection("jdbc:derby:;shutdown=true");

                // To shut down a specific database only, but keep the
                // engine running (for example for connecting to other
                // databases), specify a database in the connection URL:
                //DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
            } catch (SQLException se) {
                if (((se.getErrorCode() == 50000)
                        && ("XJ015".equals(se.getSQLState())))) {
                    // we got the expected exception
                    LogData("TraQCEr database shutdown normally", Level.FINE);
                    // Note that for single database shutdown, the expected
                    // SQL state is "08006", and the error code is 45000.
                } else {
                    // if the error code or SQLState is different, we have
                    // an unexpected exception (shutdown failed)
                    LogData("TraQCEr database did not shut down normally", Level.WARNING);
                }
            }

        } catch (Exception ex) {
            //Exception
            LogData(ex.getMessage(), Level.SEVERE);
        }

        LogData("TraQCEr Data Injector has finished importing Enrolment data", Level.FINE);
    }
    
    public static void ImportLearningsData(){
        String dbPath = "";
        String filePath = "";
        try {
            JFileChooser dbChooser = new JFileChooser();
            dbChooser.setDialogTitle("Select TraQCEr database");
            dbChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int dbcRetVal = dbChooser.showOpenDialog(mainFrame);
            if (dbcRetVal == JFileChooser.APPROVE_OPTION) {
                dbPath = dbChooser.getSelectedFile().getPath();
                LogData("Selected TraQCEr database path " + dbPath, Level.FINE);
            } else {
                throw new Exception("No TraQCEr database selected");
            }

            JFileChooser dfChooser = new JFileChooser();
            dfChooser.setDialogTitle("Select Learnings data file");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text (Semicolon delimited)", "txt");
            dfChooser.setFileFilter(filter);
            int dfCRetVal = dfChooser.showOpenDialog(mainFrame);
            if (dfCRetVal == JFileChooser.APPROVE_OPTION) {
                filePath = dfChooser.getSelectedFile().getAbsolutePath();
                LogData("Selected Learnings data file " + filePath, Level.FINE);
            } else {
                throw new Exception("No Learnings data file selected");
            }

            //read results data file
            LogData("Reading Learnings data file to inject", Level.FINE);
            //aXcelerate/InSchool/other data file
            Reader datafile = new FileReader(filePath);

            ValueProcessorProvider provider = new ValueProcessorProvider();
            CSVEntryParser<LearningDataObject> entryParser = new AnnotationEntryParser<>(LearningDataObject.class, provider);
            CSVReader<LearningDataObject> csvPersonReader = new CSVReaderBuilder<LearningDataObject>(datafile).entryParser(entryParser).build();

            List<LearningDataObject> learnings = csvPersonReader.readAll();
            LogData("InSchool data read", Level.FINE);

            //open TraQCEr DB connection
            LogData("Opening TraQCEr database", Level.FINE);
            Connection derbyCon = DriverManager.getConnection("jdbc:derby:" + dbPath);
            LogData("TraQCEr database cennection open", Level.FINE);

            //Update the TraQCEr db
            for (LearningDataObject l : learnings) {

                try{
                PreparedStatement psInsert = derbyCon.prepareStatement("insert into LEARNINGS (TYPE,ID,NAME,UNIT,MAXIMUM_PARTICIPATION,QCE_CATEGORY,QCE_LITERACY,QCE_NUMERACY,QCE_MAXCREDIT,ORIGIN_FILE)  "+
                    "values (?,?,?,?,?,?,?,?,?,?) ");
                psInsert.setString(1,l.getType());
                psInsert.setString(2,l.getId());
                psInsert.setString(3,l.getName());
                psInsert.setString(4,l.getUnit());
                psInsert.setInt(5,l.getMaximumParticipation());
                psInsert.setString(6,l.getQCECategory());
                psInsert.setString(7,l.getQCELiteracy()); 
                psInsert.setString(8,l.getQCENumeracy());
                psInsert.setInt(9,l.getQCEMaxcredit());
                psInsert.setInt(10,l.getOriginFile());
                    //if (subLenght == 8) {
                    //    psInsert.setString(2, null);
                    //    psInsert.setString(5, "VET");
                    //       psInsert.setString(6, "616");
                    //} else {
                    //    psInsert.setString(2, s.getResult());
                    //    psInsert.setString(5, "SS");
                    //       psInsert.setString(6, "555");
                    //}

                    //psInsert.setInt(1, Math.round(s.getParticipation()));
                    //psInsert.setLong(3, s.getLUI());
                    //psInsert.setString(4, s.getSubject());

                    //java.util.Date tmpDate = new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-27");
                    //psInsert.setDate(7, new java.sql.Date(tmpDate.getTime()));

                    psInsert.execute();
                    
                    derbyCon.commit();
                    } catch (Exception ex) {
                        //Exception
                        LogData(ex.getMessage(), Level.SEVERE);
                    }
                    LogData("Added Learning " + l.getId(), Level.WARNING);
            }

            LogData("Commited data to the TraQCEr database", Level.FINE);

            try {
                // the shutdown=true attribute shuts down Derby
                DriverManager.getConnection("jdbc:derby:;shutdown=true");

                // To shut down a specific database only, but keep the
                // engine running (for example for connecting to other
                // databases), specify a database in the connection URL:
                //DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
            } catch (SQLException se) {
                if (((se.getErrorCode() == 50000)
                        && ("XJ015".equals(se.getSQLState())))) {
                    // we got the expected exception
                    LogData("TraQCEr database shutdown normally", Level.FINE);
                    // Note that for single database shutdown, the expected
                    // SQL state is "08006", and the error code is 45000.
                } else {
                    // if the error code or SQLState is different, we have
                    // an unexpected exception (shutdown failed)
                    LogData("TraQCEr database did not shut down normally", Level.WARNING);
                }
            }

        } catch (Exception ex) {
            //Exception
            LogData(ex.getMessage(), Level.SEVERE);
        }

        LogData("TraQCEr Data Injector has finished importing Enrolment data", Level.FINE);
    }
    
    private static void LogData(String text, Level level) {
        mainFrame.UpdateLog(text);

        if (level == Level.SEVERE) {
            Log.severe(text);
        } else if (level == Level.WARNING) {
            Log.warning(text);
        } else {
            Log.info(text);
        }
    }

}
