package com.trybe.trybe.dto;

import java.util.Date;
import java.util.List;

/**
 * Created by Gopi on 27-Mar-16.
 */
public class JobsPostedDTO {

    public List<JobPost> RESULTS;

    public static class JobPost {
        public String JR_COMPANY;
        public Date JR_CREATE_DT;
        public int JR_ID;
        public String JR_JC_ID;
        public String JR_LOCATION;
        public String JR_POSITION;
        public int JR_SALARY_MIN;
        public int JR_SALARY_MAX;
        public String JR_REMARK;
    }

}