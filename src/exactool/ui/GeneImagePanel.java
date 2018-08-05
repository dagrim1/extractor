/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.ui;

import exactool.datahandler.BaseQuerier;
import exactool.datahandler.ExacQuerier;
import exactool.datahandler.ExternalVariant;
import exactool.datahandler.dataobjects.Feature;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JViewport;
import javax.swing.ToolTipManager;
/**
 *
 * @author flip
 */
public class GeneImagePanel extends javax.swing.JPanel {
    
    int displayedSize=0, xDrawStart=25, xDrawOffset=25;
    double canvasWidth = 0, originalCanvasWidth = 0, blockSize=0,  originalDisplayedWidth = 0;
    public boolean DRAW_FULL_UTR =false;
    public static double zoom_level=1.0;
    
    Object[][] displayedVariants = new Object[][]{};    
    Feature features[];
    ArrayList featureShapes = new ArrayList(), variantShapes = new ArrayList();
    Dimension preferredSize = new Dimension(0,0);
    Point zoom1, zoom2, panStart, viewStart, lastPoint;    
    boolean dragging = false;
    boolean trigger = true;
    GeneVisualizationPanel gvp = null;    
    BaseQuerier querier;
    //int chr = -1;
    String chr = null;
    GenePanelPopup popup;
    JFileChooser chooser = new JFileChooser();
    /**
     * Creates new form GeneImagePanel
     */
    public GeneImagePanel() {
        initComponents();
        popup = new GenePanelPopup(this);
        ToolTipManager.sharedInstance().setInitialDelay(100);
       
    }
    
    public void setUTRSizes(boolean fullUTR){
        DRAW_FULL_UTR = fullUTR;
        gvp.triggerZoomChange();
        //triggerResize();                
        //zoomOut(null, 0.99f);
    }
    
    public void setGeneVisualizationPanel(GeneVisualizationPanel gvp){
        this.gvp = gvp;
    }
    
    public void setZoomLevel(double level){
        setZoomLevel(level, false);
    }
    public void setZoomLevel(double level, boolean fromParent){
        zoom_level = level;        
        updatePreferredSize();
        
        if(!fromParent)
            gvp.setZoomLevel(level);
    }
    
    public double getZoomLevel(){
        return zoom_level;
    }
    public void setDisplayedVariants(Object[][] variants){
        displayedVariants = variants;
        //if(features.length==0)
        displayedSize = getDisplayedSize();        
        repaint();
    }
        
    //public void setFeatures(Object[][] features){ 
    public void setFeatures(Feature[] features){ 
        this.features = features;        
        //displayedSize = getDisplayedSize();        
        //repaint();
    }
    
    public int getDisplayedSize(){
        int result = 0;
        
        if(features.length>0){
            for(int fr=0; fr<features.length; fr++)
            {
                String type = features[fr].feature_type;
                int featureStart = features[fr].start;
                int featureEnd = features[fr].stop;
                        
                if(GeneShape.CDS.equalsIgnoreCase(type) || GeneShape.REGION.equalsIgnoreCase(type))
                    result = result+(featureEnd-featureStart)+GeneShape.INTRON_WIDTH;
                else if(GeneShape.SINGLE_VARIANT.equalsIgnoreCase(type) && featureStart==-1){
                    int minPos = -1, maxPos = -1;
                    for(int r=0; r<displayedVariants.length; r++){
                        int startpos = (Integer)displayedVariants[r][querier.getVarIndex(querier.OUTPUT_HEADER_START_POS)];
                        if(minPos==-1 || startpos<minPos)
                           minPos = startpos; 
                        int endpos = startpos+1;
                        if(maxPos==-1 || endpos>maxPos)
                           maxPos = endpos; 
                    }
                    featureStart = minPos-50;
                    featureEnd = maxPos+50;
                    features[fr].start = featureStart;
                    features[fr].stop = featureEnd;
            
                }
                else if(GeneShape.UTR.equalsIgnoreCase(type))
                {
                    if(DRAW_FULL_UTR || (featureEnd-featureStart)<GeneShape.UTR_WIDTH)
                        result = result+(featureEnd-featureStart);//+utrSize;
                    else result = result+GeneShape.UTR_WIDTH;//+intronSize;
                } 
            }
            if(result>0)
                result = result-GeneShape.INTRON_WIDTH;
        }
        //else
        if(result==0)
        {
            /*
            //fix when no feautre info is retrieved, use variants for display size estimations
            int minPos = -1, maxPos = -1;
            for(int r=0; r<displayedVariants.length; r++){
                int startpos = (Integer)displayedVariants[r][querier.getVarIndex(querier.OUTPUT_HEADER_START_POS)];
                if(minPos==-1 || startpos<minPos)
                   minPos = startpos; 
                int endpos = startpos+1;
                if(maxPos==-1 || endpos>maxPos)
                   maxPos = endpos; 
            }
            
            result = maxPos-minPos - 100;
            */
            result = 100;
        }
        return result;
    }
        
