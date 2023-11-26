package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * */
@DaoTable(tableName="DistAgentAuthAccount", keyName="accountName", keyIsUnique=true)
public class DistAgentAuthAccountRow extends BaseRow {

    private final String accountName;
    private final String domainName;
    private final String accountAttributes;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    // create table distagentauthaccount(accountname text not null, domainname text not null, accountattributes text not null, isactive int not null, createddate timestamp not null)

    public DistAgentAuthAccountRow(String accountName, String domainName, String accountAttributes, int isActive, LocalDateTime createdDate) {
        this.accountName = accountName;
        this.domainName = domainName;
        this.accountAttributes = accountAttributes;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = createdDate;
    }
    public DistAgentAuthAccountRow(String accountName, String domainName, String accountAttributes) {
        this.accountName = accountName;
        this.domainName = domainName;
        this.accountAttributes = accountAttributes;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getAccountAttributes() {
        return accountAttributes;
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
        return new Object[] { accountName, domainName, accountAttributes, isActive, createdDate };
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "accountName";
    }
    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentAuthAccountRow",
                "accountName", accountName,
                "domainName", domainName,
                "accountAttributes", accountAttributes,
                "isActive", ""+isActive,
                "createdDate", createdDate.toString());
    }

    public static DistAgentAuthAccountRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentAuthAccountRow(
                m.getStringOrEmpty("accountname"),
                m.getStringOrEmpty("domainname"),
                m.getStringOrEmpty("accountattributes"),
                m.getInt("isactive", 1),
                m.getLocalDateOrNow("createddate")
        );
    }
}
