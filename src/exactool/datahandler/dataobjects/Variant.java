/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 *
 * @author flip
 */
public class Variant implements Comparable {
    
    //json entries
    @JsonProperty("CANONICAL")    
    public String canonical;    
    @JsonProperty("HGVS")    
    public String consequence; //consequence
    @JsonProperty("HGVSp")
    public String hgsvp;
    @JsonProperty("variant_id")
    public String exac_id;
    //when querying specific variant(s)
    @JsonProperty("SYMBOL")    
    public String symbol;        
        
    public int pos;
    public String ref;
    public String rsid;
    public String alt;
    public String category;
    public String filter;
    public String major_consequence;
    public String chrom;
    public double allele_freq; //scientific?
    public String dataset = "ExAC";
    
    public String indel;
    //pop_allele_cnts, pop_allele_nr, pop_homs
    public int allele_count;
    public int allele_num;
    public int hom_count;
    
    public pop_base pop_acs;
    public pop_base pop_homs;
    public pop_base pop_ans;
    
    //custom entries
    public int sel_pop_acs;
    public int sel_pop_homs;
    public int sel_pop_ans;
    public double sel_pop_freq;
    
    public String search_term;
    public String gene;
    public int start_pos;
    public int end_pos;
        
    //internal variables
    public final int EXAC = 1;
    public final int GNOMAD = 2;
    public int MODE = EXAC;
        
    public boolean include = true;
    
    public boolean isExac(){
        return "exac".equalsIgnoreCase(dataset);
    }
    
    public boolean isGnomad(){
        return "gnomad".equalsIgnoreCase(dataset);
    }
    
    /*
    public Object[] toRow(){
        return new Object[]{chrom, start_pos, end_pos, ref, alt, search_term, gene, exac_id, canonical, rsid, category, filter, consequence, major_consequence, indel,
                                allele_count, allele_num, hom_count, allele_freq, sel_pop_acs, sel_pop_ans, sel_pop_homs, sel_pop_freq, pop_acs, pop_ans, pop_homs, 
                                dataset};
    }
*/
    
    public int getAlleleCountForPop(String pop){
        return pop_acs.getValueForPop(pop);
    }
    
    public int getAlleleNumberForPop(String pop){
        return pop_ans.getValueForPop(pop);
    }
    
    public double getFrequencyForPop(String pop){
        return (double)getAlleleCountForPop(pop)/(double)getAlleleNumberForPop(pop);
    }
    
    public ArrayList getPops(){
        if(pop_acs == null)
            return new ArrayList();
        return pop_acs.getPops();
    }    

    @Override
    public int compareTo(Object t) {
        return this.start_pos - ((Variant)t).start_pos;
        
    }
}
