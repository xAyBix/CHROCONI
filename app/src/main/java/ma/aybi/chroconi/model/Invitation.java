package ma.aybi.chroconi.model;

import android.content.Context;

import ma.aybi.chroconi.config.PreferencesManager;
import ma.aybi.chroconi.github.BooleanStringCallBack;
import ma.aybi.chroconi.github.GithubConnection;

public class Invitation {
    public static final int IGNORE = 0;
    public static final int ACCEPT = 1;

    int id;

    String inviter;


    public Invitation(int id, String inviter) {
        this.id = id;
        this.inviter = inviter;
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

    public static void applyToInvitationById (Context context, int id, int status, BooleanStringCallBack callBack) {
        if (status == ACCEPT) {
            GithubConnection.acceptInvitation(context, id, result -> {
                if (result) {
                    callBack.onResult(true, "Invitation accepted");
                }else {
                    callBack.onResult(false, "Something went wrong");
                }
            });
        }else {
            PreferencesManager.addToIgnoredInvitations(context, id);
        }
    }

}

