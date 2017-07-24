package com.searchview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/***
 * 自定义搜索view
 *
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SearchView search_view;
    private Button btn_atart;
    private Button btn_end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        search_view = (SearchView) findViewById(R.id.search_view);
        search_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_view.setStartSearch();
            }
        });

//        //代码中设置相关属性
//        search_view.setCircleVaule(80);
//        search_view.setBackgroundColor(Color.GREEN);
//        search_view.setPaintColor(Color.WHITE);
//        search_view.setPaintStrokeWidth(20);
//        search_view.setDivisor(2.5f);

        btn_atart = (Button) findViewById(R.id.btn_atart);
        btn_atart.setOnClickListener(this);
        btn_end = (Button) findViewById(R.id.btn_end);
        btn_end.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_atart:
                search_view.setStartSearch();
                break;
            case R.id.btn_end:
                search_view.setEndSearch();
                break;
        }
    }
}
