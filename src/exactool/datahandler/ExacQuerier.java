    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
//import com.json.parsers.*;
import exactool.ExtractorDefinitions;
import static exactool.datahandler.BaseQuerier.SEARCH_TYPE;
import static exactool.datahandler.BaseQuerier.getSearchType;
import exactool.datahandler.dataobjects.Feature;
import exactool.datahandler.dataobjects.Transcript;
import exactool.datahandler.dataobjects.Variant;
import exactool.datahandler.dataobjects.gene;
import exactool.datahandler.dataobjects.pos_cov;
import exactool.ui.GeneShape;
import exactool.ui.LogDialog;
import javax.swing.JOptionPane;


/**
 *
 * Uses Quick-Json: https://code.google.com/archive/p/quick-json/wikis/Usage.wiki
 * @author flip
 */
public class ExacQuerier extends BaseQuerier {    
    
    static String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0";     
    static final String START_BODY = "<body>";
    static final String END_BODY = "</body>";    
    static final String NO_RESULTS = "returned no results";
    String search_url = null;
    
    public static String NEXT_RESULT_ID = "HGVSp";
    public static String END_ID = "window.variants_in_transcript";
    
    //exac_json_* are the main json data identifiers, returned as part of the exac output json data
    public static final String EXAC_JSON_GENE = "window.gene"; //gene info such as canonical transcript, name, descriptions, etc
    public static final String EXAC_JSON_GENES = "window.genes"; //gene info such as canonical transcript, name, descriptions, etc
    public static final String EXAC_JSON_TRANSCRIPT = "window.transcript"; //transcript info, such as exons
    public static final String EXAC_JSON_TABLE_VARIANTS = "window.table_variants"; //
    public static final String EXAC_JSON_VARIANTS_IN_GENE = "window.variants_in_gene"; //
    public static String EXAC_JSON_VARIANTS_KEYWORD = ExacQuerier.EXAC_JSON_TABLE_VARIANTS;
    public static final String EXAC_JSON_WINDOWS_VARIANTS = "window.variants"; //
    public static final String EXAC_JSON_VARIANTS_IN_TRANSCRIPT  = "window.variants_in_transcript";
    public static final String EXAC_JSON_TRANSCRIPTS_IN_GENE = "window.transcripts_in_gene"; //various transcripts in this gene
    public static final String EXAC_JSON_COVERAGE_STATS = "window.coverage_stats";
    public static final String EXAC_JSON_PAGE_NAME = "window.page_name"; 
    
    //EXAC_*_ID's are the keywords used to identify the json keys (from the key-value combination). These are returned as part of the json data in exac
    public static final String EXAC_GENE_NAME_ID="gene_name";        
    public static final String EXAC_CANONICAL_ID="CANONICAL";    
    public static final String EXAC_CATEGORY_ID = "category";
    public static final String EXAC_RSENTRY_ID = "rsid";
    public static final String EXAC_POS_ID = "pos";    
    public static final String EXAC_CHR_ID = "chrom";    
    public static final String EXAC_VARIANT_ID ="variant_id";
    public static final String EXAC_ALLELE_NR_ID = "allele_num";    
    public static final String EXAC_ALLELE_COUNT_ID = "allele_count";
    public static final String EXAC_ALLELE_FREQ_ID = "allele_freq";    
    public static final String EXAC_FILTER_ID = "filter";
    public static final String EXAC_HOM_COUNT_ID = "hom_count";
    public static final String EXAC_POP_ACS_ID = "pop_acs";
    public static final String EXAC_POP_ANS_ID = "pop_ans";    
    public static final String EXAC_MAJOR_CONSEQUENCE_ID = "major_consequence";
    public static final String EXAC_CONSEQUENCE_ID = "HGVSp";
    
    public static final String EXAC_DATASET_ID = "dataset";
    
    public static final String EXAC_INDEL_ID = "indel";
    public static final String EXAC_EXOMES_ID = "exomes";
    public static final String EXAC_GENOMES_ID = "genomes";
    
    public static final String EXAC_POP_HOMS_ID = "pop_homs";
    public static final String EXAC_REF_ID = "ref";
    public static final String EXAC_ALT_ID = "alt";
    public static final String EXAC_MEAN_ID = "mean";
  
    //EXAC_*_VALUE, these are (part of) values from the json key-value data returned by exac
    public static final String EXAC_RS_VALUE = "rs";    
    public static final String EXAC_MISSENSE_TYPE_VALUE = "missense_variant";
    public static final String EXAC_LOF_TYPE_VALUE = "lof_variant";
    public static final String EXAC_PASS_VALUE = "PASS";    
    