    public void drawFeatures(Graphics g){
        featureShapes.clear();
        blockSize = (canvasWidth*zoom_level)/displayedSize;
        boolean drawIntron = false;   
        
        int cdsCount = 1;
        if(features==null)
            return;
        
        for(int fr=0; fr<features.length; fr++)
        {            
            GeneShape s = new GeneShape();
            //s.setType((String)features[fr][1]);
            s.setType(features[fr].feature_type);
            if(s.DRAW_SHAPE)
            {                                
                if(GeneShape.SINGLE_VARIANT.equals(s.TYPE))
                    s.shapeColor = GeneShape.SINGLE_VARIANT_FEATURE_COLOR;
                else s.shapeColor = GeneShape.DEFAULT_FEATURE_COLOR;
                //s.bpStart = (Integer)features[fr][2];
                //s.bpEnd = (Integer)features[fr][3];
                s.bpStart = features[fr].start;
                s.bpEnd = features[fr].stop;
                
                s.setStringRepresentation(s.TYPE+" ["+s.bpStart+"-"+s.bpEnd+"]");
                s.shapeStartY = (int)(this.getHeight()-s.shapeHeight)/2;
                s.shapeWidth = (int)((s.bpEnd-s.bpStart)*blockSize);            
                if(GeneShape.UTR.equals(s.TYPE))
                {
                    int fixedUtrWidth = (int)(s.UTR_WIDTH*blockSize);
                    if(!this.DRAW_FULL_UTR && fixedUtrWidth<s.shapeWidth)
                    {
                        s.shapeWidth = fixedUtrWidth;
                    }
                    if(drawIntron)
                        xDrawOffset = xDrawOffset - (int)(s.INTRON_WIDTH*blockSize);
                }
                s.shapeStartX = (int)(xDrawOffset);                                    
                s.draw(g);
                
                featureShapes.add(s);                
                xDrawOffset = xDrawOffset+s.shapeWidth;

                drawIntron = GeneShape.CDS.equalsIgnoreCase(s.TYPE) && fr<features.length-2;                
                if(drawIntron)
                {
                    StringBuffer htmlString = new StringBuffer("<html>");
                    htmlString.append("Type:  ").append(s.TYPE).append("<br>");
                    htmlString.append("Number:  ").append(cdsCount).append("<br>");
                    htmlString.append("Start-End:  ").append(s.bpStart).append("-").append(s.bpEnd).append("<br>");
                    htmlString.append("</html>");
                    s.setStringRepresentation(htmlString.toString());
                    
                    cdsCount++;
                    GeneShape intron = new GeneShape();
                    intron.setType(GeneShape.INTRON);                    
                    intron.shapeStartY  = (int)(this.getHeight()-intron.shapeHeight)/2;
                    intron.shapeWidth = (int)(intron.INTRON_WIDTH*blockSize);            
                    intron.shapeStartX = xDrawOffset;
                    intron.draw(g);                    
                    xDrawOffset = xDrawOffset+intron.shapeWidth;                    
                }                            
            }
        }
    }
    
