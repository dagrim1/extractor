    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler;

import java.util.*;
import exactool.ExtractorDefinitions;
import exactool.datahandler.dataobjects.Variant;
import htsjdk.tribble.readers.TabixReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * Uses Quick-Json: https://code.google.com/archive/p/quick-json/wikis/Usage.wiki
 * @author flip
 */
public class VCFAnnQuerier extends FileQuerier {    
    
    private HashMap<String, Integer> indexMap = new HashMap();
    public VCFAnnQuerier(){        
    }
    
    /**
     * Method to return a content array by processing the supplied queryResult parameter using the keyword as an indicator which part to use
     * @param queryResult   A String representing the html result of an ExAC query in text format
     * @param keyword   A String indicating which part of the result we are interested here
     * @return An array object containing the rows and column data of the returned exac result
    */
    public Variant[] queryVariants(String searchTerm){
        Variant result[] = null;
        Object hit = null;
        try {
                
            System.out.println("CHECK VCFAnnQuerier.queryVariants implementation!!!");
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
            
            //result = new Object[entries.size()+count][BaseQuerier.OUTPUT_VAR_HEADERS.length];
            result = new Variant[entries.size()+count];
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
                debug = start == 55516888;
                debug = start == 55516978;
                debug = start==55527041;
                //disable debugging
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
                            String proteineChange = subanns[10];
                            
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
                                cons = cons.substring(cons.indexOf(":")+1); //needed in original vcf but no influence in recalled file
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
                    
                    result[e+count] = new Variant();
                    result[e+count].chrom = tokens[0];
                    result[e+count].start_pos = pos;
                    result[e+count].end_pos =end;
                    result[e+count].ref = refid;
                    result[e+count].alt = altid;
                            
                    result[e+count].search_term = searchTerm;
                    result[e+count].exac_id = exac_id;
                    result[e+count].gene = gene_name;
                    result[e+count].canonical = canonical;
                    result[e+count].rsid = tokens[2];
                    result[e+count].category = getMajorCategory(major_cons);
                    result[e+count].consequence = cons;
                    result[e+count].filter = tokens[5];
                    result[e+count].major_consequence = major_cons;
                    
                    result[e+count].allele_count = Integer.parseInt(gac[r]);
                    result[e+count].allele_num = Integer.parseInt(gan);
                    result[e+count].hom_count = Integer.parseInt(gach[r]);
                    result[e+count].allele_freq = Double.parseDouble(gac[r])/Double.parseDouble(gan);
                    
                    result[e+count].sel_pop_acs = Integer.parseInt(pac);
                    result[e+count].sel_pop_ans = Integer.parseInt(pan);
                    result[e+count].sel_pop_homs = Integer.parseInt(pah);
                    result[e+count].sel_pop_freq =  Double.parseDouble(gac[r])/Double.parseDouble(gan); //?????
                      
                    //System.out.println("TO IMPLEMENT IN VcfAnnQuerier.GETVARIANTS");
                    
                    
                    //summed stuff {x=1,y=2,z=3}
                    //exacLine.append(getBasedOnPops(allele_count_id, infoMap, r)).append(del).append(getBasedOnPops(allele_number_id, infoMap, 0)).append(del).append(getBasedOnPops(hom_id, infoMap, r));
                    result[e+count].pop_acs = getBasedOnPops(allele_count_id, infoMap, r);
                    result[e+count].pop_ans = getBasedOnPops(allele_number_id, infoMap, 0);
                    result[e+count].pop_homs = getBasedOnPops(hom_id, infoMap, r);                 
                    
                    //result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_POP_ALLELE_CNTS)] = getBasedOnPops(allele_count_id, infoMap, r);
                    //result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_POP_ALLELE_NRS)] = getBasedOnPops(allele_number_id, infoMap, 0);
                    //result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_POP_HOMS)] = getBasedOnPops(hom_id, infoMap, r);                  
                    
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
}

