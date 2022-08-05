package eclub.com.cmsnuxeo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
public class ApplicationEclub {
    @JsonProperty("application_number")
    private String applicationNumber;
    @JsonProperty("application_type")
    @Enumerated(EnumType.ORDINAL)
    private ApplicationType applicationType;

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

}
