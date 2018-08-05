/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler.dataobjects;

import exactool.datahandler.BaseQuerier;
import exactool.datahandler.ExacQuerier;
import exactool.ui.ExacToolFrame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author flip
 */
public class SearchData implements Comparable{
 
    public String id = null;    
    public gene[] genes = null;
    //public Variant[] variants; //all variants
    public Feature[] features;
    public HashMap<String, Double> regionCovMap = new HashMap();
    String anyAcid = "(\\D+)"; //any number of non digits    
    
    public double avgExomeCoverage = 0.0;
    public double avgGenomeCoverage = 0.0;
    public String summary = "SUMMARY - IMPLEMENT";
    public HashMap<Integer, double[]> bpCovMap = new HashMap();
    
    public HashMap<String, CombinedVariant> variantMap = new HashMap();
    public SearchData(String searchTerm){        
        id = searchTerm;
    }
     @Override
    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setCombinedVariants(Variant[] variants){
        variantMap.clear();                 
        //make combined
        for(int i=0; i<variants.length; i++)
        {
            CombinedVariant stored = variantMap.get(variants[i].exac_id);
            if(stored==null)
                   stored = new CombinedVariant();
            if(variants[i].isExac())
                stored.exacData = variants[i];
            else if(variants[i].isGnomad())
                stored.gnomadData = variants[i];

            variantMap.put(variants[i].exac_id, stored);
        }
    }
    
    public String getGeneNames(){
        String result = "";
        for(int g=0; g<genes.length; g++){            
            result = result + genes[g];
            if(g<genes.length-1)
                result=result+";";
        }
        return result;
    }
    
    public void updateGeneInfo(){
        Iterator terms = variantMap.keySet().iterator();
        if(genes!=null && genes.length>0){
            while(terms.hasNext()){
                CombinedVariant var = variantMap.get(terms.next());
                if(var.exacData!=null)
                    var.exacData.gene = getGenesForRegion(var.exacData.start_pos, var.exacData.end_pos);
                if(var.gnomadData!=null)
                    var.gnomadData.gene = getGenesForRegion(var.gnomadData.start_pos, var.gnomadData.end_pos);
            }
        
            for(int i=0; i<features.length; i++)
                features[i].gene = getGenesForRegion(features[i].start, features[i].stop);
        }
    }
    
    /*
    public void updateGeneInfo(){
        if(genes!=null && genes.length>0){
            for(int i=0; i<variants.length; i++)
                variants[i].gene = getGenesForRegion(variants[i].start_pos, variants[i].end_pos);
            for(int i=0; i<features.length; i++)
                features[i].gene = getGenesForRegion(features[i].start, features[i].stop);
        }
    }
    */
    
    public String getGenesForRegion(int start, int end){
        String result = "";
        for(int i=0; i<genes.length; i++){
            if((start>=genes[i].start && start<=genes[i].stop) || (end>=genes[i].start && end<=genes[i].stop) || (end>=genes[i].stop && start<=genes[i].start))
                result = result+genes[i].gene_name+";";
        }
        if(result.endsWith(";"))
            result = result.substring(0, result.length()-1);
        return result;
    }
    
    public int getRowCount(){
        return getRowCount(false);
    }

    /*
    public int getRowCount(boolean unfiltered){                
        HashSet uniqueIds = new HashSet();
        for(int i=0; i<variants.length; i++)            
        if(unfiltered || match(variants[i]))
            uniqueIds.add(variants[i].exac_id);   
        return uniqueIds.size();   
    }
    */
    public int getRowCount(boolean unfiltered){                
        int result = 0;
        Iterator terms = variantMap.keySet().iterator();
        
        while(terms.hasNext()){
            CombinedVariant var = variantMap.get(terms.next());
            if(unfiltered || matchCombinedVariant(var))
                result++;
        }
        return result;                
    }

    public Object[][] getDisplayedData(){    
        return getDisplayedData(false);
    }
    
    public void setVariants(Variant[] variants){
        
        
    }
    
    public HashSet getMajorCons(){
        HashSet result = new HashSet();
        Iterator terms = variantMap.keySet().iterator();
        
        while(terms.hasNext()){
            CombinedVariant var = variantMap.get(terms.next());
            result.addAll(var.getMajorCons());
        }
        return result;
    }
             
    public HashSet getPops(){
        HashSet result = new HashSet();
        Iterator terms = variantMap.keySet().iterator();
        
        while(terms.hasNext()){
            CombinedVariant var = variantMap.get(terms.next());
            result.addAll(var.getPops());
        }
        return result;        
    }
    
