    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import exactool.ExtractorDefinitions;
import exactool.datahandler.dataobjects.CombinedVariant;
import exactool.datahandler.dataobjects.Feature;
import exactool.datahandler.dataobjects.Variant;
import exactool.datahandler.dataobjects.gene;
import java.io.PrintStream;

//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;

/**
 *
 * Uses Quick-Json: https://code.google.com/archive/p/quick-json/wikis/Usage.wiki
 * @author flip
 */
public abstract class BaseQuerier {    
   
    public String cachedData = null;
    public static int SEARCH_TYPE = ExtractorDefinitions.GENE_SEARCH;
    
    public static String TEMPLATE_VALUE = "template_value_base_querier";
    
    //OUTPUT_HEADER_*, the headers used for the tables and output files in the Extractor tool
    public static final String OUTPUT_HEADER_CANONICAL="CANONICAL";     
    public static final String OUTPUT_HEADER_EXAC_ID="EXAC_ID"; 
    public static final String OUTPUT_HEADER_RS_ID="RS_ID";
    public static final String OUTPUT_HEADER_CHR="CHR";    
    public static final String OUTPUT_HEADER_START_POS="START_POS";
    public static final String OUTPUT_HEADER_END_POS="END_POS";
    public static final String OUTPUT_HEADER_POS="POS";
    public static final String OUTPUT_HEADER_ALT="ALT";
    public static final String OUTPUT_HEADER_REF="REF";    
    public static final String OUTPUT_HEADER_CATEGORY="CATEGORY";
    public static final String OUTPUT_HEADER_FILTER="FILTER";
    public static final String OUTPUT_HEADER_CONSEQUENCE="CONSEQUENCE";
    public static final String OUTPUT_HEADER_MAJOR_CONSEQUENCE="MAJOR_CONSEQUENCE";
    public static final String OUTPUT_HEADER_ALLELE_COUNT_GLOBAL="ALLELE_COUNT_GLOBAL";
    public static final String OUTPUT_HEADER_ALLELE_NR_GLOBAL="ALLELE_NR_GLOBAL";
    public static final String OUTPUT_HEADER_HOM_COUNT_GLOBAL="HOM_COUNT_GLOBAL";
    public static final String OUTPUT_HEADER_FREQUENCY_GLOBAL="FREQUENCY_GLOBAL";
    public static final String OUTPUT_HEADER_SEL_POP_ALLELE_CNTS="SEL_POP_ALLELE_CNTS";
    public static final String OUTPUT_HEADER_SEL_POP_ALLELE_NRS="SEL_POP_ALLELE_NRS";
    public static final String OUTPUT_HEADER_SEL_POP_HOM_COUNT="SEL_POP_HOM_COUNT";
    public static final String OUTPUT_HEADER_SEL_POP_FREQ="SEL_POP_FREQ";
    public static final String OUTPUT_HEADER_POP_ALLELE_CNTS="POP_ALLELE_CNTS";
    public static final String OUTPUT_HEADER_POP_ALLELE_NRS="POP_ALLELE_NRS";
    public static final String OUTPUT_HEADER_POP_HOMS="POP_HOMS";
    public static final String OUTPUT_HEADER_SEARCH_TERM="SEARCH_TERM";
    
    public static final String OUTPUT_HEADER_EXOME_COVERAGE="COV_EXOME";
    public static final String OUTPUT_HEADER_GENOME_COVERAGE="COV_GENOME";
    
    //coverage headers
    public static final String OUTPUT_HEADER_GENE_NAME="GENE";
    public static final String OUTPUT_HEADER_MEAN_COVERAGE="MEAN_COVERAGE";
    public static final String OUTPUT_HEADER_MEAN_COVERAGE_EXOMES="MEAN_COVERAGE_EXOMES";
    public static final String OUTPUT_HEADER_MEAN_COVERAGE_GENOMES="MEAN_COVERAGE_GENOMES";
    public static final String OUTPUT_HEADER_REGION="REGION";
    public static final String OUTPUT_HEADER_REGION_MEAN_COVERAGE="REGION_MEAN_COVERAGE";
    public static final String OUTPUT_HEADER_GENE_SUMMARY="GENE_SUMMARY";
    
