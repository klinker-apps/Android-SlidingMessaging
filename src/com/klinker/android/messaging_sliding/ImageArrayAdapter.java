package com.klinker.android.messaging_sliding;

import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class ImageArrayAdapter extends ArrayAdapter<String> {
	  private final Activity context;
	  private final String[] images;

	  static class ViewHolder {
	    public ImageView image;
	  }

	  public ImageArrayAdapter(Activity context, String[] images) {
	    super(context, R.layout.image_view, new ArrayList<String>());
	    this.context = context;
	    this.images = images;
	  }
	  
	  @Override
	  public int getCount()
	  {
		  return images.length;
	  }

	  @Override
	  public View getView(final int position, View convertView, ViewGroup parent) {
	    View rowView = convertView;
	    
	    if (rowView == null) {
	      LayoutInflater inflater = context.getLayoutInflater();
	      rowView = inflater.inflate(R.layout.image_view, null);
	      
	      ViewHolder viewHolder = new ViewHolder();
	      viewHolder.image = (ImageView) rowView.findViewById(R.id.contactBubble1);
	      rowView.setTag(viewHolder);
	    }

	    ViewHolder holder = (ViewHolder) rowView.getTag();
	    
	    holder.image.setImageURI(Uri.parse(images[position]));
	    
	    holder.image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(images[position])));
				
			}
	    	
	    });

	    return rowView;
	  }
	} 
