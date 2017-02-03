package lavalse.kr.pickup;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

/**
 * @author LaValse
 * @date 2016-07-15
 */
public class ProgressView extends Dialog {
    private ImageView progress;
    private Button btnRefresh, btnCancel;

    private View.OnClickListener refreshListener, cancelListener;

    private RotateAnimation rotateAnim;

    public ProgressView(Context context){
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 1.0f;
        getWindow().setAttributes(lp);

        setContentView(R.layout.view_progress);

        progress = (ImageView)findViewById(R.id.progressObj);
        progress.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        rotateAnim = new RotateAnimation(-360.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.setDuration(2000);
        rotateAnim.setRepeatCount(Animation.INFINITE);

        btnRefresh = (Button)findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(refreshListener);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isShowing()){
                    btnRefresh.setVisibility(View.VISIBLE);
                }
            }
        },10000);

        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(cancelListener);
    }

    public void setOnRefreshListener(View.OnClickListener listener){
        refreshListener = listener;
    }

    public void setOnCancelListener(View.OnClickListener listener){
        cancelListener = listener;
    }

    @Override
    public void show() {
        super.show();

        progress.startAnimation(rotateAnim);
    }

    @Override
    public void dismiss() {
        progress.post(new Runnable() {
            @Override
            public void run() {
                progress.clearAnimation();
            }
        });

        super.dismiss();
    }
}