package com.kana_tutor.FontDemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
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

public class MainActivity extends Activity {
    private static final String logTag = "MainActivity";
    private static final List<String>   fontNameList = new ArrayList<String>();
    private static FontListAdapter      fontSelectListAdapter;
    private static SeekBar              fontSampleSizeSeekBar;
    private static Typeface             defaultFont;
    private class ListElement {
        private boolean selected = false; 
        private String fontPath, fontName;
        private Typeface fontHandle;
        private ListElement (String fontPath) {
            this.fontPath   = fontPath;
            String fontName = fontPath;
            fontName = fontName.replaceAll("[^/]*/",  "");
            fontName = fontName.replaceAll("\\.[^\\.]*$", "");
            this.fontName = fontName;
            fontHandle = Typeface.createFromAsset(
                    MainActivity.this.getAssets(),fontPath);
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
                TextView fontSample = (TextView)rowView.findViewById(R.id.font_sample);
                fontSample.setTypeface(el.fontHandle);
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
    private void setSelectedFont(String fontName, String fontPath) {
        TextView tv = (TextView)findViewById(R.id.selected_font_name);
        if (defaultFont == null)
            defaultFont = tv.getTypeface();
        tv.setText(fontName);
        SharedPreferences userPrefs = getSharedPreferences(
            "user_prefs.txt", Context.MODE_PRIVATE);
        Editor ed = userPrefs.edit();
        tv.setTag(fontPath);
        ed.putString("fontName", fontName);
        ed.putString("fontPath", fontPath);
        ed.commit();
        Typeface newFont = defaultFont;
        for (ListElement el : fontSampleList) {
            if (fontName.equals(el.fontName)) {
                el.selected = true;
                newFont = el.fontHandle;
                break;
            }
        }
        ((TextView)findViewById(R.id.selected_font_sample))
            .setTypeface(newFont);
        fontSelectListAdapter.notifyDataSetChanged();
    }
    private void setSelectedFontSize(int fontSize) {
        TextView size = (TextView)findViewById(R.id.selected_font_size);
        size.setText("" + fontSize + " sp");
        size.setTag(fontSize);
        TextView sample = (TextView)findViewById(R.id.selected_font_sample);
        sample.setTextSize(fontSize);
        // size.invalidate(); sample.invalidate();
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
        // read selected from the checkbox.  I've seen instaces
        // where the selected value and the check disagree.  The
        // user sees the checkbox so use that.
        if (vh.cb.isChecked()) {
            vh.fontElement.selected = true;
            setSelectedFont(vh.fontElement.fontName, vh.fontElement.fontPath);
        }
        else {
            vh.fontElement.selected = false;
            setSelectedFont(getString(R.string.default_font), vh.fontElement.fontPath);
        }
    }
    private OnSeekBarChangeListener sbChangedListener(final int min, final int max) {
        return new OnSeekBarChangeListener() {
            int selectedFontSize;
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                selectedFontSize = (int)(min + (((max - min) * progress) / 100d));
                setSelectedFontSize(selectedFontSize);
            }
            @Override
            public void onStartTrackingTouch(SeekBar sb)    {}
            @Override
            public void onStopTrackingTouch(SeekBar sb)     {
                SharedPreferences userPrefs = getSharedPreferences(
                    "user_prefs.txt", Context.MODE_PRIVATE);
                Editor ed = userPrefs.edit();
                ed.putInt("fontSize", selectedFontSize);
                ed.commit();
            }
        };
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        TextView fontName = (TextView)findViewById(R.id.selected_font_name);
        outState.putString("fontName", fontName.getText().toString());
        outState.putString("fontPath", (String)fontName.getTag());
        TextView fontSize = (TextView)findViewById(R.id.selected_font_size);
        outState.putInt("fontSize", (Integer)fontSize.getTag());
    }

    final static int minFont = 10; final static int maxFont = 60;
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
		int fontSize = minFont;
		String fontPath = "";
		String fontName = getString(R.string.default_font);
		if (savedInstanceState != null) {
            fontName = savedInstanceState.getString("fontName");
            fontPath = savedInstanceState.getString("fontPath");
            fontSize = savedInstanceState.getInt("fontSize");
		}
        fontSelectListAdapter = new FontListAdapter(this, fontSampleList);
        fontSampleSizeSeekBar = (SeekBar)findViewById(R.id.font_sample_size);
        fontSampleSizeSeekBar.setOnSeekBarChangeListener(sbChangedListener(10, 60));
        ListView lv = (ListView)findViewById(R.id.font_sample_list);
        lv.setAdapter(fontSelectListAdapter);
        setSelectedFont(fontName, fontPath);
        // default font size to minimum font size.
        int unscaled = (int)((fontSize - minFont) * (100d / (maxFont-minFont)));
        fontSampleSizeSeekBar.setProgress(unscaled);
        setSelectedFontSize(fontSize);
        findViewById(R.id.font_info_wrapper).invalidate();
	}
}
