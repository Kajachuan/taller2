package com.hypechat.models.invitations;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;

public class InvitationsListBody {

    private Map<String, String> invitations;

    public InvitationsListBody(Map<String, String> invitations) {
        this.invitations = invitations;
    }

    public Map<String, String> getInvitations() {
        return this.invitations;
    }
}
