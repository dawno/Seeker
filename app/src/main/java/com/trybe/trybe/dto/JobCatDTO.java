package com.trybe.trybe.dto;

import java.util.List;

/**
 * Created by gopi.ra on 14-Mar-16.
 */
public class JobCatDTO {

    public List<JobCat> RESULTS;

    public static class JobCat {

        public int JC_ID;
        public String JC_CAT;
        public String JC_SUB_CAT;
    }
}