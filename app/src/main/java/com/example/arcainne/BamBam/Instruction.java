package com.example.arcainne.BamBam;

/**
 * Created by Arcainne on 1/11/2015.
 */
public class Instruction {
    public final String performer;
    public final String task;
    public Instruction(String perfName, String action) {
        this.performer = perfName;
        this.task = action;
    }
}
