/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.ui;

import java.awt.event.ActionEvent;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPopupMenu;

/**
 *
 * @author flip_
 */
public class GenePanelPopup extends JPopupMenu implements java.awt.event.ActionListener{

    public final static String SAVE_IMAGE_PNG = "Save Image to .png";
    public final static String SAVE_IMAGE_JPG = "Save Image to .jpg";
    public final static String UTR_SIZE = "Show actual UTR sizes";
    GeneImagePanel geneImagePanel;
    JCheckBox utrBox = new JCheckBox(UTR_SIZE);
    public GenePanelPopup(GeneImagePanel parent){
        initializeMenu();
        geneImagePanel = parent;
    }
        
    private void initializeMenu()
    {
        
        //this.add(SAVE_IMAGE_JPG).addActionListener(this);
        this.add(SAVE_IMAGE_PNG).addActionListener(this);
        this.add(utrBox);
        utrBox.addActionListener(this);
        /*
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(anyBox);
        buttonGroup.add(wordBox);
        buttonGroup.add(endsBox);    
        buttonGroup.add(startsBox);
        buttonGroup.add(invertBox);
        
        this.addSeparator();        
        this.add(clearItem);  
        clearItem.addActionListener(this);     
        */
        
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        String command = e.getActionCommand();
        if(SAVE_IMAGE_PNG.equals(command))
            geneImagePanel.saveImage("png");
        if(SAVE_IMAGE_JPG.equals(command))
            geneImagePanel.saveImage("jpg");
        else if(UTR_SIZE.equals(command))
            geneImagePanel.setUTRSizes(((JCheckBox)e.getSource()).isSelected());
        
    }
    
    
    
}
