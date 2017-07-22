package com.bangalorebuses;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;

class NetworkingAnimation extends AsyncTask<ImageView, Void, Void>
{
    @Override
    protected Void doInBackground(final ImageView... params)
    {
        while (true)
        {
            new CountDownTimer(1000, 1000)
            {
                @Override
                public void onTick(long millisUntilFinished)
                {

                }

                @Override
                public void onFinish()
                {
                    params[0].setImageResource(R.drawable.ic_rss_feed_orange_24dp_1_bar);
                    params[0].setVisibility(View.VISIBLE);
                    new CountDownTimer(1000, 1000)
                    {
                        @Override
                        public void onTick(long millisUntilFinished)
                        {

                        }

                        @Override
                        public void onFinish()
                        {
                            params[0].setImageResource(R.drawable.ic_rss_feed_orange_24dp_2_bars);
                            new CountDownTimer(1000, 1000)
                            {
                                @Override
                                public void onTick(long millisUntilFinished)
                                {

                                }

                                @Override
                                public void onFinish()
                                {
                                    params[0].setImageResource(R.drawable.ic_rss_feed_orange_24dp_3_bars);
                                    new CountDownTimer(1000, 1000)
                                    {
                                        @Override
                                        public void onTick(long millisUntilFinished)
                                        {

                                        }

                                        @Override
                                        public void onFinish()
                                        {

                                            params[0].setImageResource(R.drawable.ic_rss_feed_orange_24dp_2_bars);
                                            new CountDownTimer(1000, 1000)
                                            {
                                                @Override
                                                public void onTick(long millisUntilFinished)
                                                {

                                                }

                                                @Override
                                                public void onFinish()
                                                {
                                                    params[0].setImageResource(R.drawable.ic_rss_feed_orange_24dp_1_bar);
                                                    new CountDownTimer(1000, 1000)
                                                    {
                                                        @Override
                                                        public void onTick(long millisUntilFinished)
                                                        {

                                                        }

                                                        @Override
                                                        public void onFinish()
                                                        {
                                                            params[0].setVisibility(View.GONE);
                                                        }
                                                    }.start();
                                                }
                                            }.start();
                                        }
                                    }.start();
                                }
                            }.start();
                        }
                    }.start();
                }
            }.start();
        }
    }
}
