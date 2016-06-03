package com.gov.culturems.provider;

import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;

import com.gov.culturems.MyApplication;

/**
 * Created by peter on 2015/11/8.
 */
public class MySuggestionProvider extends SearchRecentSuggestionsProvider {

    public static final String ALL = "全部";

    public final static String AUTHORITY = "com.gov.baishaxi.MySuggestionProvider";

    public final static int MODE = DATABASE_MODE_QUERIES;

    private static MySuggestionProvider instance;

    public static MySuggestionProvider getInstance() {
        if (instance == null) {
            synchronized (MySuggestionProvider.class) {
                if (instance == null) {
                    instance = new MySuggestionProvider();
                }
            }
        }
        return instance;
    }

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    public void clearHistory() {
        Context context = MyApplication.getInstance().getApplicationContext();
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(context,
                MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
        suggestions.clearHistory();
    }

    public void saveQuery(String query) {
        Context context = MyApplication.getInstance().getApplicationContext();
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(context,
                MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
        suggestions.saveRecentQuery(query, null);
        saveSearchQueryAll();
    }

    /**
     * 为了保证每次都出现查询全部的选项，所以保存一下
     */
    private void saveSearchQueryAll() {
        Context context = MyApplication.getInstance().getApplicationContext();
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(context,
                MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
        suggestions.saveRecentQuery(MySuggestionProvider.ALL, null);
    }

}