    public Object[][] getDisplayedData(boolean unfiltered){    
        ArrayList ids = new ArrayList(variantMap.keySet());        
        for(int i=ids.size()-1; i>=0; i--){
             if(!unfiltered && !matchCombinedVariant(variantMap.get((String)ids.get(i))))
             {
                 ids.remove(ids.get(i));
             }
             
            //table[i] = displayed.get(ids.get(i)).toRow();
        } 
        Object table[][] = new Object[ids.size()][BaseQuerier.getVarHeaders().length];
        if(CombinedVariant.perPop)
            table = new Object[ids.size()][BaseQuerier.getVarHeaders().length+32];
        for(int i=0; i<ids.size(); i++){
            table[i] = variantMap.get(ids.get(i)).toRow();
        }        
        return table;
        
        /*
        
        HashMap<String, CombinedVariant> displayed = new HashMap();       
        //make combined
        for(int i=0; i<variants.length; i++)
        {
            CombinedVariant stored = displayed.get(variants[i].exac_id);
            if(stored==null)
                   stored = new CombinedVariant();
            if(variants[i].isExac())
                stored.exacData = variants[i];
            else if(variants[i].isGnomad())
                stored.gnomadData = variants[i];

            displayed.put(variants[i].exac_id, stored);
        }
        
        
        ArrayList ids = new ArrayList(displayed.keySet());        
        for(int i=ids.size()-1; i>=0; i--){
             if(!unfiltered && !matchCombinedVariant(displayed.get((String)ids.get(i))));
             {
                 ids.remove(ids.get(i));
             }
             
            //table[i] = displayed.get(ids.get(i)).toRow();
        } 
        Object table[][] = new Object[ids.size()][BaseQuerier.getVarHeaders().length];
        for(int i=0; i<ids.size(); i++){
            table[i] = displayed.get(ids.get(i)).toRow();
        }        
        return table;
        */
        /*
        
        for(int i=0; i<variants.length; i++)
        {
            if(unfiltered || match(variants[i]))
            {
                CombinedVariant stored = displayed.get(variants[i].exac_id);
                if(stored==null)
                    stored = new CombinedVariant();
                if(variants[i].isExac())
                    stored.exacData = variants[i];
                else if(variants[i].isGnomad())
                    stored.gnomadData = variants[i];
                
                displayed.put(variants[i].exac_id, stored);
            }
        }
        ArrayList ids = new ArrayList(displayed.keySet());
        Collections.sort(ids);
        
        Object table[][] = new Object[displayed.size()][BaseQuerier.getVarHeaders().length];
        for(int i=0; i<ids.size(); i++){
            table[i] = displayed.get(ids.get(i)).toRow();
        }        
        return table;   
        */
       
    } 
    /*
    public CombinedVariant[] getDisplayedVariants(boolean unfiltered){
        HashMap<String, CombinedVariant> displayed = new HashMap();        
        for(int i=0; i<variants.length; i++)
        {
            if(unfiltered || match(variants[i]))
            {
                CombinedVariant stored = displayed.get(variants[i].exac_id);
                if(stored==null)
                    stored = new CombinedVariant();
                if(variants[i].isExac())
                    stored.exacData = variants[i];
                else if(variants[i].isGnomad())
                    stored.gnomadData = variants[i];
                
                displayed.put(variants[i].exac_id, stored);
            }
        }
        ArrayList ids = new ArrayList(displayed.keySet());
        Collections.sort(ids);
        
        CombinedVariant result[] = new CombinedVariant[displayed.size()];
        for(int i=0; i<ids.size(); i++){
            result[i] = displayed.get(ids.get(i));
        }
        return result;   
    }
    */
    
    
    /**
     * Checks if a row matches the filtering parameters set by the user
     * @param data  The row to be checked  
     * @return A boolean indicating a match (true is match, false is no match)
     */    
    //public boolean match(Object[] data)
    public boolean matchSingleVariantOrg(Variant variant)
    {
        boolean result = false;       
        
        boolean m1a = ExacToolFrame.INCL_EXOME && variant.isExac();
        boolean m1b = ExacToolFrame.INCL_GENOME && variant.isGnomad();
        
        //boolean m2 = !ExacToolFrame.CONSEQUENCE_ONLY || !"".equals(variant.major_consequence);//data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)].toString());
        boolean m2 = !ExacToolFrame.CONSEQUENCE_ONLY || !"".equals(variant.consequence);//data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)].toString());
        boolean m3 = !ExacToolFrame.CANONICAL_ONLY || "YES".equals(variant.canonical);
        boolean m4 = !ExacToolFrame.PASS || ExacQuerier.EXAC_PASS_VALUE.equals(variant.filter);
        boolean m5 = !ExacToolFrame.NORS || !variant.rsid.contains(ExacQuerier.EXAC_RS_VALUE);
        boolean m6 = !ExacToolFrame.UNIQUES || 1==variant.allele_count;
        boolean m7 = ExacToolFrame.selectedAnnotations.contains(variant.major_consequence);
        boolean m8 = ExacToolFrame.LOF_FILTER==ExacToolFrame.ALL                             
                        || (ExacToolFrame.LOF_FILTER==ExacToolFrame.LOF && ExacQuerier.EXAC_LOF_TYPE_VALUE.equals(variant.category))
                        || (ExacToolFrame.LOF_FILTER==ExacToolFrame.MISSENSE_LOF && (ExacQuerier.EXAC_LOF_TYPE_VALUE.equals(variant.category) || ExacQuerier.EXAC_MISSENSE_TYPE_VALUE.equals(variant.category)));
        
        boolean m9  =  ExacToolFrame.INCL_INDEL || (!ExacToolFrame.INCL_INDEL && "false".equals(variant.indel));
        boolean m = (m1a || m1b) && m2 && m3 && m4 && m5 && m6 && m7 && m8 && m9;
        /*
        boolean m10 = ExacToolFrame.filteredIds.size()==0 || ExacToolFrame.filteredIds.contains(variant.exac_id);
        boolean m = (m1a || m1b) && m2 && m3 && m4 && m5 && m6 && m7 && m8 && m9 && m10;
        */
        
        if(m && ExacToolFrame.REGIONS_ONLY)
        {            
            ArrayList covEntries = new ArrayList(regionCovMap.keySet());
            //if size is 0, continue to next step.
            m = covEntries.isEmpty();
            
            for(int c=0; c<covEntries.size(); c++)
            {
                String region = (String)covEntries.get(c);
                //System.out.println(" to "+region);
                String pattern = region;
                
                if(region.startsWith("p.")){
                    //System.out.print("Region: "+region);
                    boolean anyRef = region.charAt(2) == '*';
                    int offset = 0;
                    char lastChar = region.charAt(region.length()-1);
                    boolean anyChange = lastChar == '*';
                    if(anyChange) 
                        offset = 1;
                    else anyChange = ('0' <= lastChar && lastChar <= '9');
                    
                    if(anyRef && anyChange)
                        pattern = "p."+anyAcid+region.substring(3, region.length()-offset)+anyAcid;
                    else if(anyRef)
                        pattern = "p."+anyAcid+region.substring(3, region.length());
                    else if(anyChange)
                        pattern = region.substring(0, region.length()-offset)+anyAcid;                    
                                        
                    if(!"".equals(variant.consequence))
                        variant.consequence = variant.consequence;
                    m = m || variant.consequence.matches(pattern);                             
                }                                
                //if(regionCovMap.get(region) == -99.0){m = m || data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)].toString().matches(region+anyAcid);}                
                else{
                    int index = region.indexOf("-");
                    if(index>-1)
                    {
                        int pos1 = Integer.parseInt(region.substring(region.indexOf(":")+1, index));
                        int pos2 = Integer.parseInt(region.substring(index+1));
                        int startpos = variant.pos;
                        int endpos = variant.pos;
                        //int startpos = Integer.parseInt((String)data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_START_POS)]);
                        //int endpos = Integer.parseInt((String)data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_END_POS)]);
                        m = m || (startpos>=pos1 && startpos<=pos2) || (endpos>=pos1 && endpos<=pos2);                    
                        //regionMatch = regionMatch || cons.matches(region); 
                    }
                }
            }
        }
        
        
        if(m)
        {
            boolean popMatch = true, specificPopMatch = false;
            ArrayList populations = new ArrayList(ExacToolFrame.selectedPopulations);                            

            int totalAlleleCnt = 0;
            int totalAlleleNr = 0;
            int totalHomCnt = 0;

            //no specific populations selected, simply use all...
            if(populations.isEmpty())
            {
                totalAlleleCnt = variant.allele_count;
                totalAlleleNr = variant.allele_num;
                totalHomCnt = variant.hom_count;
                
            }
            else
            {                               
                popMatch = ExacToolFrame.ALL_POPS;
                for(int p=0; p<populations.size(); p++)
                {
                    Object pop = populations.get(p);
                    //Integer alleleCount = (Integer)popAcs.get(pop);                    
                    Integer alleleCount = variant.pop_acs.getValueForPop(pop.toString());
                    if(alleleCount==null) //no population info stored?
                        alleleCount  = 0;

                    totalAlleleCnt = totalAlleleCnt + alleleCount;

                    specificPopMatch = alleleCount>0;
                    if(ExacToolFrame.ALL_POPS)
                        popMatch = popMatch && specificPopMatch;                    
                    else popMatch = popMatch || specificPopMatch;


                    if(specificPopMatch || !ExacToolFrame.ALL_POPS)  
                    {
                        totalAlleleNr = totalAlleleNr += variant.pop_ans.getValueForPop(pop.toString());
                        totalHomCnt = totalHomCnt += variant.pop_homs.getValueForPop(pop.toString());
                        
                    }
                }
            }

            if(popMatch)
            {
                variant.sel_pop_acs = totalAlleleCnt;
                variant.sel_pop_ans = totalAlleleNr;
                variant.sel_pop_homs = totalHomCnt;
                double freq = (double)totalAlleleCnt/(double)totalAlleleNr;
                if(ExacToolFrame.LOCAL_MODE){
                    variant.sel_pop_freq = freq;                    
                }
                else{
                    //data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_SEL_POP_FREQ)] = freq; //update with selected populations
                    variant.sel_pop_freq = freq;
                }
                result = true;                                
            }                         
        }
        
        return result;        
    }
    