    public final static String[] VAR_IDS_EXAC = new String[]{EXAC_CHR_ID, EXAC_POS_ID, EXAC_POS_ID, EXAC_REF_ID, EXAC_ALT_ID, OUTPUT_HEADER_SEARCH_TERM, OUTPUT_HEADER_GENE_NAME, EXAC_VARIANT_ID, EXAC_CANONICAL_ID, EXAC_RSENTRY_ID, EXAC_CATEGORY_ID, EXAC_FILTER_ID, EXAC_CONSEQUENCE_ID, EXAC_MAJOR_CONSEQUENCE_ID,
            EXAC_ALLELE_COUNT_ID,  EXAC_ALLELE_NR_ID, EXAC_HOM_COUNT_ID, EXAC_ALLELE_FREQ_ID, 
            EXAC_ALLELE_COUNT_ID, EXAC_ALLELE_NR_ID, EXAC_HOM_COUNT_ID, EXAC_ALLELE_FREQ_ID, 
            EXAC_POP_ACS_ID, EXAC_POP_ANS_ID, EXAC_POP_HOMS_ID};
    
    public final static String[] VAR_IDS_GNOMAD = new String[]{EXAC_CHR_ID, EXAC_POS_ID, EXAC_POS_ID, EXAC_REF_ID, EXAC_ALT_ID, OUTPUT_HEADER_SEARCH_TERM, OUTPUT_HEADER_GENE_NAME, EXAC_VARIANT_ID, EXAC_CANONICAL_ID, EXAC_RSENTRY_ID, EXAC_CATEGORY_ID, EXAC_FILTER_ID, EXAC_CONSEQUENCE_ID, EXAC_MAJOR_CONSEQUENCE_ID, EXAC_INDEL_ID,
            EXAC_ALLELE_COUNT_ID,  EXAC_ALLELE_NR_ID, EXAC_HOM_COUNT_ID, EXAC_ALLELE_FREQ_ID, 
            EXAC_ALLELE_COUNT_ID, EXAC_ALLELE_NR_ID, EXAC_HOM_COUNT_ID, EXAC_ALLELE_FREQ_ID, 
            EXAC_POP_ACS_ID, EXAC_POP_ANS_ID, EXAC_POP_HOMS_ID,
            EXAC_DATASET_ID};
    
    public static String[] VAR_IDS = VAR_IDS_EXAC;
    
    public ExacQuerier(){
        EXAC_JSON_VARIANTS_KEYWORD =  EXAC_JSON_TABLE_VARIANTS;        
        VAR_IDS = VAR_IDS_EXAC;
        //VAR_INDICES = VAR_INDICES_EXAC;
        //OUTPUT_VAR_HEADERS = OUTPUT_VAR_HEADERS_EXAC;    
        search_url = "http://exac.broadinstitute.org/awesome?query=";
    }
    
