package moocollege.cn.kugougouslidemenu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by zsd on 2017/8/30 15:01
 * desc:
 */

public class ActivityQQSlider extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qqslider);
    }

    public void clickImage(View v){
        finish();
    }
}
