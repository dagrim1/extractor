/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.ui;

import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author flip
 */
public class ExacTable extends JTable{
    TableColumn resizingColumn = null;
    boolean editable = false;  
    public String[] columnToolTips = new String[]{};
    
     public ExacTable() {
        super();        
    }
     
    public ExacTable(TableModel in){
        super(in);
        setModel(in);
        //doInit();        
    }
    
    
   
    public void setEditable(boolean editable){
        this.editable = editable ;
    }    
     //Implement table header tool tips.
    protected JTableHeader createDefaultTableHeader() 
    {        
        return new JTableHeader(columnModel)
        {
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = columnModel.getColumn(index).getModelIndex();
                if(realIndex<columnToolTips.length)
                    return columnToolTips[realIndex];
                else return null;
            }
        };
    }
    public boolean getScrollableTracksViewportWidth()
    {
        return getPreferredSize().width < getParent().getWidth();
    }
    
    @Override
    public void doLayout()
    {        
        TableColumn resizingColumn = null;
        
        if (tableHeader != null)
            resizingColumn = tableHeader.getResizingColumn();

        //  Viewport size changed. May need to increase columns widths
        if (resizingColumn == null)
        {
            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            super.doLayout();
        }

        //  Specific column resized. Reset preferred widths
        else
        {
            TableColumnModel tcm = getColumnModel();            

            for (int i = 0; i < tcm.getColumnCount(); i++)
            {
                TableColumn tc = tcm.getColumn(i);
                tc.setPreferredWidth( tc.getWidth() );
            }
            
            // Columns don't fill the viewport, invoke default layout
            if (tcm.getTotalColumnWidth() < getParent().getWidth())
                setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                super.doLayout();
        }

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }            
    
}
