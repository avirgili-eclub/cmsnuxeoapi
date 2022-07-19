package eclub.com.cmsnuxeo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;

public class Properties {
    @JsonProperty("uid:uid")
    public Object uidUid;
    @JsonProperty("uid:major_version")
    public int uidMajorVersion;
    @JsonProperty("uid:minor_version")
    public int uidMinorVersion;
    @JsonProperty("thumb:thumbnail")
    public Object thumbThumbnail;
    @JsonProperty("file:content")
    public FileContent fileContent;
    @JsonProperty("common:icon-expanded")
    public Object commonIconExpanded;
    @JsonProperty("common:icon")
    public String commonIcon;
    @JsonProperty("files:files")
    public ArrayList<Object> filesFiles;
    @JsonProperty("dc:description")
    public Object dcDescription;
    @JsonProperty("dc:language")
    public Object dcLanguage;
    @JsonProperty("dc:coverage")
    public Object dcCoverage;
    @JsonProperty("dc:valid")
    public Object dcValid;
    @JsonProperty("dc:creator")
    public String dcCreator;
    @JsonProperty("dc:modified")
    public Date dcModified;
    @JsonProperty("dc:lastContributor")
    public String dcLastContributor;
    @JsonProperty("dc:rights")
    public Object dcRights;
    @JsonProperty("dc:expired")
    public Object dcExpired;
    @JsonProperty("dc:format")
    public Object dcFormat;
    @JsonProperty("dc:created")
    public Date dcCreated;
    @JsonProperty("dc:title")
    public String dcTitle;
    @JsonProperty("dc:issued")
    public Object dcIssued;
    @JsonProperty("dc:nature")
    public Object dcNature;
    @JsonProperty("dc:subjects")
    public ArrayList<Object> dcSubjects;
    @JsonProperty("dc:contributors")
    public ArrayList<String> dcContributors;
    @JsonProperty("dc:source")
    public Object dcSource;
    @JsonProperty("dc:publisher")
    public Object dcPublisher;
    @JsonProperty("relatedtext:relatedtextresources")
    public ArrayList<Object> relatedtextRelatedtextresources;
    @JsonProperty("nxtag:tags")
    public ArrayList<Object> nxtagTags;
}
