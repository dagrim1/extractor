/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.misc;

import exactool.datahandler.GzipReader;
import exactool.datahandler.GzipUtility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Rewrites Homo_sapiens.GRCh37.75.gtf.gz file to a more efficient format used in the Extractor tool
 * 
 * ftp://ftp.ensembl.org/pub/release-75/gtf/homo_sapiens
 * 
 * The resulting file contains a list of gene, chr, start, end, transcript, canonical, featurecount, featuretypes, featurestarts and featureends for each gene-name
 * @author flip
 */
public class GTFRewriter {
    static File gtfFile = new File("H:\\ExAC\\source_files\\Homo_sapiens.GRCh37.75.gtf.gz");//Feature (exon, cds, utr, etc) info for all transcripts in each gene.
    static File pliFile = new File("H:\\ExAC\\source_files\\forweb_cleaned_exac_r03_march16_z_data_pLI.txt.gz"); //syn, miss and lof counts (obs and expected), z-scores and pli score for each gene
    static File summFile = new File("H:\\ExAC\\source_files\\refseq_gene_summ.gz"); //refseq summary information for each gene
    static File cannFile = new File("H:\\ExAC\\source_files\\human_canonical_transcripts_v75.can"); //canonical transcript for each gene
    
    public static String chromosomes[] = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "MT"};
    /**
     * 
     * @param args 
     */
    public static void main(String args[]){
       processFiles(gtfFile, pliFile, summFile);
    }
    
    public static void processFiles(File gtfFile, File pliFile, File summFile){
        try
        {            
            //"H:\\ExAC FTP\\release0.3.1\\ensembl_data\\Homo_sapiens.GRCh37.75.gtf.gz"
            if(!gtfFile.exists())                
                return;
            
            
            
            String path = gtfFile.getParent();
            
            HashMap canonicalMap = new HashMap();
            BufferedReader canReader = new BufferedReader(new FileReader(path+System.getProperty("file.separator")+"human_canonical_transcripts_v75.can"));
            String line = null;
            while( (line = canReader.readLine()) !=null){
                String tokens[] = line.split("\t");
                canonicalMap.put(tokens[1], tokens[0]);
                
            }
            canReader.close();
            
            String tmp_path = path+System.getProperty("file.separator")+"tmp";
            
            File tmpDir = new File(tmp_path);
            if(!tmpDir.exists())
                tmpDir.mkdir();
            
            String out_path = path+System.getProperty("file.separator")+"gene_data";
            File outDir = new File(out_path);
            if(!outDir.exists())
                outDir.mkdir();
            String basename = gtfFile.getName().substring(0, gtfFile.getName().length()-".gtf.gz".length());
            HashMap<String, Gene> geneMap = new HashMap();
            
            GzipReader gzipReader = new GzipReader(gtfFile.getPath());
            String geneChrFile = tmp_path+System.getProperty("file.separator")+basename+".gene.chr";
            String transcriptChrFile = tmp_path+System.getProperty("file.separator")+basename+".transcript.chr";
            
            HashMap<String, PrintStream> printMap = new HashMap();
            PrintStream geneChrPrinter = new PrintStream(geneChrFile);            
            PrintStream transcriptChrPrinter = new PrintStream(transcriptChrFile);            
            
            HashMap infoMap = new HashMap();
            
            ArrayList orderedGenes = new ArrayList();            
            while ( (line = gzipReader.readLine()) != null)
            {
                String tokens[] = line.split("\t");                
                if(tokens.length>7)
                {
                    String chr = tokens[0];
                   
                    
                    String infoField = tokens[8];
                    infoMap = infoFieldToMap(infoField);

                    String geneName = (String)infoMap.get("gene_name");
                    String geneId = (String)infoMap.get("gene_id");
                    
                    if(geneName==null)
                        System.out.println("");
                    else{
                        Gene geneData = geneMap.get(geneName);                        
                        if(geneData==null)
                        {            
                            orderedGenes.add(geneName);
                            geneData = new Gene(geneName);                            
                            geneData.chr = chr;
                            geneData.id = geneId;
                        }

                        geneData.addFeature((String)infoMap.get("transcript_id"), tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
                        geneMap.put(geneName, geneData);
                    }
                }
                
            }            
            gzipReader.close();            
            System.out.println("GFF file read...");
            
            System.out.println("PLI File: ");
            gzipReader = new GzipReader(pliFile.getPath());
            gzipReader.readLine(); //header
            while ( (line = gzipReader.readLine()) != null)
            {
                String tokens[] = line.split("\t");   
                if(tokens.length>18){
                    String geneName = tokens[1];  //10 =n syn, mis, lof //13=exp syn, mis, lof //16=z syn, mis, lof //19=pLI
                    Gene geneData = geneMap.get(geneName);
                    if(geneData==null){
                        System.out.println(geneName);
                        geneData = new Gene(geneName);
                        orderedGenes.add(geneName);
                        geneData.chr = tokens[2];                            
                    }
                    geneData.obsSyn = Double.parseDouble(tokens[10]);
                    geneData.obsMis = Double.parseDouble(tokens[11]);
                    geneData.obsLof = Double.parseDouble(tokens[12]);

                    geneData.expSyn = Double.parseDouble(tokens[13]);
                    geneData.expMis = Double.parseDouble(tokens[14]);
                    geneData.expLof = Double.parseDouble(tokens[15]);

                    geneData.zSyn = Double.parseDouble(tokens[16]);
                    geneData.zMis = Double.parseDouble(tokens[17]);
                    geneData.zLof = Double.parseDouble(tokens[18]);

                    geneData.pli = tokens[19];

                    geneMap.put(geneName, geneData);
                }
            }
            gzipReader.close();
            System.out.println("pLI file read...");
            
            System.out.println("Summ File (no chr for these unknown genes!): ");
            gzipReader = new GzipReader(summFile.getPath());
            while ( (line = gzipReader.readLine()) != null)
            {
                String tokens[] = line.split("\t");  
                String geneName = tokens[0];  //10 =n syn, mis, lof //13=exp syn, mis, lof //16=z syn, mis, lof //19=pLI
                Gene geneData = geneMap.get(geneName);
                if(geneData==null){
                    System.out.println(geneName);
                    geneData = new Gene(geneName);
                    orderedGenes.add(geneName);
                    //geneData.chr = tokens[2];                            
                }
                
                if(tokens.length>2){
                    geneData.summ = tokens[2];
                    
                }
                geneMap.put(geneName, geneData);
            }
            gzipReader.close();
            System.out.println("summ file read...");
            
            if(true)
                return;
            
            geneChrPrinter.println("Gene\tCHR");
            transcriptChrPrinter.println("Transcript\tCHR");
            for(int g=0; g<orderedGenes.size(); g++)
            {
                String geneName = orderedGenes.get(g).toString();
                Gene gene = geneMap.get(geneName);
                //String canonical = gene.updateCanonicalTranscript();
                String canonical = (String)canonicalMap.get(gene.id);
                gene.setCanonicalTranscript(canonical);
                gene.updateCDSSize();
                
                PrintStream printer = printMap.get(gene.chr);
                if(printer ==null)
                {
                    //printer = new PrintStream("H:\\ExAC FTP\\release0.3.1\\ensembl_data\\Homo_sapiens.GRCh37.75.chr"+gene.chr);
                    printer = new PrintStream(tmp_path+System.getProperty("file.separator")+basename+".chr"+gene.chr);
                    
                    printer.println("Gene\tGene_chr\tGene_start\tGene_end\tTranscript\tCanonical\tFeature_Count\tFeature_Types\tFeature_Starts\tFeature_Ends");
                }
                ArrayList transcripts = gene.getTranscripts();
                for(int t=0; t<transcripts.size(); t++){
                    Transcript transcript = (Transcript)transcripts.get(t);
                    printer.println(geneName+"\t"+gene.chr+"\t"+gene.geneStart+"\t"+gene.geneEnd+"\t"+transcript.name+"\t"+canonical+"\t"+transcript.toString());
                    transcriptChrPrinter.println(transcript.name+"\t"+gene.chr);
                }
                geneChrPrinter.println(geneName+"\t"+gene.chr);
                printMap.put(gene.chr, printer);
            }
            Iterator<PrintStream> printers = printMap.values().iterator();
            while(printers.hasNext()){
                ((PrintStream)printers.next()).close();
            }            
            geneChrPrinter.close();
            transcriptChrPrinter.close();
            
            //next compress and remove tmp data
            System.out.println("GZipping");
            for(int c=0; c<chromosomes.length; c++){                   
                //compress
                GzipUtility.compressGzipFile(tmp_path+System.getProperty("file.separator")+basename+".chr"+chromosomes[c],out_path+System.getProperty("file.separator")+basename+".chr"+chromosomes[c]+".gz");
                //and remove original
                //new File(basename+chromosomes[c]).delete();                
            }
            
            
            GzipUtility.compressGzipFile(geneChrFile, out_path+System.getProperty("file.separator")+basename+".gene.chr.gz");
            GzipUtility.compressGzipFile(transcriptChrFile, out_path+System.getProperty("file.separator")+basename+".transcript.chr.gz");
            
            System.out.println("Removing tmp data...");
            String[]entries = tmpDir.list();
            for(String s: entries){
                File currentFile = new File(tmpDir.getPath(),s);
                currentFile.delete();
            }
            tmpDir.delete();
            
            System.out.println("Done!");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void processGTF(){
        
        //processGTF("H:\\ExAC FTP\\release0.3.1\\ensembl_data\\Homo_sapiens.GRCh37.75.gtf.gz");
        processGTF("H:\\ExAC FTP\\release0.3.1\\test\\Homo_sapiens.GRCh37.75.gtf.gz");
    }    
    
    public static void processGTF(String file){
        try
        {            
            //"H:\\ExAC FTP\\release0.3.1\\ensembl_data\\Homo_sapiens.GRCh37.75.gtf.gz"
            File gtf_file = new File(file);
            if(!gtf_file.exists())                
                return;
            
            
            
            String path = gtf_file.getParent();
            
            HashMap canonicalMap = new HashMap();
            BufferedReader canReader = new BufferedReader(new FileReader(path+System.getProperty("file.separator")+"human_canonical_transcripts_v75.can"));
            String line = null;
            while( (line = canReader.readLine()) !=null){
                String tokens[] = line.split("\t");
                canonicalMap.put(tokens[1], tokens[0]);
                
            }
            canReader.close();
            
            String tmp_path = path+System.getProperty("file.separator")+"tmp";
            
            File tmpDir = new File(tmp_path);
            if(!tmpDir.exists())
                tmpDir.mkdir();
            
            String out_path = path+System.getProperty("file.separator")+"gene_data";
            File outDir = new File(out_path);
            if(!outDir.exists())
                outDir.mkdir();
            String basename = gtf_file.getName().substring(0, gtf_file.getName().length()-".gtf.gz".length());
            HashMap<String, Gene> geneMap = new HashMap();
            
            GzipReader gzipReader = new GzipReader(file);
            String geneChrFile = tmp_path+System.getProperty("file.separator")+basename+".gene.chr";
            String transcriptChrFile = tmp_path+System.getProperty("file.separator")+basename+".transcript.chr";
            /*
            String geneChrFile = "H:\\ExAC FTP\\release0.3.1\\ensembl_data\\Homo_sapiens.GRCh37.75.gene.chr";
            String transcriptChrFile = "H:\\ExAC FTP\\release0.3.1\\ensembl_data\\Homo_sapiens.GRCh37.75.transcript.chr";
            String basename="H:\\ExAC FTP\\release0.3.1\\ensembl_data\\Homo_sapiens.GRCh37.75.chr";
            */
            
            HashMap<String, PrintStream> printMap = new HashMap();
            PrintStream geneChrPrinter = new PrintStream(geneChrFile);            
            PrintStream transcriptChrPrinter = new PrintStream(transcriptChrFile);            
            
            HashMap infoMap = new HashMap();
            
            ArrayList orderedGenes = new ArrayList();            
            while ( (line = gzipReader.readLine()) != null)
            {
                String tokens[] = line.split("\t");                
                if(tokens.length>7)
                {
                    String chr = tokens[0];
                   
                    
                    String infoField = tokens[8];
                    infoMap = infoFieldToMap(infoField);

                    String geneName = (String)infoMap.get("gene_name");
                    String geneId = (String)infoMap.get("gene_id");
                    
                    if(geneName==null)
                        System.out.println("");
                    else{
                        Gene geneData = geneMap.get(geneName);                        
                        if(geneData==null)
                        {            
                            orderedGenes.add(geneName);
                            geneData = new Gene(geneName);                            
                            geneData.chr = chr;
                            geneData.id = geneId;
                        }

                        geneData.addFeature((String)infoMap.get("transcript_id"), tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
                        geneMap.put(geneName, geneData);
                    }
                }
                
            }            
            gzipReader.close();
            
            System.out.println("Original file read...");
            
            geneChrPrinter.println("Gene\tCHR");
            transcriptChrPrinter.println("Transcript\tCHR");
            for(int g=0; g<orderedGenes.size(); g++)
            {
                String geneName = orderedGenes.get(g).toString();
                Gene gene = geneMap.get(geneName);
                //String canonical = gene.updateCanonicalTranscript();
                String canonical = (String)canonicalMap.get(gene.id);
                gene.setCanonicalTranscript(canonical);
                gene.updateCDSSize();
                
                PrintStream printer = printMap.get(gene.chr);
                if(printer ==null)
                {
                    //printer = new PrintStream("H:\\ExAC FTP\\release0.3.1\\ensembl_data\\Homo_sapiens.GRCh37.75.chr"+gene.chr);
                    printer = new PrintStream(tmp_path+System.getProperty("file.separator")+basename+".chr"+gene.chr);
                    
                    printer.println("Gene\tGene_chr\tGene_start\tGene_end\tTranscript\tCanonical\tFeature_Count\tFeature_Types\tFeature_Starts\tFeature_Ends");
                }
                ArrayList transcripts = gene.getTranscripts();
                for(int t=0; t<transcripts.size(); t++){
                    Transcript transcript = (Transcript)transcripts.get(t);
                    printer.println(geneName+"\t"+gene.chr+"\t"+gene.geneStart+"\t"+gene.geneEnd+"\t"+transcript.name+"\t"+canonical+"\t"+transcript.toString());
                    transcriptChrPrinter.println(transcript.name+"\t"+gene.chr);
                }
                geneChrPrinter.println(geneName+"\t"+gene.chr);
                printMap.put(gene.chr, printer);
            }
            Iterator<PrintStream> printers = printMap.values().iterator();
            while(printers.hasNext()){
                ((PrintStream)printers.next()).close();
            }            
            geneChrPrinter.close();
            transcriptChrPrinter.close();
            
            //next compress and remove tmp data
            System.out.println("GZipping");
            for(int c=0; c<chromosomes.length; c++){                   
                //compress
                GzipUtility.compressGzipFile(tmp_path+System.getProperty("file.separator")+basename+".chr"+chromosomes[c],out_path+System.getProperty("file.separator")+basename+".chr"+chromosomes[c]+".gz");
                //and remove original
                //new File(basename+chromosomes[c]).delete();                
            }
            
            
            GzipUtility.compressGzipFile(geneChrFile, out_path+System.getProperty("file.separator")+basename+".gene.chr.gz");
            GzipUtility.compressGzipFile(transcriptChrFile, out_path+System.getProperty("file.separator")+basename+".transcript.chr.gz");
            
            System.out.println("Removing tmp data...");
            String[]entries = tmpDir.list();
            for(String s: entries){
                File currentFile = new File(tmpDir.getPath(),s);
                currentFile.delete();
            }
            tmpDir.delete();
            
            System.out.println("Done!");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void readTest(){
         try
        {
            String gene = "PCSK9";            
            String basename="H:\\ExAC FTP\\release0.3.1\\ensembl_data\\gene_data\\Homo_sapiens.GRCh37.75.chr1";
            
            //String[] genes = new String[]{"TNNT2", "PCSK9", "TNNT2", "PCSK9", "TNNT2", "PCSK9", "TNNT2", "PCSK9"};
            
            String line = null;
            long start = System.currentTimeMillis();
            System.out.println("GZIPFILE");
            
            BufferedReader reader = new BufferedReader(new FileReader("H:\\ExAC FTP\\release0.3.1\\ensembl_data\\gene_data\\Homo_sapiens.GRCh37.75.gene.chr"));            
            while( (line = reader.readLine()) !=null){
                if(line.startsWith(gene+"\t"))
                {
                    String tokens[] = line.split("\t");
                    System.out.println(gene+" -> "+tokens[1]);
                    break;
                }
            }
            reader.close();
            
            GzipReader reader2 = new GzipReader(basename+".gz");          
            
            while( (line = reader2.readLine()) !=null){
                String tokens[] = line.split("\t");
                if(gene.equals(tokens[0]))
                    System.out.println(line);
            }
            reader2.close();
            long end = System.currentTimeMillis();
            
            System.out.println("Time in ms: "+(end-start)+" ("+((end-start)/1000)+"s).");
            
            start = System.currentTimeMillis();
            
            reader = new BufferedReader(new FileReader(basename));
            System.out.println("TEXTFILE");
            while( (line = reader.readLine()) !=null){
                String tokens[] = line.split("\t");
                if(gene.equals(tokens[0]))
                    System.out.println(line);
            }
            reader.close();
            
            end = System.currentTimeMillis();
            System.out.println("Time in ms: "+(end-start)+" ("+((end-start)/1000)+"s).");
            
            
            System.out.println("FULL GZIP");
            start = System.currentTimeMillis();
            reader2 = new GzipReader("H:\\ExAC FTP\\release0.3.1\\ensembl_data\\Homo_sapiens.GRCh37.75.compact.gz");          
            
            while( (line = reader2.readLine()) !=null){
                String tokens[] = line.split("\t");
                if(gene.equals(tokens[0]))
                    System.out.println(line);
            }
            reader2.close();
            end = System.currentTimeMillis();
            System.out.println("Time in ms: "+(end-start)+" ("+((end-start)/1000)+"s).");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
        
    
    private static  HashMap infoFieldToMap(String infoField){
        HashMap result = new HashMap();
        String infoTokens[] = infoField.split(";");
        
        for(int i=0; i<infoTokens.length; i++){
            String nameValue[] = infoTokens[i].trim().split(" ");
            result.put(nameValue[0], nameValue[1].replaceAll("\"", ""));
        }       
                
        return result;
    }
}

class Gene{
    String name, canonical_transcript, chr;
    public String id;
    int geneStart = -1, geneEnd = -1, cdsStart = -1, cdsEnd = -1, cdsSize = -1;
    String pli, summ;
    double expSyn, expMis, expLof, expCnv, obsSyn, obsMis, obsCnv, obsLof, zSyn, zMis, zLof, zCnv;
    
    HashMap<String, Transcript> transcripts = new HashMap();
    
    public Gene(String name){
        this.name = name;
    }
    
    public int getTranscriptCount(){
        return transcripts.size();
    }
    
    public ArrayList getTranscripts(){
        return new ArrayList(transcripts.values());
    }
    public void addFeature(String transcriptName, String featureType, int featureStart, int featureEnd){
        featureStart++;
        featureEnd++;
        if(featureType.equals("exon") || featureType.equals("CDS") ||   featureType.equals("UTR")){
            Transcript transcript = transcripts.get(transcriptName);
            if(transcript==null)
                transcript = new Transcript(transcriptName);
            transcript.addFeature(featureType, featureStart, featureEnd);

            //update max starts
            if(geneStart==-1 || featureStart<geneStart)
                geneStart = featureStart;
            if(geneEnd==-1 || featureEnd>geneEnd)
                geneEnd = featureEnd;        
            if("CDS".equals(featureType)){
                if(cdsStart==-1 || featureStart<cdsStart)
                    cdsStart = featureStart;
                if(cdsEnd==-1 || featureEnd>cdsEnd)
                    cdsEnd = featureEnd; 
            }
            transcripts.put(transcriptName, transcript);
        }
    }
    
    public void setCanonicalTranscript(String transcript){
        this.canonical_transcript  = transcript;
    }
    
    public void  updateCDSSize(){
        cdsSize = -1;
        Iterator it = transcripts.values().iterator();
        
        while(it.hasNext()){
            Transcript transcript = (Transcript)it.next();
            //if("BRCA1".equals(name))
            //    System.out.println(transcript.name+"\t"+transcript.getUTRSize()+"\t"+transcript.getCDSSize()+"\t"+transcript.getExonSize());
            
            int transcriptCdsSize = transcript.getCDSSize();
            //int transcriptCdsSize = transcript.getTxSize();
            if(transcriptCdsSize>cdsSize)
            {                
                cdsSize = transcriptCdsSize;
                //canonical_transcript = transcript.name;
            }
        }  
//        return canonical_transcript;
    }
}

class Transcript{
    String name;
    public int txStart = -1, txEnd = -1, cdsStart = -1, cdsEnd = -1;
    public int utrStart = -1, utrEnd=-1, exonStart=-1, exonEnd =-1;
    
    ArrayList features = new ArrayList();
        
    public Transcript(String name){        
        this.name = name;
    }
    
    public int getTxSize(){
        return txEnd - txStart;
    }
    public int getCDSSize(){
        return cdsEnd - cdsStart;
    }
    public int getUTRSize(){
        return utrEnd - utrStart;
    }
    public int getExonSize(){
        return exonEnd - exonStart;
    }
    
    public void addFeature(String type, int start, int end){
        features.add(new Feature(type, start, end));
        if(txStart==-1 || start<txStart)
            txStart = start;
        if(txEnd==-1 || end>txEnd)
            txEnd = end;        
        if("CDS".equals(type)){
            if(cdsStart==-1 || start<cdsStart)
                cdsStart = start;
            if(cdsEnd==-1 || end>cdsEnd)
                cdsEnd = end; 
        }        
        else if("exon".equals(type)){
            if(exonStart==-1 || start<exonStart)
                exonStart = start;
            if(exonEnd==-1 || end>exonEnd)
                exonEnd = end; 
        }
        else if("UTR".equals(type)){
            if(utrStart==-1 || start<utrStart)
                utrStart = start;
            if(utrEnd==-1 || end>utrEnd)
                utrEnd = end; 
        }
    }
    
    public int getFeatureCount(){
        return features.size();
    }
    
    public String toString(){
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        StringBuilder sb3 = new StringBuilder();
        sb1.append(features.size()).append("\t");
        Collections.sort(features);
        for(int i=0; i<features.size(); i++){
            Feature f = (Feature)features.get(i);
            sb1.append(f.type);
            sb2.append(f.start);
            sb3.append(f.end);
            
            if(i<features.size()-1){
                sb1.append(",");
                sb2.append(",");
                sb3.append(",");            
            }
            else{
                sb1.append("\t");
                sb2.append("\t");
            }            
        }
        return sb1.append(sb2).append(sb3).toString();
    }

   
}

class Feature implements Comparable{
    public String type;
    public int start, end;
    
    public Feature(String type, int start, int end){
        this.type = type;
        this.start = start;
        this.end = end;
    }
    
     @Override
    public int compareTo(Object o) {
        if(o instanceof Feature)
            return this.start - ((Feature)o).start;
        else return 0;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
