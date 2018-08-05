/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.ui;

import exactool.ExtractorDefinitions;
import exactool.datahandler.BaseQuerier;
import exactool.datahandler.ExacQuerier;
import exactool.datahandler.GnomadQuerier;
import exactool.datahandler.GzipReader;
import exactool.datahandler.GzipUtility;
import exactool.datahandler.dataobjects.CombinedVariant;
//import exactool.datahandler.VCFAnnQuerier;
//import exactool.datahandler.VCFQuerier;
import exactool.datahandler.dataobjects.SearchData;
import exactool.datahandler.dataobjects.Variant;
import exactool.datahandler.dataobjects.pop_base;
import exactool.misc.GTFRewriter;
import javax.swing.JFileChooser;
import java.io.*;
import java.util.*;
import gnu.trove.map.hash.THashMap;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.zip.GZIPInputStream;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.SheetData;
/**
 * 
 * @author flip
 */
public class ExacToolFrame extends javax.swing.JFrame {

    public static final String VERSION_NUMBER = "v1.0.180508.1431";
    JFileChooser chooser;//
    File inputFile;//, outputFile;
    HashMap<String, String> summaryData = null;    
    
    static String cache_extension = ExtractorDefinitions.EXAC_WEB_CACHE_EXTENSION;
    
    String defaultText = "Or enter search term(s) here...";    
    
    HashSet major_cons = new HashSet();
    HashSet populations = new HashSet();
        
    THashMap<String, SearchData> searchResultMap =  new THashMap();       
    //boolean appendMode = false;
        
    public final static int ALL=0;
    public final static int MISSENSE_LOF=1;    
    public final static int LOF=2;    
    public static int LOF_FILTER=MISSENSE_LOF;
    
    //filter settings used for variant display
    public static boolean LOCAL_MODE = false, NORS = true, PASS=true, UNIQUES=true, INCL_EXOME=true, INCL_GENOME=false, INCL_INDEL=true, INCL_SNP=true, ALL_POPS=true, CANONICAL_ONLY=false, CONSEQUENCE_ONLY=false, REGIONS_ONLY=true;
    public static HashSet selectedAnnotations=new HashSet(), selectedPopulations=new HashSet();
       
    //tables and models used to display data retrieved...
    ExacTable variantTable = new ExacTable();
    ExacTable coverageTable = new ExacTable();    
    ExacTable featureTable = new ExacTable();    
        
    QuickFilterTablePanel variantPanel = new QuickFilterTablePanel();
    QuickFilterTablePanel coveragePanel = new QuickFilterTablePanel();
    QuickFilterTablePanel featurePanel = new QuickFilterTablePanel();
    
    DefaultTableModel variantModel = new DefaultTableModel();
    DefaultTableModel coverageModel = new DefaultTableModel();
    DefaultTableModel featureModel = new DefaultTableModel();
     
    GeneVisualizationPanel gvp;// = new GeneVisualizationPanel();
    
    //preferences
    public static Preferences prefs = Preferences.userNodeForPackage(exactool.ui.ExacToolFrame.class);
    
    //default is Exac mode
    BaseQuerier querier = new ExacQuerier();
    
    //public static HashSet filteredIds = new HashSet();
    int uniqueIdIndex = -1;
    boolean recursive_call = false;
    
