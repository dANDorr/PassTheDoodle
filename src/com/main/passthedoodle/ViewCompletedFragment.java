package com.main.passthedoodle;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ViewCompletedFragment extends Fragment {
	Bundle args;
	ImageView drawingImageView;
    TextView top;
    TextView bottom;    
	ProgressBar spinningCircle;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.fragment_viewround, container, false);
    	
		top = (TextView) rootView.findViewById(R.id.completed_text_view_top);
		bottom = (TextView) rootView.findViewById(R.id.completed_text_view_bottom);
		drawingImageView = (ImageView) rootView.findViewById(R.id.completed_image_view);
		spinningCircle = (ProgressBar) rootView.findViewById(R.id.loading_image_circle2);

		args = getArguments();
		if (!args.getString("Prompt").equals(""))
			top.setText("Doodled: " +  args.getString("Prompt"));
		else
			top.setText("");		
		// Description is empty if game ended on a drawing so bottom text is blank
		if (!args.getString("Description").equals(""))
			bottom.setText("Guessed: " +  args.getString("Description"));
		else
			bottom.setText("");
		
		// passed into displayImage method of ImageLoader so progress circle is displayed
		SimpleImageLoadingListener sill = new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				spinningCircle.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				String message = null;
				switch (failReason.getType()) {
					case IO_ERROR:
						message = "Input/Output error";
						break;
					case DECODING_ERROR:
						message = "Image can't be decoded";
						break;
					case NETWORK_DENIED:
						message = "Downloads are denied";
						break;
					case OUT_OF_MEMORY:
						message = "Out Of Memory error";
						break;
					case UNKNOWN:
						message = "Unknown error";
						break;
				}
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

				spinningCircle.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				spinningCircle.setVisibility(View.GONE);
			}
		};
		
		String uri = args.getString("URL");
		// Send this fragment's ImageView reference to parent activity's ImageLoader
		// where cache lookup/downloading is handled 
    	((ViewCompletedActivity) getActivity()).imageLoader.displayImage(uri, drawingImageView, sill);
		
		return rootView;
	}    
}
