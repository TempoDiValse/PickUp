package lavalse.kr.pickup;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import lavalse.kr.pickup.connect.MultipartTest;
import lavalse.kr.pickup.connect.ParseConnector;
import lavalse.kr.pickup.model.User;

public class MainActivity extends AppCompatActivity implements ParseConnector.OnResultListener{
    private CAdapter adapter;

    private View textLayer;
    private TextView textTitle, textTotal;

    private ListView listView;

    private ClipboardManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);

        final EditText editUrl = (EditText)findViewById(R.id.editUrl);
        if(cm.hasPrimaryClip()){
            String clipboardItem = cm.getPrimaryClip().getItemAt(0).getText().toString();

            if(clipboardItem.startsWith("http://gall.dcinside.com")){
                editUrl.setText(clipboardItem);
            }
        }

        final Button btnParse = (Button)findViewById(R.id.btnParse);
        btnParse.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                String url = editUrl.getText().toString();

                Uri uri = Uri.parse(url);
                if(!uri.getHost().equals("gall.dcinside.com")){
                    Toast.makeText(MainActivity.this, "읽을 수 없는 URL입니다 제대로 된거 가져오셈", Toast.LENGTH_SHORT).show();
                    editUrl.setText("");
                    return;
                }

                ParseConnector parser = new ParseConnector(MainActivity.this, url);
                parser.setOnResultListener(MainActivity.this);
                parser.parse();
            }
        });

        textLayer = findViewById(R.id.textLayer);
        textTotal = (TextView)findViewById(R.id.textTotal);
        textTitle = (TextView)findViewById(R.id.textTitle);
        listView = (ListView)findViewById(R.id.listView);

        adapter = new CAdapter();

        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menu_next:
                if(adapter.getCount() != 0) {
                    Intent intent = new Intent(this, CutlineActivity.class);
                    intent.putParcelableArrayListExtra("LIST", adapter.getItems());
                    startActivity(intent);
                }else{
                    Toast.makeText(this, "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return true;
    }

    @Override
    public void onResult(String title, ArrayList<User> results) {
        if(results != null) {
            textTitle.setText(title);
            textTotal.setText(String.format("총 %d 명", results.size()));
            textLayer.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            adapter.setItems(results);
        }else{
            Toast.makeText(this, "댓글 단 사람이 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private class CAdapter extends BaseAdapter{
        private LayoutInflater inflater;
        private ArrayList<User> items;

        public CAdapter(){
            inflater = LayoutInflater.from(MainActivity.this);
        }

        @Override
        public int getCount() {
            return (items == null) ? 0 :items.size();
        }

        @Override
        public Object getItem(int i) {
            return (items == null) ? 0 :items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            User item = items.get(i);

            view = inflater.inflate(R.layout.row_layout, viewGroup, false);

            TextView id = (TextView)view.findViewById(R.id.r_id);
            id.setText(item.getID());

            TextView comment = (TextView)view.findViewById(R.id.r_comment);
            comment.setText(item.getComment());

            return view;
        }

        public void setItems(ArrayList<User> items){
            this.items = items;

            notifyDataSetChanged();
        }

        public ArrayList<User> getItems(){
            return items;
        }
    }
}
