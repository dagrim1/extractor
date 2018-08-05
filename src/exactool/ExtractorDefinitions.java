/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool;

/**
 *
 * @author flip
 */
public class ExtractorDefinitions 
{    
    public final static String KEY_USER_LAF = "key_user_laf";
    public static String USER_LAF = "Windows";
    
    public final static String KEY_LARGE_VARIANT_SIZE = "key_large_variant_size";//
    public final static String KEY_PATH_VCF = "key_path_vcf"; //location of vcf file used
    public final static String KEY_PATH_DATAFILES="key_path_datafiles"; //path_datafiles = null, 
    public final static String KEY_PATH_CACHED="key_path_cached";
    public final static String KEY_PATH_COVERAGE="key_path_coverage";
    public final static String KEY_GENE_SUMMARY_FILE="gene_summary_file";
    
    public final static String EXAC_WEB_CACHE_EXTENSION = "_exac_web.cache.gz";
    public final static String GNOMAD_WEB_CACHE_EXTENSION = "_gnomad_web.cache.gz";
    public final static String VCF_CACHE_EXTENSION = "_vcf.cache.gz";
    
    public static String separator = System.getProperty("file.separator");    
    public static String delimiter = "\t";
    //path and files with custom datafiles for gene and transcript to chr lookup, and lookup of features in specific chr file
    
    //public static String datafiles_path = "H:\\ExAC FTP\\release0.3.1\\raw_data\\gene_data";    
    public static String DATAFILES_PATH = System.getProperty("user.dir")+System.getProperty("file.separator")+"raw_data\\gene_data";       
    public final static String FEATURE_CHR_FILE = "Homo_sapiens.GRCh37.75.chr<chr>.gz";
    public final static String FEATURE_CHR_FILE_CHECK = "Homo_sapiens.GRCh37.75.chr1.gz";
    public final static String GENE_TO_CHR_FILE = "Homo_sapiens.GRCh37.75.gene.chr.gz";
    public final static String TRANSCRIPT_TO_CHR_FILE = "Homo_sapiens.GRCh37.75.transcript.chr.gz";
    
    //gene summary file
    public static String GENE_SUMMARY_FILE = System.getProperty("user.dir")+System.getProperty("file.separator")+"refseq_gene_summ.gz";       
    

    //path and name of vcf file to be used...
    //public static String vcf_path = "H:\\ExAC FTP\\release0.3.1\\raw_data";
    public static String VCF_PATH = System.getProperty("user.dir")+System.getProperty("file.separator")+"raw_data";
    public final static String VCF_FILE = "ExAC.r0.3.1.sites.vep.ann.subset.txt.gz";
    
    //coverage files
    //public static String coverage_path = "H:\\ExAC FTP\\release0.3.1\\raw_data\\coverage\\";
    public static String COVERAGE_PATH = System.getProperty("user.dir")+System.getProperty("file.separator")+"raw_data\\coverage";
    public final static String COVERAGE_FILE = "Panel.chr<chr>.coverage.txt.gz"; //Panel.chr1.coverage.txt.gz
    public final static String COVERAGE_FILE_CHECK = "Panel.chr1.coverage.txt.gz"; //Panel.chr1.coverage.txt.gz
    
    //cached_data
    public static String CACHED_PATH = System.getProperty("user.dir")+System.getProperty("file.separator")+"cached_data";
    
    //default values for test runs
    public static String DEFAULT_GENE = "PCSK9";
    public static String DEFAULT_REGION = "1:55505220-55530525";
    public static String DEFAULT_SEARCH = DEFAULT_GENE;
    
    public static final int GENE_SEARCH = 1;
    public static final int TRANSCRIPT_SEARCH = 2;
    public static final int REGION_SEARCH = 3;
    public static final int VARIANT_SEARCH = 4;
    
    public static String getCoverageFileForChr(String chr){
        return COVERAGE_PATH+separator+COVERAGE_FILE.replace("<chr>", chr);
    }
    
    public static String getFeatureFileForChr(String chr){
        return DATAFILES_PATH+separator+FEATURE_CHR_FILE.replace("<chr>", chr);
    }
    
    public static String getGeneToChrFile(){
        return DATAFILES_PATH+separator+GENE_TO_CHR_FILE;
    }
    
    public static String getTranscriptToChrFile(){
        return DATAFILES_PATH+separator+TRANSCRIPT_TO_CHR_FILE;
    }
    
    public static String getVcfFile(){
        return VCF_PATH+separator+VCF_FILE;
    }
    
    public static String getGeneSummaryFile(){
        return GENE_SUMMARY_FILE;
    }

}
