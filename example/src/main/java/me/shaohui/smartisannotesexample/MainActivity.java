package me.shaohui.smartisannotesexample;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import java.io.File;
import me.shaohui.smartiannotes.SmartisanNotes;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.main_image);

        Bitmap bitmap = SmartisanNotes.with(this)
                .draw("知乎是一家创立于2011 年 1 月 26 "
                        + "日的中国大陆社会化问答网站，产品形态模仿了美国类似网站Quora。“知乎”在古汉语中意为“知道吗”。2012 年 2 "
                        + "月底，知乎使用“发现更大的世界”作为其宣传口号。 ", getResources().getDrawable(R.drawable.fan))
                .asBitmap();
        File file = SmartisanNotes.with(this)
                .draw("知乎是一家创立于2011 年 1 月 26 "
                        + "日的中国大陆社会化问答网站，产品形态模仿了美国类似网站Quora。“知乎”在古汉语中意为“知道吗”。2012 年 2 "
                        + "月底，知乎使用“发现更大的世界”作为其宣传口号。 ", getResources().getDrawable(R.drawable.fan))
                .saveCacheFile();

        findViewById(R.id.action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmartisanNotes.with(MainActivity.this)
                        .draw("知乎是一家创立于2011 年 1 月 26 "
                                + "日的中国大陆社会化问答网站，产品形态模仿了美国类似网站Quora。“知乎”在古汉语中意为“知道吗”。2012 年 2 "
                                + "月底，知乎使用“发现更大的世界”作为其宣传口号。 ", getResources().getDrawable(R.drawable.test))
                        .savePublicFile();
            }
        });

        mImageView.setImageURI(Uri.parse(file.getPath()));
    }
}
