/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package exactool.ui;

//import nl.umcutrecht.gids.ui.main.*;
import exactool.ui.QuickFilterTablePanel;
import javax.swing.*;
import java.util.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author flip
 */
public class QuickFilterPopup extends JPopupMenu implements java.awt.event.ActionListener
{
    private final String CASE_SENSITIVE = "Case Sensitive Search";
    private final String COMPLETE_WORD = "Search For Whole Words";
    private final String STARTS_WITH = "Search At Start Of Word";
    private final String ENDS_WITH = "Search At End Of Word";
    private final String CLEAR_FILTER = "Clear Search Parameters";
    private final String ANYWHERE = "Search Anywhere In Word";
    private final String INVERT = "Invert settings";
    
    final JMenuItem clearItem = new JMenuItem(CLEAR_FILTER);    
    final JCheckBoxMenuItem caseBox = new JCheckBoxMenuItem(CASE_SENSITIVE);
    final JCheckBoxMenuItem wordBox = new JCheckBoxMenuItem(COMPLETE_WORD);
    final JCheckBoxMenuItem startsBox = new JCheckBoxMenuItem(STARTS_WITH);
    final JCheckBoxMenuItem endsBox = new JCheckBoxMenuItem(ENDS_WITH);
    final JCheckBoxMenuItem anyBox = new JCheckBoxMenuItem(ANYWHERE);
    final JCheckBoxMenuItem invertBox = new JCheckBoxMenuItem(INVERT);
    
    HashMap<Integer, ColumnQuickFilterSettings> settingsMap = new HashMap();
    int activeColumn = -1;
    ColumnQuickFilterSettings activeSettings;
    JLabel nameLabel = new JLabel();
    QuickFilterTablePanel tablePanel;
    
    public QuickFilterPopup(QuickFilterTablePanel parent){
        initializeMenu();
        tablePanel = parent;
    }
        
    private void initializeMenu()
    {
        this.add(nameLabel);
        this.addSeparator();  
        this.add(caseBox);
        caseBox.addActionListener(this);
        
        this.addSeparator();        
        this.add(wordBox);  
        wordBox.addActionListener(this);                
        this.add(startsBox);  
        startsBox.addActionListener(this);                
        this.add(endsBox);  
        endsBox.addActionListener(this);        
        this.add(anyBox);  
        anyBox.addActionListener(this);
        this.add(invertBox);
        invertBox.addActionListener(this);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(anyBox);
        buttonGroup.add(wordBox);
        buttonGroup.add(endsBox);    
        buttonGroup.add(startsBox);
        buttonGroup.add(invertBox);
        
        this.addSeparator();        
        this.add(clearItem);  
        clearItem.addActionListener(this);     
        
    }
    
    public void setActiveColumn(int column, String name){
        //store current settings for column if needed
        nameLabel.setText("Settings for: "+name);
        if(activeColumn>-1)
        {            
            activeSettings = settingsMap.get(activeColumn);
            if(activeSettings==null)
                activeSettings = new ColumnQuickFilterSettings();            
            updateSettings();
            settingsMap.put(activeColumn, activeSettings);
        }
        
        //update active column and retrieve settings if any are existent (or create new one if not)
        activeColumn = column;      
        activeSettings = settingsMap.get(activeColumn);
        if(activeSettings==null)
            activeSettings = new ColumnQuickFilterSettings();         
        
        updateView();
    }
    
    public boolean isCaseSensitive(int column){
        ColumnQuickFilterSettings settings = settingsMap.get(column);
        if(settings==null)
        {
            settings = new ColumnQuickFilterSettings();
            settingsMap.put(column, settings);
        }            
        return settings.caseSensitive;
    }

    public boolean isInvert(int column){
        ColumnQuickFilterSettings settings = settingsMap.get(column);
        if(settings==null)
        {
            settings = new ColumnQuickFilterSettings();
            settingsMap.put(column, settings);
        }   
        return settings.invert;
    }
    
