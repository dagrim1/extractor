/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author flip
 */
public class GeneShape {
    public static int INTRON_WIDTH = 25;
    public static int UTR_WIDTH=200;
    public static int VAR_HEIGHT=8;
    public static int VAR_WIDTH=4;
    public static int UTR_HEIGHT = 40;
    public static int CDS_HEIGHT = 80;
    public static int EXON_HEIGHT = 80;
    public static int INTRON_HEIGHT = 10;
    public static int REGION_HEIGHT=8;
    
            
    public final static String CDS = "CDS";
    public final static String UTR = "UTR";
    public final static String EXON = "EXON";
    public final static String INTRON = "INTRON";
    public final static String VARIANT = "VARIANT";    
    public final static String REGION = "REGION";    
    public final static String SINGLE_VARIANT = "SINGLE_VARIANT";
    
    public static Color SINGLE_VARIANT_FEATURE_COLOR = Color.WHITE;
    public static Color EXTERNAL_VARIANT_COLOR = Color.BLUE;
    public static Color GENERAL_VARIANT_COLOR = Color.YELLOW;
    public static Color MISSENSE_VARIANT_COLOR = Color.ORANGE;
    public static Color LOF_VARIANT_COLOR = Color.RED;
    public static Color DEFAULT_FEATURE_COLOR = new java.awt.Color(153,204,255); //default exon, intron and utr Color
    public String TYPE = "EXON";
    
    public int bpStart, bpEnd, shapeStartX, shapeStartY, shapeWidth, shapeHeight=40;    
    //public Color shapeColor = new java.awt.Color(153,204,255); 
    public Color shapeColor = DEFAULT_FEATURE_COLOR;
    public boolean DRAW_SHAPE = true;
    Shape shape;// = new Ellipse2D.Double(shapeStartX, shapeStartY, shapeWidth, shapeHeight);        
    String stringRepresentation = null;
    public int specificIndex = 1;
    public String id = "";
    
    public void GeneShape(String type, int start, int end){
        this.TYPE = type;
        this.bpStart = start;
        this.bpEnd = end;
        stringRepresentation = TYPE+" ["+bpStart+"-"+bpEnd+"]";
    }
    
    public void draw(Graphics g){
        //skip exons (they are either the same as the CDS or CDS+UTR
        if(GeneShape.EXON.equalsIgnoreCase(TYPE))
            return;
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(shapeColor);        
        if(!GeneShape.VARIANT.equalsIgnoreCase(TYPE)){
            shape = new Rectangle2D.Double(shapeStartX, shapeStartY, shapeWidth, shapeHeight);                    
        }
        else 
        {
            //shape = new Rectangle2D.Double(shapeStartX, shapeStartY, 2, shapeHeight);        
            shape = new Ellipse2D.Double(shapeStartX, shapeStartY, shapeWidth, shapeHeight);
            
            
        }        
        
        g2.fill(shape);
    }
    
    public boolean containsPoint(Point p){
        if(shape==null)
            return false;
        return shape.contains(p);
    }
    
    public void setType(String type){
        TYPE = type;
        
        if(GeneShape.CDS.equalsIgnoreCase(type)){
            shapeHeight = CDS_HEIGHT;
        }
        else if(GeneShape.UTR.equalsIgnoreCase(type)){
            shapeWidth = UTR_WIDTH;
            shapeHeight = UTR_HEIGHT;
            
        }
        else if(GeneShape.EXON.equalsIgnoreCase(type)){
            shapeHeight = EXON_HEIGHT;
            DRAW_SHAPE = false;
        }
        else if(GeneShape.INTRON.equalsIgnoreCase(type)){
            shapeWidth = INTRON_WIDTH;
            shapeHeight = INTRON_HEIGHT;
        }
        else if(GeneShape.VARIANT.equalsIgnoreCase(type)){
            shapeWidth = VAR_WIDTH;
            shapeHeight = VAR_HEIGHT;            
        }
        else if(GeneShape.REGION.equalsIgnoreCase(type)){            
            shapeHeight = REGION_HEIGHT;            
        }
    }
    
    public void setStringRepresentation(String representation){
        stringRepresentation = representation;
    }
    public String toString(){
        return stringRepresentation;
    }
}