    public void drawExternalVariants(Graphics g){
        Iterator it = gvp.externalVariants.iterator();
        while(it.hasNext())
        {
            ExternalVariant v = (ExternalVariant)it.next();
            
            //if(chr==v.chr)
            if(v.chr.equals(chr))
            {
                GeneShape vs = new GeneShape();
                
                vs.setType(GeneShape.VARIANT);                        
                int startpos = v.start ;           
                vs.bpStart = startpos;
                vs.bpEnd = startpos+1;
                vs.shapeColor = GeneShape.EXTERNAL_VARIANT_COLOR;//Color.BLUE;
                v.id = v.chr+":"+v.start+"-"+v.end;
                vs.shapeStartY =  (int)(this.getHeight()-vs.shapeHeight)/4;
                vs.setStringRepresentation(v.toString());
                                
                boolean inFeature = false;            
                for(int i=0; i<featureShapes.size();i++){
                    GeneShape fs = (GeneShape)featureShapes.get(i);

                    if(fs.bpStart<=vs.bpStart && fs.bpEnd>=vs.bpStart && fs.DRAW_SHAPE)
                    {
                        vs.shapeStartX = (int)(fs.shapeStartX+(vs.bpStart-fs.bpStart)*blockSize)-vs.shapeWidth;                    
                        inFeature = true;
                        break;
                    }
                    else if(vs.bpStart>fs.bpEnd && !inFeature)
                    {
                        //take end of drawable or start of next fs as boundary
                        int x1 = fs.shapeStartX+fs.shapeWidth;
                        int x2 = this.getWidth();
                        int bp2 = 1;
                        if(i<featureShapes.size()-1)
                        {
                            x2 = ((GeneShape)featureShapes.get(i+1)).shapeStartX;
                            bp2 =((GeneShape)featureShapes.get(i+1)).bpStart;
                        }

                        double intronBlockSize = (x2-x1)/(bp2-fs.bpEnd);                    
                        vs.shapeStartX = (int)(fs.shapeStartX+fs.shapeWidth+(vs.bpStart-fs.bpEnd)*intronBlockSize);

                        //was not correct, intron variant can be placed in following exon
                        //vs.shapeStartX = (int)(fs.shapeStartX+(vs.bpStart-fs.bpStart)*blockSize)-vs.shapeWidth;
                    }                
                }           
                vs.draw(g);         
                variantShapes.add(vs);            
            }
        }        
    }
    
