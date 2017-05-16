/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cameronjharvey;

import com.googlecode.jcsv.annotations.MapToColumn;
/**
 *
 * @author charv48
 */
public class LearningDataObject {
    @MapToColumn(column=0)
    private String type;
    @MapToColumn(column=1)
    private String id;
    @MapToColumn(column=2)
    private String name;
    @MapToColumn(column=3)
    private String unit;
    @MapToColumn(column=4)
    private int maximumParticipation;
    @MapToColumn(column=5)
    private String qceCategory;
    @MapToColumn(column=6)
    private String qceLiteracy;
    @MapToColumn(column=7)
    private String qceNumeracy;
    @MapToColumn(column=8)
    private int qceMaxcredit;
    @MapToColumn(column=9)
    private int originFile;
    
    public String getType(){
        return type;
    }
    public void setType(String val){
        this.type = val;
    }
    public String getId(){
        return id;
    }
    public void setId(String val){
        this.id = val;
    }
    public String getName(){
        return name;
    }
    public void setName(String val){
        this.name = val;
    }
    public String getUnit(){
        return unit;
    }
    public void setUnit(String val){
        this.unit = val;
    }
    public int getMaximumParticipation(){
        return maximumParticipation;
    }
    public void setMaximumParticipation(int val){
        this.maximumParticipation = val;
    }
    public String getQCECategory(){
        return qceCategory;
    }
    public void setQCECategory(String val){
        this.qceCategory = val;
    }
    public String getQCELiteracy(){
        return qceLiteracy;
    }
    public void setQCELiteracy(String val){
        this.qceLiteracy = val;
    }
    public String getQCENumeracy(){
        return qceNumeracy;
    }
    public void setQCENumeracy(String val){
        this.qceNumeracy = val;
    }
    public int getQCEMaxcredit(){
        return qceMaxcredit;
    }
    public void setQCEMaxcredit(int val){
        this.qceMaxcredit = val;
    }
    public int getOriginFile(){
        return originFile;
    }
    public void setOriginFile(int val){
        this.originFile = val;
    }    
}
