package com.kana_tutor.FontDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.javatechig.droid.ui.R;

public class MainActivity extends Activity {
    private static final String logTag = "MainActivity";
    private static final List<String>   fontNameList = new ArrayList<String>();
    private static FontListAdapter      fontSelectListAdapter;
    private static SeekBar              fontSampleSizeSeekBar;
    private class ListElement {
        private boolean selected = false; 
        private String fontPath, fontName; 
        private ListElement (String fontPath) {
            this.fontPath   = fontPath;
            String fontName = fontPath;
            fontName = fontName.replaceAll("[^/]*/",  "");
            fontName = fontName.replaceAll("\\.[^\\.]*$", "");
            this.fontName = fontName;
        }
        public String toString() {
            return String.format(
                "listElement name=%s, enabled=%b", fontName, selected);
        }
    }
    static class ViewHolder {
        private ListElement fontElement;
        private TextView    tv;
        private CheckBox    cb;
        private int         position;
        public String toString() {
            return String.format("ViewHolder %d: fontElement: %s"
                +": textView: \"%s\", CneckBox: %b"
                , position, fontElement.toString()
                , tv.getText().toString(), cb.isChecked());
        }
    }
    static private List<ListElement> fontSampleList
        = new ArrayList<ListElement>();
    private class FontListAdapter extends ArrayAdapter<ListElement> {
        private final Context context;
        private List<ListElement> fontList;
        public FontListAdapter(Context context, List<ListElement> fontList) {
            super(context, R.layout.font_list_item, fontList);
            this.context = context; this.fontList = fontList;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            ViewHolder vh = null;
            if (rowView != null) {
                vh = (ViewHolder) rowView.getTag();
            }
            if (vh == null || vh.position != position) {
                vh = new ViewHolder();
                ListElement el 
                    = vh.fontElement
                    = fontList.get(position);
                LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(LAYOUT_INFLATER_SERVICE);
                Log.i(logTag, "getView: position = " + position
                    + ", " + el.toString());
                rowView = inflater.inflate(R.layout.font_list_item, parent, false);
                vh.tv = (TextView)rowView.findViewById(R.id.font_name);
                vh.tv.setText(el.fontName);
                vh.cb = (CheckBox)rowView.findViewById(R.id.select_this_font);
                vh.position = position;
                rowView.setTag(vh);
            }
            vh = (ViewHolder) rowView.getTag();
            Log.i(logTag, "getView: " + vh.toString());
            vh.cb.setChecked(vh.fontElement.selected);
            return rowView;
        }
    }
    
    // find all the .ttf files under assets/fonts.
    private void getFontList(){
        class asset_fonts {
            void get(String path) {
                try {
                    String [] asset_files = getResources()
                        .getAssets()
                        .list(path);
                    for (String f : asset_files) {
                        f = path + "/" + f;
                        // if list gave back files, path is valid.
                        if (f.endsWith(".ttf")) fontNameList.add(f);
                        get(f);
                    }
                } catch (IOException e) {
                    Log.d(logTag, "__fonts.get():exception:IOException:"
                            + "path = " + path);
                }
            }
        }
        (new asset_fonts()).get("fonts");
    }
    // font select click callback.
    public void fontSelect(View view) {
        ViewHolder vh = (ViewHolder)view.getTag();
        boolean isChecked = vh.fontElement.selected;
        isChecked = isChecked == false;
        vh.cb.setChecked(isChecked);
        // only one element can be selected at a time.  If this one
        // is selected, turn off everyone then turn on the new one.
        if (isChecked) {
            for (ListElement el : fontSampleList) {
                el.selected = false;
            }
        }
        vh.fontElement.selected = isChecked;
        fontSelectListAdapter.notifyDataSetChanged();
    }
    private OnSeekBarChangeListener sbChangedListener = new OnSeekBarChangeListener() {
        int progressChanged = 0;

        @Override
        public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
            progressChanged = progress;
            Log.i(logTag, "sbChangedListener: change: " + progress);
        }
        @Override
        public void onStartTrackingTouch(SeekBar sb) {
            Log.i(logTag, "sbChangedListener: start tracking");
        }

        @Override
        public void onStopTrackingTouch(SeekBar sb) {
            Log.i(logTag, "sbChangedListener: stop tracking: " + progressChanged);
        }
    };


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// if we haven't inited the font list, do so.
		if (fontNameList.size() == 0) {
		    getFontList();
		    Log.i(logTag, "onCreate:assetListing:"
                + fontNameList.toString());
		    for (int i = 0; i < fontNameList.size(); i++) {
		        fontSampleList.add(new ListElement(fontNameList.get(i)));
		        // Log.i(logTag, fontSampleList.get(i).toString());
		    }
		}
        fontSelectListAdapter = new FontListAdapter(this, fontSampleList);
        fontSampleSizeSeekBar = (SeekBar)findViewById(R.id.font_sample_size);
        fontSampleSizeSeekBar.setOnSeekBarChangeListener(sbChangedListener);
        ListView lv = (ListView)findViewById(R.id.font_sample_list);
        lv.setAdapter(fontSelectListAdapter);
	}
}
