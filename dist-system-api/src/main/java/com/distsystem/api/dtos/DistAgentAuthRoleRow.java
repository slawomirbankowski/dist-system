package com.distsystem.api.dtos;

import com.distsystem.api.BaseRow;
import com.distsystem.api.DaoTable;
import com.distsystem.utils.AdvancedMap;

import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * */
@DaoTable(tableName="distagentauthrole", keyName="roleName", keyIsUnique=true)
public class DistAgentAuthRoleRow extends BaseRow {

    private final String roleName;
    private final String roleDescription;
    private final String roleAttributes;
    private final int isActive;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastUpdatedDate;

    // create table distagentauthrole(rolename text not null, roledescription text, roleattributes text, isactive int not null, createddate timestamp not null)

    public DistAgentAuthRoleRow(String roleName, String roleDescription, String roleAttributes, int isActive, LocalDateTime createdDate) {
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.roleAttributes = roleAttributes;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastUpdatedDate = createdDate;
    }
    public DistAgentAuthRoleRow(String roleName, String roleDescription, String roleAttributes) {
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.roleAttributes = roleAttributes;
        this.isActive = 1;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = createdDate;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public String getRoleAttributes() {
        return roleAttributes;
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
        return new Object[] { roleName, roleDescription, roleAttributes, isActive, createdDate };
    }

    public Map<String, String> toMap() {
        return Map.of("type", "DistAgentAuthRoleRow",
                "roleName", roleName,
                "roleDescription", roleDescription,
                "roleAttributes", roleAttributes,
                "isActive", ""+isActive,
                "createdDate", createdDate.toString());
    }

    /** get name of key attribute */
    public static String getKeyAttributeName() {
        return "roleName";
    }
    public static DistAgentAuthRoleRow fromMap(Map<String, Object> map) {
        AdvancedMap m = new AdvancedMap(map, true);
        return new DistAgentAuthRoleRow(
                m.getStringOrEmpty("rolename"),
                m.getStringOrEmpty("roledescription"),
                m.getStringOrEmpty("roleattributes"),
                m.getIntOrZero("isactive"),
                m.getLocalDateOrNow("createddate")
        );
    }
}
