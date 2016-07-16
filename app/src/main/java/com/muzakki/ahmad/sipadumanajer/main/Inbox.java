package com.muzakki.ahmad.sipadumanajer.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jeki on 6/13/15.
 */
public class Inbox extends Fragment implements AdapterView.OnItemClickListener,ListUpdater{
    ArrayAdapter<HashMap<String,String>> adapter = null;
    ArrayList<String> storeID = null;
    Activity context = null;
    Database db = null;
    ListView listView = null;
    InternetConnection ic = null;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        db = new Database(context);
        ic = new InternetConnection(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.inbox,container,false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        listView = (ListView) context.findViewById(R.id.inboxContent);
        listView.setOnItemClickListener(this);
        showLoading();
        showList();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String id = storeID.get(i);
        ((Aduan) context).showTindakLanjut(id);
    }

    @Override
    public void showList(){ // called only first time

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<HashMap<String, String>> result = db.getInbox();

                if (result.size() == 0) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.aduan_empty, R.id.txtAduanEmpty);
                    adapter.add("Data Kosong");
                    listView.setAdapter(adapter);
                    return;
                }
                List<HashMap<String, String>> list = new ArrayList<>();
                int i = 0;
                storeID = new ArrayList<>();
                for (HashMap<String, String> entry : result) {
                    HashMap<String, String> values = new HashMap<>();
                    storeID.add(entry.get("id"));
                    values.put("nosambungan", entry.get("nosambungan"));
                    values.put("aduan", entry.get("aduan"));
                    values.put("waktu", entry.get("waktu"));

                    list.add(values);
                }
                adapter = new AduanAdapter(context, R.layout.inbox_list, new ArrayList<HashMap<String,String>>());
                adapter.addAll(list);
                listView.setAdapter(adapter);
            }
        });
    }

    @Override
    public void addItemList(List<HashMap<String, String>> items) {
        if(adapter==null) {
            showList();
            return;
        }
        int i = -1;
        for(HashMap<String,String> item : items){
            storeID.add(++i, item.get("id"));
            adapter.insert(item, i);
        }
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void removeItemList(String[] ids) {
        if(adapter==null) return;
        for(String id: ids){
            for(int i=0;i<ids.length;i++){
                if(storeID.get(i).equals(id)){
                    storeID.remove(i);
                    adapter.remove(adapter.getItem(i));
                }
            }
        }
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateList(String id){
        int pos = -1;
        for(int i=0;i<storeID.size();i++){
            String _id = storeID.get(i);
            if(_id.equals(id)){
                pos = i;
                break;
            }
        }
        if(pos==-1){
            Log.i("jeki", "cannot update list");
            return;
        }
        HashMap<String,String> item = adapter.getItem(pos);
        adapter.remove(item);
        item.put("tindak_lanjut", "1");
        adapter.insert(item, pos);
        adapter.notifyDataSetChanged();
    }

    private class AduanAdapter extends ArrayAdapter<HashMap<String,String>>{

        public AduanAdapter(Context context, int resource, List<HashMap<String,String>> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.inbox_list, null);

                ViewHolder viewHolder = new ViewHolder();

                viewHolder.aduan = (TextView) rowView.findViewById(R.id.aduan);
                viewHolder.tanggal = (TextView) rowView.findViewById(R.id.tanggal);
                viewHolder.nosambungan = (TextView) rowView.findViewById(R.id.nosambungan);
                rowView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) rowView.getTag();

            holder.nosambungan.setText(getItem(position).get("nosambungan"));
            holder.aduan.setText(getItem(position).get("aduan"));
            holder.tanggal.setText(getItem(position).get("waktu"));

            return rowView;
        }
    }

    static class ViewHolder {
        public TextView nosambungan;
        public TextView aduan;
        public TextView tanggal;


    }

    private void showLoading(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,R.layout.aduan_loading,R.id.txtAduanLoading);
        adapter.add("Loading...");
        listView.setAdapter(adapter);
    }


}
