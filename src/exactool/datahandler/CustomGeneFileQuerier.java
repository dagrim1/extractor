/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler;

import exactool.datahandler.dataobjects.Feature;
import java.util.ArrayList;

/**
 *
 * @author flip_
 */
public class CustomGeneFileQuerier {
    public static String getRegionForGene(String file, String gene){
        return getRegionFromLine(getFirstMatchingLine(file, gene, 0));                
    }
    
    public static String getRegionForTransript(String file, String gene){
        return getRegionFromLine(getFirstMatchingLine(file, gene, 4));                
    }
    
    public static String getRegionFromLine(String line){
        String tokens[] = line.split("\t");
        if(tokens.length>4)
            return tokens[1]+":"+tokens[2]+"-"+tokens[3];
        else return null;
    }
    
    /*
    public static Object[][] getFeaturesForGene(String file, String gene){
        return getFeaturesForTranscript(file, getCanonicalTranscript(file, gene));
    }
    */
    public static Feature[] getFeaturesForGene(String file, String gene){
        return getFeaturesForTranscript(file, getCanonicalTranscript(file, gene));
    }
    
    public static ArrayList getTranscriptsForGene(String file, String gene){
        ArrayList result = null;
        try
        {
            GzipReader reader = new GzipReader(file);
            String line = null;
            while( (line = reader.readLine()) != null){                                
                if(line.startsWith(gene+"\t"))
                    result.add(line.split("\t")[4]);
            }
            reader.close();
        }
        catch(Exception e){e.printStackTrace();};
        return result;
    }
    
    public static ArrayList getLinesForGene(String file, String gene){
        ArrayList result = new ArrayList();
        try
        {
            GzipReader reader = new GzipReader(file);
            String line = null;
            while( (line = reader.readLine()) != null){                                
                if(line.startsWith(gene+"\t"))
                    result.add(line);
            }
            reader.close();
        }
        catch(Exception e){e.printStackTrace();};
        return result;
    }
    
    public static Feature[] getFeaturesForLine(String line){
        Feature result[] = new Feature[0];        
        if(line!=null){
            String tokens[] = line.split("\t");
            int featureCount = Integer.parseInt(tokens[6]);
            result = new Feature[featureCount];
            String types[] = tokens[7].split(",");
            String starts[] = tokens[8].split(",");
            String ends[] =  tokens[9].split(",");
            for(int f=0; f<featureCount; f++){
                result[f] = new Feature();
                result[f].chrom = tokens[0];
                result[f].feature_type = types[f];
                result[f].start = Integer.parseInt(starts[f]);
                result[f].stop = Integer.parseInt(ends[f]);                
            }
        }
        return result;
    }
   
    public static  Feature[] getFeaturesForTranscript(String file, String transcript){
        String line = getFirstMatchingLine(file, transcript, 4);
        return getFeaturesForLine(line);
    }
   
    public static  ArrayList getGenesInRegion(String file, String region){
        ArrayList result = null;
        try
        {
            String chr = region.substring(0, region.indexOf(":"));
            String st = region.substring(chr.length()+1, region.indexOf("-"));
            int start = Integer.parseInt(st);
            int end = Integer.parseInt(region.substring(chr.length()+st.length()+2, region.length()));
            GzipReader reader = new GzipReader(file);
            String line = null;
            while( (line = reader.readLine()) != null){
                String tokens[] = line.split("\t");
                if(chr.equals(tokens[1]) && start<=Integer.parseInt(tokens[2]) && end>=Integer.parseInt(tokens[3]))
                    result.add(tokens[0]);
                
            }
            reader.close();
        }
        catch(Exception e){e.printStackTrace();};
        return result;
        
    }
    
    public static  ArrayList getCanonicalTranscriptsInRegion(String file, String region){
        ArrayList result = new ArrayList();
        try
        {
            String chr = region.substring(0, region.indexOf(":"));
            String st = region.substring(chr.length()+1, region.indexOf("-"));
            int start = Integer.parseInt(st);
            int end = Integer.parseInt(region.substring(chr.length()+st.length()+2, region.length()));
            GzipReader reader = new GzipReader(file);
            String line = null;
            while( (line = reader.readLine()) != null){
                String tokens[] = line.split("\t");
                if(chr.equals(tokens[1]) && start<=Integer.parseInt(tokens[2]) && end>=Integer.parseInt(tokens[3]) && !result.contains(tokens[5]))
                    result.add(tokens[5]);
                
            }
            reader.close();
        }
        catch(Exception e){e.printStackTrace();};
        return result;
        
    }
    
     public static Feature[] getFeaturesForRegion(String file, String region){
        String line = getFirstMatchingLine(file, region, 4);
        return getFeaturesForLine(line);
     }
    
    public static String getCanonicalTranscript(String file, String gene){
        String line = getFirstMatchingLine(file, gene, 0);
        if(line!=null)
            return line.split("\t")[5];
        
        return null;
    }
    
    /*
    public static ArrayList getTranscripts(String file, String gene){
        ArrayList transcripts = getFeaturesForColumn(file, gene, 0);
        return transcripts;
    }
    */
    
    public static String getFirstMatchingLine(String file, String value, int columnIndex){
        String result = null;
        try
        {
            GzipReader reader = new GzipReader(file);
            
            while( (result = reader.readLine()) != null){
                String tokens[] = null;
                boolean hit = false;
                if(columnIndex==0)
                    hit = result.startsWith(value+"\t");
                else hit = value.equals(result.split("\t")[columnIndex]);                    
                
                if(hit)
                    break;                
            }
            reader.close();
        }
        catch(Exception e){e.printStackTrace();};
        return result;
    }
    /*
    public static Object[][] getFeaturesForColumn(String file, String value, int columnIndex){
        ArrayList result = new ArrayList();
        try{
            GzipReader reader = new GzipReader(file);
            String line = null;
            
            while( (line = reader.readLine()) != null){
                String tokens[] = null;
                boolean hit = false;
                if(columnIndex==0)
                {
                    hit = line.startsWith(value+"\t");
                    if(hit)
                        tokens = line.split("\t");
                }
                else
                {
                    tokens = line.split("\t");
                    hit = value.equals(tokens[columnIndex]);                    
                }
                if(hit){
                    //String feature[] = new String[]{value, tokens[0], tokens[0], tokens[0]};
                    result.add(line);
                }
            }
            reader.close();
        }
        catch(Exception e){e.printStackTrace();};
        
        return result;
    }
*/
    
}
