/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler.dataobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 *
 * @author flip
 */
public class CombinedVariant {
    public static boolean perPop = true;
    
    public Variant exacData, gnomadData;
    
    public boolean include(Variant variant){
        return (variant!=null && variant.include);
    }
    
    public String getChrom(){
        if(exacData!=null)            
            return exacData.chrom;
        else return gnomadData.chrom;        
    }
   
    public int getStartPos(){
        if(exacData!=null)
            return exacData.start_pos;
        else return gnomadData.start_pos;        
    }
    
    public int getEndPos(){
        if(exacData!=null)
            return exacData.end_pos;
        else return gnomadData.end_pos;        
    }
    
    public String getRef(){
        if(exacData!=null)
            return exacData.ref;
        else return gnomadData.ref;        
    }
    
    public String getAlt(){
        if(exacData!=null)
            return exacData.alt;
        else return gnomadData.alt;        
    }
    
    public String getSearchTerm(){
        if(exacData!=null)
            return exacData.search_term;
        else return gnomadData.search_term;        
    }
    
    public String getGene(){
        if(exacData!=null)
            return exacData.gene;
        else return gnomadData.gene;        
    }
    
    public String getExacId(){
        if(exacData!=null)
            return exacData.exac_id;
        else return gnomadData.exac_id;        
    }
    
    public String getCanonical(){
        if(exacData!=null)
            return exacData.canonical;
        else return gnomadData.canonical;        
    }
    
    public String getRsId(){
        if(exacData!=null)
            return exacData.rsid;
        else return gnomadData.rsid;        
    }
    
    public int getPos(){
        if(exacData!=null)
            return exacData.pos;
        else return gnomadData.pos;        
    }
    
    public String getCategory(){
        if(exacData!=null)
            return exacData.category;
        else return gnomadData.category;        
    }
    
    
    public String getFilterExtended(){
        String result = "";            
        //if(exacData!=null)
        if(include(exacData))
            result=result+"Exac: "+exacData.filter+". ";            
        //if(gnomadData!=null)
        if(include(gnomadData))
            result=result+"Gnomad: "+gnomadData.filter+". ";        
        return result;
    }
    
    public String getFilter(){
        String result = "";            
        if(exacData!=null)
            result = exacData.filter;
        else if(gnomadData!=null)
            result=gnomadData.filter;
        return result;
    }
    
    
    public String getConsequence(){
        if(exacData!=null)
            return exacData.consequence;
        else return gnomadData.consequence;        
    }
    public String getMajorConsequence(){
        if(exacData!=null)
            return exacData.major_consequence;
        else return gnomadData.major_consequence;        
    }
    public String getIndel(){        
        if(exacData!=null)
            return exacData.indel;
        else return gnomadData.indel;        
    }
    
    public int getAlleleCount(){
        int result = 0;
        if(include(exacData))
            result+=exacData.allele_count;
        if(include(gnomadData))
            result+=gnomadData.allele_count;
        return result;    
    }
    
    public int getAlleleNum(){
        int result = 0;
        if(include(exacData))
            result+=exacData.allele_num;
        if(include(gnomadData))
            result+=gnomadData.allele_num;
        return result;    
    }
    
    public int getHomCount(){
        int result = 0;
        if(include(exacData))
            result+=exacData.hom_count;
        if(include(gnomadData))
            result+=gnomadData.hom_count;
        return result;    
    }
    
    public double getAllelFreq(){
        int ac = (Integer)getAlleleCount();
        int an = (Integer)getAlleleNum();
        return (double)((double)ac/(double)an);
    }
    
     public String getDataSet(){
        String result = "";
        if(include(exacData)){
            if("PASS".equals(exacData.filter))
                result=result+"E";
            else result = result+"e";
        }
        if(include(gnomadData)){
            if("PASS".equals(gnomadData.filter))
                result=result+"G";
            else result = result+"g";
        }
        return result;        
    }
     
    public HashSet getPops(){
        HashSet result = new HashSet();
        if(include(exacData))
            result.addAll(exacData.getPops());            
        if(include(gnomadData))
            result.addAll(gnomadData.getPops());            
        return result;        
    }
    
    public HashSet getMajorCons(){
        HashSet result = new HashSet();
        if(include(exacData))
            result.add(exacData.major_consequence);            
        if(include(gnomadData))
            result.add(gnomadData.major_consequence);            
        return result;        
    }
     
    public String getPopAcs(){
        String result = "";
        if(include(exacData))
            result=result+"Exac: "+exacData.pop_acs+". ";            
        if(include(gnomadData))
            result=result+"Gnomad: "+gnomadData.pop_acs+". ";
        return result;        
    }
    
     public String getPopAns(){
        String result = "";
        if(include(exacData))
            result=result+"Exac: "+exacData.pop_ans+". ";
        if(include(gnomadData))
            result=result+"Gnomad: "+gnomadData.pop_ans+". ";
        return result;        
    }
     