    public boolean completeWord(int column){
        ColumnQuickFilterSettings settings = settingsMap.get(column);
        if(settings==null)
        {
            settings = new ColumnQuickFilterSettings();
            settingsMap.put(column, settings);
        }   
        return settings.completeWord;
    }
    
    public boolean startsWith(int column){
        ColumnQuickFilterSettings settings = settingsMap.get(column);
        if(settings==null)
        {
            settings = new ColumnQuickFilterSettings();
            settingsMap.put(column, settings);
        }   
        return settings.startsWith;
    }
    
    public boolean endsWith(int column){
        ColumnQuickFilterSettings settings = settingsMap.get(column);
        if(settings==null)
        {
            settings = new ColumnQuickFilterSettings();
            settingsMap.put(column, settings);
        }   
        return settings.endsWith;
    }
    
    public boolean anywhere(int column){
        ColumnQuickFilterSettings settings = settingsMap.get(column);
        if(settings==null)
        {
            settings = new ColumnQuickFilterSettings();
            settingsMap.put(column, settings);
        }   
        return settings.anywhere;
    }
    
    private void updateSettings(){
        activeSettings.setAnywhere(anyBox.isSelected());
        activeSettings.setEndsWith(endsBox.isSelected());
        activeSettings.setStartsWith(startsBox.isSelected());
        activeSettings.setCompleteWord(wordBox.isSelected());
        activeSettings.caseSensitive = caseBox.isSelected();
        activeSettings.invert = invertBox.isSelected();
    }
    
    private void updateView(){
        caseBox.setSelected(activeSettings.caseSensitive);
        startsBox.setSelected(activeSettings.startsWith);
        endsBox.setSelected(activeSettings.endsWith);
        wordBox.setSelected(activeSettings.completeWord);
        anyBox.setSelected(activeSettings.anywhere);
        invertBox.setSelected(activeSettings.invert);
    }

    public void actionPerformed(ActionEvent e) 
    {
        String command = e.getActionCommand();
        if(CASE_SENSITIVE.equals(command))
            activeSettings.caseSensitive = caseBox.isSelected();
        else if(STARTS_WITH.equals(command))
            activeSettings.setStartsWith(startsBox.isSelected());
        else if(ENDS_WITH.equals(command))
            activeSettings.setEndsWith(endsBox.isSelected());
        else if(COMPLETE_WORD.equals(command))
            activeSettings.setCompleteWord(wordBox.isSelected());
        else if(ANYWHERE.equals(command))
            activeSettings.setAnywhere(anyBox.isSelected());
        else if(INVERT.equals(command))
            activeSettings.invert = invertBox.isSelected();
        else if(CLEAR_FILTER.equals(command))
            clearQuickFilter();
        
        tablePanel.executeFilter();
    }
    
    private void clearQuickFilter(){
        settingsMap.clear();      
        tablePanel.clearQuickFilter();
    }    
}
class ColumnQuickFilterSettings
{
    boolean caseSensitive = true;
    boolean completeWord = false;
    boolean startsWith = false;
    boolean endsWith = false;
    boolean anywhere = true;
    boolean invert = false;
    
    public ColumnQuickFilterSettings(){
        
    }
    
    public void setCompleteWord(boolean value)            
    {
        completeWord = value;
        if(completeWord)
        {
            startsWith = false;
            endsWith = false;
            anywhere = false;
        }
    }
    
    public void setStartsWith(boolean value)            
    {
        startsWith = value;
        if(startsWith)
        {
            completeWord = false;
            endsWith = false;
            anywhere = false;
        }
    }
    
    public void setEndsWith(boolean value)            
    {
        endsWith = value;
        if(endsWith)
        {
            startsWith = false;
            completeWord = false;
            anywhere = false;
        }
    }
    
    public void setAnywhere(boolean value)            
    {
        anywhere = value;
        if(anywhere)
        {
            startsWith = false;
            endsWith = false;
            completeWord = false;
        }
    }
}
