package com.gov.culturems.common.http;
import java.util.ArrayList;

/**
 * Created by peter on 2015/3/29.
 */
public class ListResponse<E> {
    private int rc;

    private String rm;
    private int TotalCount;
    private int PageCount;
    private int PageIndex;
    private int PageSize;

    private ArrayList<E> ListData;

    public int getRc() {
        return rc;
    }

    public void setRc(int rc) {
        this.rc = rc;
    }

    public String getRm() {
        return rm;
    }

    public void setRm(String rm) {
        this.rm = rm;
    }

    public int getTotalCount() {
        return TotalCount;
    }

    public void setTotalCount(int totalCount) {
        TotalCount = totalCount;
    }

    public int getPageIndex() {
        return PageIndex;
    }

    public void setPageIndex(int pageIndex) {
        PageIndex = pageIndex;
    }

    public int getPageSize() {
        return PageSize;
    }

    public void setPageSize(int pageSize) {
        PageSize = pageSize;
    }

    public ArrayList<E> getListData() {
        return ListData;
    }

    public void setListData(ArrayList<E> listData) {
        ListData = listData;
    }


}
