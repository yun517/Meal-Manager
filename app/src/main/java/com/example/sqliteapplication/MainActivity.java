package com.example.sqliteapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.ByteArrayInputStream;

public class MainActivity extends AppCompatActivity {

    private Button btnMealManagement;

    private ListView lvMainMeals;

    private DatabaseHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMealManagement = findViewById(R.id.btn_meal_manage);
        lvMainMeals = findViewById(R.id.lv_main_meals);

        handler = new DatabaseHandler(this);
        handler.open();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MealManagementActivity.class);
                startActivity(intent);
            }
        };

        btnMealManagement.setOnClickListener(listener);
        showAllMeals();
    }

    //使得每次有餐點新增，再回到首頁時資料能更新
    @Override
    protected void onResume() {
        super.onResume();
        showAllMeals();
    }

    private void showAllMeals() {
        Cursor cursor = handler.getAllMeals();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainActivity.this,
                R.layout.meal_list_item,
                cursor,
                new String[]{"name","description", "price"},
                new int[]{R.id.tv_meal_name,R.id.tv_meal_description, R.id.tv_meal_price},
                0) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView ivMealImage = view.findViewById(R.id.iv_meal);

                // Move the cursor to the current position
                cursor.moveToPosition(position);
                byte[] mealImage = cursor.getBlob(1);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(mealImage);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                bitmap = Bitmap.createScaledBitmap(bitmap,200,200,false);
                ivMealImage.setImageBitmap(bitmap);

                return view;
            }
        };

        lvMainMeals.setAdapter(adapter);
    }
}