    /**
     * Creates new form ExacToolFrame
     */
    public ExacToolFrame() {
        initComponents(); 
        
           
        this.setTitle("FlipTastic EXtrACtor "+VERSION_NUMBER+" - Use at your own risk, no guarantees!    -    Made by Flip Mulder - UMC Utrecht");         
        
        localBox.setVisible(false);
        snpsBox.setVisible(false);
        
        gvp = new GeneVisualizationPanel(this);
        
        searchField.setText(defaultText);                
        missenseLofBox.setName("missenseLofBox");        
        
        popButtonPanel.setVisible(false);
        annotButtonPanel.setVisible(false);
        popMatchBox.setVisible(false);
        sepPopBox.setVisible(false);
        progressBar.setVisible(false);        
        this.setSize(1024,768);        
        this.setLocationRelativeTo(null);            
        
        //variant table contains all the queried variants
        variantTable.columnToolTips = querier.VARIANT_COLUMN_TOOLTIPS;
        variantTable.getTableHeader().setReorderingAllowed(false);
        variantTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        variantTable.setModel(variantModel);
        variantTable.setRowSorter(new TableRowSorter(variantModel));          
        variantPanel.setDataTable(variantTable);
        variantPanel.setParentFrame(this);
        resultPane.addTab("Variants", variantPanel);
        
        //coverage table contains the coverage for either the genes, or the regions in case these were specified
        coverageTable.columnToolTips = querier.INFO_COLUMN_TOOLTIPS;        
        coverageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        coverageTable.setModel(coverageModel);
        coverageTable.setRowSorter(new TableRowSorter(coverageModel));                
        coveragePanel.setDataTable(coverageTable);
        coveragePanel.setParentFrame(this);
        resultPane.addTab("Coverage", coveragePanel);
                
        //exon table contains the exons for the queried genes/transcripts and 'desertness' info
        featureTable.columnToolTips = querier.FEATURE_COLUMN_TOOLTIPS;
        featureTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        featureTable.setModel(featureModel);
        featureTable.setRowSorter(new TableRowSorter(featureModel));                
        featurePanel.setDataTable(featureTable);
        featurePanel.setParentFrame(this);        
        featureTable.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent evnt) {
                if(featureTable.getSelectedRow() == -1)
                    JOptionPane.showMessageDialog(null, "Please select a feature row first...");
                else if (evnt.getButton()!=1 && evnt.isControlDown()) {
                    //gvp.setSelectedGene((String)featureTable.getValueAt(featureTable.getSelectedRow(), 0));
                    String id =(String)featureTable.getValueAt(featureTable.getSelectedRow(), 0) ;
                    String gene = (String)featureTable.getValueAt(featureTable.getSelectedRow(), 1);
                    if(!id.equals(gene))
                        id = id+" ("+gene+")";
                    gvp.setSelectedItem(id);
                    resultPane.setSelectedIndex(3); 
                    gvp.gip.zoomToRegionBp((Integer)featureTable.getValueAt(featureTable.getSelectedRow(), 4), (Integer)featureTable.getValueAt(featureTable.getSelectedRow(), 5));
                    
                 }
             }
        });
        resultPane.addTab("Features", featurePanel);
        resultPane.addTab("Gene visualization", gvp);
                
        loadPreferences();  
        
        summaryData = getSummaryData();
        chooser =  new JFileChooser(); 
    }
    
   public void openPdf(String pdf){
        if (Desktop.isDesktopSupported())   
        {   
            InputStream jarPdf = getClass().getClassLoader().getResourceAsStream(pdf);

            try {
                File pdfTemp = new File("Extractor_123.pdf");
                // Extraction du PDF qui se situe dans l'archive
                FileOutputStream fos = new FileOutputStream(pdfTemp);
                while (jarPdf.available() > 0) {
                      fos.write(jarPdf.read());
                }   // while (pdfInJar.available() > 0)
                fos.close();
                // Ouverture du PDF
                Desktop.getDesktop().open(pdfTemp);
            }   // try

            catch (IOException e) {
                System.out.println("error : " + e);
            }   // catch (IOException e)
        }
    }
    public void openHelpFile(){
        
        openPdf("resources/Extractor.pdf");
        if(true)
            return;
        try {
        String fileName = "resources/Extractor.pdf";
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        //File is found
        //System.out.println("File Found : " + file.exists());//
         if (Desktop.isDesktopSupported())
            {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(file);
            }
        //Read File Content
        //String content = new String(Files.readAllBytes(file.toPath()));
        //System.out.println(content);
         } catch (Exception e1) {
            e1.printStackTrace();
        }            
    }
    public void setLookAndFeel(String laf){
        try {       
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) 
                if(info.getName().equals(laf))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());            
                    SwingUtilities.updateComponentTreeUI(this);
                }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ExacToolFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(ExacToolFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ExacToolFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ExacToolFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
    /**
     * Restores the settings used before previous shutdown of the application
     */
    public void loadPreferences(){
        passBox.setSelected(prefs.getBoolean(passBox.getText(), true));
        uniquesBox.setSelected(prefs.getBoolean(uniquesBox.getText(), false));
        rsIdBox.setSelected(prefs.getBoolean(rsIdBox.getText(), false));
        missenseLofBox.setSelectedItem(prefs.get(missenseLofBox.getName(), "All"));        
        popMatchBox.setSelected(prefs.getBoolean(popMatchBox.getText(), true));        
        sepPopBox.setSelected(prefs.getBoolean(sepPopBox.getText(), true));        
        useCachedBox.setSelected(prefs.getBoolean(useCachedBox.getText(), false));               
        saveBox.setSelected(prefs.getBoolean(saveBox.getText(), false));        
        gnomadBox.setSelected(prefs.getBoolean(gnomadBox.getText(), false));        
        genomeBox.setSelected(prefs.getBoolean(genomeBox.getText(), true));        
        indelBox.setSelected(prefs.getBoolean(indelBox.getText(), true));        
        exomeBox.setSelected(prefs.getBoolean(exomeBox.getText(), true));        
        
        
        
        ExtractorDefinitions.USER_LAF = prefs.get(ExtractorDefinitions.KEY_USER_LAF, ExtractorDefinitions.USER_LAF);
        ExtractorDefinitions.DATAFILES_PATH = prefs.get(ExtractorDefinitions.KEY_PATH_DATAFILES, ExtractorDefinitions.DATAFILES_PATH);
        ExtractorDefinitions.CACHED_PATH = prefs.get(ExtractorDefinitions.KEY_PATH_CACHED, ExtractorDefinitions.CACHED_PATH);
        
        //ExtractorDefinitions.VCF_PATH = prefs.get(ExtractorDefinitions.KEY_PATH_VCF, ExtractorDefinitions.VCF_PATH);
        ExtractorDefinitions.COVERAGE_PATH = prefs.get(ExtractorDefinitions.KEY_PATH_COVERAGE, ExtractorDefinitions.COVERAGE_PATH);
        ExtractorDefinitions.GENE_SUMMARY_FILE = prefs.get(ExtractorDefinitions.KEY_GENE_SUMMARY_FILE, ExtractorDefinitions.GENE_SUMMARY_FILE);
        
        setLookAndFeel(ExtractorDefinitions.USER_LAF);

        if(!checkPaths(true))
            System.exit(0);
        // vcf support                
        localBox.setSelected(prefs.getBoolean(localBox.getText(), false));
        if(localBox.isSelected()) localBoxActionPerformed(null);        
        gnomadBoxActionPerformed(null);
    }
    
    private boolean checkPaths(boolean init){
        if(! new File(ExtractorDefinitions.CACHED_PATH).exists() 
                || ! new File(ExtractorDefinitions.GENE_SUMMARY_FILE).exists()
                //|| ! new File(ExtractorDefinitions.DATAFILES_PATH).exists()
                //|| ! new File(ExtractorDefinitions.VCF_PATH).exists()
                //|| ! new File(ExtractorDefinitions.COVERAGE_PATH).exists()                
                )        
        {            
            if(init){
                if(! new File(ExtractorDefinitions.CACHED_PATH).exists())
                    JOptionPane.showMessageDialog(this, "No valid cache folder specified, caching will not work without this!");
                if(! new File(ExtractorDefinitions.GENE_SUMMARY_FILE).exists())
                    JOptionPane.showMessageDialog(this, "No valid gene summary file specified, no gene data will be displayed without this!");
                
                preferencesItemActionPerformed(null);
                return checkPaths(false);
            }            
        }
        return true;
        
    }
    
    /*
    private boolean checkDataFiles(){
        localBox.setSelected(false);
        String dataFile = ExtractorDefinitions.getVcfFile();
        String vcfPath = ExtractorDefinitions.VCF_PATH;
        if(!new File(dataFile).exists())
        {
            JOptionPane.showMessageDialog(this, "Local mode selected but datafile not found, please locate '"+ExtractorDefinitions.VCF_FILE+"' file...");                        
            dataFile = selectFile(false);            
            
            //dataFile = vcfPath+ExtractorDefinitions.delimiter+ExtractorDefinitions.vcf_file;
            if(dataFile != null && new File(dataFile).exists())
            {
                vcfPath = new File(dataFile).getParent();
                ExtractorDefinitions.VCF_PATH = vcfPath;                
                prefs.put(ExtractorDefinitions.KEY_PATH_VCF, vcfPath);
            }   
            else
            {
                JOptionPane.showMessageDialog(this, "'"+ExtractorDefinitions.VCF_FILE+"' not found in specified directory. Reverting to webpage mode.");
                localBox.setSelected(false);
                localBoxActionPerformed(null);
                return false;
            }
        }
            
        //Assume if one exists, all exist
        String dataPath = "";
        dataFile = ExtractorDefinitions.getGeneToChrFile();
        if(!new File(dataFile).exists())
        {
            //retry with vcf path setting used above
            dataPath = vcfPath+System.getProperty("file.separator")+"gene_data";
            ExtractorDefinitions.DATAFILES_PATH = dataPath;
            dataFile = ExtractorDefinitions.getGeneToChrFile();
        }
        
        if(!new File(dataFile).exists())
        {
            //vcfPath + gene_data
            JOptionPane.showMessageDialog(this, "VCF mode selected but genedata folder not found, please locate geneda folder (Containing the '"+ExtractorDefinitions.GENE_TO_CHR_FILE+"' file...");
            dataPath = selectFile(true);
            ExtractorDefinitions.DATAFILES_PATH = dataPath;
                        
            if(new File(ExtractorDefinitions.getGeneToChrFile()).exists())
            {                
                prefs.put(ExtractorDefinitions.KEY_PATH_DATAFILES, dataPath);
            }   
            else
            {
                ExtractorDefinitions.DATAFILES_PATH = prefs.get(ExtractorDefinitions.KEY_PATH_DATAFILES, null);
                JOptionPane.showMessageDialog(this, "'"+ExtractorDefinitions.GENE_TO_CHR_FILE+"' could not be found in specified directory. Reverting to webpage mode.");
                localBox.setSelected(false);
                localBoxActionPerformed(null);
                return false;
            }
        }
        else{
            prefs.put(ExtractorDefinitions.KEY_PATH_DATAFILES, dataPath);
        }
        
        //check for coverage file
        dataFile = ExtractorDefinitions.getCoverageFileForChr("1");
        String coveragePath  = null;
        if(!new File(dataFile).exists())
        {
            coveragePath = vcfPath+System.getProperty("file.separator")+"coverage";
            ExtractorDefinitions.COVERAGE_PATH = coveragePath;
            dataFile = ExtractorDefinitions.getCoverageFileForChr("1");
            //try vcfPath as base
        }
        if(!new File(dataFile).exists())
        {
            JOptionPane.showMessageDialog(this, "VCF mode selected but coverage folder not found, please locate coverage folder...");
            coveragePath = selectFile(true);
            ExtractorDefinitions.COVERAGE_PATH = coveragePath;            
            if(new File(ExtractorDefinitions.getCoverageFileForChr("1")).exists())
            {   
                prefs.put(ExtractorDefinitions.KEY_PATH_COVERAGE, coveragePath);
            }   
            else
            {
                ExtractorDefinitions.COVERAGE_PATH = prefs.get(ExtractorDefinitions.KEY_PATH_COVERAGE, null);          
                JOptionPane.showMessageDialog(this, "'"+ExtractorDefinitions.getCoverageFileForChr("1")+"' could not be found in specified directory. Reverting to webpage mode.");
                localBox.setSelected(false);
                localBoxActionPerformed(null);
                return false;
            }
        }
        else{
            coveragePath = ExtractorDefinitions.COVERAGE_PATH;
            prefs.put(ExtractorDefinitions.KEY_PATH_COVERAGE, coveragePath);
        }
        //all is found, set to true
        localBox.setSelected(true);
        return true;
    }
    */
    
    public String selectFile(boolean folderOnly){
        String result = null;
        try{
            if(folderOnly)
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                result = chooser.getSelectedFile().getPath();
            }            
        }
        catch(Exception e){
            e.printStackTrace();
        }
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        return result;
    }
        
    /**
     * Selects the specified variant in the variant table
     * @param id 
     */
    public void selectVariant(String id){
        for(int r=0; r<variantTable.getRowCount(); r++){
            int exac_id_index = querier.getVarIndex(querier.OUTPUT_HEADER_EXAC_ID);
            if(id.equals(variantTable.getValueAt(r, exac_id_index))){
                //System.out.println("Match at: "+r);
                variantTable.setRowSelectionInterval(r, r);
                variantTable.getSelectionModel().setSelectionInterval(r, r);
                variantTable.scrollRectToVisible(new Rectangle(variantTable.getCellRect(r, 0, true)));
                resultPane.setSelectedIndex(0);
                break;
            }
        }
    }
  
    /**
     * Remembers and restores user preferences (program settings wrt filters)
     * @param source   The Component triggering this save
     */
    public void savePref(Object source){
        if(source instanceof JCheckBox){
            String key = ((JCheckBox)source).getText();
            boolean value = ((JCheckBox)source).isSelected();
            prefs.putBoolean(key, value);
        }
        else if(source instanceof JComboBox){
            String key = ((JComboBox)source).getName();
            String value = ((JComboBox)source).getSelectedItem().toString();
            prefs.put(key, value);
        }
        else{
            System.out.println("Unknown trigger source: "+source.toString());
        }
    }
    
    /**
     * Clears current data, tables and filters...
     */
    private void clearData(){
        if(variantModel.getRowCount()>0)
        for(int r=variantModel.getRowCount()-1; r>=0; r--)
            variantModel.removeRow(r);
        if(coverageModel.getRowCount()>0)
        for(int r=coverageModel.getRowCount()-1; r>=0; r--)
            coverageModel.removeRow(r);
        if(featureModel.getRowCount()>0)
        for(int r=featureModel.getRowCount()-1; r>=0; r--)
            featureModel.removeRow(r);
        
        searchResultMap.clear();        
        selectedAnnotations.clear();        
        selectedPopulations.clear();
        infoLabel.setText("");
        major_cons.clear();
        populations.clear();

        updateFilters(true);            
    }
      
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        inputButton = new javax.swing.JButton();
        inputField = new javax.swing.JTextField();
        queryButton = new javax.swing.JButton();
        resultPane = new javax.swing.JTabbedPane();
        infoLabel = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        saveBox = new javax.swing.JCheckBox();
        extendedFilterPanel = new javax.swing.JPanel();
        globalFilterPanel = new javax.swing.JPanel();
        missenseLofBox = new javax.swing.JComboBox<>();
        passBox = new javax.swing.JCheckBox();
        uniquesBox = new javax.swing.JCheckBox();
        rsIdBox = new javax.swing.JCheckBox();
        canonicalBox = new javax.swing.JCheckBox();
        consequenceBox = new javax.swing.JCheckBox();
        regionsBox = new javax.swing.JCheckBox();
        exomeBox = new javax.swing.JCheckBox();
        genomeBox = new javax.swing.JCheckBox();
        indelBox = new javax.swing.JCheckBox();
        snpsBox = new javax.swing.JCheckBox();
        annotFilterScrollPane = new javax.swing.JScrollPane();
        annotFilterPanel = new javax.swing.JPanel();
        annotButtonPanel = new javax.swing.JPanel();
        annotAllButton = new javax.swing.JButton();
        annotNoteButton = new javax.swing.JButton();
        annotInvButton = new javax.swing.JButton();
        popFilterScrollPane = new javax.swing.JScrollPane();
        popFilterPanel = new javax.swing.JPanel();
        popButtonPanel = new javax.swing.JPanel();
        popAllButton = new javax.swing.JButton();
        popNoneButton = new javax.swing.JButton();
        popInvButton = new javax.swing.JButton();
        popMatchBox = new javax.swing.JCheckBox();
        gnomadBox = new javax.swing.JCheckBox();
        searchField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        useCachedBox = new javax.swing.JCheckBox();
        localBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        sepPopBox = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        saveItem = new javax.swing.JMenuItem();
        exitItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        preferencesItem = new javax.swing.JMenuItem();
        geneDataItem = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        helpItem = new javax.swing.JMenuItem();
        logItem = new javax.swing.JMenuItem();
        aboutItem = new javax.swing.JMenuItem();

        jCheckBox1.setText("jCheckBox1");

        jCheckBox2.setText("jCheckBox2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FlipTastic EXtrACtor - Use at your own risk, no guarantees!    -    Made by Flip Mulder - UMC Utrecht (180228-1540)");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        inputButton.setText("Browse...");
        inputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputButtonActionPerformed(evt);
            }
        });

        inputField.setEditable(false);
        inputField.setText("Please choose gene/transcript list...");

        queryButton.setText("Query");
        queryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryButtonActionPerformed(evt);
            }
        });

        resultPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                resultPaneStateChanged(evt);
            }
        });
        resultPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                resultPaneMouseClicked(evt);
            }
        });

        infoLabel.setText("Nothing loaded...");

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        saveBox.setText("Save raw query data");
        saveBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBoxActionPerformed(evt);
            }
        });

        extendedFilterPanel.setLayout(new java.awt.GridLayout(1, 3));

        globalFilterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Global Filters: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        globalFilterPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        missenseLofBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Missense+LoF", "LoF" }));
        missenseLofBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                missenseLofBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(missenseLofBox);

        passBox.setSelected(true);
        passBox.setText("PASS");
        passBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(passBox);

        uniquesBox.setText("UNIQUES (Allele count = 1)");
        uniquesBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uniquesBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(uniquesBox);

        rsIdBox.setText("WITHOUT RSID");
        rsIdBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rsIdBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(rsIdBox);

        canonicalBox.setText("CANONICAL ANNOTATIONS ONLY");
        canonicalBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canonicalBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(canonicalBox);

        consequenceBox.setText("CONSEQUENCE ONLY");
        consequenceBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consequenceBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(consequenceBox);

        regionsBox.setSelected(true);
        regionsBox.setText("SPECIFIED REGIONS ONLY (IF ANY)");
        regionsBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regionsBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(regionsBox);

        exomeBox.setSelected(true);
        exomeBox.setText("EXOME DATA");
        exomeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exomeBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(exomeBox);

        genomeBox.setSelected(true);
        genomeBox.setText("GENOME DATA");
        genomeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genomeBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(genomeBox);

        indelBox.setSelected(true);
        indelBox.setText("INDELS");
        indelBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indelBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(indelBox);

        snpsBox.setSelected(true);
        snpsBox.setText("SNPS");
        snpsBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snpsBoxActionPerformed(evt);
            }
        });
        globalFilterPanel.add(snpsBox);

        extendedFilterPanel.add(globalFilterPanel);

        annotFilterScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Annotation Filter: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        annotFilterPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        annotButtonPanel.setMaximumSize(new java.awt.Dimension(32767, 50));
        annotButtonPanel.setPreferredSize(new java.awt.Dimension(240, 23));
        annotButtonPanel.setLayout(new java.awt.GridLayout(1, 3));

        annotAllButton.setText("All");
        annotAllButton.setPreferredSize(new java.awt.Dimension(80, 23));
        annotAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                annotAllButtonActionPerformed(evt);
            }
        });
        annotButtonPanel.add(annotAllButton);

        annotNoteButton.setText("None");
        annotNoteButton.setPreferredSize(new java.awt.Dimension(80, 23));
        annotNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                annotNoteButtonActionPerformed(evt);
            }
        });
        annotButtonPanel.add(annotNoteButton);

        annotInvButton.setText("Invert");
        annotInvButton.setPreferredSize(new java.awt.Dimension(80, 23));
        annotInvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                annotInvButtonActionPerformed(evt);
            }
        });
        annotButtonPanel.add(annotInvButton);

        annotFilterPanel.add(annotButtonPanel);

        annotFilterScrollPane.setViewportView(annotFilterPanel);

        extendedFilterPanel.add(annotFilterScrollPane);

        popFilterScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Population Filter: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        popFilterPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 2));

        popButtonPanel.setMaximumSize(new java.awt.Dimension(32767, 50));
        popButtonPanel.setPreferredSize(new java.awt.Dimension(240, 23));
        popButtonPanel.setLayout(new java.awt.GridLayout(1, 3));

        popAllButton.setText("All");
        popAllButton.setPreferredSize(new java.awt.Dimension(80, 23));
        popAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popAllButtonActionPerformed(evt);
            }
        });
        popButtonPanel.add(popAllButton);

        popNoneButton.setText("None");
        popNoneButton.setPreferredSize(new java.awt.Dimension(80, 23));
        popNoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popNoneButtonActionPerformed(evt);
            }
        });
        popButtonPanel.add(popNoneButton);

        popInvButton.setText("Invert");
        popInvButton.setPreferredSize(new java.awt.Dimension(80, 23));
        popInvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popInvButtonActionPerformed(evt);
            }
        });
        popButtonPanel.add(popInvButton);

        popFilterPanel.add(popButtonPanel);

        popFilterScrollPane.setViewportView(popFilterPanel);

        extendedFilterPanel.add(popFilterScrollPane);

        popMatchBox.setSelected(true);
        popMatchBox.setText("Present in all populations (if deselected, present in any)");
        popMatchBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popMatchBoxActionPerformed(evt);
            }
        });

        gnomadBox.setText("Use GNOMAD");
        gnomadBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gnomadBoxActionPerformed(evt);
            }
        });

        searchField.setText("Or enter search term here...");
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                searchFieldFocusGained(evt);
            }
        });
        searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFieldActionPerformed(evt);
            }
        });

        jLabel1.setText("Search term: ");

        useCachedBox.setText("Use cached data if available");
        useCachedBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useCachedBoxActionPerformed(evt);
            }
        });

        localBox.setText("Local Mode");
        localBox.setEnabled(false);
        localBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Settings: ");

        sepPopBox.setSelected(true);
        sepPopBox.setText("Write output per population");
        sepPopBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sepPopBoxActionPerformed(evt);
            }
        });

        jMenu1.setText("File");

        saveItem.setText("Save As...");
        saveItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveItemActionPerformed(evt);
            }
        });
        jMenu1.add(saveItem);

        exitItem.setText("Quit");
        exitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        jMenu1.add(exitItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        preferencesItem.setText("Preferences...");
        preferencesItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferencesItemActionPerformed(evt);
            }
        });
        jMenu2.add(preferencesItem);

        geneDataItem.setText("Create GenData from gtf.gz");
        geneDataItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geneDataItemActionPerformed(evt);
            }
        });
        jMenu2.add(geneDataItem);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Help");

        helpItem.setText("Help...");
        helpItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpItemActionPerformed(evt);
            }
        });
        jMenu3.add(helpItem);

        logItem.setText("Log");
        logItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logItemActionPerformed(evt);
            }
        });
        jMenu3.add(logItem);

        aboutItem.setText("About");
        aboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutItemActionPerformed(evt);
            }
        });
        jMenu3.add(aboutItem);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(292, 292, 292)
                        .addComponent(sepPopBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(popMatchBox, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(queryButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(inputButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(gnomadBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useCachedBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(localBox)
                                .addGap(0, 185, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(inputField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(resultPane)
                    .addComponent(extendedFilterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inputButton)
                    .addComponent(inputField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gnomadBox)
                    .addComponent(saveBox)
                    .addComponent(useCachedBox)
                    .addComponent(localBox)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(queryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultPane, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(infoLabel)
                    .addComponent(popMatchBox)
                    .addComponent(sepPopBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(extendedFilterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName("FlipTastic EXtrACtor - Use at your own risk, no guarantees!    -    Made by Flip Mulder - UMC Utrecht (161021)");

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    /**
     * Main querying done when pressing the query button
     * Reads file with search terms, searches for these and processes results
     * @param evt 
     */
    private void queryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryButtonActionPerformed
        processSearchTerms(false);        
    }//GEN-LAST:event_queryButtonActionPerformed
        
    private void processSearchTerms(boolean fromTextField){
        try
        {          
            //clear any current data
            clearData();            
           
            //keep track of search terms and regions
            final ArrayList orderedSearchTerms = new ArrayList();
            final HashMap<String, ArrayList> geneRegionMap = new HashMap();
            
            //if input file is specified, fill search terms and regions based on first 2 columns of file (tab delimited)
            if(!fromTextField && inputFile!=null){
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                String line = null;                
                while ( (line=reader.readLine()) !=null){
                    String tokens[] = line.trim().split("\t");
                    String gene= tokens[0];
                    orderedSearchTerms.add(gene);
                    ArrayList regions = geneRegionMap.get(gene);
                    if(regions == null)
                        regions = new ArrayList();                    
                    if(tokens.length>1)
                        regions.add(tokens[1]);
                    
                    geneRegionMap.put(gene, regions);
                    
                }                
                reader.close();
            }
            else
            {
                //else either use default terms or terms entered in search textfield                
                if(searchField.getText().equals("") || defaultText.equals(searchField.getText())){
                    searchField.setText(defaultText);
                    
                    orderedSearchTerms.add(ExtractorDefinitions.DEFAULT_GENE);
                    ArrayList regionList = new ArrayList();
                    if(ExtractorDefinitions.DEFAULT_GENE.indexOf(":")>0 && ExtractorDefinitions.DEFAULT_GENE.indexOf("-")>0)
                        regionList.add(ExtractorDefinitions.DEFAULT_GENE);
                    geneRegionMap.put(ExtractorDefinitions.DEFAULT_GENE, regionList);                    
                    //orderedGenes.add("BRCA1"); geneRegionMap.put("BRCA1", new ArrayList());
                }
                else{
                    String tokens[] = searchField.getText().split(";");
                    for(int t=0; t<tokens.length; t++){
                        String gene = tokens[t].trim();
                        orderedSearchTerms.add(gene);
                        geneRegionMap.put(gene, new ArrayList());
                    }
                }
            }
            
            final ProgressDialog pd = new ProgressDialog(this, true);
            //pd.setVisible(true);
            Thread t2 = new Thread(new Runnable() {
                public void run() {
                  pd.setVisible(true);
                }
              });
              t2.start();
            
            //process actual querying in seperate thread
            Thread t = new Thread()
            {                
                public void run() 
                {
                    LogDialog.log = "";
                    //check if new query is required or cached data is to be used...
                    boolean useCached = useCachedBox.isSelected() && useCachedBox.isEnabled();
                    boolean saveCache = saveBox.isSelected() && saveBox.isEnabled();
                    //boolean showOverwrite = true, write = true;
                    File rawFolder = null;
                    
                    try
                    {
                        if(useCached || saveCache)
                        {
                            //rawFolder = new File(raw_dir); //only relevant for http querying?
                            rawFolder = new File(ExtractorDefinitions.CACHED_PATH); //only relevant for http querying?
                            //System.out.println("Cache folder: "+ExtractorDefinitions.CACHED_PATH);
                            if(saveCache && !rawFolder.exists())
                                rawFolder.mkdir();                                                                                        
                        }
                        
                        //appendMode = false;
                        
                        
                        progressBar.setVisible(true);
                        progressBar.setStringPainted(true);
                        progressBar.setIndeterminate(true);
                        
                        //go through terms
                        for(int i=0; i<orderedSearchTerms.size(); i++)
                        {
                            String searchTerm = (String)orderedSearchTerms.get(i);
                            pd.setLabel("Processing '"+searchTerm+"' ("+(i+1)+"/"+orderedSearchTerms.size()+")");
                            progressBar.setString("Processing searchterm "+(i+1)+"/"+orderedSearchTerms.size());                                       
                            //ExacData geneData = searchResultMap.get(searchTerm);
                            SearchData geneData = searchResultMap.get(searchTerm);
                            querier.cachedData = null;
                            
                            //region search contains illegal charactor for cached storage, replace illegal character...
                            String cachedTerm = searchTerm;
                            if(searchTerm.contains(":"))
                                cachedTerm = searchTerm.replaceAll(":", "_");
                            
                            File cachedFile = new File(rawFolder+System.getProperty("file.separator")+cachedTerm+cache_extension);
                            
                            if(geneData==null)
                            {   
                                geneData = new SearchData(searchTerm); //GeneData object with just genename
                                                                
                                 //either use cached data or query new data, storing raw data in rawResult                                
                                if(useCached && cachedFile.exists())// && !saveCache)
                                    querier.cachedData = GzipUtility.readGzipFileContent(cachedFile.getPath());                                                                     
                                
                                querier.prepareLocalData(searchTerm);                                
                                //System.out.println("CachedData: "+querier.cachedData);
                                if(querier.cachedData!=null && !"".equals(querier.cachedData))
                                {
                                    geneData.genes = querier.queryGenes(searchTerm); //set gene data, double check variant search?                                    
                                    geneData.features = querier.queryFeatures(searchTerm); //no features when searching for region in exac                                    
                                    //geneData.variants = querier.queryVariants(searchTerm);//working
                                    geneData.setCombinedVariants(querier.queryVariants(searchTerm));
                                    geneData.bpCovMap = querier.queryCoverage(searchTerm);       
                                    
                                    geneData.updateGeneInfo();
                                    
                                    double[] avgCov = querier.calculateAvgCoverage(geneData.bpCovMap, null);                                
                                    geneData.avgExomeCoverage = avgCov[0];
                                    geneData.avgGenomeCoverage = avgCov[1];
                                    
                                    geneData.summary = getSummaryData(searchTerm);

                                    //geneDataquerier.convertVariantsToCombined(geneData.variants);
                                    //addFilterEntries(geneData.variants); 
                                    addFilterEntries(geneData);
                                    searchResultMap.put(searchTerm, geneData);
                                }    
                                else{                                    
                                    LogDialog.log = LogDialog.log+searchTerm+"\tNo results found...\n";
                                }
                            }
                            else saveCache = false;
                                
                            ArrayList regions = geneRegionMap.get(searchTerm);
                            for(int r=0; r<regions.size(); r++)                                    
                            {                                
                                String region = (String)regions.get(r);                                

                                if( region.indexOf(":")>0 && region.indexOf("-")>1 && !geneData.regionCovMap.containsKey(region))
                                    geneData.regionCovMap.put(region, querier.calculateAvgCoverage(geneData.bpCovMap, region)[0]);  
                                else if(region.startsWith("p."))
                                    geneData.regionCovMap.put(region, -99.0);                                
                            }                                                    
                            if(saveCache && rawFolder!=null && rawFolder.exists())
                            {                                
                                if(!cachedFile.exists())
                                    GzipUtility.writeGzipFileContent(cachedFile.getPath(), querier.cachedData );
                            }
                            adjustFilters();    
                        }
                        
                        progressBar.setString("Processed genes");
                        progressBar.setIndeterminate(false);
                        progressBar.setVisible(false);
                        
                        pd.setLabel("Processed genes");
                        pd.setVisible(false);
                        pd.dispose();
                        gvp.setGeneList(new Vector(searchResultMap.values()));                        
                        updateDisplay(true);
                        if(LogDialog.log.contains("No results found"))                            
                            JOptionPane.showMessageDialog(null, "<html>There were issues with some search-terms.<br><br>Please check Help -> Log for details.</html>");
                        else JOptionPane.showMessageDialog(null, "Done!");
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }                
            };            
            t.start();            
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }    
    
    /**
     * Updates 'counts' label
     */
    public void updateLabel(){        
        
        int rowCount= 0;
        if(variantPanel!=null)
            rowCount = variantPanel.getDisplayedRowCount();
        /*
        filteredIds.clear();       
        if(variantPanel.isFilterActive())
           for(int r=0;r<variantTable.getRowCount(); r++)
                filteredIds.add(variantTable.getValueAt(r, uniqueIdIndex));
        */
        
        //recursive call...
        
        if(!recursive_call){
            recursive_call = true;
            gvp.updateGeneData();
            recursive_call = false;
        }
        
        String text = rowCount+ " "+resultPane.getTitleAt(0)+".";
        String selectedGene=gvp.getSelectedId();
        if(selectedGene!=null)
            text = text+ searchResultMap.get(selectedGene).getDisplayedData().length+" in "+selectedGene+".";        
        infoLabel.setText(text);     
    }
    
    
    private String getSummaryData(String gene){
        String result = "";
        
        if(summaryData ==null)
            summaryData = getSummaryData();
        if(summaryData.containsKey(gene))
            result = summaryData.get(gene);
        
        return result;
    }
    
    private HashMap getSummaryData(){
        HashMap result = new HashMap();
        
        try{
            //File sumFile = new File( ExtractorDefinitions.DATAFILES_PATH+System.getProperty("file.separator")+"refseq_gene_summ.gz");
            File sumFile = new File( ExtractorDefinitions.GENE_SUMMARY_FILE);
            
            if(sumFile.exists()){   
                BufferedReader reader = null;
                if(sumFile.getName().toLowerCase().endsWith(".gz"))
                {
                    try{      
                        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(sumFile));
                        reader = new BufferedReader(new InputStreamReader(gzip));
                    }
                    catch(java.util.zip.ZipException ze){
                        reader = new BufferedReader(new FileReader(sumFile));
                    }
                }else{
                    reader = new BufferedReader(new FileReader(sumFile));
                }
                
                String line = null;
                
                while ( (line=reader.readLine()) != null){                
                    String tokens[] = line.split("\t");
                    if(tokens.length>1)
                    result.put(tokens[0], tokens[1]);
                }
                reader.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        
        return result;
    }
    
    private HashMap getSummaryDataOrg(){
        HashMap result = new HashMap();
        
        try{
            //File sumFile = new File( ExtractorDefinitions.DATAFILES_PATH+System.getProperty("file.separator")+"refseq_gene_summ.gz");
            File sumFile = new File( ExtractorDefinitions.GENE_SUMMARY_FILE);
            if(sumFile.exists()){                
                GzipReader reader = new GzipReader(sumFile.getPath());                
                String line = null;
                
                while ( (line=reader.readLine()) != null){                
                    String tokens[] = line.split("\t");
                    if(tokens.length>2)
                    result.put(tokens[0], tokens[2]);
                }
                reader.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        
        return result;
    }
    
    private void addFilterEntries(SearchData searchData){
        major_cons.addAll(searchData.getMajorCons());
        populations.addAll(searchData.getPops());
        
    }
    private void addFilterEntries(Variant entries[]){
        for(int i=0; i<entries.length; i++)
        {
            Variant entry = entries[i];
            if(entry.major_consequence!=null)
                major_cons.add(entry.major_consequence);
            populations.addAll(entry.getPops());
            
            
            
            
        }   
        /*
        if(entries.length>0)
        {
            ArrayList pops = entries[0].getPops();
            for(int i=0; i<pops.size(); i++){
                String pop = pops.get(i).toString();
                System.out.println(entries[0].exac_id+": "+pop+" -> "+entries[0].getAlleleCountForPop(pop)+"/"+entries[0].getAlleleNumberForPop(pop)+" = "+entries[0].getFrequencyForPop(pop));
            }        
        }
        */
    }
   
    /**
     * Reinitializes and repopulates the population and annotation filter panels
     */
    private void adjustFilters()
    {
        for(int c=annotFilterPanel.getComponentCount()-1; c>=1; c--)
            annotFilterPanel.remove(c);
        ArrayList major_cons_list = new ArrayList(major_cons);
        Collections.sort(major_cons_list);
        for(int i=0; i<major_cons_list.size(); i++)
        {
            final JCheckBox box = new JCheckBox(major_cons_list.get(i).toString(), true);
            
            box.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {                    
                    updateDisplay();
                    savePref(box);
                }
            });            
            annotFilterPanel.add(box);
            box.setSelected(prefs.getBoolean(box.getText(), true));
            selectedAnnotations.add(major_cons_list.get(i).toString());
        
        for(int c=popFilterPanel.getComponentCount()-1; c>=1; c--)
            popFilterPanel.remove(c);
        }
        ArrayList pop_list = new ArrayList(populations);
        Collections.sort(pop_list);
        for(int i=0; i<pop_list.size(); i++)
        {
            boolean selected = prefs.getBoolean(pop_list.get(i).toString(), true);
            final JCheckBox box = new JCheckBox(pop_list.get(i).toString(), selected);
            box.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {            
                    updateDisplay();
                    savePref(box);
                }
            });            
            popFilterPanel.add(box);
            box.setSelected(prefs.getBoolean(box.getText(), true));
            if(selected)
                selectedPopulations.add(pop_list.get(i).toString());
        }
    }
    
    /**
     * Updates filter settings based on selection in UI
     */
    private void updateFilters(boolean initial){
        selectedPopulations.clear();
        selectedAnnotations.clear();
        
        LOF_FILTER=missenseLofBox.getSelectedIndex();            
        NORS = rsIdBox.isSelected();
        PASS = passBox.isSelected();
        UNIQUES = uniquesBox.isSelected();
        
        INCL_EXOME = exomeBox.isSelected();
        INCL_GENOME = genomeBox.isSelected();
        INCL_INDEL = indelBox.isSelected();
        INCL_SNP = snpsBox.isSelected();
        ALL_POPS = popMatchBox.isSelected();
        REGIONS_ONLY = regionsBox.isSelected();
        
        CANONICAL_ONLY = canonicalBox.isSelected();
        CONSEQUENCE_ONLY = consequenceBox.isSelected();
        
        JCheckBox box = null;
        for(int c=0; c<annotFilterPanel.getComponentCount(); c++){
            if(annotFilterPanel.getComponent(c) instanceof JCheckBox)
            {
                box = (JCheckBox)annotFilterPanel.getComponent(c);
                if(box.isSelected())
                    selectedAnnotations.add(box.getText());
            }
        }
        for(int c=0; c<popFilterPanel.getComponentCount(); c++){
            if(popFilterPanel.getComponent(c) instanceof JCheckBox)
            {
                box = (JCheckBox)popFilterPanel.getComponent(c);
                if(box.isSelected())
                    selectedPopulations.add(box.getText());
            }
        }       
    }
       
    
    private void clearDisplay(){
        for(int i=resultPane.getTabCount()-1; i>=0; i--)
        {
            JScrollPane pane = (JScrollPane)resultPane.getTabComponentAt(i);
            if(pane!=null && pane.getViewport()!=null)
                pane.getViewport().removeAll();
        }
    }
    
    private void displayData(){
        clearDisplay();
        
        if(searchResultMap.size()>0)
        {               
            //must be Vector for DefaultTableModel
            //set headers for variant table            
            Vector variantHeaders = new Vector();
            for(int i=0; i<querier.getVarHeaders().length; i++)
                variantHeaders.add(querier.getVarHeaders()[i]);   
            
            if(CombinedVariant.perPop){
                ArrayList sortedPops = new ArrayList(populations);
                Collections.sort(sortedPops);
                for(int p=0; p<sortedPops.size(); p++){
                    String pop = sortedPops.get(p).toString();
                    variantHeaders.add(pop_base.getAbbreviationForPop(pop)+"_ac");
                    variantHeaders.add(pop_base.getAbbreviationForPop(pop)+"_an");
                    variantHeaders.add(pop_base.getAbbreviationForPop(pop)+"_hom");
                    variantHeaders.add(pop_base.getAbbreviationForPop(pop)+"_frq");
                }
            }
            //check if data should be displayed per population as well
            /*
            ArrayList popSort = null;
            CombinedVariant.perPop = sepPopBox.isSelected();
            if(CombinedVariant.perPop)
            {
                //if so collect pops
                popSort = new ArrayList(pop);
                Collections.sort(popSort);
                //and add relevant columns
                for(int i=0; i<popSort.size(); i++){
                    variantHeaders.add("AC "+popSort.get(i));   
                    variantHeaders.add("AN "+popSort.get(i));   
                    variantHeaders.add("AF "+popSort.get(i));   
                }
            }
            */
            
             //set headers for feature table
            Vector featureHeaders = new Vector();            
            for(int i=0; i<querier.getFeatureHeaders().length; i++)
                featureHeaders.add(querier.getFeatureHeaders()[i]);
            
            //set headers for coverage table
            Vector coverageHeaders = new Vector();            
            for(int i=0; i<querier.getCovHeaders().length; i++)
                coverageHeaders.add(querier.getCovHeaders()[i]);
            
            String searchTerm=null;
            //ExacData searchResult = null;
            SearchData searchResult = null;
            
            //must be Vector for DefaultTableModel
            Vector variantRows = new Vector();       
            Vector coverageRows = new Vector();     
            Vector featureRows = new Vector();
                        
            ArrayList searchTerms = new ArrayList(searchResultMap.keySet());
            Collections.sort(searchTerms);
            //searchTerms = orderedSearchTerms;
            for(int g=0; g<searchTerms.size(); g++)
            {
                searchTerm = (String)searchTerms.get(g);
                searchResult = searchResultMap.get(searchTerm);
                
                //first populate variants table
                Object displayedData[][] = searchResult.getDisplayedData();
                //collect all data if to be displayed in a single tab...
                int rowCount = searchResult.getRowCount();
                for(int r=0; r<rowCount; r++)
                {
                    Vector rowVariant = new Vector();                    
                    
                    for(int c=0; c<displayedData[r].length; c++)
                        rowVariant.add(displayedData[r][c]);      
                    if(BaseQuerier.TEMPLATE_VALUE.equals(rowVariant.get(5)))
                        rowVariant.set(5, searchTerm);
                    if(BaseQuerier.TEMPLATE_VALUE.equals(rowVariant.get(6)))                        
                        rowVariant.set(6, searchResult.getGeneNames());
                    
                    double cov[] = searchResult.bpCovMap.get(displayedData[r][1]);
                    if(cov!=null){
                        rowVariant.add(cov[0]);
                        rowVariant.add(cov[1]);
                    }
                    variantRows.add(rowVariant);
                }                
                
                //next the coverage table
                if(searchResult.regionCovMap.size()>0){
                    Vector regions = new Vector(searchResult.regionCovMap.keySet());
                    java.util.Collections.sort(regions);
                    for(int r=0; r<regions.size(); r++){
                        Vector row = new Vector();
                        row.add(searchTerm);
                        row.add(searchResult.getGeneNames());
                        row.add(searchResult.avgExomeCoverage);
                        row.add(searchResult.avgGenomeCoverage);
                        row.add(regions.get(r));
                        row.add(searchResult.regionCovMap.get(regions.get(r)));
                        row.add(searchResult.summary);
                        coverageRows.add(row);                
                    }
                }
                else{
                    Vector row = new Vector();
                    row.add(searchTerm);
                    row.add(searchResult.getGeneNames());
                    row.add(searchResult.avgExomeCoverage);
                    row.add(searchResult.avgGenomeCoverage);
                    row.add("");
                    row.add("");
                    row.add(searchResult.summary);
                    coverageRows.add(row);       
                }
                
                //and the features table
                //HashMap featureMap = searchResult.getFeatureData(true);
                HashMap featureMap = searchResult.getFeatureData(false);
                ArrayList indices = new ArrayList(featureMap.keySet());
                Collections.sort(indices);
                for(int i=0; i<indices.size(); i++)
                {
                    Vector featureRow = new Vector(Arrays.asList((Object[])featureMap.get(indices.get(i))));
                    featureRow.add(0, searchTerm);                                        
                    featureRow.set(1, searchResult.getGeneNames());
                    featureRows.add(featureRow);
                }
            }            
            List sortKeys = variantTable.getRowSorter().getSortKeys();
            //System.out.println("Sortkeys before: "+variantTable.getRowSorter().getSortKeys());                 
            variantModel.setDataVector(variantRows, variantHeaders);
            variantTable.setAutoCreateColumnsFromModel( false );       
            //System.out.println("Sortkeys after: "+variantTable.getRowSorter().getSortKeys());
            variantTable.getRowSorter().setSortKeys(sortKeys);
            variantPanel.setDataTable(variantTable);
            
            //update index of unique id for later filtering steps...
            for(int c=0;c<variantTable.getColumnCount(); c++){
                if(BaseQuerier.OUTPUT_HEADER_EXAC_ID.equals(variantTable.getColumnName(c))){
                    uniqueIdIndex=c;
                    break;
                }
            }
            coverageModel.setDataVector(coverageRows, coverageHeaders);
            coverageTable.setAutoCreateColumnsFromModel( false );
            coveragePanel.setDataTable(coverageTable);
            
            featureModel.setDataVector(featureRows, featureHeaders);
            featureTable.setAutoCreateColumnsFromModel( false );
            featurePanel.setDataTable(featureTable);            

            
            if(searchResultMap.size()==1){
                String name = searchResultMap.keySet().iterator().next().toString();
                resultPane.setTitleAt(0, "Variants for: "+name);           
                resultPane.setTitleAt(1, "Other info for: "+name);
                resultPane.setTitleAt(2, "Feature info for: "+name);
                resultPane.setTitleAt(3, "Gene visualization");
            }
            else
            {
                resultPane.setTitleAt(0, "Variants");           
                resultPane.setTitleAt(1, "Other info");
                resultPane.setTitleAt(2, "Features");
                resultPane.setTitleAt(3, "Gene visualization");
            }
            
            gvp.updateGeneData();            
        }
        
    }
    
    private void inputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputButtonActionPerformed
        try
        {
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                inputFile = chooser.getSelectedFile();
                inputField.setText(inputFile.getPath());
                JOptionPane.showMessageDialog(this, "Input file loaded, press Query button to start");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_inputButtonActionPerformed
    public void updateDisplay(){
        updateDisplay(false);
    }
    
    public void updateDisplay(boolean initial){
        updateFilters(initial);        
        displayData();
        updateLabel();       
        
        if(initial){
            popButtonPanel.setVisible(true);
            annotButtonPanel.setVisible(true);
            popMatchBox.setVisible(true);
            //sepPopBox.setVisible(true);
        
            annotFilterScrollPane.setPreferredSize(new Dimension((int) popFilterScrollPane.getPreferredSize().getWidth(),
                    (int)(popFilterScrollPane.getPreferredSize().getHeight()+30)));
            popFilterScrollPane.setPreferredSize(new Dimension((int) popFilterScrollPane.getPreferredSize().getWidth(),
                    (int)(popFilterScrollPane.getPreferredSize().getHeight()+30)));
        }        
    }
    private void resultPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_resultPaneStateChanged
        int selectedIndex = resultPane.getSelectedIndex();
        if(selectedIndex>-1){
            String selectedGene = resultPane.getTitleAt(selectedIndex);

            //ExacData data = searchResultMap.get(selectedGene);
            SearchData data = searchResultMap.get(selectedGene);
            if(data!=null)
                updateLabel();
        }
    }//GEN-LAST:event_resultPaneStateChanged

    public String parseBooleanValue(boolean input){
        if(input)
            return "1";
        return "0";
    }
        
    private void printTable(File file, JTable table){
        try
        {
            StringBuffer lineBuffer = new StringBuffer();
             //New way (simply print entire table...)
            PrintStream printer = new PrintStream(file); 
            for(int c=0; c<table.getColumnCount(); c++)
            {
                printer.print(table.getColumnName(c));
                if(c<table.getColumnCount()-1)
                    printer.print("\t");
                else printer.println();
            }
            for(int r=0; r<table.getRowCount(); r++)
                for(int c=0; c<table.getColumnCount(); c++)
                {
                    lineBuffer.append(table.getValueAt(r, c));
                    if(c<table.getColumnCount()-1)
                        lineBuffer.append("\t");
                    else{
                        printer.println(lineBuffer.toString());
                        lineBuffer.setLength(0);
                        lineBuffer.trimToSize();
                    }
                    
                    /*
                    printer.print(table.getValueAt(r, c));
                    if(c<table.getColumnCount()-1)
                        printer.print("\t");
                    else printer.println();
                    */
                }               

            printer.close();
            
        }
        catch(Exception e){
            e.printStackTrace();
        }        
    }
    
    private void printTableExcel(File file, JTable table) throws InvalidFormatException{
        try {
             //New way (simply print entire table...)
            //PrintStream printer = new PrintStream(file); 
            String outputfilepath = file.getPath();            
            SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
            WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
            
            SheetData sheetData = sheet.getJaxbElement().getSheetData();
            
            // Create a new row
            Row row = Context.getsmlObjectFactory().createRow();
            //add headers
            for(int c=0; c<table.getColumnCount(); c++) {
                row.getC().add(this.newCellWithInlineString(table.getColumnName(c)));
            }
            // Add the row to our sheet
            sheetData.getRow().add(row);

            for(int r=0; r<table.getRowCount(); r++){
                row = Context.getsmlObjectFactory().createRow();                
                for(int c=0; c<table.getColumnCount(); c++)
                {
                    Object rowValue = table.getValueAt(r, c);
                    if(rowValue!=null)
                        rowValue = ""+rowValue;
                    row.getC().add(this.newCellWithInlineString((String)rowValue));                
                }
                // Add the row to our sheet
                sheetData.getRow().add(row);                    
            }
            //printer.close();
            
            
            pkg.save(new File(outputfilepath));
            //System.out.println("\n\n done .. " + outputfilepath);
        } catch (Exception ex) {
            Logger.getLogger(ExacToolFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Cell newCellWithInlineString(String string) {
	
	CTXstringWhitespace ctx = Context.getsmlObjectFactory().createCTXstringWhitespace();
	ctx.setValue(string);
	
	CTRst ctrst = new CTRst();
	ctrst.setT(ctx);
	
	Cell newCell = Context.getsmlObjectFactory().createCell();
	newCell.setIs(ctrst);
	newCell.setT(STCellType.INLINE_STR);
	
	return newCell;
    }
    
    
   
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
       
            //if(outputFile == null)
            //outputButtonActionPerformed(evt);
            final File outputFile = specifyOutput();
                    
            if(outputFile!=null)
            {               
                final ProgressDialog pd = new ProgressDialog(this, true);
                //pd.setVisible(true);
                Thread t2 = new Thread(new Runnable() {
                    public void run() {
                      pd.setVisible(true);
                      pd.setLabel("Saving results, please wait...");
                    }
                  });
                t2.start();
                  
                Thread t = new Thread()
                {                
                    public void run() 
                    {
                         try
                        {

                            //Determine filenames for coverage and counts
                            String basename = outputFile.getPath();
                            int indexOf = outputFile.getPath().lastIndexOf('.');
                            if(indexOf>-1)
                                basename = basename.substring(0, outputFile.getPath().lastIndexOf('.'));

                            if(!outputFile.getPath().toLowerCase().endsWith(".xls") && !outputFile.getPath().toLowerCase().endsWith(".xlsx")){
                                printTable(outputFile, variantTable);
                                printTable(new File(basename+"_cov.txt"), coverageTable);
                                printTable(new File(basename+"_features.txt"), featureTable);
                            }
                            else{
                                printTableExcel(outputFile, variantTable);
                                printTableExcel(new File(basename+"_cov.xlsx"), coverageTable);
                                printTableExcel(new File(basename+"_features.xlsx"), featureTable);
                                //dirty trick to get output in proper format... (only xlsx is supported so xls is renamed)
                                if(outputFile.getPath().toLowerCase().endsWith(".xls"))
                                    Files.move(outputFile.toPath(), new File(outputFile.getPath()+"x").toPath());

                            }
                            //writeCountFile(new File(basename+"_counts.txt"));
                            pd.setVisible(false);
                            pd.dispose();
                            JOptionPane.showMessageDialog(null, "File saved!");  
                         }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
                
            }
            else JOptionPane.showMessageDialog(this, "No outputfile specified, saving cancelled.");
       
       
    }//GEN-LAST:event_saveButtonActionPerformed

    private File specifyOutput(){
        if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            return chooser.getSelectedFile();
        }
        return null;
    }
    private void passBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passBoxActionPerformed
        updateDisplay();
        savePref(passBox);
    }//GEN-LAST:event_passBoxActionPerformed

    private void uniquesBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uniquesBoxActionPerformed
        updateDisplay();
        savePref(uniquesBox);
    }//GEN-LAST:event_uniquesBoxActionPerformed

    private void rsIdBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rsIdBoxActionPerformed
        updateDisplay();
        savePref(rsIdBox);
    }//GEN-LAST:event_rsIdBoxActionPerformed

    private void missenseLofBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_missenseLofBoxActionPerformed
        updateDisplay();
        savePref(missenseLofBox);
        
    }//GEN-LAST:event_missenseLofBoxActionPerformed

    private void resultPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultPaneMouseClicked
        
    }//GEN-LAST:event_resultPaneMouseClicked

    private void annotAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotAllButtonActionPerformed
        setAnnotationSelection(1);
    }//GEN-LAST:event_annotAllButtonActionPerformed

    private void annotNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotNoteButtonActionPerformed
        setAnnotationSelection(0);
    }//GEN-LAST:event_annotNoteButtonActionPerformed

    private void annotInvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotInvButtonActionPerformed
        setAnnotationSelection(2);
    }//GEN-LAST:event_annotInvButtonActionPerformed

    private void setAnnotationSelection(int mode){         
        JCheckBox box = null;
        for(int c=0; c<annotFilterPanel.getComponentCount(); c++){
            if(annotFilterPanel.getComponent(c) instanceof JCheckBox)
            {
                box = (JCheckBox)annotFilterPanel.getComponent(c);
                if(mode==0)
                    box.setSelected(false);
                else if(mode==1)
                    box.setSelected(true);
                else box.setSelected(!box.isSelected());
                
                prefs.putBoolean(box.getText(), box.isSelected());
            }
        }
        updateDisplay();
    }
    private void popAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popAllButtonActionPerformed
        setPopulationSelection(1);
    }//GEN-LAST:event_popAllButtonActionPerformed

    private void popNoneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popNoneButtonActionPerformed
        setPopulationSelection(0);
    }//GEN-LAST:event_popNoneButtonActionPerformed

    private void popInvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popInvButtonActionPerformed
        setPopulationSelection(2);
    }//GEN-LAST:event_popInvButtonActionPerformed

    private void gnomadBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gnomadBoxActionPerformed
        //set proper usage mode, exac or gnomad        
        savePref(gnomadBox);
        
        if(gnomadBox.isSelected())   
        {
            querier = new GnomadQuerier();
            cache_extension=ExtractorDefinitions.GNOMAD_WEB_CACHE_EXTENSION;
        }
        else
        {
            querier = new ExacQuerier();
            cache_extension=ExtractorDefinitions.EXAC_WEB_CACHE_EXTENSION;
            exomeBox.setSelected(true);
            genomeBox.setSelected(true);
            snpsBox.setSelected(true);
            indelBox.setSelected(true);
            localBoxActionPerformed(evt);
        }
            
        
        exomeBox.setVisible(gnomadBox.isSelected());
        genomeBox.setVisible(gnomadBox.isSelected());
        snpsBox.setVisible(false);
        indelBox.setVisible(gnomadBox.isSelected());
        
            
    }//GEN-LAST:event_gnomadBoxActionPerformed

    private void indelBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indelBoxActionPerformed
        updateDisplay();
        savePref(indelBox);
    }//GEN-LAST:event_indelBoxActionPerformed

    private void exomeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exomeBoxActionPerformed
        updateDisplay();
        savePref(exomeBox);
    }//GEN-LAST:event_exomeBoxActionPerformed

    private void genomeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genomeBoxActionPerformed
        updateDisplay();
        savePref(genomeBox);
    }//GEN-LAST:event_genomeBoxActionPerformed

    private void snpsBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snpsBoxActionPerformed
        updateDisplay();
    }//GEN-LAST:event_snpsBoxActionPerformed

    private void popMatchBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popMatchBoxActionPerformed
        updateDisplay();
        savePref(popMatchBox);
    }//GEN-LAST:event_popMatchBoxActionPerformed

    private void canonicalBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canonicalBoxActionPerformed
        updateDisplay();
    }//GEN-LAST:event_canonicalBoxActionPerformed

    private void consequenceBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consequenceBoxActionPerformed
        updateDisplay();
    }//GEN-LAST:event_consequenceBoxActionPerformed

    private void regionsBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regionsBoxActionPerformed
        updateDisplay();
    }//GEN-LAST:event_regionsBoxActionPerformed

    private void saveBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBoxActionPerformed
        savePref(saveBox);
        checkSaveAndCached();
    }//GEN-LAST:event_saveBoxActionPerformed

    private void checkSaveAndCached(){
        if(saveBox.isSelected() && useCachedBox.isSelected()){
            //JOptionPane.showMessageDialog(this, "Notice! Both raw data save and use cache are enabled, this will force a query for each term");
        }
    }
    private void searchFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchFieldFocusGained
        searchField.selectAll();
    }//GEN-LAST:event_searchFieldFocusGained

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        processSearchTerms(true);
    }//GEN-LAST:event_searchFieldActionPerformed

    private void useCachedBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useCachedBoxActionPerformed
        savePref(useCachedBox);
        checkSaveAndCached();
    }//GEN-LAST:event_useCachedBoxActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        gvp.gip.triggerResize();
        
    }//GEN-LAST:event_formComponentResized

    private void localBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localBoxActionPerformed
        /*
        savePref(localBox);        
        if(localBox.isSelected())            
        {
            boolean check = checkDataFiles();
            if(check)
            {
                System.out.println("Local mode selected, datafiles found!");
                //defaultGene = "1:55505220-55530525"; 
                //querier = new VCFQuerier(ExtractorDefinitions.getVcfFile());
                if("ExAC.r0.3.1.sites.vep.vcf.gz".equals(ExtractorDefinitions.VCF_FILE))
                    querier = new VCFQuerier();
                else querier = new VCFAnnQuerier();
                
                cache_extension = ExtractorDefinitions.VCF_CACHE_EXTENSION;
                saveBox.setEnabled(false);
                useCachedBox.setEnabled(false);
                LOCAL_MODE = true;
                return;
            }
        }
        saveBox.setEnabled(true);
        useCachedBox.setEnabled(true);        
        gnomadBox.setSelected(false);
        cache_extension = ExtractorDefinitions.EXAC_WEB_CACHE_EXTENSION;
        querier = new ExacQuerier();        
        LOCAL_MODE = false;
        */
    }//GEN-LAST:event_localBoxActionPerformed

    private void saveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveItemActionPerformed
        saveButtonActionPerformed(evt);
    }//GEN-LAST:event_saveItemActionPerformed

    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitItemActionPerformed
        if(JOptionPane.showConfirmDialog(this, "Are you sure? Unsaved changes will be lost!") == JOptionPane.OK_OPTION)
            System.exit(0);
    }//GEN-LAST:event_exitItemActionPerformed

    public void specifyDataFolder(){
        String folder = selectFile(true);
        if(folder!=null && new File(folder+ExtractorDefinitions.separator+ExtractorDefinitions.FEATURE_CHR_FILE_CHECK).exists())
            setDataFolder(folder);        
        else JOptionPane.showMessageDialog(this, "'"+ExtractorDefinitions.FEATURE_CHR_FILE_CHECK+"' not found in specified folder.");
        
    }
    
    public void specifyGeneSummaryData(){
        String file = selectFile(false);
        if(file!=null)
            setGeneSummaryFile(file);        
        else JOptionPane.showMessageDialog(this, "No gene summary file selected.");
        
    }
    public void setGeneSummaryFile(String file){
        prefs.put(ExtractorDefinitions.KEY_GENE_SUMMARY_FILE, file);        
        ExtractorDefinitions.GENE_SUMMARY_FILE = file;
    }
    public void setDataFolder(String folder){
        prefs.put(ExtractorDefinitions.KEY_PATH_DATAFILES, folder);        
        ExtractorDefinitions.DATAFILES_PATH = folder;
    }
    
    
    public void specifyVcfFolder(){
        String folder = selectFile(true);
        if(folder!=null && new File(folder+ExtractorDefinitions.separator+ExtractorDefinitions.VCF_FILE).exists())
            setVcfFolder(folder);
        else JOptionPane.showMessageDialog(this, "'"+ExtractorDefinitions.VCF_FILE+"' not found in specified folder.");
    }
    
    public void setVcfFolder(String folder){
        prefs.put(ExtractorDefinitions.KEY_PATH_VCF, folder);        
        ExtractorDefinitions.VCF_PATH = folder;
    }

    public void specifyCacheFolder(){
        String folder = selectFile(true);
        if(folder!=null){
            //String currentFolder = prefs.get(key_path_cached, ExtractorDefinitions.cached_path); //check if cached data should be moved/copied?
            setCacheFolder(folder);
        }        
    }
    
    public void setCacheFolder(String folder){
        prefs.put(ExtractorDefinitions.KEY_PATH_CACHED, folder);        
        ExtractorDefinitions.CACHED_PATH = folder;
    }
    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu2ActionPerformed

    private void preferencesItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesItemActionPerformed
        new PreferencesDialogGnomad(this, true).setVisible(true);
    }//GEN-LAST:event_preferencesItemActionPerformed

    private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutItemActionPerformed
        new AboutDialog(this, true).setVisible(true);
    }//GEN-LAST:event_aboutItemActionPerformed

    private void geneDataItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geneDataItemActionPerformed
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            String gtfFile = chooser.getSelectedFile().getPath();
            if(gtfFile.endsWith(".gtf.gz"))
            {                
                GTFRewriter.processGTF(gtfFile);                
                JOptionPane.showMessageDialog(this, "Finished, result can be found in '"+chooser.getSelectedFile().getParent()+System.getProperty("file.separator")+"gene_data'");
            }
            else JOptionPane.showMessageDialog(this, "Please select valid gtf.gz file (such as 'Homo_sapiens.GRCh37.75.gtf.gz')");
        }
    }//GEN-LAST:event_geneDataItemActionPerformed

    private void logItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logItemActionPerformed
        new LogDialog(this, true).setVisible(true);
    }//GEN-LAST:event_logItemActionPerformed

    private void helpItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpItemActionPerformed
        openHelpFile();
    }//GEN-LAST:event_helpItemActionPerformed

    private void sepPopBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sepPopBoxActionPerformed
        updateDisplay();
        savePref(sepPopBox);
    }//GEN-LAST:event_sepPopBoxActionPerformed
    
    
    public void specifyCoverageFolder(){
        String folder = selectFile(true);
        if(folder!=null && new File(folder+ExtractorDefinitions.separator+ExtractorDefinitions.COVERAGE_FILE_CHECK).exists())
            setCoverageFolder(folder);
        else JOptionPane.showMessageDialog(this, "'"+ExtractorDefinitions.COVERAGE_FILE+"' not found in specified folder.");
    }
    
    public void setCoverageFolder(String folder){
        prefs.put(ExtractorDefinitions.KEY_PATH_COVERAGE, folder);        
        ExtractorDefinitions.COVERAGE_PATH = folder;
    }
    
    
    
    private void setPopulationSelection(int mode){
        JCheckBox box = null;
        for(int c=0; c<popFilterPanel.getComponentCount(); c++){
            if(popFilterPanel.getComponent(c) instanceof JCheckBox)
            {
                box = (JCheckBox)popFilterPanel.getComponent(c);
                if(mode==0)
                    box.setSelected(false);
                else if(mode==1)
                    box.setSelected(true);
                else box.setSelected(!box.isSelected());
                
                prefs.putBoolean(box.getText(), box.isSelected());
                
            }
        }
        updateDisplay();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        
        /*
        Metal
        Nimbus
        CDE/Motif
        Windows
        Windows Classic
        */
        
        /*
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                System.out.println(info.getClassName());
                lookAndFeels.add(info.getClassName());
                if ("Windows".equals(info.getName())) {
                    //javax.swing.UIManager.setLookAndFeel(info.getClassName());                    
                    setLookAndFeel(info.getClassName());
                    
                    //break;
                }
                //System.out.println(info.getName());
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ExacToolFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ExacToolFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ExacToolFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ExacToolFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        */
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ExacToolFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutItem;
    private javax.swing.JButton annotAllButton;
    private javax.swing.JPanel annotButtonPanel;
    private javax.swing.JPanel annotFilterPanel;
    private javax.swing.JScrollPane annotFilterScrollPane;
    private javax.swing.JButton annotInvButton;
    private javax.swing.JButton annotNoteButton;
    private javax.swing.JCheckBox canonicalBox;
    private javax.swing.JCheckBox consequenceBox;
    private javax.swing.JMenuItem exitItem;
    private javax.swing.JCheckBox exomeBox;
    private javax.swing.JPanel extendedFilterPanel;
    private javax.swing.JMenuItem geneDataItem;
    private javax.swing.JCheckBox genomeBox;
    private javax.swing.JPanel globalFilterPanel;
    private javax.swing.JCheckBox gnomadBox;
    private javax.swing.JMenuItem helpItem;
    private javax.swing.JCheckBox indelBox;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JButton inputButton;
    private javax.swing.JTextField inputField;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JCheckBox localBox;
    private javax.swing.JMenuItem logItem;
    private javax.swing.JComboBox<String> missenseLofBox;
    private javax.swing.JCheckBox passBox;
    private javax.swing.JButton popAllButton;
    private javax.swing.JPanel popButtonPanel;
    private javax.swing.JPanel popFilterPanel;
    private javax.swing.JScrollPane popFilterScrollPane;
    private javax.swing.JButton popInvButton;
    private javax.swing.JCheckBox popMatchBox;
    private javax.swing.JButton popNoneButton;
    private javax.swing.JMenuItem preferencesItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton queryButton;
    private javax.swing.JCheckBox regionsBox;
    private javax.swing.JTabbedPane resultPane;
    private javax.swing.JCheckBox rsIdBox;
    private javax.swing.JCheckBox saveBox;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveItem;
    private javax.swing.JTextField searchField;
    private javax.swing.JCheckBox sepPopBox;
    private javax.swing.JCheckBox snpsBox;
    private javax.swing.JCheckBox uniquesBox;
    private javax.swing.JCheckBox useCachedBox;
    // End of variables declaration//GEN-END:variables
}




