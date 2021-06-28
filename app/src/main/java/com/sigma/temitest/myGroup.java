package com.sigma.temitest;

import java.util.ArrayList;
import java.util.List;

public class myGroup {
    public String groupName;
    public List<String> child;

    public myGroup(String name) {
        groupName = name;
        child = new ArrayList<String>();
    }
}