    //features just use their sizes, variants work differently (use offset from feature start?)
    public void drawVariants(Graphics g)
    {
        variantShapes.clear();
        ArrayList lof = new ArrayList();
        for(int r=0; r<displayedVariants.length; r++){
            GeneShape vs = new GeneShape();
            
            vs.setType(GeneShape.VARIANT);                        
            //if(chr==-1)
            //    chr = Integer.parseInt((String)displayedVariants[r][querier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CHR)]);
                
            if(chr==null)
                chr = (String)displayedVariants[r][querier.getVarIndex(BaseQuerier.OUTPUT_HEADER_CHR)];
            
            int startpos = (Integer)displayedVariants[r][querier.getVarIndex(querier.OUTPUT_HEADER_START_POS)];
            vs.bpStart = startpos;
            vs.bpEnd = startpos+1;
            vs.shapeColor = GeneShape.GENERAL_VARIANT_COLOR;//Color.YELLOW;
            vs.id = (String)displayedVariants[r][querier.getVarIndex(querier.OUTPUT_HEADER_EXAC_ID)];
            Object cat = displayedVariants[r][querier.getVarIndex(querier.OUTPUT_HEADER_CATEGORY)];
            if(ExacQuerier.EXAC_LOF_TYPE_VALUE.equals(cat))
                vs.shapeColor = GeneShape.LOF_VARIANT_COLOR;//Color.RED;
            if(ExacQuerier.EXAC_MISSENSE_TYPE_VALUE.equals(cat))
                vs.shapeColor = GeneShape.MISSENSE_VARIANT_COLOR;//Color.ORANGE;
            
            vs.shapeStartY =  (int)(this.getHeight()-vs.shapeHeight)/2;
            StringBuffer htmlString = new StringBuffer("<html>");
            htmlString.append("Exac id:  ").append((String)displayedVariants[r][querier.getVarIndex(querier.OUTPUT_HEADER_EXAC_ID)]).append("<br>");
            htmlString.append("Annotation:  ").append((String)displayedVariants[r][querier.getVarIndex(querier.OUTPUT_HEADER_MAJOR_CONSEQUENCE)]).append("<br>");
            htmlString.append("Consequence:  ").append((String)displayedVariants[r][querier.getVarIndex(querier.OUTPUT_HEADER_CONSEQUENCE)]).append("<br>");
            htmlString.append("</html>");
            vs.setStringRepresentation(htmlString.toString());
                    
            boolean inFeature = false;            
            for(int i=0; i<featureShapes.size();i++){
                GeneShape fs = (GeneShape)featureShapes.get(i);
                
                if(fs.bpStart<=vs.bpStart && fs.bpEnd>=vs.bpStart && fs.DRAW_SHAPE)
                {
                    if(i==0 && fs.TYPE.equals(GeneShape.UTR))
                        //vs.shapeStartX = (int)(fs.shapeStartX+(vs.bpStart-fs.bpStart)*blockSize)-vs.shapeWidth;                    
                        vs.shapeStartX = (int)(fs.shapeStartX+fs.shapeWidth-(fs.bpEnd-vs.bpStart)-vs.shapeWidth);                    
                    else vs.shapeStartX = (int)(fs.shapeStartX+(vs.bpStart-fs.bpStart)*blockSize)-vs.shapeWidth;                    
                    inFeature = true;
                    break;
                }
                else if(vs.bpStart>fs.bpEnd && !inFeature)
                {
                    //take end of drawable or start of next fs as boundary
                    int x1 = fs.shapeStartX+fs.shapeWidth;
                    int x2 = this.getWidth();
                    int bp2 = 1;
                    if(i<featureShapes.size()-1)
                    {
                        x2 = ((GeneShape)featureShapes.get(i+1)).shapeStartX;
                        bp2 =((GeneShape)featureShapes.get(i+1)).bpStart;
                    }
                    
                    double intronBlockSize = (x2-x1)/(bp2-fs.bpEnd);                    
                    vs.shapeStartX = (int)(fs.shapeStartX+fs.shapeWidth+(vs.bpStart-fs.bpEnd)*intronBlockSize);
                                            
                    //was not correct, intron variant can be placed in following exon
                    //vs.shapeStartX = (int)(fs.shapeStartX+(vs.bpStart-fs.bpStart)*blockSize)-vs.shapeWidth;
                }                
            }           
                        
            if(featureShapes.size()==0){
                vs.shapeStartX = xDrawStart+(int)((vs.bpStart-xDrawStart)*blockSize);
            }
            if(ExacQuerier.EXAC_LOF_TYPE_VALUE.equals(cat))
                lof.add(vs);
            else vs.draw(g);            
            variantShapes.add(vs);
        }        
        for(int l=0; l<lof.size(); l++){
            GeneShape s = (GeneShape)lof.get(l);
            s.draw(g);
        }
    }
        
    public void zoomToRegionBp(int bpStart, int bpEnd){                
        if(featureShapes.size()==0)
            drawFeatures(this.getGraphics());
        
        boolean startInFeature = false, endInFeature = false;
        int zoomStart=0, zoomEnd = 0;
        for(int i=0; i<featureShapes.size();i++)
        {
            GeneShape gs = (GeneShape)featureShapes.get(i);

            if(gs.bpStart<=bpStart && gs.bpEnd>=bpStart && gs.DRAW_SHAPE)
            {
                zoomStart = (int)(gs.shapeStartX+(bpStart-gs.bpStart)*blockSize);                    
                startInFeature = true;
                //break;
            }
            else if(bpStart>gs.bpEnd && !startInFeature)
            {
                zoomStart = (int)(gs.shapeStartX+(bpStart-gs.bpStart)*blockSize);
            }     
            
            if(gs.bpStart<=bpEnd && gs.bpEnd>=bpEnd && gs.DRAW_SHAPE)
            {
                zoomEnd = (int)(gs.shapeStartX+(bpEnd-gs.bpStart)*blockSize);                    
                endInFeature = true;
                //break;
            }
            else if(bpEnd>gs.bpEnd && !endInFeature)
            {
                zoomEnd = (int)(gs.shapeStartX+(bpEnd-gs.bpStart)*blockSize);
            }     
            if(startInFeature && endInFeature)
                break;
        }           
         
        zoomToRegionPixel(zoomStart, zoomEnd);
        
        
    }
    public void paintComponent(Graphics g) {        
        super.paintComponent(g);             
       
        xDrawOffset = xDrawStart;                
        canvasWidth = originalCanvasWidth-(2*xDrawStart);
        
        //if(features==null)  
        //    return;
        drawFeatures(g);        
        drawVariants(g);
        drawExternalVariants(g);
        if(dragging && zoom1!=null && zoom2!=null){
            int x = zoom1.x;
            int w = zoom2.x-zoom1.x;
            int y = zoom1.y;
            int h = zoom2.y-zoom1.y;
            
            if(zoom2.x<zoom1.x){
                x = zoom2.x;
                w = zoom1.x-zoom2.x;
            }
            
            if(zoom2.y<zoom1.y){
                y = zoom2.y;
                h = zoom1.y-zoom2.y;
            }
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, w, h);
        }
        
