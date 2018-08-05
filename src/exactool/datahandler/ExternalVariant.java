/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author flip
 */
public class ExternalVariant {
    public String id;
    public int start, end;
    public String chr;    
    HashMap infoMap = new HashMap();
    
    public void addInfo(String key, String value){
        infoMap.put(key, value);        
    }
    
    
    public String toString(){
        String result = "<html>"+id+"<br>";
        Iterator keys = infoMap.keySet().iterator();
        while(keys.hasNext())
        {
            Object key = keys.next();
            result=result+key.toString()+": "+infoMap.get(key)+"<br>";
        }
        result = result+"</html>";
        return result;
        
    }

    
}
