package lavalse.kr.pickup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import lavalse.kr.pickup.model.User;
import lavalse.kr.pickup.util.StringUtil;

/**
 * @author LaValse
 * @date 2016-07-14
 */
public class CutlineActivity extends AppCompatActivity {
    private static final int MODE_TIME = 0;
    private static final int MODE_PERSON = 1;

    private User[] list;
    private String[] idList;
    private String[] ipList;
    private int mode = MODE_TIME;

    private int cYear, cMonth, cDay, cHour, cMinute;

    private Calendar cal;

    private String currentDate;
    private String deadline;

    private View container;
    private Button btnPick;
    private TextView textResult;

    private RandThread thread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cutline_activity);

        list = getIntent().getParcelableArrayListExtra("LIST").toArray(new User[]{});

        idList = new String[list.length];
        for(int i=0; i<idList.length; i++){
            idList[i] = list[i].getID();
        }

        ArrayList<String> tmpIp = new ArrayList<>();
        for(int i=0; i<list.length; i++){
            if(list[i].getIP() != null)
                tmpIp.add(list[i].getIP());
        }

        ipList = tmpIp.toArray(new String[]{});

        container = findViewById(R.id.container);

        final RadioGroup group = (RadioGroup)findViewById(R.id.radGroup);

        final View layerTime = findViewById(R.id.layer_time);
        final Button btnDate = (Button)layerTime.findViewById(R.id.btnDate);

        cal = Calendar.getInstance();
        cYear = cal.get(Calendar.YEAR);
        cMonth = cal.get(Calendar.MONTH)+1;
        cDay = cal.get(Calendar.DAY_OF_MONTH);
        cHour = cal.get(Calendar.HOUR);
        cMinute = cal.get(Calendar.MINUTE);

        currentDate = deadline = StringUtil.getDateString(cal.getTime());

        btnDate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {

                TimePickerDialog dialog = new TimePickerDialog(CutlineActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        cal.set(Calendar.AM_PM, (hour < 12) ? Calendar.AM : Calendar.PM);
                        cal.set(Calendar.HOUR, hour);
                        cal.set(Calendar.MINUTE, minute);

                        deadline = StringUtil.getDateString(cal.getTime());
                        System.out.println(deadline);
                        btnDate.setText(String.format("%d년 %d월 %d일 %d시 %d분",cYear, cMonth, cDay, hour, minute));
                    }

                }, cHour, cMinute, false);
                dialog.show();
            }
        });

        final View layerPerson = findViewById(R.id.layer_person);
        final EditText editLimit = (EditText)layerPerson.findViewById(R.id.editLimit);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (id){
                    case R.id.rad1:
                        mode = MODE_TIME;

                        layerTime.setVisibility(View.VISIBLE);
                        layerPerson.setVisibility(View.GONE);

                        editLimit.setText("");
                        break;
                    case R.id.rad2:
                        mode = MODE_PERSON;

                        layerTime.setVisibility(View.GONE);
                        layerPerson.setVisibility(View.VISIBLE);

                        deadline = currentDate;
                        btnDate.setText("기준 설정");

                        break;
                }
            }
        });

        final EditText editWinner = (EditText)findViewById(R.id.editWinner);

        final CheckBox chkExceptId = (CheckBox)findViewById(R.id.checkExceptId);
        final EditText editExceptId = (EditText)findViewById(R.id.editExceptId);
        editExceptId.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(CutlineActivity.this).setItems(idList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int len = editExceptId.getText().length();

                        if(len == 0) {
                            editExceptId.append(idList[i]);
                        }else{
                            if(editExceptId.getText().toString().contains(idList[i])){
                                Toast.makeText(CutlineActivity.this, "이미 추가되어 있습니다.", Toast.LENGTH_SHORT).show();
                            }else{
                                editExceptId.append(","+idList[i]);
                                dialogInterface.dismiss();
                            }
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
            }
        });

        chkExceptId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editExceptId.setVisibility((b) ? View.VISIBLE : View.GONE);
            }
        });

        final CheckBox chkExceptIp = (CheckBox)findViewById(R.id.checkExceptIp);
        final EditText editExceptIp = (EditText)findViewById(R.id.editExceptIp);
        editExceptIp.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                new AlertDialog.Builder(CutlineActivity.this).setItems(ipList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int len = editExceptIp.getText().length();

                        if(len == 0) {
                            editExceptIp.append(ipList[i]);
                        }else{
                            if(editExceptIp.getText().toString().contains(ipList[i])){
                                Toast.makeText(CutlineActivity.this, "이미 추가되어 있습니다.", Toast.LENGTH_SHORT).show();
                            }else{
                                editExceptIp.append(","+ipList[i]);
                                dialogInterface.dismiss();
                            }
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
            }
        });

        chkExceptIp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editExceptIp.setVisibility((b) ? View.VISIBLE : View.GONE);
            }
        });

        if(ipList.length == 0){
            chkExceptIp.setVisibility(View.GONE);
        }


        btnPick = (Button)findViewById(R.id.btnPick);

        btnPick.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {

                textResult.setText("");

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editWinner.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editExceptId.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editExceptIp.getWindowToken(), 0);

                String[] exceptIds = null, exceptIps = null;

                ArrayList<User> tmp = new ArrayList<User>();

                Collections.addAll(tmp, list);
                Collections.sort(tmp, new Comparator<User>() {
                    @Override
                    public int compare(User t1, User t2) {
                        return t1.getDate().compareTo(t2.getDate());
                    }
                });

                boolean isIDChecked = chkExceptId.isChecked();
                boolean isIPChecked = chkExceptIp.isChecked();

                int winner = 0;
                try {
                    winner = Integer.parseInt(editWinner.getText().toString());
                }catch(NumberFormatException e){
                    Toast.makeText(CutlineActivity.this, "당첨자 수를 확인해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isIDChecked){
                    exceptIds = editExceptId.getText().toString().split(",");

                    System.out.println(exceptIds.length);

                    for(int i=0; i<exceptIds.length; i++){
                        String stand = exceptIds[i];

                        if(stand.equals("")) break;

                        for(int j=0; j<tmp.size(); j++){
                            if(stand.equals(tmp.get(j).getID())){
                                tmp.remove(j);
                                break;
                            }
                        }
                    }
                }

                if(isIPChecked){
                    exceptIps = editExceptIp.getText().toString().split(",");
                    for(int i=0; i<exceptIps.length; i++){
                        String stand = exceptIps[i];

                        if(stand.equals("")) break;

                        for(int j=0; j<tmp.size(); j++){
                            if(stand.equals(tmp.get(j).getIP())){
                                tmp.remove(j);
                                break;
                            }
                        }
                    }
                }

                if(mode == MODE_TIME){
                    int j=0;
                    int start = 0;

                    for(int i=0; i<tmp.size(); i++){
                        String stand = tmp.get(i).getDate();

                        if(stand.compareTo(deadline) > 0){
                            if(start == 0) start = i;
                            j++;
                        }
                    }

                    for (int i=start,end=start+j; i<end; i++) {
                        tmp.remove(start);
                    }

                }else{
                    int limit = Integer.parseInt(editLimit.getText().toString());

                    if(limit >= tmp.size()){
                        Toast.makeText(CutlineActivity.this, "인원 수가 제한 인원 수를 안 넘네요. 그냥 하세요", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    for(int i=tmp.size()-1; tmp.size() != limit; i=tmp.size()-1){
                        tmp.remove(i);
                    }
                }


                btnPick.setEnabled(false);

                thread = new RandThread(tmp.toArray(new User[]{}), winner);
                thread.start();
            }
        });

        textResult = (TextView)findViewById(R.id.textResult);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menur, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_capture:
                container.buildDrawingCache();
                Bitmap captureView = container.getDrawingCache();
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+"/win_result.jpg");
                    captureView.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "캡쳐 완료", Toast.LENGTH_LONG).show();
                break;
        }

        return true;
    }

    private class RandThread extends Thread{
        private User[] user;
        private int cycle;
        private String result="";

        public RandThread(User[] user, int cycle){
            this.user = user;
            this.cycle = cycle;
        }

        public void run(){
            Random lSeed = new Random();

            int loop = user.length * (lSeed.nextInt(10) + 2);

            Message m = new Message();
            m.what = -2;
            m.obj = loop;
            handler.sendMessage(m);

            for(int i=0; i<cycle; i++) {

                String idRunning = null;
                for (int j = 0; j < loop; j++) {
                    Random rand = new Random();
                    int seed = rand.nextInt(user.length);

                    String willResult = user[seed].getID();
                    if(result.contains(willResult)){
                        j--;
                        continue;
                    }

                    Message msg = new Message();
                    msg.what = i;
                    msg.obj = idRunning = willResult;

                    handler.sendMessage(msg);

                    try {
                        sleep(j * 3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                result = (cycle == 0) ? idRunning : ","+idRunning;
                if(i == cycle - 1)
                    handler.sendEmptyMessage(-1);
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String id = null;
            switch (msg.what){
                case -2:
                    String loop = String.valueOf(msg.obj);

                    Toast.makeText(CutlineActivity.this, loop+"번 돌게 됩니다",Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    thread.interrupt();
                    thread = null;

                    btnPick.setEnabled(true);
                    break;
                case 0:
                    id = msg.obj.toString();

                    textResult.setText(id);

                    break;
                default:
                    id = msg.obj.toString();

                    int cycle = msg.what;

                    String resultStr = textResult.getText().toString();
                    String[] resultArr = resultStr.split(",");

                    if(resultArr.length == cycle){
                        textResult.append(","+id);
                    }else {
                        resultArr[cycle] = id;

                        String append = "";
                        for (int i = 0; i < resultArr.length; i++) {
                            append += (i == 0) ? resultArr[i] : "," + resultArr[i];
                        }

                        textResult.setText(append);
                    }

                    break;
            }
        }
    };
}
