package com.trybe.trybe.dto;

import java.util.Date;
import java.util.List;

/**
 * Created by gopi.ra on 29-Feb-16.
 */
public class UserDataWrapper {

    public List<DataSchool> data_school;
    public List<DataWork> data_work;
    public String data_skills;
    public String data_projects;
    public String data_sector;

    public static class DataSchool {

        public Integer DS_ID;
        public String DS_NAME;
        public String DS_BOARD;
        public String DS_SCORE;
        public Date DS_START_DT;
        public Date DS_END_DT;
    }

    public static class DataWork {

        public Integer DW_ID;
        public String DW_NAME;
        public String DW_POSITION;
        public Date DW_START_DT;
        public Date DW_END_DT;
    }
}