    public boolean matchSingleVariant(Variant variant)
    {
        boolean result = false;       
        
        if(variant == null)
            return false;
        
        
        boolean m1a = ExacToolFrame.INCL_EXOME && variant.isExac();
        boolean m1b = ExacToolFrame.INCL_GENOME && variant.isGnomad();
        variant.include = (m1a || m1b);
        
        //boolean m2 = !ExacToolFrame.CONSEQUENCE_ONLY || !"".equals(variant.major_consequence);//data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)].toString());
        boolean m2 = !ExacToolFrame.CONSEQUENCE_ONLY || !"".equals(variant.consequence);//data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)].toString());
        boolean m3 = !ExacToolFrame.CANONICAL_ONLY || "YES".equals(variant.canonical);
        boolean m4 = !ExacToolFrame.PASS || ExacQuerier.EXAC_PASS_VALUE.equals(variant.filter);
        boolean m5 = !ExacToolFrame.NORS || !variant.rsid.contains(ExacQuerier.EXAC_RS_VALUE);
        boolean m6 = !ExacToolFrame.UNIQUES || 1==variant.allele_count;
        boolean m7 = ExacToolFrame.selectedAnnotations.contains(variant.major_consequence);
        boolean m8 = ExacToolFrame.LOF_FILTER==ExacToolFrame.ALL                             
                        || (ExacToolFrame.LOF_FILTER==ExacToolFrame.LOF && ExacQuerier.EXAC_LOF_TYPE_VALUE.equals(variant.category))
                        || (ExacToolFrame.LOF_FILTER==ExacToolFrame.MISSENSE_LOF && (ExacQuerier.EXAC_LOF_TYPE_VALUE.equals(variant.category) || ExacQuerier.EXAC_MISSENSE_TYPE_VALUE.equals(variant.category)));
        
        boolean m9  =  ExacToolFrame.INCL_INDEL || (!ExacToolFrame.INCL_INDEL && "false".equals(variant.indel));
        boolean m = (m1a || m1b) && m2 && m3 && m4 && m5 && m6 && m7 && m8 && m9;
        /*
        boolean m10 = ExacToolFrame.filteredIds.size()==0 || ExacToolFrame.filteredIds.contains(variant.exac_id);
        boolean m = (m1a || m1b) && m2 && m3 && m4 && m5 && m6 && m7 && m8 && m9 && m10;
        */
        
        if(m && ExacToolFrame.REGIONS_ONLY)
        {            
            ArrayList covEntries = new ArrayList(regionCovMap.keySet());
            //if size is 0, continue to next step.
            m = covEntries.isEmpty();
            
            for(int c=0; c<covEntries.size(); c++)
            {
                String region = (String)covEntries.get(c);
                //System.out.println(" to "+region);
                String pattern = region;
                
                if(region.startsWith("p.")){
                    //System.out.print("Region: "+region);
                    boolean anyRef = region.charAt(2) == '*';
                    int offset = 0;
                    char lastChar = region.charAt(region.length()-1);
                    boolean anyChange = lastChar == '*';
                    if(anyChange) 
                        offset = 1;
                    else anyChange = ('0' <= lastChar && lastChar <= '9');
                    
                    if(anyRef && anyChange)
                        pattern = "p."+anyAcid+region.substring(3, region.length()-offset)+anyAcid;
                    else if(anyRef)
                        pattern = "p."+anyAcid+region.substring(3, region.length());
                    else if(anyChange)
                        pattern = region.substring(0, region.length()-offset)+anyAcid;                    
                                        
                    if(!"".equals(variant.consequence))
                        variant.consequence = variant.consequence;
                    m = m || variant.consequence.matches(pattern);                             
                }                                
                //if(regionCovMap.get(region) == -99.0){m = m || data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)].toString().matches(region+anyAcid);}                
                else{
                    int index = region.indexOf("-");
                    if(index>-1)
                    {
                        int pos1 = Integer.parseInt(region.substring(region.indexOf(":")+1, index));
                        int pos2 = Integer.parseInt(region.substring(index+1));
                        int startpos = variant.pos;
                        int endpos = variant.pos;
                        //int startpos = Integer.parseInt((String)data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_START_POS)]);
                        //int endpos = Integer.parseInt((String)data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_END_POS)]);
                        m = m || (startpos>=pos1 && startpos<=pos2) || (endpos>=pos1 && endpos<=pos2);                    
                        //regionMatch = regionMatch || cons.matches(region); 
                    }
                }
            }
        }
        
        
        if(m)
        {
            boolean popMatch = true, specificPopMatch = false;
            ArrayList populations = new ArrayList(ExacToolFrame.selectedPopulations);                            

            int totalAlleleCnt = 0;
            int totalAlleleNr = 0;
            int totalHomCnt = 0;

            //no specific populations selected, simply use all...
            if(populations.isEmpty())
            {
                totalAlleleCnt = variant.allele_count;
                totalAlleleNr = variant.allele_num;
                totalHomCnt = variant.hom_count;
                
            }
            else
            {                               
                popMatch = ExacToolFrame.ALL_POPS;
                for(int p=0; p<populations.size(); p++)
                {
                    Object pop = populations.get(p);
                    //Integer alleleCount = (Integer)popAcs.get(pop);                    
                    Integer alleleCount = variant.pop_acs.getValueForPop(pop.toString());
                    if(alleleCount==null) //no population info stored?
                        alleleCount  = 0;

                    totalAlleleCnt = totalAlleleCnt + alleleCount;

                    specificPopMatch = alleleCount>0;
                    if(ExacToolFrame.ALL_POPS)
                        popMatch = popMatch && specificPopMatch;                    
                    else popMatch = popMatch || specificPopMatch;


                    if(specificPopMatch || !ExacToolFrame.ALL_POPS)  
                    {
                        totalAlleleNr = totalAlleleNr += variant.pop_ans.getValueForPop(pop.toString());
                        totalHomCnt = totalHomCnt += variant.pop_homs.getValueForPop(pop.toString());
                        
                    }
                }
            }

            if(popMatch)
            {
                variant.sel_pop_acs = totalAlleleCnt;
                variant.sel_pop_ans = totalAlleleNr;
                variant.sel_pop_homs = totalHomCnt;
                double freq = (double)totalAlleleCnt/(double)totalAlleleNr;
                if(ExacToolFrame.LOCAL_MODE){
                    variant.sel_pop_freq = freq;                    
                }
                else{
                    //data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_SEL_POP_FREQ)] = freq; //update with selected populations
                    variant.sel_pop_freq = freq;
                }
                result = true;                                
            }                         
        }
        
        return result;        
    }
     
