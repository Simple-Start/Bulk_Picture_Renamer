package bilderrenamer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BilderRenamer {

    public static void main(String[] argv) throws IOException, SQLException, ClassNotFoundException {

        // DB
        //Please edit these Variables accordingly
        String database = "essey";
        String username = "root";
        String password = "pass123";
        String tableName = "sampleTable";
        String rowName = "Picture";

        String foldername = "FolderXY";
        String destinationFolder = "Destination";

        String filePrefix = "project1-";

        Connection con = null;
        Statement st = null;
        Driver resultat;
        String sqlStatement;
        //Initialize Class
        Class.forName("com.mysql.jdbc.Driver");

        resultat = DriverManager.getDriver("jdbc:mysql://localhost:3306/" + database);

        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + database, username, password);

        //first delete ClientCertInformation
        sqlStatement = "SELECT * FROM `" + tableName + "`";

        PreparedStatement statement = con.prepareStatement(sqlStatement);

        ResultSet resultSet = statement.executeQuery();

        if (!resultSet.next()) {

            System.out.println("resultSet empty");

            if (st != null) {

                st.close();
            }
            if (resultSet != null) {

                resultSet.close();

            }

            System.out.println("resultset was returned empty");

            return;

        }

        int coloumnCount = resultSet.getMetaData().getColumnCount();
        resultSet.last();
        int rowCount = resultSet.getRow();

        resultSet.first();

        //Filling dataArray with table content
        String[][] dataArray = new String[1000][30];

        for (int reihe = 0; reihe < rowCount; reihe++) {

            if (dataArray[reihe] != null) {

                for (int ix = 1; ix <= coloumnCount; ix++) {

                    if (resultSet.getString(ix) != null && resultSet.getString(ix).isEmpty() == false) {

                        dataArray[reihe][ix - 1] = resultSet.getString(ix);

                    }

                }
                resultSet.next();
            }

        }

        //now we got the data array filled
        // FILE PART
        File folder = new File(foldername);
        File[] listOfFiles = folder.listFiles();

        final String dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir);
        System.out.println("amount of files: " + listOfFiles.length);

        for (int i = 0; i < listOfFiles.length; i++) {

            if (listOfFiles[i].isFile()) {

                File f = new File(foldername + listOfFiles[i].getName());

                String fileExtension = getFileExtension(f);

                for (int aussen = 0; aussen < dataArray.length; aussen++) {

                    if (dataArray[aussen][1] != null) {

                        if (dataArray[aussen][1].isEmpty() == false && dataArray[aussen] != null && dataArray[aussen][1] != null && dataArray[aussen][1] != "NULL") {

                            String filePath = getFilePath(dataArray[aussen][1]);

                            String fullPath = filePath + "/" + listOfFiles[i].getName();

                            if (dataArray[aussen][1].matches(fullPath) == true) {

                                System.out.println("this file is being processed : " + fullPath);

                                System.out.println("Matching " + dataArray[aussen][1]);
                                try {
                                    System.out.println("and " + listOfFiles[i].getName());
                                    Path src = Paths.get(foldername + "/" + listOfFiles[i].getName());
                                    Path dest = Paths.get(destinationFolder + "/" + filePrefix + dataArray[aussen][0] + "." + fileExtension);
                                    Files.copy(src, dest);
                                } catch (Exception e) {

                                    System.out.println("Exception spawn " + e);

                                }

                                //            boolean worked =    f.renameTo(new File("cat1-"+dataArray[aussen][0]+"."+fileExtension));
                                //           System.out.println(worked);
                                System.out.println("File created " + dataArray[aussen][0] + "." + fileExtension);

                                //now we need to update table
                                sqlStatement = "UPDATE `" + tableName + "` SET `" + rowName + "` = '" + filePrefix + "" + dataArray[aussen][0] + "." + fileExtension + "' WHERE `" + tableName + "`.`ID` =" + dataArray[aussen][0] + ";";

                                System.out.println(sqlStatement);

                                statement = con.prepareStatement(sqlStatement);

                                statement.executeUpdate();

                            } else {

                                //  System.out.println(" NOPE " +aussen);
                            }

                        }

                    }

                }
            }

            System.out.println("Converting is done");
        }

    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    private static String getFilePath(String file) {
        String fileName = file;
        if (fileName.lastIndexOf("/") != -1 && fileName.lastIndexOf("/") != 0) {
            return fileName.substring(0, fileName.lastIndexOf("/"));
        } else {
            return "";
        }
    }

}