    //feature headers
    public static final String OUTPUT_HEADER_FEATURE_INDEX = "INDEX";
    public static final String OUTPUT_HEADER_FEATURE_TYPE = "TYPE";
    public static final String OUTPUT_HEADER_FEATURE_START = "START";
    public static final String OUTPUT_HEADER_FEATURE_END = "END";
    public static final String OUTPUT_HEADER_FEATURE_SIZE = "SIZE_BP";
    public static final String OUTPUT_HEADER_FEATURE_MAX_EMPTY_SIZE = "SIZE_MAX_EMPTY";
    public static final String OUTPUT_HEADER_FEATURE_MAX_EMPTY_PERCENTGAGE = "PERCENTAGE_MAX_EMPTY";  
    public static final String OUTPUT_HEADER_FEATURE_MAX_EMPTY_START = "START_MAX_EMPTY";
    public static final String OUTPUT_HEADER_FEATURE_MAX_EMPTY_END = "END_MAX_EMPTY";
    public static final String OUTPUT_HEADER_FEATURE_FIRST_VAR_POS = "FIRST_VAR_POS";
    public static final String OUTPUT_HEADER_FEATURE_LAST_VAR_POS = "LAST_VAR_POS";
    
    //gnomad header
    public static final String OUTPUT_HEADER_INDEL="INDEL";
    public static final String OUTPUT_HEADER_DATASET="DATASET";
    
     public final static String[] OUTPUT_VAR_HEADERS = new String[]{OUTPUT_HEADER_CHR, OUTPUT_HEADER_START_POS, OUTPUT_HEADER_END_POS, OUTPUT_HEADER_REF, OUTPUT_HEADER_ALT, OUTPUT_HEADER_SEARCH_TERM, OUTPUT_HEADER_GENE_NAME, OUTPUT_HEADER_EXAC_ID, 
            OUTPUT_HEADER_DATASET, OUTPUT_HEADER_CANONICAL, OUTPUT_HEADER_RS_ID, OUTPUT_HEADER_CATEGORY, OUTPUT_HEADER_FILTER, OUTPUT_HEADER_CONSEQUENCE, OUTPUT_HEADER_MAJOR_CONSEQUENCE, OUTPUT_HEADER_INDEL, 
            OUTPUT_HEADER_ALLELE_COUNT_GLOBAL,  OUTPUT_HEADER_ALLELE_NR_GLOBAL, OUTPUT_HEADER_HOM_COUNT_GLOBAL, OUTPUT_HEADER_FREQUENCY_GLOBAL, 
            OUTPUT_HEADER_SEL_POP_ALLELE_CNTS, OUTPUT_HEADER_SEL_POP_ALLELE_NRS, OUTPUT_HEADER_SEL_POP_HOM_COUNT, OUTPUT_HEADER_SEL_POP_FREQ, 
            OUTPUT_HEADER_POP_ALLELE_CNTS, OUTPUT_HEADER_POP_ALLELE_NRS, OUTPUT_HEADER_POP_HOMS
             , OUTPUT_HEADER_EXOME_COVERAGE,OUTPUT_HEADER_GENOME_COVERAGE
     };        
  
    //public final static String[] OUTPUT_COV_HEADERS = new String[]{OUTPUT_HEADER_SEARCH_TERM, OUTPUT_HEADER_GENE_NAME, OUTPUT_HEADER_MEAN_COVERAGE, OUTPUT_HEADER_REGION, OUTPUT_HEADER_REGION_MEAN_COVERAGE, OUTPUT_HEADER_GENE_SUMMARY};
    
    public final static String[] OUTPUT_COV_HEADERS = new String[]{OUTPUT_HEADER_SEARCH_TERM, OUTPUT_HEADER_GENE_NAME, OUTPUT_HEADER_MEAN_COVERAGE_EXOMES,  OUTPUT_HEADER_MEAN_COVERAGE_GENOMES, OUTPUT_HEADER_REGION, OUTPUT_HEADER_REGION_MEAN_COVERAGE, OUTPUT_HEADER_GENE_SUMMARY};
    