        if(gvp!=null && gvp.getSelectedItem()!=null){        
            g.setColor(Color.BLACK);            
            g.drawString(gvp.getSelectedItem(), this.getVisibleRect().x+this.getVisibleRect().width/2-g.getFontMetrics().stringWidth(gvp.getSelectedItem())/2, 50);            
        }
    }  
    
    private void updatePreferredSize() {        
        preferredSize.setSize( (int) (originalCanvasWidth * zoom_level), getHeight());
        getParent().doLayout();        
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }        

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setForeground(new java.awt.Color(153, 204, 255));
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });
        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                formMouseWheelMoved(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        triggerResize();
        
    }//GEN-LAST:event_formComponentResized
    
    public void triggerResize(){       
        double currentDisplayedWidth = ((JViewport)this.getParent()).getVisibleRect().getWidth();
        if(originalDisplayedWidth==0)
            originalDisplayedWidth = currentDisplayedWidth;
        if(originalDisplayedWidth != currentDisplayedWidth){
            //System.out.println("Current vs new: "+originalDisplayedWidth+", "+((JViewport)this.getParent()).getVisibleRect().getWidth());            
            //zoom(null, (float)(currentDisplayedWidth/originalDisplayedWidth), 1.0f, 20.0f);
        }
        
        originalCanvasWidth = this.getWidth()/zoom_level;
        canvasWidth = originalCanvasWidth;
        //System.out.println(System.currentTimeMillis()+": Resized canvas: "+this.getVisibleRect()+". Visible rectangle from parent: "+((JViewport)this.getParent()).getVisibleRect());
        
        getParent().doLayout();
        validate();
        repaint();        
        originalDisplayedWidth = ((JViewport)this.getParent()).getVisibleRect().getWidth();
    }
    /**
     * Adjusts current zoom by factor using the point as reference, not going below minzoom and not going above maxzoom...
     * 
     * @param point The Point used as zoom center, if null the current center is used
     * @param factor The factor by which to multiply the current zoom
     * @param minzoom The minimal total zoom factor (usually 1)
     * @param maxzoom  The maximum total zoom factor (usually 20)
     */
    public void zoom(Point point, float factor, float minzoom, float maxzoom){
        double current_zoom =  this.getZoomLevel();
        if(point==null){
            //use center
            point = new Point(this.getVisibleRect().x+(this.getVisibleRect().width/2), 0);
        }
        if( (current_zoom>1.0&&factor<1.0) || (current_zoom<20.0 && factor>1.0))
        {
            current_zoom = current_zoom*factor;
            if(current_zoom<minzoom)
                current_zoom=minzoom;
            else if(current_zoom>maxzoom)
                current_zoom=maxzoom;
            
            setZoomLevel(current_zoom);
            Point pos = ((JViewport)this.getParent()).getViewPosition();        
            int dx = point.x-pos.x;
            //int newX = (int)(point.x*(0.9f - 1f) + 0.9f*pos.x);
            int newX = (int)(point.x*factor-dx);
            ((JViewport)this.getParent()).setViewPosition(new Point(newX, pos.y));

            revalidate();
            repaint();            
        }
    }

    public void zoomIn(Point point, float factor){
        zoom(point, factor, 1f, 20f);
    }
    
    public void zoomOut(Point point, float factor){
        zoom(point, factor, 1f, 20f);
    }
    
    public GeneShape getVariantAtPoint(Point p){
        GeneShape vs = null;
        
        for(int v=0; v<variantShapes.size(); v++){            
            vs = (GeneShape)variantShapes.get(v);
            if(vs.containsPoint(p))
                break;
            else vs = null;
        }        
        return vs;
    }
    
    public GeneShape getFeatureAtPoint(Point p){
        GeneShape vs = null;
        
        for(int f=0; f<featureShapes.size(); f++){            
            vs = (GeneShape)featureShapes.get(f);
            if(vs.containsPoint(p))
                break;            
            else vs = null;
        }                   
        return vs;
    }
    
    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        java.awt.Point p = evt.getPoint();
        GeneShape vs = getVariantAtPoint(p);        
        if(vs==null)
            vs = getFeatureAtPoint(p);         
        if(vs!=null)
            this.setToolTipText(vs.toString());
        else this.setToolTipText(null);
    }//GEN-LAST:event_formMouseMoved

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        
        Point point = evt.getPoint();
                
        if(evt.isControlDown()){
            //zoom
            if(dragging){
                zoom2 = point;                
            }
        }
        else if(lastPoint!=null && point.x != lastPoint.x && false)
        {
            Point viewPos = ((JViewport)this.getParent()).getViewPosition();
            
            double dx = point.x-panStart.x;//+(viewPos.x-viewStart.x);         
            double newX = viewStart.x-dx;
            double newY = viewStart.y;
            //System.out.println("Viewpos: "+viewPos+".Viewstart: "+viewStart+". PanStartPos: "+panStart+". Currentpos: "+point+". Delta: "+dx+". NewX: "+newX);
            
            if(newX<0)
                newX = 0;
            //System.out.println("NewX: "+newX+". AsInt: "+(int)newX);
            ((JViewport)this.getParent()).setViewPosition(new Point((int)newX, (int)newY));           
        }
        lastPoint = point;
        revalidate();
        repaint();
        
    }//GEN-LAST:event_formMouseDragged

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        if(!dragging)
        {
            dragging = true;
            if(evt.isControlDown())
            {
                zoom1 = evt.getPoint();
            }
            else
            {
                panStart = evt.getPoint();
                viewStart =  ((JViewport)this.getParent()).getViewPosition();
            }
        }
        
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        if(dragging && evt.isControlDown()){ 
            zoomToRegionPixel(zoom1.x, zoom2.x);           
        }
        dragging = false;
        
    }//GEN-LAST:event_formMouseReleased

    private void zoomToRegionPixel(int x1, int x2){
        double px1 = this.getVisibleRect().x;
        double px2 = px1+this.getVisibleRect().width;            
        double zoom = (px2-px1)/(x2-x1);

        setZoomLevel(zoom_level*zoom);

        //correction?        
        int newx = (int)((x1*zoom) - (25*zoom) + 25);
        ((JViewport)this.getParent()).setViewPosition(new Point(newx, 0));

        repaint();
    }
    private void formMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_formMouseWheelMoved
        if(evt.isControlDown()){
            int notches = evt.getWheelRotation();
            if(notches<0)                
                zoomIn(evt.getPoint(), 1.1f);
            else zoomOut(evt.getPoint(), 0.9f);
        }
    }//GEN-LAST:event_formMouseWheelMoved

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        if(evt.getClickCount()==2){
            GeneShape variant = getVariantAtPoint(evt.getPoint());
            if(variant!=null)
                gvp.parent.selectVariant(variant.id);
        }
        else if(evt.getButton()!=MouseEvent.BUTTON1){
            popup.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_formMouseClicked

    public void saveImage(String extension){
        if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            this.paint(g);
            try {                
                File file= chooser.getSelectedFile();
                if(!file.getName().endsWith("."+extension))
                    file = new File(file.getPath()+"."+extension);
                ImageIO.write(image, extension, file);
            } catch (IOException ex) {
                //Logger.getLogger(CustomApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

/*
class Feature{
    String type;
    int bpStart, bpEnd;
    
    public Feature(String type, int start, int end){
        this.type = type;
        this.bpStart = start;
        this.bpEnd = end;        
    }
}
*/


