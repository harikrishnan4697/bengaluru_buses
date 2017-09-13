package com.bangalorebuses;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ErrorMessageFragment extends Fragment implements View.OnClickListener
{
    private ImageView errorImageView;
    private TextView errorMessageTextView;
    private TextView errorResolutionTextView;

    private OnErrorResolutionButtonClicked callback;

    public ErrorMessageFragment()
    {
        // Required empty public constructor
    }

    public static ErrorMessageFragment newInstance(int errorImageResId, int errorMessageStringResId,
                                                   int errorResolutionButtonStringResId)
    {
        ErrorMessageFragment fragment = new ErrorMessageFragment();

        Bundle args = new Bundle();

        args.putInt("ERROR_IMAGE_RES_ID", errorImageResId);
        args.putInt("ERROR_MESSAGE", errorMessageStringResId);
        args.putInt("ERROR_RESOLUTION_BUTTON_TEXT", errorResolutionButtonStringResId);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_error_message, container, false);

        // Initialise the views
        errorImageView = (ImageView) view.findViewById(R.id.error_image_view);
        errorMessageTextView = (TextView) view.findViewById(R.id.error_message_text_view);
        errorResolutionTextView = (TextView) view.findViewById(R.id.error_resolution_text_view);


        if (getArguments() != null)
        {
            Bundle args = getArguments();

            errorImageView.setImageResource(args.getInt("ERROR_IMAGE_RES_ID"));
            errorMessageTextView.setText(args.getInt("ERROR_MESSAGE"));
            errorResolutionTextView.setText(args.getInt("ERROR_RESOLUTION_BUTTON_TEXT"));

            errorResolutionTextView.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onClick(View v)
    {
        if (callback != null)
        {
            callback.onResolveError();
        }
    }

    public void setOnResolutionButtonClickListener(OnErrorResolutionButtonClicked callback)
    {
        this.callback = callback;
    }

    public interface OnErrorResolutionButtonClicked
    {
        void onResolveError();
    }
}
