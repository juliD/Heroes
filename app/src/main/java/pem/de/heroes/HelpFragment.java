package pem.de.heroes;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HelpFragment extends Fragment {

    private static final String ARG_TYPE = "fragment_type";
    String fragment_type = "ask";

    public HelpFragment() {
        // Required empty public constructor
    }


    public static HelpFragment newInstance(String type) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragment_type = getArguments().getString(ARG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //für test zwecke
        ArrayList<ListItem> listItems =new ArrayList<ListItem>();
        if(fragment_type.equals("ask")){

            listItems.add(new ListItem("Suche Einkaufshilfe!!","Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
                    "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.",""));
            listItems.add(new ListItem("Suche Umzugshelfer","Brauche dringend Hilfe für einem Umzug morgen Nachmittag.",""));
            listItems.add(new ListItem("Bierbänke aufstellen","Muss noch 200 Bierbänke aufstellen, kann mir wer helfen?.",""));
            listItems.add(new ListItem("Müll","Kann mir jemand den Müll wegbringen?? Dankeee :D",""));
        }
        else{

            listItems.add(new ListItem("Gehe Einkaufen","Kann gerne jemandem was mitbringen!",""));
            listItems.add(new ListItem("Fahre morgen zum Baumarkt","Wenn jemand was haben möchte, dann bescheid sagen, kann auch etwas größeres sein, da ich mit nem Transporter hinfahre.",""));

        }



        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);
        final ListView listView = (ListView) rootView.findViewById(R.id.listView);
        final CustomArrayAdapter adapter = new CustomArrayAdapter(getContext(),listItems);
        listView.setAdapter(adapter);


        return rootView;
    }


    public class CustomArrayAdapter extends ArrayAdapter<ListItem> {
        private final Context context;
        private final ArrayList<ListItem> item;

        public CustomArrayAdapter(Context context,  ArrayList<ListItem> item) {
            super(context, -1, item);
            this.context = context;
            this.item = item;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item, parent, false);
            TextView titleView = (TextView) rowView.findViewById(R.id.item_title);
            TextView infosView = (TextView) rowView.findViewById(R.id.item_description);
            titleView.setText(item.get(position).getTitle());
            infosView.setText(item.get(position).getDescription());

            return rowView;
        }
    }

}
