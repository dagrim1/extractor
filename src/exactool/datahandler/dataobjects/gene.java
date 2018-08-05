/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exactool.datahandler.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 *
 * @author flip
 */
public class gene {
    public String canonical_transcript;
    public String full_gene_name;
    public String omim_description;
    public String gene_name_upper;
    public String gene_name;
    public String[] other_names;
    public int start;
    public int stop;
    
    public String toString(){
        return gene_name_upper;
    }
}