    public final static String[] OUTPUT_FEATURE_HEADERS = new String[]{OUTPUT_HEADER_SEARCH_TERM, OUTPUT_HEADER_GENE_NAME, OUTPUT_HEADER_FEATURE_INDEX, OUTPUT_HEADER_FEATURE_TYPE, OUTPUT_HEADER_FEATURE_START, OUTPUT_HEADER_FEATURE_END, OUTPUT_HEADER_FEATURE_SIZE, OUTPUT_HEADER_FEATURE_MAX_EMPTY_SIZE, OUTPUT_HEADER_FEATURE_MAX_EMPTY_PERCENTGAGE, 
                        OUTPUT_HEADER_FEATURE_FIRST_VAR_POS, OUTPUT_HEADER_FEATURE_LAST_VAR_POS,
                        OUTPUT_HEADER_FEATURE_MAX_EMPTY_START, OUTPUT_HEADER_FEATURE_MAX_EMPTY_END};
    
     public static final String[] VARIANT_COLUMN_TOOLTIPS = {
        "Chromosome variant is located on", // CHR
        "Start position of variant", // START_POS
        "End position of variant", // END_POS
        "Reference allele", // REF
        "Alternate allele", // ALT    
        
        "ID searched in exac, such as gene, transcript, region", //SEARCH_ID
        "Gene corresponding to id searched for (can be duplicate)", //GENE NAME
        "Variant ID (represented as 'chr-pos-reference-alternate')", //EXAC_ID
        "Dataset in which variant appears (E=ExAC, G=GnoMAD. Uppercase means FILTER=PASS, lowercase means not passed filter for specific dataset)", //DATASET
        "Indicates if variant also occurs on the canonical transcript", //CANONICAL			
        "Rs-id", //RS_ID					
        "Variant main category (missense, synonymous, lof, other)", //CATEGORY
        "VSQR sensitivty filter & hard filters", //FILTER
        "HGSV annotation (protein change, where defined, or transcript change for splice variants, otherwise empty)", //CONSEQUENCE
        "Variant Effect Predictor (VEP) annotation using Gencode 81. Worst across all transcripts of this gene.", //MAJOR_CONSEQUENCE
        "Is variant an INDEL.", //INDLE
        
        "Alternate allele count in genotypes (genotype quality >= 20 & depth >=10)", //ALLELE_COUNT_GLOBAL
        "Total number of called genotypes (genotype quality >= 20 & depth >=10)", //ALLELE_NR_GLOBAL
        "Number of homozygous individuals for this alternate allele", //HOM_COUNT_GLOBAL		
        "Frequency using only high-quality genotypes. (The graphic displays allele frequency on a discrete scale: Singletons, <1/10,000, <1/1000, <1%, <5%, <50%, >50% )", //FREQUENCY_GLOBAL		
        
        "Alternate allele count in genotypes (genotype quality >= 20 & depth >=10) for selected populations", //SEL_POP_ALLELE_CNTS		
        "Total number of called genotypes (genotype quality >= 20 & depth >=10) for selected populations", //SEL_POP_ALLELE_NRS		
        "Number of homozygous individuals for this alternate allele for selected populations", //SEL_POP_HOM_COUNT		
        "Frequency using only high-quality genotypes for selected populations", //SEL_POP_FREQ			
        
        "Alternate allele count in genotypes (genotype quality >= 20 & depth >=10) per populations", //POP_ALLELE_CNTS			
        "Total number of called genotypes (genotype quality >= 20 & depth >=10) per populations", //POP_ALLELE_NRS			
        "Number of homozygous individuals for this alternate allele per populations" //POP_HOMS				
    };
     
    public final static String[] INFO_COLUMN_TOOLTIPS = {
        "ID searched in exac, such as gene, transcript, region", //SEARCH_ID
        "Gene corresponding to id searched for (can be duplicate)", //SEARCH_ID
                
        "The mean coverage for the searched term in the exome data", // MEAN_COV_EXOME
        "The mean coverage for the searched term in the genome data", // MEAN_COV_GENOME
        "Sub-region if specified ( in the form CHR:START-END )", // REGION
        "The mean coverage for the specific sub-region", // REGION_MEAN_COVERAGE
        "Gene summary info from refseq, based on gene-name (if any)" // GENE_SUMMARY        
    };
    