    public boolean matchCombinedVariant(CombinedVariant variant){        
        
        boolean exacMatch = matchSingleVariant(variant.exacData);     //check if exact variant matches constraints
        boolean gnomadMatch = matchSingleVariant(variant.gnomadData); //check if gnomad variant matches constraints
        
        boolean unique = !ExacToolFrame.UNIQUES || 1==variant.getAlleleCount();        
        return unique && (exacMatch || gnomadMatch);
    }
            
    public boolean matchCombinedVariant2(CombinedVariant variant)
    {        
        boolean result = false;       
        
        //boolean m1a = ExacToolFrame.INCL_EXOME && variant.isExac();
        boolean m1a = ExacToolFrame.INCL_EXOME && variant.exacData!=null;
        boolean m1b = ExacToolFrame.INCL_GENOME && variant.gnomadData!=null;
        
        //boolean m2 = !ExacToolFrame.CONSEQUENCE_ONLY || !"".equals(variant.major_consequence);//data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)].toString());
        
        //boolean m2 = !ExacToolFrame.CONSEQUENCE_ONLY || !"".equals(variant.consequence);//data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)].toString());
        //boolean m3 = !ExacToolFrame.CANONICAL_ONLY || "YES".equals(variant.canonical);
        boolean m2 = !ExacToolFrame.CONSEQUENCE_ONLY || !"".equals(variant.getConsequence());//data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)].toString());
        boolean m3 = !ExacToolFrame.CANONICAL_ONLY || "YES".equals(variant.getCanonical());
        
        //boolean m4 = !ExacToolFrame.PASS || ExacQuerier.EXAC_PASS_VALUE.equals(variant.filter);
        boolean m4 = !ExacToolFrame.PASS || ExacQuerier.EXAC_PASS_VALUE.equals(variant.getFilter());
        //boolean m5 = !ExacToolFrame.NORS || !variant.rsid.contains(ExacQuerier.EXAC_RS_VALUE);
        boolean m5 = !ExacToolFrame.NORS || !variant.getRsId().contains(ExacQuerier.EXAC_RS_VALUE);
        
        //boolean m6 = !ExacToolFrame.UNIQUES || 1==variant.allele_count;
        boolean m6 = !ExacToolFrame.UNIQUES || 1==variant.getAlleleCount();
        
//        boolean m7 = ExacToolFrame.annotations.contains(variant.major_consequence);
        boolean m7 = ExacToolFrame.selectedAnnotations.contains(variant.getMajorConsequence());
        boolean m8 = ExacToolFrame.LOF_FILTER==ExacToolFrame.ALL                             
                        || (ExacToolFrame.LOF_FILTER==ExacToolFrame.LOF && ExacQuerier.EXAC_LOF_TYPE_VALUE.equals(variant.getCategory()))
                        || (ExacToolFrame.LOF_FILTER==ExacToolFrame.MISSENSE_LOF && (ExacQuerier.EXAC_LOF_TYPE_VALUE.equals(variant.getCategory()) || ExacQuerier.EXAC_MISSENSE_TYPE_VALUE.equals(variant.getCategory())));
        
        boolean m9  =  ExacToolFrame.INCL_INDEL || (!ExacToolFrame.INCL_INDEL && "false".equals(variant.getIndel()));
        boolean m = (m1a || m1b) && m2 && m3 && m4 && m5 && m6 && m7 && m8 && m9;
        /*
        boolean m10 = ExacToolFrame.filteredIds.size()==0 || ExacToolFrame.filteredIds.contains(variant.exac_id);
        boolean m = (m1a || m1b) && m2 && m3 && m4 && m5 && m6 && m7 && m8 && m9 && m10;
        */
        
        if(m && ExacToolFrame.REGIONS_ONLY)
        {            
            ArrayList covEntries = new ArrayList(regionCovMap.keySet());
            //if size is 0, continue to next step.
            m = covEntries.isEmpty();
            
            for(int c=0; c<covEntries.size(); c++)
            {
                String region = (String)covEntries.get(c);
                //System.out.println(" to "+region);
                String pattern = region;
                
                if(region.startsWith("p.")){
                    //System.out.print("Region: "+region);
                    boolean anyRef = region.charAt(2) == '*';
                    int offset = 0;
                    char lastChar = region.charAt(region.length()-1);
                    boolean anyChange = lastChar == '*';
                    if(anyChange) 
                        offset = 1;
                    else anyChange = ('0' <= lastChar && lastChar <= '9');
                    
                    if(anyRef && anyChange)
                        pattern = "p."+anyAcid+region.substring(3, region.length()-offset)+anyAcid;
                    else if(anyRef)
                        pattern = "p."+anyAcid+region.substring(3, region.length());
                    else if(anyChange)
                        pattern = region.substring(0, region.length()-offset)+anyAcid;                    
                                        
                    //if(!"".equals(variant.getConsequence()))
                    //    variant.getConsequence() = variant.getConsequence();
                    //m = m || variant.consequence.matches(pattern);                             
                    m = m || variant.getConsequence().matches(pattern);                             
                }                                
                //if(regionCovMap.get(region) == -99.0){m = m || data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CONSEQUENCE)].toString().matches(region+anyAcid);}                
                else{
                    int index = region.indexOf("-");
                    if(index>-1)
                    {
                        int pos1 = Integer.parseInt(region.substring(region.indexOf(":")+1, index));
                        int pos2 = Integer.parseInt(region.substring(index+1));
                        /*int startpos = variant.pos;
                        int endpos = variant.pos;
                        */
                        int startpos = variant.getPos();
                        int endpos = variant.getPos();
                        //int startpos = Integer.parseInt((String)data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_START_POS)]);
                        //int endpos = Integer.parseInt((String)data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_END_POS)]);
                        m = m || (startpos>=pos1 && startpos<=pos2) || (endpos>=pos1 && endpos<=pos2);                    
                        //regionMatch = regionMatch || cons.matches(region); 
                    }
                }
            }
        }
        
        
        if(m)
        {
            boolean popMatch = true, specificPopMatch = false;
            ArrayList populations = new ArrayList(ExacToolFrame.selectedPopulations);                            

            int totalAlleleCnt = 0;
            int totalAlleleNr = 0;
            int totalHomCnt = 0;

            //no specific populations selected, simply use all...
            if(populations.isEmpty())
            {
                /*
                totalAlleleCnt = variant.allele_count;
                totalAlleleNr = variant.allele_num;
                totalHomCnt = variant.hom_count;
                */
                totalAlleleCnt = variant.getAlleleCount();
                totalAlleleNr = variant.getAlleleNum();
                totalHomCnt = variant.getHomCount();
            }
            else
            {                               
                popMatch = ExacToolFrame.ALL_POPS;
                for(int p=0; p<populations.size(); p++)
                {
                    Object pop = populations.get(p);
                    //Integer alleleCount = (Integer)popAcs.get(pop);                    
                    //Integer alleleCount = variant.pop_acs.getValueForPop(pop.toString());
                    Integer alleleCount = variant.getAlleleCountForPop(pop.toString());
                    if(alleleCount==null) //no population info stored?
                        alleleCount  = 0;

                    totalAlleleCnt = totalAlleleCnt + alleleCount;

                    specificPopMatch = alleleCount>0;
                    if(ExacToolFrame.ALL_POPS)
                        popMatch = popMatch && specificPopMatch;                    
                    else popMatch = popMatch || specificPopMatch;


                    if(specificPopMatch || !ExacToolFrame.ALL_POPS)  
                    {
                        //totalAlleleNr = totalAlleleNr += variant.pop_ans.getValueForPop(pop.toString());
                        //totalHomCnt = totalHomCnt += variant.pop_homs.getValueForPop(pop.toString());
                        totalAlleleNr = totalAlleleNr += variant.getAlleleNumberForPop(pop.toString());
                        totalHomCnt = totalHomCnt += variant.getHomsForPop(pop.toString());
                    }
                }
            }

            if(popMatch)
            {
                /*
                variant.sel_pop_acs = totalAlleleCnt;
                variant.sel_pop_ans = totalAlleleNr;
                variant.sel_pop_homs = totalHomCnt;
                double freq = (double)totalAlleleCnt/(double)totalAlleleNr;
                if(ExacToolFrame.LOCAL_MODE){
                    variant.sel_pop_freq = freq;                    
                }
                else{
                    //data[BaseQuerier.getVarIndex(BaseQuerier.OUTPUT_HEADER_SEL_POP_FREQ)] = freq; //update with selected populations
                    variant.sel_pop_freq = freq;
                }
                */
                result = true;                                
            }                         
        }
        
        return result;        
    }
    
    
    //public HashMap<String, Integer> featureIndexMap = new HashMap();
    
