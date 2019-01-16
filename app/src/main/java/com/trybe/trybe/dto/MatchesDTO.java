package com.trybe.trybe.dto;

import java.util.Date;
import java.util.List;

/**
 * Created by gopi.ra on 29-Mar-16.
 */
public class MatchesDTO {

    public List<Match> SEEK;
    public List<Match> REFER;

    public static class Match {
        public int SW_ID;
        public Date SW_SWIPE_DT;
        public int JR_ID;
        public String JR_COMPANY;
        public String JR_POSITION;
        public String JR_LOCATION;
        public String JR_SALARY_MIN;
        public String JR_SALARY_MAX;
        public String UD_ID;
        public String UD_NAME;
        public String UD_IMG;
        public String UD_EMAIL;
        public String UD_RESUME;
    }
}