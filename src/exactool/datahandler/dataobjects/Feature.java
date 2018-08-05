/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

/**
 *
 * @author flip
 */
public class Feature {
    public String gene;
    public String chrom;
    public String feature_type;
    public int start;
    public int stop;
    
    //public String search_term;
}
