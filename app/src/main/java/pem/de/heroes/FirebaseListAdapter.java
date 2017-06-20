package pem.de.heroes;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Julia on 18.06.2017.
 */

public abstract class FirebaseListAdapter<T> extends BaseAdapter implements Filterable {

    private static final String LOG_TAG = "FirebaseListAdapter";
    private Query mRef;
    private Class<T> mModelClass;
    private int mLayout;
    private LayoutInflater mInflater;
    private List<T> mModels;
    private List<T> mFilteredModels;
    private Map<String, T> mModelKeys;
    private Map<String, T> mFilteredKeys;
    private ChildEventListener mListener;
    private Context mContext;
    private ValueFilter valueFilter;


    /**
     * @param mRef        The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                    combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     * @param mModelClass Firebase will marshall the data at a location into an instance of a class that you provide
     * @param mLayout     This is the mLayout used to represent a single list item. You will be responsible for populating an
     *                    instance of the corresponding view with the data from an instance of mModelClass.
     * @param activity    The activity containing the ListView
     */
    public FirebaseListAdapter(Query mRef, Class<T> mModelClass, int mLayout, Activity activity) {
        this.mRef = mRef;
        this.mModelClass = mModelClass;
        this.mLayout = mLayout;
        mInflater = activity.getLayoutInflater();
        mModels = new ArrayList<>();
        mModelKeys = new LinkedHashMap<>();
        // Look for all child events. We will then map them to our own internal ArrayList, which backs ListView
        mListener = this.mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                T model = dataSnapshot.getValue(FirebaseListAdapter.this.mModelClass);
                mModelKeys.put(dataSnapshot.getKey(), model);

                // Insert into the correct location, based on previousChildName
                if (previousChildName == null) {
                    mModels.add(0, model);
                } else {
                    T previousModel = mModelKeys.get(previousChildName);
                    int previousIndex = mModels.indexOf(previousModel);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(model);
                    } else {
                        mModels.add(nextIndex, model);
                    }
                }

                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, "onChildChanged");
                // One of the mModels changed. Replace it in our list and name mapping
                String modelName = dataSnapshot.getKey();
                T oldModel = mModelKeys.get(modelName);
                T newModel = dataSnapshot.getValue(FirebaseListAdapter.this.mModelClass);
                int index = mModels.indexOf(oldModel);

                mModels.set(index, newModel);
                mModelKeys.put(modelName, newModel);

                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "onChildRemoved");
                // A model was removed from the list. Remove it from our list and the name mapping
                String modelName = dataSnapshot.getKey();
                T oldModel = mModelKeys.get(modelName);
                mModels.remove(oldModel);
                mModelKeys.remove(modelName);
                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(LOG_TAG, "onChildMoved");
                // A model changed position in the list. Update our list accordingly
                String modelName = dataSnapshot.getKey();
                T oldModel = mModelKeys.get(modelName);
                T newModel = dataSnapshot.getValue(FirebaseListAdapter.this.mModelClass);
                int index = mModels.indexOf(oldModel);
                mModels.remove(index);
                if (previousChildName == null) {
                    mModels.add(0, newModel);
                } else {
                    T previousModel = mModelKeys.get(previousChildName);
                    int previousIndex = mModels.indexOf(previousModel);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mModels.size()) {
                        mModels.add(newModel);
                    } else {
                        mModels.add(nextIndex, newModel);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur");
            }
        });
    }



    public void cleanup() {
        // We're being destroyed, let go of our mListener and forget about all of the mModels
        mRef.removeEventListener(mListener);
        mModels.clear();
        mModelKeys.clear();
    }

    @Override
    public int getCount() {
        return mModels.size();
    }

    @Override
    public Object getItem(int i) {
        return mModels.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void remove(String key) {
        T oldModel = mModelKeys.get(key);
        mModels.remove(oldModel);
        mModelKeys.remove(key);
        notifyDataSetChanged();
    }
    public String getKey(int i){

        for(Map.Entry<String, T>entry : mModelKeys.entrySet()){
            if(i==0){
                return entry.getKey();
            }
            else{
                i--;
            }

        }
        return "";
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(mLayout, viewGroup, false);
        }

        T model = mModels.get(i);
        if (model != null) {
            // Call out to subclass to marshall this model into the provided view
            populateView(view, model);
        }
        return view;
    }

    /**
     * Each time the data at the given Firebase location changes, this method will be called for each item that needs
     * to be displayed. The arguments correspond to the mLayout and mModelClass given to the constructor of this class.
     * <p/>
     * Your implementation should populate the view using the data contained in the model.
     *
     * @param v     The view to populate
     * @param model The object containing the data used to populate the view
     */
    protected abstract void populateView(View v, T model);

    public void addSingle(DataSnapshot snapshot) {
        T model = snapshot.getValue(FirebaseListAdapter.this.mModelClass);
        mModelKeys.put(snapshot.getKey(), model);

        mModels.add(model);

        notifyDataSetChanged();
    }

    public void update(DataSnapshot snapshot, String key) {
        T oldModel = mModelKeys.get(key);
        T newModel = snapshot.getValue(FirebaseListAdapter.this.mModelClass);
        int index = mModels.indexOf(oldModel);

        if (index >= 0) {
            mModels.set(index, newModel);
            mModelKeys.put(key, newModel);

            notifyDataSetChanged();
        }
    }

    public boolean exists(String key) {
        return mModelKeys.containsKey(key);
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    protected abstract List<T> filters(List<T> models, CharSequence constraint);

    private class ValueFilter extends Filter {

        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (mFilteredModels == null) {
                mFilteredModels = new ArrayList<>(mModels); // saves the original data in mOriginalValues
                mFilteredKeys = new HashMap<>(mModelKeys); // saves the original data in mOriginalValues
            }
            if (constraint != null && constraint.length() > 0) {
                List<T> filtered = filters(mFilteredModels, constraint);

                results.count = filtered.size();
                results.values = filtered;
                mModelKeys = filterKeys(mModels);
            } else {
                results.count = mFilteredModels.size();
                results.values = mFilteredModels;
                mModelKeys = mFilteredKeys;
            }
            return results;
        }


        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            Log.d(LOG_TAG, "filter for " + constraint + ", results nr: " + results.count);
            mModels = (List<T>) results.values;

            notifyDataSetChanged();
        }
    }

    protected abstract Map<String, T> filterKeys(List<T> mModels);
}