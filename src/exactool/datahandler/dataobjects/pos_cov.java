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
public class pos_cov {
    public int pos;
    public double mean;
    public double median;
}
