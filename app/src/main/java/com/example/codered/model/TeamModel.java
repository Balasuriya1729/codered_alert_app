package com.example.codered.model;

import java.io.Serializable;
import java.util.ArrayList;

public class TeamModel implements Serializable {
    private ArrayList<MemberModel> members;
    private MemberModel team_lead;
    private String team_name;
    private boolean selected=false;
    public TeamModel(ArrayList<MemberModel> members, MemberModel team_lead, String team_name) {
        this.members = members;
        this.team_lead = team_lead;
        this.team_name = team_name;
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public ArrayList<MemberModel> getMembers() {
        return members;
    }
    public MemberModel getTeam_lead() {
        return team_lead;
    }
    public String getTeam_name() {
        return team_name;
    }
    public void setTeamMember(MemberModel member) {
        members.add(member);
    }
}
