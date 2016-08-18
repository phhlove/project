package com.jredrain.startup;


import java.io.File;

public final class Globals {

    /**
     * Name of the system property containing
     */
    public static final String REDRAIN_HOME = "redrain.home";

    /**
     * port
     */
    public static String REDRAIN_PORT = System.getProperty("redrain.port");

    /**
     * password
     */
    public static String REDRAIN_PASSWORD =System.getProperty("redrain.password");

    /**
     * password file
     */

    public static File REDRAIN_PASSWORD_FILE = new File(System.getProperty(REDRAIN_HOME) + File.separator + ".password");

    /**
     *
     * conf file
     */

    public static File REDRAIN_CONF_FILE = new File(System.getProperty(REDRAIN_HOME) + File.separator + "/conf/redrain.properties");

    /**
     * pid
     */
    public static File REDRAIN_PID_FILE = new File(System.getProperty(REDRAIN_HOME) + File.separator + "redrain.pid");


    /**
     * monitor file
     */
    public static File REDRAIN_MONITOR_SHELL = new File(System.getProperty(REDRAIN_HOME) + "/bin/monitor.sh");

    /**
     * kill file
     */
    public static File REDRAIN_KILL_SHELL = new File(System.getProperty(REDRAIN_HOME) + "/bin/kill.sh");

}
