package eclub.com.cmsnuxeo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class SearchNuxeoDocument {
    @JsonProperty("entity-type")
    public String entityType;
    public boolean isPaginable;
    public int resultsCount;
    public int pageSize;
    public int maxPageSize;
    public int resultsCountLimit;
    public int currentPageSize;
    public int currentPageIndex;
    public int currentPageOffset;
    public int numberOfPages;
    public boolean isPreviousPageAvailable;
    public boolean isNextPageAvailable;
    public boolean isLastPageAvailable;
    public boolean isSortable;
    public boolean hasError;
    public Object errorMessage;
    public int totalSize;
    public int pageIndex;
    public int pageCount;
    public ArrayList<NuxeoDocument> entries;
}