    public HashMap<Object, Object[]> getFeatureData(boolean filter){
       
        HashMap result = new HashMap();
        HashMap<String, Integer> featureIndexMap = new HashMap();
        //featureIndexMap.clear();
        //CombinedVariant[] displayedVariants = getDisplayedVariants(filter);
        
        
        for(int fr=0; fr<features.length; fr++)
        {
            String featureType = features[fr].feature_type;
            String geneName = features[fr].gene;            
            
            Integer featureIndex = featureIndexMap.get(featureType);
            if(featureIndex == null)
                featureIndex = new Integer(1);
            else featureIndex++;
                    
            int featureStart = features[fr].start;
            int featureEnd = features[fr].stop;                
            int featureSize = featureEnd - featureStart +1;
            int firstHit = -1, lastHit = -1;
            int firstDiff = -1, lastDiff = -1;
            
            //int lastStart = exstart;
            int lastEnd = featureStart;
            int emptySize = -1;

            boolean emptyRegionSet = false;
            //for(int vr=0; vr<variants.length; vr++)
            //for(int vr=0; vr<displayedVariants.length; vr++)
            Iterator displayedVariants = variantMap.values().iterator();
            while(displayedVariants.hasNext())                
            {
                CombinedVariant var = (CombinedVariant)displayedVariants.next();
                ////get start and end for variant                
                //int vstart = (Integer)displayedVariants[vr].getStartPos();
                //int vend = (Integer)displayedVariants[vr].getEndPos();
                int vstart = var.getStartPos();
                int vend = var.getEndPos();
                
                boolean startInFeature = vstart>=featureStart && vstart<=featureEnd;                    
                
                if(startInFeature)
                {   
                    if(firstHit==-1)
                        firstHit = vstart;
                    lastHit = vstart;
                    int newdiff = vstart - lastEnd;
                    if(newdiff>emptySize){
                        emptySize = newdiff;
                        firstDiff = lastEnd;
                        lastDiff = vstart;
                    }                    
                }
                
                boolean endInFeature = vend>=featureStart && vend<=featureEnd;
                if(endInFeature)
                {
                    lastEnd = vend;
                    lastHit = vend;
                }

                //not in this exon, break and go to next exon starting with this variant as next starting point
                if(!startInFeature && !endInFeature && (firstHit>-1 && vstart>featureEnd))
                {
                    if(emptySize==-1)
                    {
                        emptySize = featureSize;
                        firstDiff = featureStart;
                        lastDiff = featureEnd;
                    }
                    //varOffset = vr-1;
                    //vr = variants.length;
                    //vr = displayedVariants.length;                    
                    
                    int percentage = (emptySize*100)/featureSize;
                    Object resultEntry[] = new Object[]{geneName, featureIndex, featureType, featureStart, featureEnd, featureSize, emptySize, percentage, firstHit, lastHit, firstDiff, lastDiff};
                    result.put(fr, resultEntry);
                    emptyRegionSet = true;
                    break;
                }     
            }
            
            if(!emptyRegionSet){
                //System.out.println("Not set for: "+featureType+"-"+featureIndex);
                if(emptySize==-1)
                    emptySize = featureSize;
                int percentage = (emptySize*100)/featureSize;
                
                Object resultEntry[] = new Object[]{geneName, featureIndex, featureType, featureStart, featureEnd, featureSize, emptySize, percentage, firstHit, lastHit, firstDiff, lastDiff};
                result.put(fr, resultEntry);
            }
            
            featureIndexMap.put(featureType, featureIndex);
        }
        return result;
    }
    
