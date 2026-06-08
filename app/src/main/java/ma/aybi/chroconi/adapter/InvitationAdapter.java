package ma.aybi.chroconi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ma.aybi.chroconi.R;
import ma.aybi.chroconi.model.Invitation;


public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {
    Context context;
    public static View vNotificationsDot;
    TextView toastText;
    LayoutInflater inflater;

    private OnInvitationAcceptedListener onAcceptedListener;

    public interface OnInvitationAcceptedListener {
        void onAccepted(String repoFullName, String inviter);
    }

    public void setOnInvitationAcceptedListener(OnInvitationAcceptedListener listener) {
        this.onAcceptedListener = listener;
    }


    public InvitationAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public InvitationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_item_notification,
                        parent,
                        false);
        inflater = LayoutInflater.from(context);

        return new InvitationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationAdapter.ViewHolder holder, int position) {
        Invitation invitation = Invitation.invitations.get(position);

        holder.tvUsername.setText(invitation.getInviter());
        holder.tvId.setText(String.valueOf(invitation.getId()));

        holder.btnAccept.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Invitation invitationAtPos = Invitation.invitations.get(pos);
            Invitation.applyToInvitationById(context, invitationAtPos.getId(), Invitation.ACCEPT, (res, repoFullName) ->{
                if (res) {
                    showToast(true, "Invitation accepted", holder);
                    if (repoFullName != null && onAcceptedListener != null) {
                        onAcceptedListener.onAccepted(repoFullName, invitationAtPos.getInviter());
                    }
                    removeItem(pos);
                }else {
                    showToast(false, "Something went wrong", holder);
                }
            });
        });

        holder.btnIgnore.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Invitation invitationAtPos = Invitation.invitations.get(pos);
            Invitation.applyToInvitationById(context, invitationAtPos.getId(), Invitation.IGNORE, (res, text) ->{
                showToast(true, text, holder);
                removeItem(pos);
            });
        });

    }

    @Override
    public int getItemCount() {
        return Invitation.invitations.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvUsername, tvId;

        ImageButton btnAccept, btnIgnore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvId = itemView.findViewById(R.id.tvId);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnIgnore = itemView.findViewById(R.id.btnIgnore);

        }
    }
    private void removeItem(int position) {
        if (position < 0 || position >= Invitation.invitations.size()) return;
        int id = Invitation.invitations.get(position).getId();
        notifyItemRemoved(position);
        Invitation.removeById(id);
        int newSize = Invitation.invitations.size();
        if (position < newSize) {
            notifyItemRangeChanged(position, newSize - position);
        }
        if (Invitation.invitations.isEmpty()) {
            vNotificationsDot.setVisibility(View.INVISIBLE);
        }
    }
    private void showToast(boolean success, String text, ViewHolder holder) {

        LayoutInflater inflater = LayoutInflater.from(context);

        int layoutId = success ?
                R.layout.success_toast :
                R.layout.error_toast;

        View view = inflater.inflate(
                layoutId,
                null,
                false
        );

        TextView tv = view.findViewById(
                success ? R.id.tvToastSuccess : R.id.tvToastError
        );

        tv.setText(text);

        Toast toast = new Toast(context);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
