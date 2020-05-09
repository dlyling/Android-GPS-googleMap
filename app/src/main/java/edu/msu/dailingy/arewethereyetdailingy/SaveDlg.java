package edu.msu.dailingy.arewethereyetdailingy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class SaveDlg extends DialogFragment {

    private AlertDialog dlg;

    static final String Name = "name";
    static final String Longitude = "long";
    static final String Latitude = "lat";

    SharedPreferences sharedPreferences;

    private MainActivity longitude;

    private MainActivity latitude;

    /**
     * Create the dialog box
     * @param savedInstanceState The saved instance bundle
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the title
        builder.setTitle("Save Favorite");

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.save_dlg, null);
        builder.setView(view);

        // Add an OK button
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                EditText editName = (EditText)dlg.findViewById(R.id.editName);
                String name = editName.getText().toString();
                //String longitude = editName.getText().toString();
                //String latitude = editName.getText().toString();

                //SharedPreferences.Editor editor = sharedPreferences.edit();
                //editor.putString(Name, name);
                //editor.putString(Longitude, longitude);
                //editor.putString(Latitude, latitude);
                //editor.apply();
            }
        });

        // Add a cancel button
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Cancel just closes the dialog box
            }
        });

        // Create the dialog box
        dlg = builder.create();
        return dlg;
    }

    /**
     * Actually save the hatting
     * @param name name to save it under
     */
    private void save(final String name) {

    }

    public MainActivity getLongitude() {
        return longitude;
    }

    public MainActivity getLatitude() {
        return latitude;
    }
}
