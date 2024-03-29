package org.gnuzero.pub.pituwa.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import org.gnuzero.pub.pituwa.R;
import org.gnuzero.pub.pituwa.app.App;
import org.gnuzero.pub.pituwa.model.Category;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.MyViewHolder> {

    private Context mContext;
    private List<Category> itemList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public ImageView thumbnail;

        public MyViewHolder(View view) {

            super(view);

            title = (TextView) view.findViewById(R.id.title);
            title.setTypeface(App.getInstance().getFont());
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }


    public CategoryListAdapter(Context mContext, List<Category> itemList) {

        this.mContext = mContext;
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        Category c = itemList.get(position);
        holder.title.setText(c.getTitle());

        // loading album cover using Glide library
        Glide.with(mContext).load(c.getImgUrl()).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {

        return itemList.size();
    }
}