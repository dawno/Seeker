package com.trybe.trybe.app;

/**
 * Created by Gopi on 13-Apr-16.
 */
public class Config {

    public static class Server {
        public static final String SERVER_BASE_URL = "http://www.trybe.in/Request/";
        public static final String T_AND_C_URL = "http://www.trybe.in/files/TnC/signup.html";
        public static final String PRIVACY_POLICY_URL = "http://www.trybe.in/files/tnc/privacypolicy.html";
        public static final String SERVER_ERROR_REPORTING_URL = "http://www.trybe.in/appCrashLogs.php";
        public static final String SERVER_APP_LINK = "http://www.trybe.in/App/Share/Gplay.php";
        public static final String SERVER_APP_OPEN_URL = SERVER_BASE_URL + "AppOpen.php";
        public static final String SERVER_APP_CLOSE_URL = SERVER_BASE_URL + "AppClose.php";
        public static final String SERVER_PASS_RESET = SERVER_BASE_URL + "PasswordReset.php";

        public static final String SERVER_REQUESTS_URL = SERVER_BASE_URL + "Requests.php";
        public static final String SERVER_LOGIN_URL = SERVER_BASE_URL + "Login.php";
        public static final String SERVER_LOGOUT_URL = SERVER_BASE_URL + "Applogout.php";
        public static final String SERVER_REGISTER_URL = SERVER_BASE_URL + "Register.php";
        public static final String PROFILE_REQUESTS_URL = SERVER_BASE_URL + "ProfileRequests.php";
        public static final String CHAT_REQUESTS_URL = SERVER_BASE_URL + "ChatRequests.php";
        public static final String FILE_UPLOAD_URL = SERVER_BASE_URL + "FileUpload.php";
        public static final String FETCH_SEEKERS_URL = SERVER_BASE_URL + "GetSeekers.php";
        public static final String FETCH_JOBS_URL = SERVER_BASE_URL + "GetJobReferrals.php";
        public static final String FETCH_MATCHES_URL = SERVER_BASE_URL + "GetMatched.php";
    }

    // flag to identify whether to show single line
    // or multi line text in push notification tray
    public static boolean appendNotificationMessages = true;

    // broadcast receiver intent filters
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // type of push messages i.e flag from server
    public static final String PUSH_TYPE_CHAT = "CHAT";
    public static final String PUSH_TYPE_NOTIF = "NOT1";
    public static final String PUSH_TYPE_MATCH = "MATCH";
    public static final String PUSH_TYPE_UPDATE = "UPDATE";

    // id to handle the notification in the notification try
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;
}
