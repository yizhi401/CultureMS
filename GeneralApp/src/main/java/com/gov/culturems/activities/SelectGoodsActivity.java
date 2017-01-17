package com.gov.culturems.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import com.gov.culturems.R;
import com.gov.culturems.common.base.BaseActivity;
import com.gov.culturems.entities.Goods;
import com.gov.culturems.views.IndexableListView;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by peter on 17/01/2017.
 */

public class SelectGoodsActivity extends BaseActivity {

    private ArrayList<String> mItems;
    private IndexableListView mListView;

    private List<Goods> goodsList;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_goods);

        goodsList = (List<Goods>) getIntent().getSerializableExtra("goodsList");
        if (goodsList == null) {
            return;
        }

        mItems = new ArrayList<String>();
        for (Goods item : goodsList) {
            try {
                if (TextUtils.isEmpty(item.GoodsName)) {
                    continue;
                }
                char firstChar = item.GoodsName.charAt(0);
                String[] goodsName = PinyinHelper.toHanyuPinyinStringArray(firstChar);
                if (goodsName.length >= 1) {
                    mItems.add(goodsName[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        Collections.sort(mItems);

        ContentAdapter adapter = new ContentAdapter(this,
                android.R.layout.simple_list_item_1, mItems);

        mListView = (IndexableListView) findViewById(R.id.listview);
        mListView.setAdapter(adapter);
        mListView.setFastScrollEnabled(true);
    }

    private class ContentAdapter extends ArrayAdapter<String> implements SectionIndexer {

        private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        public ContentAdapter(Context context, int textViewResourceId,
                              List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public int getPositionForSection(int section) {
            // If there is no item for current section, previous section will be selected
            for (int i = section; i >= 0; i--) {
                for (int j = 0; j < getCount(); j++) {
                    if (i == 0) {
                        // For numeric section
                        for (int k = 0; k <= 9; k++) {
                            if (String.valueOf(getItem(j).charAt(0)).toLowerCase().equals(String.valueOf(mSections.charAt(i))))
                                return j;
                        }
                    } else {
                        if (String.valueOf(getItem(j).charAt(0)).toLowerCase().equals(String.valueOf(mSections.charAt(i))))
                            return j;
                    }
                }
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

        @Override
        public Object[] getSections() {
            String[] sections = new String[mSections.length()];
            for (int i = 0; i < mSections.length(); i++)
                sections[i] = String.valueOf(mSections.charAt(i));
            return sections;
        }
    }
}
