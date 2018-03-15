package com.example.dries.project;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import android.util.Log;

import static android.provider.Settings.System.getString;

public class ListViewAdapter extends ArrayAdapter<Herinnering> {

    private MainActivity activity;
    private DatabaseHelper databaseHelper;
    private List<Herinnering> herinneringList;

    public ListViewAdapter(MainActivity context, int resource, List<Herinnering> objects, DatabaseHelper helper) {
        super(context, resource, objects);
        this.activity = context;
        this.databaseHelper = helper;
        this.herinneringList = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_listview, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(getItem(position).getName());

        //Delete an item
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseHelper.deleteHerinnering(getItem(position)); //delete in db
                Toast.makeText(activity, "Deleted!", Toast.LENGTH_SHORT).show();

                //reload the database to view
                activity.reloadingDatabase();
            }
        });

        //Edit/Update an item
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                alertDialog.setTitle("Update a Notification");

                LinearLayout layout = new LinearLayout(activity);
                layout.setPadding(10, 10, 10, 10);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText nameBox = new EditText(activity);
                nameBox.setHint("Name");
                layout.addView(nameBox);

                final EditText descriptionBox = new EditText(activity);
                descriptionBox.setHint("description");
                layout.addView(descriptionBox);

                final EditText lat = new EditText(activity);
                descriptionBox.setHint(getItem(position).getCoordlat());
                layout.addView(lat);

                final EditText longt = new EditText(activity);
                descriptionBox.setHint(getItem(position).getCoordlong());
                layout.addView(longt);


                lat.setText(getItem(position).getCoordlat());
                longt.setText(getItem(position).getCoordlong());

                nameBox.setText(getItem(position).getName());
                descriptionBox.setText(getItem(position).getDescription());

                alertDialog.setView(layout);

                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Herinnering herinnering = new Herinnering(nameBox.getText().toString(), descriptionBox.getText().toString(),lat.getText().toString(),longt.getText().toString());
                        herinnering.setId(getItem(position).getId());
                        databaseHelper.updateHerinnering(herinnering); //update to db
                        Toast.makeText(activity, "Updated!", Toast.LENGTH_SHORT).show();

                        //reload the database to view
                        activity.reloadingDatabase();
                    }
                });

                alertDialog.setNegativeButton("Cancel", null);

                //show alert dialog
                alertDialog.show();
            }
        });

        //show details when each row item clicked
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                alertDialog.setTitle("Herinnering ");

                LinearLayout layout = new LinearLayout(activity);
                layout.setPadding(10, 10, 10, 10);
                layout.setOrientation(LinearLayout.VERTICAL);

                TextView nameBox = new TextView(activity);
                layout.addView(nameBox);

                TextView descriptionBox = new TextView(activity);
                layout.addView(descriptionBox);

                nameBox.setText("Herinnering name: " + getItem(position).getName());
                descriptionBox.setText("Herinnering description: " + getItem(position).getDescription());

                alertDialog.setView(layout);
                alertDialog.setNegativeButton("OK", null);

                //show alert
                alertDialog.show();
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        private TextView name;
        private View btnDelete;
        private View btnEdit;

        public ViewHolder (View v) {
            name = (TextView)v.findViewById(R.id.item_name);
            btnDelete = v.findViewById(R.id.delete);
            btnEdit = v.findViewById(R.id.edit);
        }
    }
}