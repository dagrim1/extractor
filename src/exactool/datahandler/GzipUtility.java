/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * Class allowing for compression and decompression of gzip files...
 * @author flip
 */
public class GzipUtility {
    public static String readGzipFileContent(File gzipFile){
        return readGzipFileContent(gzipFile.getPath());
    }
    
    public static String readGzipFileContent(String gzipFile){
        StringBuffer tempResult = new StringBuffer();
        try {
            BufferedReader is = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(gzipFile))));
            String line;
            // Now read lines of text: the BufferedReader puts them in lines,            // the InputStreamReader does Unicode conversion, and the
            // GZipInputStream "gunzip"s the data from the FileInputStream.
            while ((line = is.readLine()) != null)
                tempResult.append(line).append("\n");              
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempResult.toString();
    }
    
    public static boolean writeGzipFileContent(File gzipFile, String content){
        return writeGzipFileContent(gzipFile.getPath(), content);
    }
    
    public static boolean writeGzipFileContent(String gzipFile, String content){
        try{
            FileOutputStream fos = new FileOutputStream(gzipFile);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            
            byte[] data = content.getBytes();
            gzipOS.write(data, 0, data.length);
            gzipOS.close();
            fos.close();    
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }        
    }
    public static boolean decompressGzipFile(String gzipFile, String newFile) {
        try {
            FileInputStream fis = new FileInputStream(gzipFile);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(newFile);
            
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            //close resources
            fos.close();
            gis.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }        
    }

    public static boolean compressGzipFile(String file, String gzipFile) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(gzipFile);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fos.close();
            fis.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }        
    }        
}