    /**
     * 
     * @param queryResult   The resulting text from querying the exac website
     * @return A HashMap containing a mapping between position and an array of double with exome coverage stored at index 0 and genome coverate at index 1. -1 means no entry 
     */
    public HashMap<Integer, double[]> queryCoverage(String searchTerm){        
        pos_cov[] cov_data = new pos_cov[0];
        try{
            String jsonData = getJsonPart(cachedData, ExacQuerier.EXAC_JSON_COVERAGE_STATS);
            cov_data = objectMapper.readValue(jsonData, pos_cov[].class);         
            if(cov_data==null)
                cov_data = new pos_cov[0];
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        
        
        HashMap<Integer, double[]> result = new HashMap();        
        for(int i=0; i<cov_data.length; i++)
            result.put(cov_data[i].pos, new double[]{cov_data[i].mean, -1});            
        return result;
    }
   
   
    public double[] calculateAvgCoverage(HashMap coverageMap, String region){
        double result[] = new double[]{-1.0, -1.0};
        
        int exomeCovCount = 0, genomeCovCount = 0;
        double exomeCovSum = 0.0, genomeCovSum = 0.0;
        
        int start = -1, end = -1;
        if(region!=null){            
            String start_end[] = region.split(":")[1].split("-");                       
            start = Integer.parseInt(start_end[0]);
            end = Integer.parseInt(start_end[1]);
        }
        
        ArrayList entries = new ArrayList(coverageMap.keySet());        
        for(int i=0; i<entries.size(); i++)
        {
            Integer pos = (Integer)entries.get(i);
            if(start==-1 || (pos>=start && pos<=end))
            {
                double cov[] = (double[])coverageMap.get(pos);
                if(cov[0]>0){
                    exomeCovSum+=cov[0];
                    exomeCovCount++;
                }
                if(cov[1]>0){
                    genomeCovSum+=cov[1];
                    genomeCovCount++;
                }                
            }                                 
        }    
        //System.out.println("Nr of mean cov found: "+exomeCovCount+", "+genomeCovCount);
        if(exomeCovCount>0)
            result[0] = exomeCovSum/(double)exomeCovCount;   
        if(genomeCovCount>0)
            result[1] = genomeCovSum/(double)genomeCovCount;   
        
        return result;
    }
            
    public  String getJsonPart(String queryResult, String keyword){
        String result = "";
        
        int firstIndex=queryResult.indexOf(keyword+" = ");                
        if(firstIndex>-1)           
        {
            firstIndex = queryResult.indexOf(" = ", firstIndex)+3;        
            char firstChar = queryResult.charAt(firstIndex);

            //firstIndex++;        
            int lastIndex = queryResult.indexOf("];", firstIndex+1)+1;        
            if(firstChar=='{')
                lastIndex =  queryResult.indexOf("};", firstIndex+1)+1;        

            if(firstIndex>-1 && lastIndex>firstIndex){
                result = queryResult.substring(firstIndex, lastIndex);
            }
        }
        return result;        
    }
    
    public gene[] queryGenes(String searchTerm){
        gene result[] = new gene[0];
        int searchType = BaseQuerier.getSearchType(searchTerm);
        try{
            if(searchType == ExtractorDefinitions.GENE_SEARCH || searchType == ExtractorDefinitions.TRANSCRIPT_SEARCH){
                String jsonData = getJsonPart(cachedData, ExacQuerier.EXAC_JSON_GENE);
                result = new gene[]{objectMapper.readValue(jsonData, gene.class)};    
            }
            else if(searchType == ExtractorDefinitions.REGION_SEARCH){
                String jsonData = getJsonPart(cachedData, ExacQuerier.EXAC_JSON_GENES);
                result = objectMapper.readValue(jsonData, gene[].class);                
            }            
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
    /*
    public gene[] queryGenes(String searchTerm){
        gene result[] = new gene[0];
        try{
            String jsonData = getJsonPart(cachedData, ExacQuerier.EXAC_JSON_GENES);
            result = objectMapper.readValue(jsonData, gene[].class);                
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
    
    public gene queryGene(String searchTerm){
        gene result = null;
        try{
            String jsonData = getJsonPart(cachedData, ExacQuerier.EXAC_JSON_GENE);
            result = objectMapper.readValue(jsonData, gene.class);                
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
*/
    public Feature[] queryFeatures(String searchTerm){    
        Feature result[] = new Feature[0];
        
        try{
            if(SEARCH_TYPE==ExtractorDefinitions.REGION_SEARCH || SEARCH_TYPE==ExtractorDefinitions.VARIANT_SEARCH)
            {
                result = new Feature[]{new Feature()};
                if(SEARCH_TYPE==ExtractorDefinitions.REGION_SEARCH)
                {
                    result[0].chrom = searchTerm.substring(0, searchTerm.indexOf(":"));
                    result[0].start = Integer.parseInt(searchTerm.substring(searchTerm.indexOf(":")+1, searchTerm.indexOf("-")));
                    result[0].stop =  Integer.parseInt(searchTerm.substring(searchTerm.indexOf("-")+1, searchTerm.length()));
                    result[0].feature_type = GeneShape.REGION;
                }
                else if(SEARCH_TYPE==ExtractorDefinitions.VARIANT_SEARCH)
                {
                    result[0].chrom = "";
                    result[0].start = -1;
                    result[0].stop =  -1;
                    result[0].feature_type = GeneShape.SINGLE_VARIANT;
                }
                return result;
            }
             
            String jsonData = getJsonPart(cachedData, ExacQuerier.EXAC_JSON_TRANSCRIPT);            
            Transcript transcript = objectMapper.readValue(jsonData, Transcript.class);     
            result = transcript.exons;           
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }     
    
    public String prepareLocalData(String searchTerm){
        return prepareLocalData(searchTerm, true);
    }
    public String prepareLocalData(String searchTerm, boolean retry){
        SEARCH_TYPE = getSearchType(searchTerm);
        if(SEARCH_TYPE == ExtractorDefinitions.REGION_SEARCH){
             
            String chr = searchTerm.substring(0, searchTerm.indexOf(":"));
            String st = searchTerm.substring(chr.length()+1, searchTerm.indexOf("-"));
            int start = Integer.parseInt(st);
            int end = Integer.parseInt(searchTerm.substring(chr.length()+st.length()+2, searchTerm.length()));
            if(end-start>100000)
                return "Your region is too large. Please submit a region of at most 100 kb. Use VCF mode if you require larger regions.";
        }
        
        if(cachedData==null)
            cachedData = getRawDataForSearchTerm(searchTerm);
        else if(!isValidBody(cachedData))
        {
            //JOptionPane.showMessageDialog(null, "<html>Cached data for '"+searchTerm+"' appears to be corrupt.<br><br>Trying to retrieve results from the website.</hmtl>");            
            cachedData = getRawDataForSearchTerm(searchTerm);
            LogDialog.log+=searchTerm+"\tCached results corrupted, re-queried site.";            
            if(isValidBody(cachedData))
                LogDialog.log+=" Success!\n";
            else LogDialog.log+=" Failed! No results found...\n";
        }
        if(cachedData==null){
            if(retry)
                prepareLocalData(searchTerm, false);
            else cachedData = "";            
        }
        //BaseQuerier.printData("E:\\exac_gnomad\\"+searchTerm+"_raw.txt", cachedData);
        return null;
        
    }
    
    public Variant[] queryVariants(String searchTerm){
        Variant result[] = new Variant[0];        
        try
        {
            String jsonData = "";
            //fetch relevant json part from data
            if( (searchTerm.contains(":") && searchTerm.contains("-")) || searchTerm.startsWith("rs"))
                jsonData = getJsonPart(cachedData, EXAC_JSON_WINDOWS_VARIANTS); 
            else jsonData = getJsonPart(cachedData, EXAC_JSON_VARIANTS_KEYWORD);               
            //and get parsed result
            result = objectMapper.readValue(jsonData, Variant[].class);           
            //reformat results to match representation on exac/gnomad webpage
            for(int i=0; i<result.length; i++)
            {
                //update searchterm and gene name
                result[i].search_term = searchTerm;
                //result[i].gene = searchTerm;
                
                if(result[i].ref.length()>result[i].alt.length()){
                    result[i].ref = result[i].ref.substring(1);
                    result[i].alt = "-";
                    result[i].pos++;
                }
                else if(result[i].ref.length()<result[i].alt.length()){
                    result[i].alt=result[i].alt.substring(1);
                    result[i].ref = "-";                
                }                
                result[i].start_pos = result[i].pos;                
                result[i].end_pos = result[i].pos+result[i].ref.length()-1;
            }
            Arrays.sort(result);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
       
        
    }    
    
    private boolean isValidBody(String content){
        return content!=null && content.indexOf(START_BODY)>-1 && content.indexOf(END_BODY)>-1;// && content.indexOf(END_BODY)>-1;
    }
    
    public String getRawDataForSearchTerm(String searchTerm){
        String result = null;        
        try
        {
            URL url = new URL(search_url+searchTerm);  
            //System.out.println(search_url+searchTerm);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");            
            con.setRequestProperty("User-Agent",USER_AGENT);
            
            if(con.getResponseCode()!=404)
            {                
            
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));            
                StringBuffer response = new StringBuffer();

                String inputLine = null;        
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);                
                }

                //boolean validBody = response.indexOf(END_BODY)>-1;
                if(isValidBody(response.toString()))
                {
                    if(response.indexOf(NO_RESULTS)==-1){
                        int indexOfId = response.indexOf(NEXT_RESULT_ID);      
                        int indexOfEnd = response.indexOf(END_ID);      

                        if(indexOfId>-1 && indexOfEnd>indexOfId)
                            result=response.substring(indexOfId, indexOfEnd+END_ID.length());                
                        else result="";
                        result=response.toString();

                        if(result.startsWith("["))
                            result = result.substring(1, result.length()-1);
                    }
                    else{
                        result = "";
                    }
                }
                in.close();
            }
            con.disconnect();    
        }
        catch(Exception e){
            e.printStackTrace();            
        }
        return result;
    }
    public String translateSearchTerm(String searchTerm){
        return searchTerm;
    }
    
}
