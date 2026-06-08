package ma.aybi.chroconi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ma.aybi.chroconi.R;
import ma.aybi.chroconi.config.PreferencesManager;
import ma.aybi.chroconi.model.Conversation;

public class PriorityAdapter extends RecyclerView.Adapter<PriorityAdapter.ViewHolder> {

    Context context;
    private Runnable onPriorityChanged;

    public PriorityAdapter(Context context) {
        this.context = context;
    }

    public void setOnPriorityChanged(Runnable listener) {
        this.onPriorityChanged = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_item_priority, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = Conversation.allConversations.get(position);
        String name = conversation.getName();
        String currentPriority = PreferencesManager.getPriorityConversation(context);

        holder.tvName.setText(name);

        if (name.equals(currentPriority)) {
            holder.tvPriorityStatus.setText("Currently prioritized");
            holder.btnAction.setBackgroundDrawable(
                    context.getDrawable(R.drawable.bg_ignore_glow));
            holder.ivAction.setBackgroundDrawable(
                    context.getDrawable(R.drawable.bg_ignore_inner));
            holder.ivAction.setImageDrawable(
                    context.getDrawable(R.drawable.ic_ignore));
        } else {
            holder.tvPriorityStatus.setText("Set as priority");
            holder.btnAction.setBackgroundDrawable(
                    context.getDrawable(R.drawable.bg_accept_glow));
            holder.ivAction.setBackgroundDrawable(
                    context.getDrawable(R.drawable.bg_accept_inner));
            holder.ivAction.setImageDrawable(
                    context.getDrawable(R.drawable.ic_priority));
        }

        View.OnClickListener togglePriority = v -> {
            String current = PreferencesManager.getPriorityConversation(context);
            if (name.equals(current)) {
                PreferencesManager.setPriorityConversation(context, null);
            } else {
                PreferencesManager.setPriorityConversation(context, name);
            }
            notifyDataSetChanged();
            if (onPriorityChanged != null) onPriorityChanged.run();
        };
        holder.ivAction.setOnClickListener(togglePriority);
        holder.itemView.setOnClickListener(togglePriority);
    }

    @Override
    public int getItemCount() {
        return Conversation.allConversations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPriorityStatus;
        FrameLayout btnAction;
        ImageButton ivAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPriorityStatus = itemView.findViewById(R.id.tvPriorityStatus);
            btnAction = itemView.findViewById(R.id.btnPriorityAction);
            ivAction = itemView.findViewById(R.id.ivPriorityAction);
        }
    }
}
