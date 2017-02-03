package lavalse.kr.pickup.connect;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import lavalse.kr.pickup.ProgressView;
import lavalse.kr.pickup.model.User;

/**
 * @author LaValse
 * @date 2016-07-14
 */
public class ParseConnector{
    private static final String TAG = "ParseConnector";
    private static final String COMMENT_URL = "http://gall.dcinside.com/comment/view";

    private static final int PROGRESS_RECEIVE = 0;
    private static final int PROGRESS_CRAWL = 1;
    private int currentProgress = -1;

    private WebView webView;
    private String url;

    private OnResultListener callback;
    private ProgressView indicator;

    private Task task;

    public ParseConnector(Context context, String url){
        this.url = url;

        indicator = new ProgressView(context);
        indicator.setOnRefreshListener(new View.OnClickListener(){
            public void onClick(View view) {
                parse();
            }
        });
        indicator.setOnCancelListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                indicator.hide();

                if(currentProgress == PROGRESS_RECEIVE) {
                    webView.stopLoading();
                }else if(currentProgress == PROGRESS_CRAWL){
                    task.cancel(true);
                }
            }
        });

        webView = new WebView(context);
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JSInterface(), "jsInterface");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.jsInterface.getHTML($('.tit_view').text(),$('#comment_first').html());");
            }
        });

        Log.d(TAG, url);
    }

    private class Task extends AsyncTask<Void, Void, ArrayList<User>>{
        private String title;
        private Elements elms;

        public Task(String title, Elements elms){
            this.title = title;
            this.elms = elms;
        }

        @Override
        protected ArrayList<User> doInBackground(Void... voids) {
            LinkedList<User> tmp = new LinkedList<>();

            if(elms.size() == 0) return null;

            for(Element elm : elms){
                String names = elm.child(0).text();
                names = names.replace("[","").replace("]","");

                String comment = elm.select(".txt").text();
                if(comment.equals("")) continue;

                String ip = elm.select(".ip").text();
                String date = elm.select(".date").text();

                User user = new User((!ip.isEmpty()) ? names+"("+ip+")" : names, comment);
                if(!ip.isEmpty()) user.setIP(ip);
                user.setDate(date);

                tmp.add(user);
            }

            Collections.sort(tmp, new Comparator<User>() {
                @Override
                public int compare(User t1, User t2) {
                    return t1.getID().compareTo(t2.getID());
                }
            });

            ArrayList<User> arr = new ArrayList<>();

            int i=0, j=0;

            arr.add(tmp.get(0));

            while(true){
                String stand = arr.get(i).getID();

                while(j != tmp.size() && stand.equals(tmp.get(j).getID())){ j++; }

                if(j == tmp.size()) break;

                arr.add(tmp.get(j));
                i++;
            }

            return arr;
        }

        @Override
        protected void onPostExecute(ArrayList<User> result) {
            webView = null;

            if(callback != null) {
                callback.onResult(title, result);
                indicator.hide();
            }
        }
    }

    private class JSInterface{
        @android.webkit.JavascriptInterface
        public void getHTML(String title, String html){
            currentProgress = PROGRESS_CRAWL;

            final Document parseResult = Jsoup.parse(html);
            final Elements elms = parseResult.select(".inner_best");

            task = new Task(title, elms);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }
    }

    public void parse(){
        currentProgress = PROGRESS_RECEIVE;

        if(!indicator.isShowing()) indicator.show();
        webView.loadUrl(url);
    }

    public void setOnResultListener(OnResultListener callback){
        this.callback = callback;
    }

    public interface OnResultListener{
        public void onResult(String title, ArrayList<User> results);
    }
}