     public final static String[] FEATURE_COLUMN_TOOLTIPS = {
        "ID searched in exac, such as gene, transcript, region", //SEARCH_ID
         "Gene corresponding to id searched for (can be duplicate)", //SEARCH_ID
        "Index of this feature", // FEATURE_INDEX
        "Type of feature (exon, UTR, CDS)", // FEATURE_TYPE
        "Start position of this feature", // FEATURE_START
        "End position of the feature", // FEATURE_END
        "Size of the feature", // FEATURE_SIZE
        "Size of largest empty region in this feature", // FEATURE_MAX_EMPTY_SIZE
        "Percentage of largest empty region from this feature", // FEATURE_MAX_EMPTY_PERCENTAGE
        "Position of first variant found for this feature", // FEATURE_FIRST_VAR_POS
        "Position of last variant found for this feature", // FEATURE_LAST_VAR_POS
        "Start position of largest empty region in this feature", // FEATURE_MAX_EMPTY_START
        "End position of largest empty region in this feature" // FEATURE_MAX_EMPT_END		

    };
    
    ObjectMapper objectMapper = new ObjectMapper();                        
    public BaseQuerier(){
        
        
    }
    
    public static void printData(String filename, String content){
        try
        {
            PrintStream printer = new PrintStream(filename);
            printer.println(content);
            printer.close();
        }
        catch(Exception e){e.printStackTrace();}
    }
    
    //public abstract void setMode(int mode);
    
    public static String[] getVarHeaders(){
        return OUTPUT_VAR_HEADERS;
    }   
    
    public static String[] getCovHeaders(){
        return OUTPUT_COV_HEADERS;
    }   
    
    public static String[] getFeatureHeaders(){
        return OUTPUT_FEATURE_HEADERS;
    }
    public static int getVarIndex(String header){
        for(int i=0; i<OUTPUT_VAR_HEADERS.length; i++)
            if(header.equals(OUTPUT_VAR_HEADERS[i]))
                return i;                       
        return -1;        
    }
        
    public static int getSearchType(String term){
        if(term.indexOf(":")>0 && term.indexOf("-")>1)
            return ExtractorDefinitions.REGION_SEARCH;
        else if(term.startsWith("ENST"))
            return ExtractorDefinitions.TRANSCRIPT_SEARCH;
        else if(term.startsWith("VARIANT") || term.startsWith("rs"))
            return ExtractorDefinitions.VARIANT_SEARCH;
        
        return ExtractorDefinitions.GENE_SEARCH;
    }
        
    public abstract double[] calculateAvgCoverage(HashMap coverageMap, String region);
    
    //fetches the relevant json data from the entire queryresult, based on the specified keyword
    public abstract String getJsonPart(String queryResult, String keyword);
    
    //public abstract String queryGeneName(String searchTerm);
    
    //public abstract gene queryGene(String searchTerm);
        
    public abstract gene[] queryGenes(String searchTerm);
    
    //public abstract HashMap<Integer, double[]> queryCoverage(String queryResult, String searchTerm);
    
    public abstract Variant[] queryVariants(String searchTerm);
    
    public HashMap convertVariantsToCombined(Variant[] singleVariants){
        HashMap<String, CombinedVariant> result = new HashMap();       
        
        //make combined
        for(int i=0; i<singleVariants.length; i++)
        {
            CombinedVariant stored = result.get(singleVariants[i].exac_id);
            if(stored==null)
                   stored = new CombinedVariant();
            if(singleVariants[i].isExac())
                stored.exacData = singleVariants[i];
            else if(singleVariants[i].isGnomad())
                stored.gnomadData = singleVariants[i];

            result.put(singleVariants[i].exac_id, stored);
        }
        return result;
    }
    
    public abstract Feature[] queryFeatures(String searchTerm);
    
    public abstract HashMap<Integer, double[]> queryCoverage(String searchTerm);
       
    //public abstract String getRawDataForSearchTerm(String searchTerm);
    
    public abstract String prepareLocalData(String searchTerm);
    
    public abstract String prepareLocalData(String searchTerm, boolean retry);
    
    public abstract String translateSearchTerm(String searchTerm);
}
