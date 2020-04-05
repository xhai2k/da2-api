package utils;

import org.apache.commons.io.FilenameUtils;
import play.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CommonUtils {
    private static  long GIGABYTES = 1024L * 1024L * 1024L;
    public static String humanReadableByteCountBin(long bytes) {
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1024L ? bytes + " B"
                : b <= 0xfffccccccccccccL >> 40 ? String.format("%.1f KB", bytes / 0x1p10)
                : b <= 0xfffccccccccccccL >> 30 ? String.format("%.1f MB", bytes / 0x1p20)
                : b <= 0xfffccccccccccccL >> 20 ? String.format("%.1f GB", bytes / 0x1p30)
                : b <= 0xfffccccccccccccL >> 10 ? String.format("%.1f TB", bytes / 0x1p40)
                : b <= 0xfffccccccccccccL ? String.format("%.1f PB", (bytes >> 10) / 0x1p40)
                : String.format("%.1f EB", (bytes >> 20) / 0x1p40);
    }
    public static double bytesToGB(long bytes){
       Float valueLong = (float)bytes/(float)GIGABYTES;
       if(valueLong==0){
           return 0;
       }
       double valueGb = (double) Math.round(valueLong * 1000) / 1000;
       return valueGb ==0.0 ? 0.01 : valueGb;
    }
    public static String unzipfile(String zipFilePath) {
        String destDir = FilenameUtils.removeExtension(zipFilePath);
        //final String destDir  = zipFilePath.substring(0,zipFilePath)
        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to " + newFile.getAbsolutePath());

                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error(e, e.getMessage());
            return null;
        }
        return destDir;
    }

    public static String formatDate(Date date, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String formatCurrnetDate(String format){
        return formatDate(getCurrentDate(), format);
    }

    public static Date getCurrentDate(){
        long milliseconds = System.currentTimeMillis();
        return new Date(milliseconds);
    }
}
