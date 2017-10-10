import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;

/**
 * Created by kernel32 on 02.08.2017.
 */
public class WcamArchiveManager {

    public static String path = "/var/lib/tomcat7/webapps/wcam/resources/images/";
    private static Logger log = Logger.getLogger("WcamArchiveManager");

    public static void main(String[] args) {
        archiveFiles();
    }

    private static void archiveFiles() {
        try {
            while (true) {
                File folder = new File(path);
                File[] listOfFiles = folder.listFiles();
                for (int i = 0; i < listOfFiles.length; i++) {
                    File file = listOfFiles[i];
                    if (file.isFile() && file.getName().endsWith(".jpg")) {

                        //move file if it is older than 1 day
                        DateTime createDate = new DateTime(file.lastModified());
                        if (getDateDifference(createDate) >= 1) {
                            moveFile(createDate, file, folder);
                        }
                    }
                    // delete archive if it is older than 14 days
                    else if (getFolderCreatedDate(file) > 14) {
                        FileUtils.forceDelete(file);
                        log.info("WcamArchiveManager: Deleted archive: " + file.getName());
                    }
                }
            }
        } catch (IOException e) {
            String errorMessage = "WcamArchiveManager: screen transfer app failed";
            log.error(errorMessage, e);
            e.printStackTrace();
            Phone.sendMessage(errorMessage);
        }
    }

    private static int getDateDifference(DateTime createDate) {
        DateTime today = new DateTime();
        int days = Days.daysBetween(createDate, today).getDays();
        return days;
    }

    private static void moveFile(DateTime createDate, File file, File folder) {
        File theDir = new File(folder + File.separator + createDate.dayOfMonth().getAsString() + "-" + createDate.monthOfYear().getAsString() + "-" + createDate.year().getAsString());
        if (!theDir.exists()) {
            theDir.mkdir();
        }
        file.renameTo(new File(theDir.getPath() + File.separator + file.getName()));
    }

    private static int getFolderCreatedDate(File file) throws IOException {
        DateTime today = new DateTime();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
        DateTime createdDate = dtf.parseDateTime(file.getName());
        int days = Days.daysBetween(createdDate, today).getDays();
        return days;
    }
}
