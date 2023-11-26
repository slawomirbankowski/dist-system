package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * */
@DaoTable(tableName="distagentauthrole", keyName="tokenParserName", keyIsUnique=true)
public class DistAgentAuthTokenParserRow extends BaseRow {

    private final String tokenParserName;
    private final String tokenParserAttributes;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    // create table distagentauthtokenparser(tokenparsername text not null, tokenparserattributes text not null, isactive int not null, createddate timestamp not null)

    public DistAgentAuthTokenParserRow(String tokenParserName, String tokenParserAttributes, int isActive, LocalDateTime createdDate) {
        this.tokenParserName = tokenParserName;
        this.tokenParserAttributes = tokenParserAttributes;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = createdDate;
    }

    public DistAgentAuthTokenParserRow(String tokenParserName, String tokenParserAttributes) {
        this.tokenParserName = tokenParserName;
        this.tokenParserAttributes = tokenParserAttributes;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getTokenParserName() {
        return tokenParserName;
    }

    public String getTokenParserAttributes() {
        return tokenParserAttributes;
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

    public Object[] toInsertRow() {
        return new Object[] { tokenParserName, tokenParserAttributes, isActive, createdDate };
    }

    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentAuthTokenParserRow",
                "tokenParserName", tokenParserName,
                "tokenParserAttributes", tokenParserAttributes,
                "isActive", ""+isActive,
                "createdDate", createdDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "tokenParserName";
    }
    public static DistAgentAuthTokenParserRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentAuthTokenParserRow(
                m.getStringOrEmpty("tokenparsername"),
                m.getStringOrEmpty("tokenparserattributes"),
                m.getIntOrZero("isactive"),
                m.getLocalDateOrNow("createddate")
        );
    }
}
