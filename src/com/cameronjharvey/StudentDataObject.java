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
public class StudentDataObject {
    @MapToColumn(column=0)
    private long lui;
    @MapToColumn(column=1)
    private String subject;
    @MapToColumn(column=2)
    private String result;
    @MapToColumn(column=3)
    private float participation;

    /**
     * @return the LUI
     */
    public long getLUI() {
        return lui;
    }

    /**
     * @param lui
     */
    public void setLUI(long lui) {
        this.lui = lui;
    }

    /**
     * @return the Subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the Result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the Participation
     */
    public float getParticipation() {
        return participation;
    }

    /**
     * @param participation
     */
    public void setParticipation(float participation) {
        this.participation = participation;
    }
}
