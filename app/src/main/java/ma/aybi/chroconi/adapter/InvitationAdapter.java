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
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;
        Invitation invitation = Invitation.invitations.get(pos);
        Toast toast = new Toast(context);



        holder.tvUsername.setText(invitation.getInviter());
        holder.tvId.setText(String.valueOf(invitation.getId()));

        holder.btnAccept.setOnClickListener(v -> {
            Invitation.applyToInvitationById(context, invitation.getId(), Invitation.ACCEPT, (res, text) ->{
                if (res) {
                    showToast(true, text, holder);
                    removeItem(pos);
                }else {
                    showToast(false, text, holder);
                }
                if (Invitation.invitations.isEmpty()) {
                    vNotificationsDot.invalidate();
                    vNotificationsDot.setVisibility(View.INVISIBLE);
                }
            });
        });

        holder.btnIgnore.setOnClickListener(v -> {
            Invitation.applyToInvitationById(context, invitation.getId(), Invitation.IGNORE, (res, text) ->{
                    showToast(true, "Invitation ignored", holder);
                    removeItem(pos);
            });
            if (Invitation.invitations.isEmpty()) {
                vNotificationsDot.invalidate();
                vNotificationsDot.setVisibility(View.INVISIBLE);
            }
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
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, Invitation.invitations.size());
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
