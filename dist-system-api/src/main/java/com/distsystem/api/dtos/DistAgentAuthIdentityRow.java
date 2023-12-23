package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * */
public class DistAgentAuthIdentityRow extends BaseRow {

    private final String identityName;
    private final String identityType;
    private final String identityAttributes;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    // create table distagentauthidentity(identityname text not null, identitytype text not null, identityattributes text not null, isactive int not null, createddate timestamp not null)

    public DistAgentAuthIdentityRow(String identityName, String identityType, String identityAttributes, int isActive, LocalDateTime createdDate) {
        this.identityName = identityName;
        this.identityType = identityType;
        this.identityAttributes = identityAttributes;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = createdDate;
    }
    public DistAgentAuthIdentityRow(String identityName, String identityType, String identityAttributes) {
        this.identityName = identityName;
        this.identityType = identityType;
        this.identityAttributes = identityAttributes;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getIdentityName() {
        return identityName;
    }

    public String getIdentityType() {
        return identityType;
    }

    public String getIdentityAttributes() {
        return identityAttributes;
    }

    public int getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    /** insert row for distagentauthidentity */
    public Object[] toInsertRow() {
        return new Object[] {identityName,identityType,identityAttributes,isActive,createdDate,lastUpdatedDate};
    }

    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentAuthDomainRow",
                "identityName", identityName,
                "identityType", identityType,
                "identityAttributes", identityAttributes,
                "isActive", ""+isActive,
                "createdDate", createdDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "identityName";
    }
    public static DistAgentAuthIdentityRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentAuthIdentityRow(
                m.getStringOrEmpty("identityname"),
                m.getStringOrEmpty("identitytype"),
                m.getStringOrEmpty("identityattributes"),
                m.getIntOrZero("isactive"),
                m.getLocalDateOrNow("createddate")
        );
    }
}
