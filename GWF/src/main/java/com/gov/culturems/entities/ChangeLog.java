package com.gov.culturems.entities;

/**
 * Created by peter on 7/28/16.
 */
public class ChangeLog {

    public String name;
    public int version;
    public String changelog;
    public long updated_at;
    public String versionShort;
    public int build;
    public String installUrl;
    public String install_url;
    public String direct_install_url;
    public String update_url;
    public Binary binary;

    public class Binary {
        public long fsize;
    }

}
