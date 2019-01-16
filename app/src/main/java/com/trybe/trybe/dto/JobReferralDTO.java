package com.trybe.trybe.dto;

import java.util.Date;
import java.util.List;

/**
 * Created by Gopi on 19-Feb-16.
 */
public class JobReferralDTO {

    public String BG_MSG;
    public List<JobReferral> Results;

    public static class JobReferral {

        public int JR_ID;
        public String JR_USER_ID;
        public String JR_COMPANY;
        public String JR_POSITION;
        public String JR_LOCATION;
        public int JR_SALARY_MIN;
        public int JR_SALARY_MAX;
        public String JR_REMARK;
        public String UD_ID;
        public String UD_NAME;
        public String UD_IMG;
        public String DW_NAME;
        public String DW_POSITION;
        public Date DW_START_DT;
        public String JC_CAT;
        public String JC_SUB_CAT;
    }
}