    /*
    public HashMap<Object, Object[]> getFeatureDataOriginal(boolean filter){
       
        HashMap result = new HashMap();
        //Variant[] workdata = variants;
                
        HashMap<String, Integer> featureIndexMap = new HashMap();
        //featureIndexMap.clear();
        for(int fr=0; fr<features.length; fr++)
        {
            String featureType = features[fr].feature_type;
            String geneName = features[fr].gene;            
            Integer featureIndex = featureIndexMap.get(featureType);
            if(featureIndex == null)
                featureIndex = new Integer(1);
            else featureIndex++;
                    
            int exstart = features[fr].start;
            int exend = features[fr].stop;                
            int exonSize = exend - exstart +1;
            int firstHit = -1, lastHit = -1;
            int firstDiff = -1, lastDiff = -1;
            
            //int lastStart = exstart;
            int lastEnd = exstart;
            int emptySize = -1;

            boolean emptyRegionSet = false;
            for(int vr=0; vr<variants.length; vr++)
            {
                //get start and end for variant
                int vstart = variants[vr].start_pos;
                int vend = variants[vr].end_pos;

                boolean startInExon = vstart>=exstart && vstart<=exend;                    
                if(startInExon)
                {   
                    if(firstHit==-1)
                        firstHit = vstart;
                    lastHit = vstart;
                    int newdiff = vstart - lastEnd;
                    if(newdiff>emptySize){
                        emptySize = newdiff;
                        firstDiff = lastEnd;
                        lastDiff = vstart;
                    }                    
                }
                
                boolean endInExon = vend>=exstart && vend<=exend;
                if(endInExon)
                {
                    lastEnd = vend;
                    lastHit = vend;
                }

                //not in this exon, break and go to next exon starting with this variant as next starting point
                if(!startInExon && !endInExon && (firstHit>-1 && vstart>exend))
                {
                    if(emptySize==-1)
                    {
                        emptySize = exonSize;
                        firstDiff = exstart;
                        lastDiff = exend;
                    }
                    //varOffset = vr-1;
                    vr = variants.length;
                    int percentage = (emptySize*100)/exonSize;
                    Object resultEntry[] = new Object[]{geneName, featureIndex, featureType, exstart, exend, exonSize, emptySize, percentage, firstHit, lastHit, firstDiff, lastDiff};
                    result.put(fr, resultEntry);
                    emptyRegionSet = true;
                }     
                /*
                else if(vr==workdata.length-1){
                    int percentage = (emptySize*100)/exonSize;
                    Object resultEntry[] = new Object[]{geneName, featureIndex, featureType, exstart, exend, exonSize, emptySize, percentage, firstHit, lastHit, firstDiff, lastDiff};
                    result.put(fr, resultEntry);
                }
                */
                
    /*
            }
            if(!emptyRegionSet){
                int percentage = (emptySize*100)/exonSize;
                Object resultEntry[] = new Object[]{geneName, featureIndex, featureType, exstart, exend, exonSize, emptySize, percentage, firstHit, lastHit, firstDiff, lastDiff};
                result.put(fr, resultEntry);
            }
            featureIndexMap.put(featureType, featureIndex);
        }
        return result;
    }
    */
    
    public String toString(){
        String result = id;
        
        if(genes.length>1 || (genes.length==1 && !genes[0].equals(id)))
            result = id+" ("+getGeneNames()+")";
        /*
        if(!id.equals(gene)){
            result = id+" ("+gene+")";
        }
        */
        return result;
    }
}
