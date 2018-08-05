/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler.dataobjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

/**
 *
 * @author flip
 */
public class pop_base {
    
    private final String enf = "European (Non-Finnish)";
    private final String ef = "European (Finnish)";
    private final String ea = "East Asian";
    private final String sa = "South Asian";
    private final String ot = "Other";
    private final String af = "African";
    private final String la = "Latino";
    private final String aj = "Ashkenazi Jewish";
    ArrayList popsList = null;
    public final String[]  pops = new String[]{enf, ef, ea, sa, ot, af, la, aj};
    
    //exac and gnomad
    @JsonProperty("European (Non-Finnish)")
    public int european_non_finnish;
    @JsonProperty("European (Finnish)")
    public int european_finnish;
    @JsonProperty("East Asian")
    public int east_asian;
    @JsonProperty("South Asian")
    public int south_asian;
    @JsonProperty("Other")
    public int other;
    @JsonProperty("African")
    public int african;
    @JsonProperty("Latino")
    public int latino;
    
    //gnomad only
    @JsonProperty("Ashkenazi Jewish")
    public int ashkenazi_jewish;
    
    public int getValueForPop(String pop){        
        if("European (Non-Finnish)".equals(pop))
            return european_non_finnish;
        else if("European (Finnish)".equals(pop))
            return european_finnish;
        else if("East Asian".equals(pop))
            return east_asian;
        else if("South Asian".equals(pop))
            return south_asian;        
        else if("Other".equals(pop))
            return other;
        else if("African".equals(pop))
            return african;
        else if("Latino".equals(pop))
            return latino;
        else if("Ashkenazi Jewish".equals(pop))
            return ashkenazi_jewish;
        return 0;
    }
    
    public void setValueForPop(String pop, int value){        
        if("European (Non-Finnish)".equals(pop))
            european_non_finnish = value;
        else if("European (Finnish)".equals(pop))
            european_finnish = value;
        else if("East Asian".equals(pop))
            east_asian = value;
        else if("South Asian".equals(pop))
            south_asian = value;
        else if("Other".equals(pop))
            other = value;
        else if("African".equals(pop))
            african = value;
        else if("Latino".equals(pop))
            latino = value;
        else if("Ashkenazi Jewish".equals(pop))
            ashkenazi_jewish = value;        
    }
    
    public String toString()
    {
        String result = "{";
        for(int i=0; i<pops.length; i++){
            result = result+pops[i]+"="+getValueForPop(pops[i]);
            if(i<pops.length-1)
                result = result+", ";
        }
        result = result +"}";
        return result;
    }
    
    public static  String getAbbreviationForPop(String pop){
         if("European (Non-Finnish)".equals(pop))
            return "eun";
        else if("European (Finnish)".equals(pop))
            return "euf";
        else if("East Asian".equals(pop))
            return "eas";
        else if("South Asian".equals(pop))
            return "sas";
        else if("Other".equals(pop))
            return "oth";
        else if("African".equals(pop))
            return "afr";
        else if("Latino".equals(pop))
            return "lat";
        else if("Ashkenazi Jewish".equals(pop))
            return "asj";
        else return pop;
    }
    public ArrayList getPops(){     
        if(popsList==null)
        {
            popsList = new ArrayList();
            for(int p=0; p<pops.length; p++)
                popsList.add(pops[p]);
        }
        return popsList;
    }
    
}
