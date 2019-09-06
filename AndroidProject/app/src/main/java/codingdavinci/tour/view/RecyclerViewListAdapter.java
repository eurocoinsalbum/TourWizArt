package codingdavinci.tour.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import codingdavinci.tour.R;
import codingdavinci.tour.activity.CdvActivity;
import codingdavinci.tour.listener.LoadImageListener;
import codingdavinci.tour.listener.UuidListItemListener;
import codingdavinci.tour.model.UuidListItem;

public class RecyclerViewListAdapter extends RecyclerView.Adapter<RecyclerViewListAdapter.GenericViewHolder> implements View.OnClickListener {
    CdvActivity cdvActivity;
    private List<? extends UuidListItem> uuidItemList;
    private UuidListItemListener uuidListItemListener;
    private int layoutListItem;
    private int selectedPosition = -1;

    public RecyclerViewListAdapter(CdvActivity cdvActivity, List<? extends UuidListItem> itemList, int layoutListItem, @NonNull UuidListItemListener uuidListItemListener) {
        this.cdvActivity = cdvActivity;
        this.uuidItemList = itemList;
        this.uuidListItemListener = uuidListItemListener;
        this.layoutListItem = layoutListItem;
    }

    @NonNull
    @Override
    public RecyclerViewListAdapter.GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutListItem, parent, false);
        final GenericViewHolder viewHolder = new GenericViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = viewHolder.getAdapterPosition();
                uuidListItemListener.onClick(position, uuidItemList.get(position));
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final GenericViewHolder holder, final int position) {
        final UuidListItem uuidListItem = uuidItemList.get(position);

        // title
        if (holder.titleView != null) {
            holder.titleView.setText(uuidListItem.getTitle());
        }

        // description
        if (holder.descriptionView != null) {
            holder.descriptionView.setText(uuidListItem.getDescription());
        }

        holder.position = position;
        // selected item
        holder.imageView.setBackgroundColor(position == selectedPosition ? Color.DKGRAY : Color.WHITE);

        // alternate background
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
        } else {
            holder.itemView.setBackgroundColor(Color.argb(0xFF, 0xEE, 0xEE, 0xEE));
        }
        holder.imageView.setImageResource(R.drawable.icons8_draft);

        if (uuidListItem.hasIcon()) {
            // image
            try {
                Log.i("cdv", "Load icon");
                uuidListItem.getIcon(cdvActivity, new LoadImageListener() {
                    @Override
                    public void imageLoaded(Bitmap bitmap) {
                        Log.i("cdv", "image Loaded: " + holder.position);
                        // check if this holder-object should still show the requested download
                        if (holder.position != position) {
                            Log.i("cdv", "position: " + position);
                            return;
                        }
                        holder.imageView.setImageBitmap(bitmap);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return uuidItemList.size();
    }

    public void removeItem(int position) {
        uuidItemList.remove(position);
        notifyItemRemoved(position);
    }

    public void setItems(List<? extends UuidListItem> itemList) {
        this.uuidItemList = itemList;
        notifyDataSetChanged();
    }

    public UuidListItem getUuidListItem(int position) {
        return uuidItemList.get(position);
    }

    @Override
    public void onClick(View v) {

    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(position);
    }

    public class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView descriptionView;
        ImageView imageView;
        // because an object of this item might be reused by the RecyclerView the position is used in the callback of the image download
        // to check if this object is still showing the requested download
        int position;

        public GenericViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.icon);
            titleView = view.findViewById(R.id.title);
            descriptionView = view.findViewById(R.id.description);
        }
    }
}
