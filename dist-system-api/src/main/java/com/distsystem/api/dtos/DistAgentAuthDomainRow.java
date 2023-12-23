package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * */
public class DistAgentAuthDomainRow extends BaseRow {

    private final String domainName;
    private final String domainDescription;
    private final String domainAttributes;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    // create table distagentauthdomain(domainname text not null, domaindescription text not null, domainattributes text not null, isactive int not null, createddate timestamp not null)

    public DistAgentAuthDomainRow(String domainName, String domainDescription, String domainAttributes, int isActive, LocalDateTime createdDate) {
        this.domainName = domainName;
        this.domainDescription = domainDescription;
        this.domainAttributes = domainAttributes;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = createdDate;
    }
    public DistAgentAuthDomainRow(String domainName, String domainDescription, String domainAttributes) {
        this.domainName = domainName;
        this.domainDescription = domainDescription;
        this.domainAttributes = domainAttributes;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getDomainDescription() {
        return domainDescription;
    }

    public String getDomainAttributes() {
        return domainAttributes;
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

    /** insert row for distagentauthdomain */
    public Object[] toInsertRow() {
        return new Object[] {domainName,domainDescription,domainAttributes,isActive,createdDate,lastUpdatedDate};
    }

    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentAuthDomainRow",
                "domainName", domainName,
                "domainDescription", domainDescription,
                "domainAttributes", domainAttributes,
                "isActive", ""+isActive,
                "createdDate", createdDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "domainName";
    }
    public static DistAgentAuthDomainRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentAuthDomainRow(
                m.getStringOrEmpty("domainname"),
                m.getStringOrEmpty("domaindescription"),
                m.getStringOrEmpty("domainattributes"),
                m.getIntOrZero("isactive"),
                m.getLocalDateOrNow("createddate")
        );
    }
}
