package com.trybe.trybe.dto;

import java.util.Date;
import java.util.List;

/**
 * Created by Gopi on 16-Mar-16.
 */
public class JobSeekerDTO {

    public List<JobSeeker> RESULTS;

    public static class JobSeeker {

        public int SW_ID;
        public int JR_ID;
        public String JR_COMPANY;
        public String JR_POSITION;
        public String JR_LOCATION;
        public Date SW_SWIPE_DT;
        public UserDef user_def;
        public UserDataWrapper data;

        public JobSeeker() {
            user_def = new UserDef();
            data = new UserDataWrapper();
        }
    }
}