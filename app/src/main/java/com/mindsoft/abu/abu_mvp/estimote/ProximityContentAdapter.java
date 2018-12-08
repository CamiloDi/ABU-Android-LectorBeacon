package com.mindsoft.abu.abu_mvp.estimote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.mindsoft.abu.abu_mvp.R;

import java.util.ArrayList;
import java.util.List;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class ProximityContentAdapter extends BaseAdapter {

    private Context context;

    public ProximityContentAdapter(Context context) {
        this.context = context;
    }

    private List<Beacon> nearbyContent = new ArrayList<>();

    public void setNearbyContent(List<Beacon> nearbyContent) {
        this.nearbyContent = nearbyContent;
    }

    @Override
    public int getCount() {
        return getNearbyContent().size();
    }

    @Override
    public Beacon getItem(int position) {
        return getNearbyContent().get(position);
    }


    public List<Beacon> getBeacons(){return nearbyContent;}

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;

            convertView = inflater.inflate(R.layout.activity_main, parent, false);
        }

        ProximityContent content = new ProximityContent("","");

        //TextView title = convertView.findViewById(R.id.txtID);
        //TextView subtitle = convertView.findViewById(R.id.txtNombre);

        //title.setText(content.getTitle());
        //subtitle.setText(content.getSubtitle());

        //convertView.setBackgroundColor(Utils.getEstimoteColor(content.getTitle()));

        return convertView;
    }

    public List<Beacon> getNearbyContent() {
        return nearbyContent;
    }
}
