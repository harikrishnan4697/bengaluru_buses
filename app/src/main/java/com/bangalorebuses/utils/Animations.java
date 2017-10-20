package com.bangalorebuses.utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.bangalorebuses.R;

public class Animations
{
    public static Animation rotateForwardForever(Context context)
    {
        return AnimationUtils.loadAnimation(context,
                R.anim.rotate_forever_forward);
    }

    public static Animation rotateForwardOnce(Context context)
    {
        return AnimationUtils.loadAnimation(context,
                R.anim.rotate_once_forward);
    }

    public static Animation rotateBackwardOnce(Context context)
    {
        return AnimationUtils.loadAnimation(context,
                R.anim.rotate_once_backward);
    }
}