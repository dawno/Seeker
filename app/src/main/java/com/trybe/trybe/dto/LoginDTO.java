package com.trybe.trybe.dto;

/**
 * Created by gopi.ra on 29-Feb-16.
 */
public class LoginDTO {
    public boolean LOGINSTATUS;
    public boolean PROFILESTATUS;
    public boolean PREFERENCE_STATUS;
    public Boolean REGISTERSTATUS;
    public String UC_TOKEN;
    public UserDef user_def;
    public UserDataWrapper data;
    public JobPref job_seek;

    public LoginDTO() {
        user_def = new UserDef();
        data = new UserDataWrapper();
        job_seek = new JobPref();
    }
}