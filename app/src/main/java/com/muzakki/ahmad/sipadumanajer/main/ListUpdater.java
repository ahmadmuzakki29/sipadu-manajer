package com.muzakki.ahmad.sipadumanajer.main;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jeki on 6/14/15.
 */
public interface ListUpdater {
    void showList();
    void addItemList(List<HashMap<String, String>> items);
    void removeItemList(String[] ids);
    void updateList(String id);
}
