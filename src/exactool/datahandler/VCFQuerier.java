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
public class VCFQuerier extends FileQuerier {    
    public static String annotations = "CSQ";
    
    public VCFQuerier(){        
    }       
     
    
    /**
     * Method to return a content array by processing the supplied queryResult parameter using the keyword as an indicator which part to use
     * @param queryResult   A String representing the html result of an ExAC query in text format
     * @param keyword   A String indicating which part of the result we are interested here
     * @return An array object containing the rows and column data of the returned exac result
     */
    public Variant[] queryVariants(String searchTerm){
        Variant result[] = null;
        try {
            
            String translatedSearchTerm = translateSearchTerm(searchTerm);           
            TabixReader tr = new TabixReader(ExtractorDefinitions.getVcfFile());
            TabixReader.Iterator it = tr.query(translatedSearchTerm);
            ArrayList entries = new ArrayList();
            
            Object hit = null;
            int count = 0;        
            //int offset = 0;
        
            //counts the nr of results, also taking into account alternative reference alleles
            while( (hit = it.next()) != null){
                entries.add(hit);
                String tokens[] = hit.toString().split("\t");
                String alts[] = tokens[4].split(",");
                count = count+alts.length-1;                
            }
            
            //result = new Object[entries.size()+count][BaseQuerier.OUTPUT_VAR_HEADERS.length];
            result = new Variant[entries.size()+count];
            count = 0;
            //HashSet impacts = new HashSet();
            for(int e=0; e<entries.size(); e++)
            {                
                hit = entries.get(e);
                if(gene_name==null)
                    gene_name = TEMPLATE_VALUE;
                //System.out.println("At line "+count);
                String line = hit.toString();
                String tokens[] = line.split("\t");
                
                int start = Integer.parseInt(tokens[1]);
                boolean debug = (start==55516888 || start==55518281 || start==55518282 || start==55517940 || start==55517941 || start==55524340);
                debug = (start == 55524340 || start == 55521900  || start == 55521899);
                debug = start==41244145;
                
                if(debug)
                    System.out.println("Check");
                String orgRefid = tokens[3];
                String alts[] = tokens[4].split(",");
                
                //info field in vcf file
                String infoElements[] = tokens[7].split(";");
                //convert to hashmap for easier lookup
                HashMap infoMap = tokensToMap(null, infoElements);
                
                String gac[] = infoMap.get(glob_allele_cnts).toString().split(",");
                String gach[] = infoMap.get(glob_hom_cnt).toString().split(",");
                String gan= infoMap.get(glob_allele_nr).toString();
                
                //get consequence entry in INFO field
                String csq = (String)infoMap.get(annotations);
                //which can in turn contain multiple entries (per alternative allele)
                String csqs[] = null;                
                if(csq!=null)
                    csqs = csq.split(",");
                
                //if(alts.length>1)
                //    System.out.println("Multiple alts");
                
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
                    String exac_id = tokens[0]+"-"+start+"-"+tokens[3]+"-"+alts[r];

                    String major_cons = "-";
                    String cons = "-";
                    //csq
                    String canonical = "NO";
                    
                    //int hits = 0;
                    if(debug) 
                    {
                        System.out.println("Ref-alt: '"+refid+" > "+altid+"'");
                            pos = pos;
                    }
                    for(int c=0; c<csqs.length; c++)
                    {                            
                        
                        String subcsqs[] = csqs[c].split("\\|");
                        String allele = subcsqs[0];
                        String codons = subcsqs[16];
                        String impact = subcsqs[2];
                        String symbol = subcsqs[3];
                        
                        String change = subcsqs[10];
                        change = change.substring(change.indexOf(":")+1);     
                            
                        if(alts.length==1 || codons.equals(refid+"/"+altid) || (allele.equals(altid) && registeredChange!=null && change.endsWith(registeredChange)))
                        {
                            if(debug) System.out.println("HIT: "+csqs[c]);
                            int impactLevel = getImpactLevel(impact);
                            if(impactLevel>worstCons || (impactLevel==worstCons && "YES".equals(subcsqs[25]))){
                                
                                if("YES".equals(subcsqs[25])) //YES?
                                    canonical = "YES";   
                                else canonical = "NO";

                                gene_name=symbol;
                                major_cons = getWorstConsequence(subcsqs[1]);
                                //cons = subcsqs[11].substring(subcsqs[11].indexOf(":")+1);                                                                
                                cons = subcsqs[11];
                                cons = cons.substring(cons.indexOf(":")+1);
                                worstCons = impactLevel;
                                if(debug && false)
                                {                                    
                                    System.out.println("Updated consequence for ref-alt '"+refid+" - "+altid+"': "+csqs[c]);
                                    System.out.println("Impactlevel: "+impactLevel+". "+major_cons+" -> "+cons);
                                }
                                
                            }
                           // }
                        }                        
                    }

                    result[e+count] = new Variant();
                    //print chr, start, end, ref alt, searchterm
                    //exacLine.append(tokens[0]).append(del).append(pos).append(del).append(end).append(del).append(refid).append(del).append(altid).append(del);
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
                    result[e+count].filter = tokens[6];
                    result[e+count].major_consequence = major_cons;
                    
                    result[e+count].allele_count = Integer.parseInt(gac[r]);
                    result[e+count].allele_num = Integer.parseInt(gan);
                    result[e+count].hom_count = Integer.parseInt(gach[r]);
                    result[e+count].allele_freq = Double.parseDouble(gac[r])/Double.parseDouble(gan);
                    
                    result[e+count].sel_pop_acs = Integer.parseInt(pac);
                    result[e+count].sel_pop_ans = Integer.parseInt(pan);
                    result[e+count].sel_pop_homs = Integer.parseInt(pah);
                    result[e+count].sel_pop_freq =  Double.parseDouble(gac[r])/Double.parseDouble(gan); //?????
                    
                    /*  
                    System.out.println("TO IMPLEMENT IN VCFQUERIER.GETVARIANTS");
                    
                    result[e+count].pop_acs = getBasedOnPops(allele_count_id, infoMap, r);
                    result[e+count].pop_ans = getBasedOnPops(allele_number_id, infoMap, 0);
                    result[e+count].pop_homs = getBasedOnPops(hom_id, infoMap, r);                  
                    */
                    /*
                    //summed stuff {x=1,y=2,z=3}
                    //exacLine.append(getBasedOnPops(allele_count_id, infoMap, r)).append(del).append(getBasedOnPops(allele_number_id, infoMap, 0)).append(del).append(getBasedOnPops(hom_id, infoMap, r));
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_POP_ALLELE_CNTS)] = getBasedOnPops(allele_count_id, infoMap, r);
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_POP_ALLELE_NRS)] = getBasedOnPops(allele_number_id, infoMap, 0);
                    result[e+count][BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_POP_HOMS)] = getBasedOnPops(hom_id, infoMap, r);                  
                    */
                }                
            }
            //System.out.println("impacts: "+impacts);
        } catch (IOException ex) {
            Logger.getLogger(VCFQuerier.class.getName()).log(Level.SEVERE, null, ex);
        }
        //tr.close();
        return result;
    }    
      
    public static HashMap tokensToMap(String header[], String tokens[]){
        HashMap result = new HashMap();
        for(int t=0; t<tokens.length; t++){
            if(tokens[t].indexOf("=")>-1){
                String keyVal[] = tokens[t].split("=");
                result.put(keyVal[0], keyVal[1]);
            }
        }
        return result;
    }
}

