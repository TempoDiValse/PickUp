package lavalse.kr.pickup.connect;

import android.os.AsyncTask;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import cz.msebera.android.httpclient.Header;

/**
 * Created by LaValse on 2016. 8. 13..
 */
public class MultipartTest extends AsyncTask<Void,Void,Void>{
    String url;
    AsyncHttpClient client;
    public MultipartTest(String url){
        client = new AsyncHttpClient();
        this.url = url;
    }

    @Override
    protected void onPreExecute() {
        RequestParams params = new RequestParams();
        params.add("ci_t","7829a8b0d27eae9ab65056710648da03");
        params.add("id","lovelyz");
        params.add("no","1597631");
        params.add("comment_page", "1");

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println(new String(responseBody));
            }
        });
    }

    @Override
    protected Void doInBackground(Void... voids) {

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

    }
}
