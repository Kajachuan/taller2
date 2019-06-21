package com.hypechat.models.organizations;

import java.util.List;

public class OrganizationListBody {

    private List<String> organizations;

    public OrganizationListBody(List<String> organizations) {
        this.organizations = organizations;
    }

    public List<String> getOrganizations() {
        return this.organizations;
    }
}
