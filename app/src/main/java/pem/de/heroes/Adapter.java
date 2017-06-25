package pem.de.heroes;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Julia on 25.06.2017.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {


    private List<ListItem> items;
    private String userid;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, info, dist;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.item_title);
            info = (TextView) view.findViewById(R.id.item_description);
            dist = (TextView) view.findViewById(R.id.distance);
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

        //check if I am agent or if it is my offer and set background color depending on that
        if(item.getAgent().equals(userid)){
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.accepted));
        }
        if(item.getUserID().equals(userid)){
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.own));
        }


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