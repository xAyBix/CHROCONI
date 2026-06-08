package ma.aybi.chroconi.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ma.aybi.chroconi.config.PreferencesManager;
import ma.aybi.chroconi.github.BooleanStringCallBack;
import ma.aybi.chroconi.github.GithubConnection;

public class Invitation {
    public static final List<Invitation> invitations= new ArrayList<>();
    public static final int IGNORE = 0;
    public static final int ACCEPT = 1;

    int id;
    String inviter;
    String repoFullName;


    public Invitation(int id, String inviter, String repoFullName) {
        this.id = id;
        this.inviter = inviter;
        this.repoFullName = repoFullName;
        invitations.add(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInviter() {
        return inviter;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    public String getRepoFullName() {
        return repoFullName;
    }

    public static void applyToInvitationById (Context context, int id, int status, BooleanStringCallBack callBack) {
        if (status == ACCEPT) {
            Invitation inv = findById(id);
            String repoFullNameResult = (inv != null) ? inv.repoFullName : null;
            GithubConnection.acceptInvitation(context, id, result -> {
                if (result) {
                    callBack.onResult(true, repoFullNameResult);
                }else {
                    callBack.onResult(false, null);
                }
            });
        }else {
            PreferencesManager.addToIgnoredInvitations(context, id);
            callBack.onResult(true, null);
        }
    }

    public static Invitation findById(int id) {
        for (Invitation inv : invitations) {
            if (inv.id == id) return inv;
        }
        return null;
    }

    public static void removeById(int id) {
        for (int i = 0; i < invitations.size(); i++) {
            if (invitations.get(i).id == id) {
                invitations.remove(i);
                return;
            }
        }
    }

}

