/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mta.tehreer.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bidiAlgorithmButton = (Button) findViewById(R.id.button_bidi_algorithm);
        bidiAlgorithmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BidiAlgorithmActivity.class);
                startActivity(intent);
            }
        });

        Button fontInfoButton = (Button) findViewById(R.id.button_font_info);
        fontInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FontInfoActivity.class);
                startActivity(intent);
            }
        });

        Button fontGlyphsButton = (Button) findViewById(R.id.button_font_glyphs);
        fontGlyphsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FontGlyphsActivity.class);
                startActivity(intent);
            }
        });

        Button opentypeShapingButton = (Button) findViewById(R.id.button_opentype_shaping);
        opentypeShapingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OpenTypeShapingActivity.class);
                startActivity(intent);
            }
        });

        Button labelWidgetButton = (Button) findViewById(R.id.button_label_widget);
        labelWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LabelWidgetActivity.class);
                startActivity(intent);
            }
        });
    }
}
