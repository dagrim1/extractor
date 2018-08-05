/*
 * Class for directly reading gzip files as if they were normal text files.
 * Can be used in the same way as a BufferedReader
 */
package exactool.datahandler;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author flip
 */
public class GzipReader{
    BufferedReader  reader;
    
    public GzipReader(String file) throws IOException{
        initializeGzipReader(new File(file), null);                
    }
    
    public GzipReader(String file, boolean gzip) throws IOException{
        if(gzip)
            initializeGzipReader(new File(file), null);        
        else reader = new BufferedReader(new FileReader(file));
    }
    
    /*
    public GzipReader(File file) throws IOException{
        initializeGzipReader(file, null);
    }
    
    public GzipReader(String file, String encoding) throws IOException{
        initializeGzipReader(new File(file), encoding);        
    }
    
    public GzipReader(File file, String encoding) throws IOException{
        initializeGzipReader(file, encoding);        
    }
    */
        
    public void initializeGzipReader(File file, String encoding) throws FileNotFoundException, IOException{
        InputStream fileStream = new FileInputStream(file);
        InputStream gzipStream = new GZIPInputStream(fileStream);        
        if(encoding != null)
        {
            Reader decoder = new InputStreamReader(gzipStream, encoding);
            reader = new BufferedReader(decoder);
            
        }
        else reader = new BufferedReader(new InputStreamReader(gzipStream));
    }
            
    public String readLine() throws IOException{
        if(reader!=null)
            return reader.readLine();
        return null;
    }
    
    public void close() throws IOException{
        if(reader != null)
        reader.close();
    }

}
