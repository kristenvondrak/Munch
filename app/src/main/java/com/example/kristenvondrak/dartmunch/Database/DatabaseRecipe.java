package com.example.kristenvondrak.dartmunch.Database;

/**
 * Created by kristenvondrak on 2/10/16.
 */
public class DatabaseRecipe {

    private String m_Group;
    private String m_Name;
    private String m_NBDNo;

    public DatabaseRecipe(String group, String name, String ndbno) {
        m_Group = group;
        m_Name = name;
        m_NBDNo = ndbno;
    }

    public String getName() {
        return m_Name;
    }

    public String getNDBNo() {
        return m_NBDNo;
    }

    public String getGroup() {
        return m_Group;
    }

}
