/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.ui;

/**
 *
 * @author flip
 */
public class AboutDialog extends javax.swing.JDialog {

    /**
     * Creates new form AboutDialog
     */
    public AboutDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setSize(800,480);
        this.setLocationRelativeTo(parent);
        changeLogTextArea.setCaretPosition(0);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        changeLogTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("EXtrACtor - Use at your own risk...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jLabel1, gridBagConstraints);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Flip Mulder - UMC Utrecht");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jLabel2, gridBagConstraints);

        changeLogTextArea.setEditable(false);
        changeLogTextArea.setColumns(20);
        changeLogTextArea.setRows(5);
        changeLogTextArea.setText("Changelog:\n\nv0.2.180228:\n\t- Quickfilter settings and sorting status are now kept when changing main filters\n\t- Fixed bug in UNIQUE filter (variants not unique across genome AND exome data would be kept when unique in oone of the datasets)\n\t- Proper statistics and handling of population specific filtering\n\t- Better processing of including/excluding genome and/or exome data\n\nv0.2.180208:\n\t- Fix bug where using quickfilter row in combination with main filters could cause filters not to update properly\n\t- Fix issue with data staying in memory when switching between exac and gnomad mode\n\nv0.2.170511:\n\t- Fix in population filter regarding counts\n\nv0.2.170509:\n\t- Feature info (empty sizes) now takes into acocunt filtered variants instead of all\n\t- Include logging for current session for search-terms causing issues...\n\t- Made progress dialog more visible\n\t- Proper processing of searches not returning results and corrupt cached results\n\nv0.2.170508:\n\t- Include coverage info in variant table \n\t- Fix average coverage display in coverage tab for genome and exome data\n\t- Improved handling of region search\n\t- Allow and improved visualtization of region search\n\t- Fixed gene handling for region and non-gene search\n\t- Fixed bug with searches on X-chromosome\n\nv0.2.170428:\n\t- Improved quickfitler implementation (allows filtering using >, <, >=, <=, etc)\n\t- Gene Visualization now also reflects quickfilter settings\n\t- Right-click in GeneView allows saving to .png file and display of actual size UTR\n\t- Bugfixes\n\nv0.2.170426:\n\t- Modified for use with official GnoMAD webpage\n\t- Disabled local file mode due to issues\n\t- Bugfixes and moved to different JSON library for data handling\n\t- Added possibilities to filter on exomes and genomes in GnoMAD mode\n\t- Allow loading of external variant list for plotting in Gene View\n\nv0.1.161209:\n\t- Improved handling of path and file preferences\n\t- Quickfilter settings saved when querying or adjusting main options\n\t- Improved handling VCF and WEB mode\n\t- Bugfixes\n\nv0.1.161207:\n\t- VCF and WEB mode handling\n\t- Gene and variant plotting for VCF mode\n\t- Added coverage info for VCF mode\n\t- Added feature info for VCF mode\n\nv0.1.161202:\n\t- Gene plotting for WEB mode\n\nv0.1.0\t- Initial release\n\t- Only WEB mode\n");
        jScrollPane1.setViewportView(changeLogTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea changeLogTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