      public String getPopHoms(){
        String result = "";
        if(include(exacData))
            result=result+"Exac: "+exacData.pop_homs+". ";            
        if(include(gnomadData))
            result=result+"Gnomad: "+gnomadData.pop_homs+". ";
        return result;        
    }
    
    public int getSelPopAcs(){
        int result = 0;
        if(include(exacData))        
            result+=exacData.sel_pop_acs;
        if(include(gnomadData))
            result+=gnomadData.sel_pop_acs;
        return result;    
    }
    
     public int getSelPopAns(){
        int result = 0;
        if(include(exacData))
            result+=exacData.sel_pop_ans;
        if(include(gnomadData))
            result+=gnomadData.sel_pop_ans;
        return result;    
    }
     
      public int getSelPopHoms(){
        int result = 0;
        if(include(exacData))
            result+=exacData.sel_pop_homs;
        if(include(gnomadData))
            result+=gnomadData.sel_pop_homs;
        return result;    
    }
      
    public double getSelPopFreqs(){        
        int ac = (Integer)getSelPopAcs();
        int an = (Integer)getSelPopAns();
        return (double)((double)ac/(double)an);
        //return (Double)((Double)getSelPopAcs()/(Double)getSelPopAns());
    }
    
    public int getAlleleCountForPop(String pop){
        int result = 0;
        if(include(exacData))
            result+=exacData.pop_acs.getValueForPop(pop);
        if(include(gnomadData))
            result+=gnomadData.pop_acs.getValueForPop(pop);
        return result;
    }
    
    public int getAlleleNumberForPop(String pop){
        int result = 0;
        if(include(exacData))
            result+=exacData.pop_ans.getValueForPop(pop);
        if(include(gnomadData))
            result+=gnomadData.pop_ans.getValueForPop(pop);
        return result;
    }
    
     public int getHomsForPop(String pop){
        int result = 0;
        if(include(exacData))
            result+=exacData.pop_homs.getValueForPop(pop);
        if(include(gnomadData))
            result+=gnomadData.pop_homs.getValueForPop(pop);
        return result;
    }
        
    public double getFrequencyForPop(String pop){
        return (double)getAlleleCountForPop(pop)/(double)getAlleleNumberForPop(pop);
    }
    
    public void setGnomadData(Variant data){
        gnomadData = data;
    }
    public Object[] toRow(){
        Object row[] = new Object[]{getChrom(), getStartPos(), getEndPos(), getRef(), getAlt(), getSearchTerm(), getGene(), getExacId(), getDataSet(), getCanonical(), getRsId(), getCategory(), getFilterExtended(), getConsequence(), getMajorConsequence(), getIndel(),                                 getAlleleCount(), getAlleleNum(), getHomCount(), getAllelFreq(), getSelPopAcs(), getSelPopAns(), getSelPopHoms(), getSelPopFreqs(), getPopAcs(), getPopAns(), getPopHoms()};
        //if("55505651".equals(""+row[1]))
        //    System.out.println("break");
        if(perPop)
        {
            ArrayList pops = new ArrayList(getPops());
            Collections.sort(pops);
            
            Object popRow[] = new Object[row.length+2+pops.size()*4];
            for(int p=0; p<row.length; p++)
                popRow[p] = row[p];
            for(int p=0; p<pops.size(); p++){
                popRow[2+row.length+(4*p)] = this.getAlleleCountForPop(pops.get(p).toString());
                popRow[2+row.length+(4*p)+1] = this.getAlleleNumberForPop(pops.get(p).toString());
                popRow[2+row.length+(4*p)+2] = this.getHomsForPop(pops.get(p).toString());
                popRow[2+row.length+(4*p)+3] = this.getFrequencyForPop(pops.get(p).toString());
            }
            row = popRow;
            //return new Object[]{getChrom(), getStartPos(), getEndPos(), getRef(), getAlt(), getSearchTerm(), getGene(), getExacId(), getDataSet(), getCanonical(), getRsId(), getCategory(), getFilterExtended(), getConsequence(), getMajorConsequence(), getIndel(),                                getAlleleCount(), getAlleleNum(), getHomCount(), getAllelFreq(), getSelPopAcs(), getSelPopAns(), getSelPopHoms(), getSelPopFreqs(), getPopAcs(), getPopAns(), getPopHoms()};
        }
        //return new Object[]{getChrom(), getStartPos(), getEndPos(), getRef(), getAlt(), getSearchTerm(), getGene(), getExacId(), getDataSet(), getCanonical(), getRsId(), getCategory(), getFilterExtended(), getConsequence(), getMajorConsequence(), getIndel(),                                 getAlleleCount(), getAlleleNum(), getHomCount(), getAllelFreq(), getSelPopAcs(), getSelPopAns(), getSelPopHoms(), getSelPopFreqs(), getPopAcs(), getPopAns(), getPopHoms()};
                   
        return row;
    }
}
