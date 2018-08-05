    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import exactool.datahandler.dataobjects.gnomad_coverage_stats;
import exactool.datahandler.dataobjects.pos_cov;

/**
 *
 * Uses Quick-Json: https://code.google.com/archive/p/quick-json/wikis/Usage.wiki
 * @author flip
 */
public class GnomadQuerier extends ExacQuerier {    
    //public static String search_url =  "http://gnomad.broadinstitute.org/awesome?query=";
    
    
    public GnomadQuerier(){
        //EXAC_JSON_VARIANTS_KEYWORD = EXAC_JSON_VARIANTS_IN_GENE;            
        EXAC_JSON_VARIANTS_KEYWORD = EXAC_JSON_VARIANTS_IN_TRANSCRIPT;
        VAR_IDS = VAR_IDS_GNOMAD;
        // = VAR_INDICES_GNOMAD;
        //OUTPUT_VAR_HEADERS = OUTPUT_VAR_HEADERS_GNOMAD;        
        search_url =  "http://gnomad.broadinstitute.org/awesome?query=";
    }
    
     public HashMap<Integer, double[]> queryCoverage(String searchTerm){        
        gnomad_coverage_stats cov_data = null;
        try{
            String jsonData = getJsonPart(cachedData, ExacQuerier.EXAC_JSON_COVERAGE_STATS);
            cov_data = objectMapper.readValue(jsonData, gnomad_coverage_stats.class);                
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        
        HashMap<Integer, double[]> result = new HashMap();        
        if(cov_data!=null){
            for(int i=0; i<cov_data.exomes.length; i++){
                result.put(cov_data.exomes[i].pos, new double[]{cov_data.exomes[i].mean, -1});            
            }
            for(int i=0; i<cov_data.genomes.length; i++)
            {
                double stored[] = result.get(cov_data.genomes[i].pos);
                if(stored==null)
                    stored = new double[]{-1.0, cov_data.genomes[i].mean};
                else stored[1] = cov_data.genomes[i].mean;
                result.put(cov_data.genomes[i].pos, stored);            
            }
        }
        return result;
    }
    
    
    public ArrayList queryEntries(String queryResult, String keyword){
        System.out.println("GnomadQuerier.queryEntries");
        
        ArrayList entries = new ArrayList();
        try{
            int firstIndex=queryResult.indexOf(keyword);        

            if(firstIndex>-1)        
                firstIndex = queryResult.indexOf(" = ", firstIndex)+3;
            char firstChar = queryResult.charAt(firstIndex);

            int lastIndex = queryResult.indexOf("];", firstIndex+1)+1;        
            if(firstChar=='{')
                lastIndex =  queryResult.indexOf("};", firstIndex+1)+1;        

            if(firstIndex>-1 && lastIndex>firstIndex){

                String jsonPart = queryResult.substring(firstIndex, lastIndex);

                Map map = new HashMap();                
                ObjectMapper objectMapper=new ObjectMapper();            
                
                if(jsonPart.startsWith("[")){
                    entries = objectMapper.readValue(jsonPart, ArrayList.class);
                    map.put("root", entries);
                }
                else map = objectMapper.readValue(jsonPart, HashMap.class);

                boolean containsroot = map.containsKey("root");
                boolean containsexons = map.containsKey("exons");                        

                if(containsroot)
                    entries = (ArrayList)map.get("root") ;
                else if(containsexons)
                    entries = (ArrayList)map.get("exons") ;
                else entries.add(map);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        return entries;
        
    }    
   
}
