package ma.aybi.chroconi.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ma.aybi.chroconi.ChatActivity;
import ma.aybi.chroconi.R;
import ma.aybi.chroconi.model.Conversation;

public class ConversationAdapter
        extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    Context context;
    List<Conversation> conversationList;

    public ConversationAdapter(Context context,
                               List<Conversation> conversationList) {

        this.context = context;
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_item_conversation,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Conversation conversation =
                conversationList.get(position);

        holder.name.setText(conversation.getName());
        holder.message.setText(conversation.getLastMessage());
        holder.time.setText(conversation.getTime());

        holder.itemView.setOnClickListener(v -> {

            Intent intent =
                    new Intent(context, ChatActivity.class);

            intent.putExtra(
                    "username",
                    conversation.getName());

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);

            context.startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView name, message, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvName);
            message = itemView.findViewById(R.id.tvMessage);
            time = itemView.findViewById(R.id.tvTime);
        }
    }
}
