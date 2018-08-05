/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * QuickFilterTablePanel.java
 *
 * Created on 16-jan-2009, 16:11:54
 */

package exactool.ui;

import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.util.*;
import java.awt.event.*;
import java.math.*;

/**
 *
 * @author flip
 */
public class QuickFilterTablePanel extends javax.swing.JPanel {

    //ColoredTable filterTable, dataTable;
    ExacTable filterTable, dataTable;
    ExacToolFrame parentFrame;
    QuickFilterPopup quickFilterPopup;
    boolean executeShowSubSelection = true;
    TableRowSorter sorter;
    RowFilter activeRowFilter = null;
    /** Creates new form QuickFilterTablePanel */
    public QuickFilterTablePanel() {
        initComponents();
        quickFilterPopup = new QuickFilterPopup(this);
        tableScrollPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals("FINISHED"))
                {                    
                    updateFilterTable();                    
                }
            }
        });
    }
    
    public boolean isFilterActive(){
        return activeRowFilter!=null;
    }
    public void setParentFrame(ExacToolFrame tf){
        parentFrame = tf;
    }
    public void executeFilter(){
        if(executeShowSubSelection){     
            activeRowFilter = getRowFilter();
            sorter.setRowFilter(activeRowFilter);   
            parentFrame.updateLabel();              
        }
    }

    public void setDataTable(ExacTable table){
        dataTable = table;        
        updateFilterTable();
    }
    
    public ExacTable getFilterTable(){
        return filterTable;        
    }
    
    public void setFilterTable(ExacTable table){
        if(table==null)
            return;
        
        this.filterTable = table;        
        executeFilter();
    }
    
    public void clearQuickFilter()
    {
        executeShowSubSelection = false;
        for(int i=0; i<filterTable.getColumnCount(); i++)
            filterTable.setValueAt(null, 0, i);
        executeShowSubSelection = true;
        
        executeFilter();        
    }

    public int getDisplayedRowCount(){
        int result = -1;
        if(dataTable!=null)
            result = dataTable.getRowCount();
        return result;
    }
    private RowFilter getRowFilter()
    {
        java.util.List<RowFilter<Object,Object>> filters = new ArrayList<RowFilter<Object,Object>>();
        
        for(int i=0; i<filterTable.getColumnCount(); i++)
        {
            Object value = filterTable.getValueAt(0, i);
            if(value!=null && !"".equals(value))
            {
                String option = "";
                String endsWith = "";
                int modelIndex = filterTable.convertColumnIndexToModel(i);
                Object dataElement = getDataForColumn(dataTable.getModel(), modelIndex);
                Date date = null;
                //Class dataElement = defaultTableModel.getColumnClass(modelIndex);

                if(dataElement!=null)   
                {                    
                    boolean isDate = dataElement instanceof java.util.Date;
                    //boolean isDate = dataElement == java.util.Date.class;
                    String items[] = getOperatorAndValue(value.toString());

                    if(items!=null && isDate)
                        date = umcutilities.DateUtilities.stringToDate(items[1]);


                    if(dataElement instanceof String || items==null || (isDate && date==null)){
                        if(!quickFilterPopup.isCaseSensitive(modelIndex))
                            option = "(?i)";
                        if(quickFilterPopup.startsWith(modelIndex))
                            option+= "^";
                        else if(quickFilterPopup.completeWord(modelIndex))
                            option += "\\b";
                        else if(quickFilterPopup.endsWith(modelIndex))
                            endsWith = "$";

                        String result = option+value.toString()+endsWith;                        
                        if(quickFilterPopup.isInvert(modelIndex))
                            filters.add(RowFilter.notFilter(RowFilter.regexFilter(result, filterTable.convertColumnIndexToModel(i))));
                        else filters.add(RowFilter.regexFilter(result, filterTable.convertColumnIndexToModel(i)));
                    }
                    else if(dataElement instanceof Integer || dataElement instanceof Double ||  dataElement instanceof Float || dataElement instanceof java.util.Date || dataElement instanceof BigDecimal  || dataElement instanceof BigInteger)
                    {
                        //String items[] = value.toString().trim().split(" ", -1);
                        Number number = null;

                        if(!isDate)
                        {
                            if(dataElement instanceof Integer)
                                number = Integer.parseInt(items[1]);
                            else if(dataElement instanceof Double)
                                number = Double.parseDouble(items[1]);
                            else if(dataElement instanceof Long)
                                number = Long.parseLong(items[1]);
                            else if(dataElement instanceof Short)
                                number = Short.parseShort(items[1]);
                            else if(dataElement instanceof Float)
                                number = Float.parseFloat(items[1]);
                            else if(dataElement instanceof BigDecimal)
                                number = BigDecimal.valueOf(Double.parseDouble(items[1]));
                            else if(dataElement instanceof BigInteger)
                                number = BigInteger.valueOf(Long.parseLong(items[1]));
                        }

                        if(items[0].equals(">") ||items[0].equals("!<="))
                        {
                            if(isDate)
                                filters.add(RowFilter.dateFilter(RowFilter.ComparisonType.AFTER, date, filterTable.convertColumnIndexToModel(i)));
                            else filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, number, filterTable.convertColumnIndexToModel(i)));
                        }
                        else if(items[0].equals("<") || items[0].equals("!>="))
                        {
                            if(isDate)
                                filters.add(RowFilter.dateFilter(RowFilter.ComparisonType.BEFORE, date, filterTable.convertColumnIndexToModel(i)));
                            else filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, number, filterTable.convertColumnIndexToModel(i)));
                        }
                        else if((items[0]).equals("="))
                        {
                            if(isDate)
                                filters.add(RowFilter.dateFilter(RowFilter.ComparisonType.EQUAL, date, filterTable.convertColumnIndexToModel(i)));
                            else filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, number, filterTable.convertColumnIndexToModel(i)));
                        }
                        else if(items[0].equals("!=") || items[0].equals("<>"))
                        {
                            if(isDate)
                                filters.add(RowFilter.dateFilter(RowFilter.ComparisonType.NOT_EQUAL, date, filterTable.convertColumnIndexToModel(i)));
                            else filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.NOT_EQUAL, number, filterTable.convertColumnIndexToModel(i)));
                        }
                        else if(items[0].equals("!>") || items[0].equals("<="))
                        {
                            Vector temp = new Vector();
                            if(isDate)
                            {
                                temp.add(RowFilter.dateFilter(RowFilter.ComparisonType.BEFORE, date, filterTable.convertColumnIndexToModel(i)));
                                temp.add(RowFilter.dateFilter(RowFilter.ComparisonType.EQUAL, date, filterTable.convertColumnIndexToModel(i)));
                            }
                            else
                            {
                                temp.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, number, filterTable.convertColumnIndexToModel(i)));
                                temp.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, number, filterTable.convertColumnIndexToModel(i)));
                            }
                            filters.add(RowFilter.orFilter(temp));
                        }
                        else if(items[0].equals("!<") || items[0].equals(">="))
                        {
                            Vector temp = new Vector();
                            if(isDate)
                            {
                                temp.add(RowFilter.dateFilter(RowFilter.ComparisonType.AFTER, date, filterTable.convertColumnIndexToModel(i)));
                                temp.add(RowFilter.dateFilter(RowFilter.ComparisonType.EQUAL, date, filterTable.convertColumnIndexToModel(i)));
                            }
                            else
                            {
                                temp.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, number, filterTable.convertColumnIndexToModel(i)));
                                temp.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, number, filterTable.convertColumnIndexToModel(i)));
                            }
                            filters.add(RowFilter.orFilter(temp));
                        }
                        else
                        {
                            
                        }
                    }
                }             
            }
        }

        if(filters.size()>0)
            return RowFilter.andFilter(filters);
        else return null;
    }

    /**
     * Given an entry in the Filter table it returns an array containing the operator and value.
     * @param input The entry from the Filter table
     * @return  An array containing the operator on the first place and the value on the second place
     */
    private String[] getOperatorAndValue(String input){
        String ops[] = {"!>=", "!<=", "<=", ">=", "<>", "!=", "!>", "!<", ">", "<", "="};
        String result[] = null;
        if(input!=null)
        {
            input = input.trim();
            for(int i=0; i<ops.length; i++)
            {
                if(input.startsWith(ops[i]))
                {
                    result = new String[2];
                    result[0] = ops[i];
                    result[1] = input.substring(result[0].length()).trim();
                    break;
                }
            }
        }
        return result;
    }

    public Object getDataForColumn(TableModel model, int column){
        Object result = null;
        for(int i=0; i<model.getRowCount(); i++)
        {
            result = model.getValueAt(i, column);
            if(result!=null)
                break;
        }
        return result;
    }

    public void updateFilterTable()
    {
        if(dataTable==null || dataTable.getColumnCount()==0)
            return;

        List sortKeys = new ArrayList();
        if(sorter!=null){
            //System.out.println("Sort keys: "+sorter.getSortKeys());
            sortKeys = sorter.getSortKeys();            
        }
        
        dataTable.getRowSorter().getSortKeys();
        
        //only        
        if(filterTable!=null && filterTable.getColumnCount()==dataTable.getColumnCount())
        {
            boolean identical = true;
            for(int c=0; c<filterTable.getColumnCount(); c++)
                identical = identical && filterTable.getColumnName(c).equals(dataTable.getColumnName(c));
                
            if(identical)
            {
                sorter.setSortKeys(sortKeys);
                sorter.sort();
                return;
            }
        }
        
       
        Vector headers = new Vector();
        Vector data = new Vector();
        Vector row = new Vector();

        for(int i=0; i<dataTable.getColumnCount(); i++){
            headers.add(dataTable.getColumnName(i));
            row.add(null);
        }
        data.add(row);
        
        filterTable = new ExacTable(new DefaultTableModel(data, headers));
        
        filterTable.addMouseListener(new java.awt.event.MouseListener() {

            public void mouseClicked(MouseEvent e){}

            public void mousePressed(MouseEvent e){maybeShowPopup(e);}

            public void mouseReleased(MouseEvent e) {maybeShowPopup(e);}

            public void mouseEntered(MouseEvent e) {}

            public void mouseExited(MouseEvent e) {}

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger())
                {
                    int selectedColumn = filterTable.columnAtPoint(e.getPoint());
                    if(selectedColumn>-1)
                    {
                        int modelColumn = filterTable.convertColumnIndexToModel(selectedColumn);
                        quickFilterPopup.setActiveColumn(modelColumn, filterTable.getColumnName(selectedColumn));
                        quickFilterPopup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }});

     
        filterTable.setColumnModel(dataTable.getColumnModel());
        filterTable.getModel().addTableModelListener(
            new javax.swing.event.TableModelListener(){
                public void tableChanged(javax.swing.event.TableModelEvent e){
                        executeFilter();
                }
            }
        );

        filterTable.setEditable(true);
        filterScrollPane.setViewportView(filterTable);        
        tableScrollPane.setViewportView(dataTable);
        
        if(tableScrollPane.getHorizontalScrollBar() != null)
        {
            tableScrollPane.getHorizontalScrollBar().addAdjustmentListener(new java.awt.event.AdjustmentListener()
            {
                public void adjustmentValueChanged(AdjustmentEvent e)
                {
                    if(filterScrollPane.getHorizontalScrollBar() != null)
                        filterScrollPane.getHorizontalScrollBar().setValue(tableScrollPane.getHorizontalScrollBar().getValue());                                        
                }
            });



        filterScrollPane.getHorizontalScrollBar().addAdjustmentListener(new java.awt.event.AdjustmentListener()
            {
                public void adjustmentValueChanged(AdjustmentEvent e)
                {
                    if(tableScrollPane.getHorizontalScrollBar() != null)
                    {
                        tableScrollPane.getHorizontalScrollBar().setValue(filterScrollPane.getHorizontalScrollBar().getValue());                        
                    }
                }
            });
        }
                
        sorter = new TableRowSorter<TableModel>(dataTable.getModel());
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {                
                if(o1 instanceof String || o2 instanceof String)
                   return o1.toString().compareToIgnoreCase(o2.toString());
                else return ((Comparable) o1).compareTo(o2);
            }
        };

        
        for(int i=0; i<dataTable.getModel().getColumnCount(); i++)
            sorter.setComparator(i, c);
        
        dataTable.setRowSorter(sorter);        
        //sorter.toggleSortOrder(0);
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }

    public void updateFilterTableAutoResize(){
        if(filterTable==null || dataTable==null)
            return;
        filterTable.setAutoResizeMode(dataTable.getAutoResizeMode());

    }

    public JScrollPane getTableScrollPane(){
        return tableScrollPane;
    }

    public void showQuickFilter(boolean show){
        filterScrollPane.setVisible(show);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterScrollPane = new javax.swing.JScrollPane();
        tableScrollPane = new javax.swing.JScrollPane();

        filterScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        filterScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        tableScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        tableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(filterScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(filterScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane filterScrollPane;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables

}
