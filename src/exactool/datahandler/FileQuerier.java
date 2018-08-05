    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler;

import java.util.*;
import exactool.ExtractorDefinitions;
import exactool.datahandler.dataobjects.Feature;
import exactool.datahandler.dataobjects.Variant;
import exactool.datahandler.dataobjects.gene;
import exactool.datahandler.dataobjects.pop_base;
import htsjdk.tribble.readers.TabixReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Uses Quick-Json: https://code.google.com/archive/p/quick-json/wikis/Usage.wiki
 * @author flip
 */
public class FileQuerier extends BaseQuerier {    
    
    public final static String african_full = "African";
    public final static String east_asian_full = "East Asian";
    public final static String european_finnish_full = "European (Finnish)";
    public final static String european_non_finnish_full = "European (Non-Finnish)";
    public final static String latino_full = "Latino";
    public final static String other_full = "Other";
    public final static String south_asian_full = "South Asian";
    public final static String pops_full[] = new String[]{european_non_finnish_full, latino_full, east_asian_full, european_finnish_full, south_asian_full, african_full, other_full};

    public final static String african = "AFR";
    public final static String east_asian = "EAS";
    public final static String european_finnish = "FIN";
    public static final String european_non_finnish = "NFE";
    public static final String latino = "AMR";
    public static final String other = "OTH";
    public static final String south_asian = "SAS";
    public static final String pops_id[] = new String[]{european_non_finnish, latino, east_asian, european_finnish, south_asian, african, other};
    
    public static String glob_allele_cnts = "AC_Adj"; //per var
    public static String glob_allele_nr = "AN_Adj"; //total
    public static String glob_hom_cnt = "AC_Hom"; //total
    //glob_freq = ""; //allele_cnt/allele_nr
    
    public static String del = "\t";
    //##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence annotations from Ensembl VEP. Format: Allele|Consequence|IMPACT|SYMBOL|Gene|Feature_type|Feature|BIOTYPE|EXON|INTRON|HGVSc|HGVSp|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|ALLELE_NUM|DISTANCE|STRAND|VARIANT_CLASS|MINIMISED|SYMBOL_SOURCE|HGNC_ID|CANONICAL|TSL|CCDS|ENSP|SWISSPROT|TREMBL|UNIPARC|SIFT|PolyPhen|DOMAINS|HGVS_OFFSET|GMAF|AFR_MAF|AMR_MAF|ASN_MAF|EAS_MAF|EUR_MAF|SAS_MAF|AA_MAF|EA_MAF|CLIN_SIG|SOMATIC|PHENO|PUBMED|MOTIF_NAME|MOTIF_POS|HIGH_INF_POS|MOTIF_SCORE_CHANGE|LoF_info|LoF_flags|LoF_filter|LoF|context|ancestral">
    //public static String consequences = "CSQ";
    public static String annotations = "ANN";
    
    public static String allele_count_id = "AC_";
    public static String allele_number_id = "AN_";
    public static String hom_id = "Hom_";    
          
    //TabixReader tr =null;// = new TabixReader();
    public static String high_impact_variants[] = new String[]{"transcript_ablation", "splice_acceptor_variant", "splice_donor_variant", "stop_gained", "frameshift_variant", "stop_lost","start_lost", "transcript_amplification"};
    public static String moderate_impact_variants[] = new String[]{"inframe_insertion", "inframe_deletion", "disruptive_inframe_deletion", "missense_variant", "protein_altering_variant", "disruptive_inframe_insertion"};
    public static String low_impact_variants[] = new String[]{"splice_region_variant", "incomplete_terminal_codon_variant", "stop_retained_variant", "synonymous_variant"};
    public static String modifier_impact_variants[] = new String[]{"coding_sequence_variant", "mature_miRNA_variant", "5_prime_UTR_variant", "3_prime_UTR_variant", "non_coding_transcript_exon_variant", "intron_variant",
                                                                   "NMD_transcript_variant", "non_coding_transcript_variant", "upstream_gene_variant", "downstream_gene_variant", "TFBS_ablation", "TFBS_amplification",
                                                                   "TF_binding_site_variant", "regulatory_region_ablation", "regulatory_region_amplification", "feature_elongation", "regulatory_region_variant", "feature_truncation", 
                                                                   "intergenic_variant"};
    
