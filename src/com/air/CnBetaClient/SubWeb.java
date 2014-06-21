package com.air.CnBetaClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Air on 14-2-12.
 */
public class SubWeb extends Activity {
    private static TextView tv;
    private ImageView iv;
    private String webId;
    private SpannableStringBuilder htmlSpannable;
    private final static int UPDATE_IMAGE = 1;
    private final static int UPDATE_TEXT = 2;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sublayout);


        Intent intent = this.getIntent();
        webId = intent.getStringExtra("href");
        tv = (TextView) findViewById(R.id.tvIntro);
        tv.setMovementMethod(new ScrollingMovementMethod());
        iv = (ImageView) findViewById(R.id.iv);
        new PageTask(iv).execute(webId);

    }


    private static String changeArticle(String article) {
        StringBuilder sb = new StringBuilder();
        sb.append("  \t  ");
        sb.append(article + " \n");
        return sb.toString();
    }

    private static Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == UPDATE_TEXT)
            {
                tv.setText((SpannableStringBuilder)msg.obj);
            }
        }
    };
    class PageTask extends AsyncTask<String, Integer, Bitmap> {
        // 可变长的输入参数，与AsyncTask.exucute()对应
        ImageView imageView;

        public PageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理
            tv.setText(R.string.task_started);
            // we need this to properly scale the images later
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap tmpBitmap = null;

            try {
                Document document = Jsoup.connect(url).get();
                //setTitle(document.title());
                // first parse the html
                // replace getHtmlCode() with whatever generates/fetches your html
                Spanned spanned = Html.fromHtml(changeArticle(document.getElementsByClass("introduction").text()));

                // we need a SpannableStringBuilder for later use
                if (spanned instanceof SpannableStringBuilder) {
                    // for now Html.fromHtml() returns a SpannableStringBuiler
                    // so we can just cast it
                    htmlSpannable = (SpannableStringBuilder) spanned;
                } else {
                    // but we have a fallback just in case this will change later
                    // or a custom subclass of Html is used
                    htmlSpannable = new SpannableStringBuilder(spanned);
                }
                Elements content = document.getElementsByClass("content");
                Elements paragraph = content.select("p");
                for (Element p : paragraph) {
                    htmlSpannable.append(changeArticle(p.text()));
                }
                Message mes = new Message();
                mes.what = UPDATE_TEXT;
                mes.obj = htmlSpannable;
                mHandler.sendMessage(mes);

                Elements image = content.select("img");
                InputStream is = new java.net.URL(image.attr("src")).openStream();
                Log.i("DEBUG_INFO", image.attr("src"));
                tmpBitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return tmpBitmap;
        }


        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }


    }

}