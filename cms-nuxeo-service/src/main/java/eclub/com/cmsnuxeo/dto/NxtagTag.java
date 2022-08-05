package eclub.com.cmsnuxeo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NxtagTag {
    String username;
    String label;
    @JsonProperty("label")
    public String getLabel() {
        return this.label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    @JsonProperty("username")
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

}