    public static String lof_variants[] = new String[]{"frameshift_variant", "splice_acceptor_variant", "splice_donor_variant", "stop_gained"};
    public static String missense_variants[] = new String[]{"inframe_insertion", "inframe_deletion", "disruptive_inframe_deletion", "missense_variant", "stop_lost", "start_lost", "disruptive_inframe_insertion"};
    public static String synonymous_variants[] = new String[]{"splice_region_variant", "synonymous_variant"};
    public static String other_variants[] = new String[]{"upstream_gene_variant", "downstream_gene_variant", "5_prime_UTR_variant", "3_prime_UTR_variant", "non_coding_transcript_exon_variant", "intron_variant"};
        
    public static HashMap searchtermToChrMap = new HashMap();
    public String gene_name = null;
    
    private HashMap<String, Integer> indexMap = new HashMap();
    public FileQuerier(){
        
    }
    
    public static String getMajorCategory(String consequence){        
        for(int v=0; v<lof_variants.length; v++)        
            if(lof_variants[v].equals(consequence)) return "lof_variant";
        for(int v=0; v<missense_variants.length; v++)        
            if(missense_variants[v].equals(consequence)) return "missense_variant";
        for(int v=0; v<synonymous_variants.length; v++)        
            if(synonymous_variants[v].equals(consequence)) return "synonymous_variant";
        for(int v=0; v<other_variants.length; v++)        
            if(other_variants[v].equals(consequence)) return "other_variant";
        return "unknown_variant";
    }
    
     public static int getConsequenceValue(String consequence){
         for(int v=0; v<high_impact_variants.length; v++)        
            if(high_impact_variants[v].equals(consequence)) return 3;
         for(int v=0; v<moderate_impact_variants.length; v++)        
            if(moderate_impact_variants[v].equals(consequence)) return 2;
         for(int v=0; v<low_impact_variants.length; v++)        
            if(low_impact_variants[v].equals(consequence)) return 1;
         for(int v=0; v<modifier_impact_variants.length; v++)        
            if(modifier_impact_variants[v].equals(consequence)) return 0;
         return -1;         
    }
     
    public FileQuerier(String vcfFile) {
        /*
        try
        {            
            tr = new TabixReader(vcfFile);                              
        }
        catch(IOException e){
         e.printStackTrace();
                }
        */
    }
        
    public String prepareLocalData(String searchTerm){
        return prepareLocalData(searchTerm, true);
    }
    /**
     * Populates lookup maps for gene and transcript to chr
     * @param searchTerm
     * @return 
     */
    public String prepareLocalData(String searchTerm, boolean retry)
    {
        if(searchtermToChrMap==null || searchtermToChrMap.isEmpty()){
            try{                
                String line = null;                    
                GzipReader reader = new GzipReader(ExtractorDefinitions.getGeneToChrFile());
                while( (line = reader.readLine()) !=null) {
                    String tokens[] = line.split("\t");
                    searchtermToChrMap.put( tokens[0], tokens[1]);                    
                }
                reader.close();
                
                reader = new GzipReader(ExtractorDefinitions.getTranscriptToChrFile());
                while( (line = reader.readLine()) !=null) {
                    String tokens[] = line.split("\t");
                    searchtermToChrMap.put( tokens[0], tokens[1]);                    
                }
                reader.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }   
        }
        return null;
        /*
        SEARCH_TYPE = getSearchType(searchTerm);        
        //map is only used for gene/transcript --> chr mapping. For region and variant chr is in the query itself
        if(!searchtermToChrMap.containsKey(searchTerm)){
            try{                
                
                GzipReader reader = null;
                //Reader reader = null;
                //only gene names and transcripts need mapping
                
                if(SEARCH_TYPE == ExtractorDefinitions.GENE_SEARCH)
                    reader = new GzipReader(ExtractorDefinitions.getGeneToChrFile());
                else if(SEARCH_TYPE == ExtractorDefinitions.TRANSCRIPT_SEARCH)
                    reader = new GzipReader(ExtractorDefinitions.getTranscriptToChrFile());                
                if(reader!=null){
                    String line = null;                    
                    while( (line = reader.readLine()) !=null) {
                        String tokens[] = line.split("\t");
                        searchtermToChrMap.put( tokens[0], tokens[1]);                    
                    }
                    reader.close();
                }                
            }
            catch(Exception e){
                e.printStackTrace();
            }            
        }     
        return null;
        */
        
    }
    
