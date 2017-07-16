package pem.de.heroes.main;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pem.de.heroes.R;
import pem.de.heroes.model.ListItem;

/**
 * Created by Julia on 25.06.2017.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {


    private List<ListItem> items;
    private String userid;
    private Context context;



    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, info, dist, status;
        public ImageView category;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.item_title);
            info = (TextView) view.findViewById(R.id.item_description);
            dist = (TextView) view.findViewById(R.id.distance);
            status = (TextView) view.findViewById(R.id.status);
            category = (ImageView) view.findViewById(R.id.category_view);
        }


    }

    public Adapter(List<ListItem> items, String userid, Context context){
        this.items =items;
        this.userid=userid;
        this.context=context;
    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0,0,0,8);
        itemView.setLayoutParams(layoutParams);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final ListItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.info.setText(item.getDescription());
        if(item.getDistance()>1000){
            holder.dist.setText(Math.round(item.getDistance()/1000)+"km");
        }else{
            holder.dist.setText(item.getDistance()+"m");
        }

        holder.category.setImageResource(item.getImage());


        //check if I am agent or if it is my offer and set background color depending on that
        if(item.getAgent().equals(userid)){
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.acceptedItem));
            holder.title.setTextColor(ContextCompat.getColor(context,R.color.white));
            holder.info.setTextColor(ContextCompat.getColor(context,R.color.white));
            holder.dist.setTextColor(ContextCompat.getColor(context,R.color.lightgray));
            holder.status.setTextColor(ContextCompat.getColor(context,R.color.lightgray));
            holder.status.setText("Ich helfe!");
        }else if(item.getUserID().equals(userid)){
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.ownItem));
            holder.title.setTextColor(ContextCompat.getColor(context,R.color.white));
            holder.info.setTextColor(ContextCompat.getColor(context,R.color.white));
            holder.dist.setTextColor(ContextCompat.getColor(context,R.color.lightgray));
            holder.status.setTextColor(ContextCompat.getColor(context,R.color.lightgray));
            holder.status.setText("Meins!");
        }else{
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.openItem));
            holder.title.setTextColor(ContextCompat.getColor(context,R.color.text_grey));
            holder.info.setTextColor(ContextCompat.getColor(context,R.color.text_grey));
            holder.dist.setTextColor(ContextCompat.getColor(context,R.color.text_grey));
            holder.status.setTextColor(ContextCompat.getColor(context,R.color.text_grey));
        }


    }

    public void setFilter(List<ListItem> filtereditems) {
        items = new ArrayList<>();
        items.addAll(filtereditems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<ListItem>listItems){
        this.items = listItems;
        notifyDataSetChanged();
    }

}
