package com.air.CnBetaClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyActivity extends Activity{
    private static final String DEBUG_TAG = "HttpExample";
    final static String domain = "http://www.cnbeta.com";
    private SwipeRefreshLayout swipeLayout;
    String  html;
    Document document;
    private ListView listView;

    private final static int HTML_INFO = 1;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initView();
        myClickHandler(getCurrentFocus());
        SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                       swipeLayout.setRefreshing(false);
                       load(html);
                    }
                }, 3000);

            }
        };

        swipeLayout.setOnRefreshListener(listener);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

}

    public void initView(){
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        listView = (ListView) findViewById(R.id.listView);
    }


    public static String downloadUrl(String urlString) throws IOException {
        // Only display the first 500 characters of the retrieved
        // web page content.
        DataInputStream dis = null;
        int len = 500;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();

            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            dis = new DataInputStream(bis);

            ByteArrayBuffer baf = new ByteArrayBuffer(1024);
            int current;
            while ((current = dis.read()) != -1) {
                baf.append((byte) current);
            }
            return EncodingUtils.getString(baf.toByteArray(), "utf-8");
        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "网络连接失败，请重试",
//                    Toast.LENGTH_LONG).show();
            return "";
        }finally {
            if(dis != null)
                dis.close();
        }
    }

    protected void load(String html) {
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        document = Jsoup.parse(html);
        Elements es = document.getElementsByClass("items_area");
        for (Element e : es) {
            for (Element a : e.getElementsByTag("dt")) {
                HashMap<String, String> map = new HashMap<String, String>();
                Elements link = a.getElementsByTag("a");
                map.put("title", link.text());
                map.put("href", domain + link.attr("href"));
                list.add(map);

            }

        }

        listView.setCacheColorHint(0);
        SimpleAdapter listItemAdapter = new SimpleAdapter(this, list,//data source
                //layout of this list
                R.layout.listview,
                //android.R.layout.simple_list_item_1,
                // Correspond to the tag in xml
                new String[]{"title"},
                // Correspond to the IDs in xml
                new int[]{R.id.tv})  ;
        //添加并显示
        listView.setAdapter(listItemAdapter);

        final ArrayList<HashMap<String, String>> finalListItem = (ArrayList<HashMap<String, String>>) list;

        //添加点击
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setTitle("点击第" + i + "个项目");
                Intent intent = new Intent();
                intent.setClass(MyActivity.this, SubWeb.class);
                intent.putExtra("href", ChangeActivity(finalListItem, i));
                startActivity(intent);
            }
        });

    }

    public static String ChangeActivity(ArrayList<HashMap<String, String>> listItem, int num){

        String key;
        String value = "";
        for(Map.Entry<String , String> mapEntry : listItem.get(num).entrySet()){
            key = mapEntry.getKey();
            if(key == "href")
                value = mapEntry.getValue();
        }
        return value;
    }

    public boolean isOnline() {
        //判断网络是否连接
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void myClickHandler(View view){
        if (isOnline()) {
                new DownloadWebpageTask().execute(domain);
        } else {
            Toast.makeText(getApplicationContext(), "网络未连接，请重试",
                    Toast.LENGTH_LONG).show();
        }
    }
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            // params comes from the execute() call: params[0] is the url.
            try {
                html = downloadUrl(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return html;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            load(html);
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理
            Toast.makeText(getApplicationContext(), "正在加载，请稍候",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