    public String getChrForSearchTerm(String searchTerm){
        String result = "";
        if(SEARCH_TYPE== ExtractorDefinitions.GENE_SEARCH || SEARCH_TYPE== ExtractorDefinitions.TRANSCRIPT_SEARCH){
                result = (String)searchtermToChrMap.get(searchTerm);
        }
        else if(SEARCH_TYPE== ExtractorDefinitions.REGION_SEARCH){
            if(searchTerm.indexOf(":")>0)
                result = searchTerm.substring(0, searchTerm.indexOf(":"));                
        }
        else if(SEARCH_TYPE== ExtractorDefinitions.TRANSCRIPT_SEARCH){
             if(searchTerm.indexOf("-")>0)
                result = searchTerm.substring(0, searchTerm.indexOf("-"));           
        }
        return result;
    }
    /**
     * 
     * @param queryResult   The resulting text from querying the exac website
     * @return A HashMap containing a mapping between position and an array of double with exome coverage stored at index 0 and genome coverate at index 1. -1 means no entry 
     */
    //public HashMap<Integer, double[]> queryCoverage(String coverageVCF, String searchTerm){
    public HashMap<Integer, double[]> queryCoverage(String searchTerm){
        HashMap<Integer, double[]> result = new HashMap();
        
        try
        {
            
            String translatedSearchTerm = translateSearchTerm(searchTerm);  
            TabixReader cr = new TabixReader(ExtractorDefinitions.getCoverageFileForChr(getChrForSearchTerm(searchTerm))); 
            
            Object hit = null;
            TabixReader.Iterator it = cr.query(translatedSearchTerm);
            while( (hit = it.next()) != null){
                String tokens[] = hit.toString().split("\t");
                int pos = Integer.parseInt(tokens[1]);
                double mean = Double.parseDouble(tokens[2]);
                result.put((Integer)pos, new double[]{mean, -1});
            }
            
            cr.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
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
                   
    public ArrayList queryEntries(String queryResult, String keyword){        
        ArrayList entries = new ArrayList();
     
        return entries;
        
    }
    
    public gene[] queryGenes(String searchTerm){
        System.out.println("FileQuerier.queryGenes(): IMPLEMENT!");
        gene result[] = new gene[0];        
        
        return result;
    }
    public gene queryGene(String searchTerm){
        gene result = new gene();
        if(gene_name!=null){
            result.gene_name = gene_name;
            result.gene_name_upper = gene_name;
        }
        else
        {
            result.gene_name = "FILE_GENE_"+searchTerm;
            result.gene_name_upper = "FILE_GENE_"+searchTerm;        
        }
        return result;
    }
    public String queryGeneName(String searchTerm){
         if(gene_name!=null)
            return gene_name;
         else return "FILE_GENE_"+searchTerm;
    }
        
    public Feature[] queryFeatures(String searchTerm){

        //searchTerm must be translated to chr:start-end (using corresponding chr file)
        Feature result[] = new Feature[0];
        String translatedSearchTerm = searchTerm;
        String chrFile = ExtractorDefinitions.getFeatureFileForChr(getChrForSearchTerm(searchTerm));

        if(chrFile!=null){
           if(SEARCH_TYPE == ExtractorDefinitions.GENE_SEARCH)
           {              
               translatedSearchTerm = CustomGeneFileQuerier.getCanonicalTranscript(chrFile, searchTerm);
               result = CustomGeneFileQuerier.getFeaturesForTranscript(chrFile, translatedSearchTerm);
               //System.out.println(translatedSearchTerm);
           }
           else if(SEARCH_TYPE == ExtractorDefinitions.TRANSCRIPT_SEARCH){
               result = CustomGeneFileQuerier.getFeaturesForTranscript(chrFile, searchTerm);
           }
           else if(SEARCH_TYPE == ExtractorDefinitions.REGION_SEARCH){
               ArrayList ct = CustomGeneFileQuerier.getCanonicalTranscriptsInRegion(chrFile, searchTerm);
               int count = 0;
               for(int i=0; i<ct.size(); i++){
                   //Object tempResult[][] = CustomGeneFileQuerier.getFeaturesForTranscript(chrFile, (String)ct.get(i));
                   Feature tempResult[] = CustomGeneFileQuerier.getFeaturesForTranscript(chrFile, (String)ct.get(i));
                   count = count+tempResult.length;
                   ct.set(i, tempResult);
               }
               result = new Feature[count];
               //result = new Object[count][4];
               count = 0;
               for(int i=0; i<ct.size(); i++)
               {
                   Feature tempResult[] = (Feature[])ct.get(i);
                   for(int t=0; t<tempResult.length; t++)
                    {
                        result[t+count].chrom = tempResult[t].chrom;
                        result[t+count].start = tempResult[t].start;
                        result[t+count].stop = tempResult[t].stop;
                        result[t+count].feature_type = tempResult[t].feature_type;
                        result[t+count].gene = tempResult[t].gene;                                              
                    }
                   count = count+tempResult.length;
                }
           }
        }   
        return result;
    }
    public String translateSearchTerm(String searchTerm){
        String translatedSearchTerm = searchTerm;
         //convert query to region where necessary
            if(SEARCH_TYPE==ExtractorDefinitions.GENE_SEARCH || SEARCH_TYPE==ExtractorDefinitions.TRANSCRIPT_SEARCH)
            {
                String chr = (String)searchtermToChrMap.get(searchTerm);
                if(SEARCH_TYPE==ExtractorDefinitions.GENE_SEARCH )
                    translatedSearchTerm = CustomGeneFileQuerier.getRegionForGene(ExtractorDefinitions.getFeatureFileForChr(chr), searchTerm);
                else translatedSearchTerm = CustomGeneFileQuerier.getRegionForTransript(ExtractorDefinitions.getFeatureFileForChr(chr), searchTerm);
            }
         return translatedSearchTerm;
    }
   
    public String getCanonicalTranscriptForGene(String gene){
        //convert query to region where necessary
        if(SEARCH_TYPE==ExtractorDefinitions.GENE_SEARCH)
        {
            String chr = (String)searchtermToChrMap.get(gene);
            return CustomGeneFileQuerier.getCanonicalTranscript(ExtractorDefinitions.getFeatureFileForChr(chr), gene);                
        }
        return null;
    }
    //backup plan for
    public String getChange(String refid, String altid, String orgRefid, String orgAltid){
        String rep = "";
        
        if(refid.length()==1)
        {
            if(refid.equals("-"))
            {
                if(altid.length()==1)
                {
                    if(orgRefid.length()==1)
                        return "ins"+altid;
                    else return "dup"+altid;
                }
                else return null;//"dup"+altid;
            }
            else
            {
                if(altid.equals("-"))
                    return "del"+refid;
                    //return null;
                else if(altid.length()==1)
                    return refid+">"+altid;
                else return null;//"dup"+altid;
            }
        }
        
        //System.out.println("Unknown change: "+refid+" - "+altid);
        
        
        
        return null;
            
    }
    
    public Variant[] queryVariants(String searchTerm){
        return new Variant[]{};
    }
    /**
     * Method to return a content array by processing the supplied queryResult parameter using the keyword as an indicator which part to use
     * @param queryResult   A String representing the html result of an ExAC query in text format
     * @param keyword   A String indicating which part of the result we are interested here
     * @return An array object containing the rows and column data of the returned exac result
     
    //public static Object[][] queryToArray(String queryResult, String keyword){
    //public Object[][] queryVariants(String queryResult, String searchTerm){
    public Object[][] queryVariantsOrg(String searchTerm){
        Object result[][] = null;
        Object hit = null;
        try {
            
            String translatedSearchTerm = translateSearchTerm(searchTerm);           
            String canonicalTranscript = getCanonicalTranscriptForGene(searchTerm);
            TabixReader tr = new TabixReader(ExtractorDefinitions.getVcfFile());
            
            String header[] = tr.readLine().split("\t");
            for(int h=0; h<header.length; h++)
                indexMap.put(header[h], h);
            
            TabixReader.Iterator it = tr.query(translatedSearchTerm);
            ArrayList entries = new ArrayList();
            
            
//            Object hit = null;
            int count = 0;        
            //int offset = 0;
        
            //counts the nr of results, also taking into account alternative reference alleles
            while( (hit = it.next()) != null){
                entries.add(hit);
                String tokens[] = hit.toString().split("\t");
                String alts[] = tokens[indexMap.get("ALT")].split(",");
                count = count+alts.length-1;                
            }
            
            result = new Object[entries.size()+count][BaseQuerier.OUTPUT_VAR_HEADERS.length];
            count = 0;
            //HashSet impacts = new HashSet();
            System.out.println("Going through: "+entries.size()+ " results");
            for(int e=0; e<entries.size(); e++)
            {                
                if(e==13)
                    e=e;
                hit = entries.get(e);
                if(gene_name==null)
                    gene_name = TEMPLATE_VALUE;
                //System.out.println("At line "+count);
                String line = hit.toString();
                String tokens[] = line.split("\t");
                
                int start = Integer.parseInt(tokens[indexMap.get("POS")]);
                
                boolean debug = start == 166930136 || start==41196369 || start==41243299;
                debug = false;
                
                if(debug)
                    System.out.println("Check");
                String orgRefid = tokens[indexMap.get("REF")];
                String alts[] = tokens[indexMap.get("ALT")].split(",");
                
                String ann = tokens[indexMap.get("ANN")];
                String anns[] = null;                
                if(ann!=null)
                    anns = ann.split(",");
                
                //info field in vcf file  //changed in custom tab
                //String infoElements[] = tokens[7].split(";");
                //convert to hashmap for easier lookup
                //HashMap infoMap = tokensToMap(infoElements);
                HashMap infoMap = tokensToMap(header, tokens);
                
                String gac[] = tokens[indexMap.get(glob_allele_cnts)].split(","); //one per alt
                String gach[] = tokens[indexMap.get(glob_hom_cnt)].split(","); //one per alt
                String gan = tokens[indexMap.get(glob_allele_nr)];
                
                for(int r=0; r<alts.length; r++)
                {   
                    int worstCons = -1;
                    //keep track of extra nr of entries due to multiple alternative alleles
                    if(r>0)
                        count++;
                    
                    int pos = start;
                    
                    String refid = orgRefid;
                    String altid = alts[r];
                    //String orgRefid = refid;
                    String orgAltid = altid;
                                        
                    if(refid.length()>altid.length()){
                        refid = refid.substring(altid.length());
                        altid="-";
                        pos++;
                    }
                    //insertion
                    else if(refid.length()<altid.length()){
                        altid=altid.substring(refid.length());
                        refid="-";
                    }
                    else if(refid.length()>1 && refid.length()==altid.length()){
                        refid = refid.substring(0,1);
                        altid = altid.substring(0,1);
                    }
                    
                    String registeredChange = getChange(refid, altid, orgRefid, orgAltid);
                    
                    int end = (pos+refid.length()-1);                    
                    
                    String pac = gac[r]; // getPopCounts(allele_count_id, infoMap, selpop, r);                   
                    String pan = gan; //getPopCounts(allele_number_id, infoMap, selpop, 0);
                    String pah = gach[r];//getPopCounts(hom_id, infoMap, selpop, r);
                    
                    //double paf = Double.parseDouble(pac)/Double.parseDouble(pan);
                    String exac_id = tokens[indexMap.get("CHROM")]+"-"+start+"-"+tokens[indexMap.get(OUTPUT_HEADER_REF)]+"-"+alts[r];
                    //String exac_id = tokens[indexMap.get("CHROM")]+"-"+start+"-"+tokens[indexMap.get(OUTPUT_HEADER_REF)]+"-"+alts[r];

                    String major_cons = "-";
                    String cons = "-";
                    //csq
                    String canonical = "";
                    
                    
                    //int hits = 0;
                    if(debug) 
                    {
                        System.out.println("Ref-alt: '"+refid+" > "+altid+"'");
                            pos = pos;
                    }
                    for(int c=0; c<anns.length; c++)
                    {                            
                        
                        String subanns[] = anns[c].split("\\|", -1);
                        String allele = subanns[0];
                        String impact = subanns[2];
                        String symbol = subanns[3];
                        
                        
                        String transcriptKeyword = subanns[5];
                        String transcript = subanns[6];
                        String exon = subanns[8];
                        //change = change.substring(change.indexOf(":")+1);     
                            
                        if(transcriptKeyword.equalsIgnoreCase("transcript"))
                        //if(alts.length==1 || codons.equals(refid+"/"+altid) || (allele.equals(altid) && registeredChange!=null && change.endsWith(registeredChange)))
                        if(allele.equals(orgAltid))
                        {
                            if(debug) System.out.println("HIT: "+anns[c]);
                            int impactLevel = getImpactLevel(impact);
                            //if(impactLevel>worstCons || (impactLevel==worstCons && "YES".equals(subcsqs[25]))){
                            if(impactLevel>worstCons || (impactLevel==worstCons && transcript.equals(canonicalTranscript))){
                            //if(impactLevel>worstCons || (impactLevel==worstCons && !"".equals(exon))){
                                
                                //canonical? Check with transcript id?
                                if(transcript.equals(canonicalTranscript))
                                    canonical = "YES";   
                                //else canonical = "NO";

                                gene_name=symbol;
                                major_cons = getWorstConsequence(subanns[1]);
                                cons = subanns[10];
                                cons = cons.substring(cons.indexOf(":")+1);
                                worstCons = impactLevel;
                                if(debug && false)
                                {                                    
                                    System.out.println("Updated consequence for ref-alt '"+refid+" - "+altid+"': "+anns[c]);
                                    System.out.println("Impactlevel: "+impactLevel+". "+major_cons+" -> "+cons);
                                }
                                
                            }
                           // }
                        }                        
                    }

                    //print chr, start, end, ref alt, searchterm
                    //exacLine.append(tokens[0]).append(del).append(pos).append(del).append(end).append(del).append(refid).append(del).append(altid).append(del);
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CHR)] = tokens[0]; 
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_START_POS)] = ""+pos;
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_END_POS)] = ""+end;
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_REF)] = refid;
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_ALT)] = altid;
                    
                    
                    //searchterm, exacid, canonical, etc.
                    //exacLine.append(del).append(region).append(del).append(exac_id).append(del).append("YES").append(del).append(tokens[2]).append(del).append(cons).append(del).append(major_cons).append(del).append(tokens[6]).append(del);
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_SEARCH_TERM)] = searchTerm;                    
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_EXAC_ID)] = exac_id;
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_GENE_NAME)] = gene_name;
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CANONICAL)] = canonical;
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_RS_ID)] = tokens[2];
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CATEGORY)] = getMajorCategory(major_cons);
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)] = cons;
                    //result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_FILTER)] = tokens[6];
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_FILTER)] = tokens[5];
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_MAJOR_CONSEQUENCE)] = major_cons;
                    
                    
                    //global counts, frequencies
                    //exacLine.append(gac[r]).append(del).append(gan).append(del).append(gach[r]).append(del).append(Double.parseDouble(gac[r])/Double.parseDouble(gan)).append(del);
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_ALLELE_COUNT_GLOBAL)] = gac[r];
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_ALLELE_NR_GLOBAL)] = gan;
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_HOM_COUNT_GLOBAL)] = gach[r];
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_FREQUENCY_GLOBAL)] = ""+Double.parseDouble(gac[r])/Double.parseDouble(gan);
                    
                    //selected population counts, frequencies
                    //exacLine.append(pac).append(del).append(pan).append(del).append(pah).append(del).append(paf).append(del);
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_SEL_POP_ALLELE_CNTS)] = pac;
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_SEL_POP_ALLELE_NRS)] = pan;
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_SEL_POP_HOM_COUNT)] = pah;
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_SEL_POP_FREQ)] = ""+Double.parseDouble(gac[r])/Double.parseDouble(gan);
                    
                    //summed stuff {x=1,y=2,z=3}
                    //exacLine.append(getBasedOnPops(allele_count_id, infoMap, r)).append(del).append(getBasedOnPops(allele_number_id, infoMap, 0)).append(del).append(getBasedOnPops(hom_id, infoMap, r));
                    
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_POP_ALLELE_CNTS)] = getBasedOnPops(allele_count_id, infoMap, r);
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_POP_ALLELE_NRS)] = getBasedOnPops(allele_number_id, infoMap, 0);
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_POP_HOMS)] = getBasedOnPops(hom_id, infoMap, r);                  
                }                
            }
            //System.out.println("impacts: "+impacts);
        } catch (IOException ex) {
            System.out.println("Error at: "+hit);
            Logger.getLogger(VCFAnnQuerier.class.getName()).log(Level.SEVERE, null, ex);
        }
        //tr.close();
        return result;
    }    
    */
    public static int getIndex(String value, String[] tokens){
        for(int i=0; i<tokens.length; i++)
            if(value.equals(tokens[i]))
                return i;                       
        return -1;        
    }
    
    public String getWorstConsequence(String consequence){
        String result = consequence;
        if(consequence.contains("&")){
            int worstValue = -1;
            String tokens[] = consequence.split("&");
            for(int t=0; t<tokens.length; t++){
                int cv = getConsequenceValue(tokens[t]);
                if(cv>worstValue)
                {
                    worstValue = cv;
                    result = tokens[t];
                }                
            }
        }
        return result;
    }
    public int getImpactLevel(String impact){
        int result = -1;
        if("MODIFIER".equals(impact))
            result = 0;
        else if("LOW".equals(impact))
            result = 1;
        else if("MODERATE".equals(impact))
            result = 2;
        else if("HIGH".equals(impact))
            result = 3;
        
        return result;
    }
          
     
    public static int getPopCounts(String value_id, HashMap valueMap, HashSet selectedPops, int index){
        int count = 0;
        for(int p=0; p<pops_full.length; p++){
            if(selectedPops==null || selectedPops.contains(pops_full[p]))
            {
                String value = (String)valueMap.get(value_id+pops_id[p]);
                if(value!=null){
                    String values[] = value.split(",");
                    count+=Integer.parseInt(values[index]);                    
                }                
            }
        }
        
        return count;
    }
    
    public static pop_base getBasedOnPops(String value_id, HashMap valueMap, int index){
        pop_base result = new pop_base();
         for(int p=0; p<pops_full.length; p++)
        {
            //String pop = pops_full[p];
            String value = (String)valueMap.get(value_id+pops_id[p]);            
            if(value!=null){
                String values[] = value.split(",");
                value = values[index];
            }
            else value = "0";
            result.setValueForPop(pops_full[p], Integer.parseInt(value));
            
        }                
        return result;
        
        /*
        HashMap result = new HashMap();
        
        //StringBuilder sb = new StringBuilder("{");
        for(int p=0; p<pops_full.length; p++)
        {
            String pop = pops_full[p];
            String value = (String)valueMap.get(value_id+pops_id[p]);            
            if(value!=null){
                String values[] = value.split(",");
                value = values[index];
            }
            else value = "0";
            result.put(pop, Integer.parseInt(value));
        }        
        
        return result;
        */
    }
    
    public static HashMap getHashMapBasedOnPops(String value_id, HashMap valueMap, int index){
        HashMap result = new HashMap();
        
        //StringBuilder sb = new StringBuilder("{");
        for(int p=0; p<pops_full.length; p++)
        {
            String pop = pops_full[p];
            String value = (String)valueMap.get(value_id+pops_id[p]);            
            if(value!=null){
                String values[] = value.split(",");
                value = values[index];
            }
            else value = "0";
            result.put(pop, Integer.parseInt(value));
        }        
        
        return result;
    }
    
    
    public static String getBasedOnPopsAsString(String value_id, HashMap valueMap, int index)
    {
        return new pop_base().toString();
        /*
        StringBuilder sb = new StringBuilder("{");
        for(int p=0; p<pops_full.length; p++){
            sb.append(pops_full[p]).append("=");
            String value = (String)valueMap.get(value_id+pops_id[p]);
            if(value!=null){
                String values[] = value.split(",");
                sb.append(values[index]);
            }
            else sb.append("NA");
            if(p<pops_full.length-1)
                sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    */
    }
    
    
    //public static HashMap tokensToMap(String tokens[]){
    public static HashMap tokensToMap(String header[], String tokens[]){
        HashMap result = new HashMap();
        for(int t=0; t<tokens.length; t++)
            result.put(header[t], tokens[t]);
        return result;
        
    }

    @Override
    public String getJsonPart(String queryResult, String keyword) {
        return queryResult;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

