package com.example.sqliteapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MealManagementActivity extends AppCompatActivity {

    private ImageView ivMeal;
    private EditText etMealName;
    private EditText etMealDescription;
    private EditText etMealPrice;
    private Button btnAddMeal;
    private Button btnDelMeal;
    private ListView lvMeals;
    private DatabaseHandler databaseHandler;

    int SELECT_PICTURE = 200;
    String selectedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_management);

        ivMeal = findViewById(R.id.iv_meal);
        etMealName = findViewById(R.id.et_meal_name);
        etMealDescription = findViewById(R.id.et_meal_description);
        etMealPrice = findViewById(R.id.et_meal_price);
        btnAddMeal = findViewById(R.id.btn_add_meal);
        btnDelMeal = findViewById(R.id.btn_delete_meal);
        lvMeals = findViewById(R.id.lv_all_meal);
        databaseHandler = new DatabaseHandler(this);
        databaseHandler.open();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.iv_meal) {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);

                    // pass the constant to compare it
                    // with the returned requestCode
                    startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
                } else if (view.getId() == R.id.btn_add_meal) {

                    byte[] image = null;
                    try {
                        Bitmap bitmap = ((BitmapDrawable) ivMeal.getDrawable()).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        image = stream.toByteArray();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String mealName = etMealName.getText().toString();
                    String mealDescription = etMealDescription.getText().toString();
                    int mealPrice = Integer.parseInt(etMealPrice.getText().toString());

                    databaseHandler.addMeal(image, mealName, mealDescription, mealPrice);
                    showAllMeals();
                    clearInputs();
                } else if (view.getId() == R.id.btn_delete_meal) {
                    if (selectedId != null) {
                        deleteMeal(selectedId);
                        clearInputs();
                    } else {
                        Toast.makeText(MealManagementActivity.this, "Please select a meal to delete", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        ivMeal.setOnClickListener(listener);
        btnAddMeal.setOnClickListener(listener);
        btnDelMeal.setOnClickListener(listener);
        showAllMeals();

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MealManagementActivity.this, position + " is selected", Toast.LENGTH_SHORT).show();

                selectedId = String.valueOf(parent.getItemIdAtPosition(position));
                showSelectedMeal(Integer.parseInt(String.valueOf(position)));
            }
        };

        lvMeals.setOnItemClickListener(itemClickListener);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    ivMeal.setImageURI(selectedImageUri);
                }
            }
        }
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        launchSomeActivity.launch(i);
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null
                            && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        Bitmap selectedImageBitmap = null;
                        try {
                            selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    selectedImageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ivMeal.setImageBitmap(selectedImageBitmap);
                    }
                }
            });


    private void showAllMeals() {
        Cursor cursor = databaseHandler.getAllMeals();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(MealManagementActivity.this,
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
                bitmap = Bitmap.createScaledBitmap(bitmap,300,300,false);
                ivMealImage.setImageBitmap(bitmap);

                return view;
            }
        };

        lvMeals.setAdapter(adapter);
    }

    private void showSelectedMeal(int id) {
        Cursor cursor = databaseHandler.getAllMeals();
        if (cursor.moveToFirst()) {
            for (int i = 0; i < id; i++) {
                cursor.moveToNext();
            }

            byte[] mealImage = cursor.getBlob(1);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(mealImage);
            ivMeal.setImageBitmap(BitmapFactory.decodeStream(inputStream));
            etMealName.setText(cursor.getString(2));
            etMealDescription.setText(cursor.getString(3));
            etMealPrice.setText(cursor.getString(4));
        }
    }

    private void clearInputs() {
        ivMeal.setImageResource(R.drawable.ic_launcher_background);
        etMealName.setText("");
        etMealDescription.setText("");
        etMealPrice.setText("");
        selectedId = null;
    }

    private void deleteMeal(String name) {
        databaseHandler.open();
        databaseHandler.deleteMeal(name);
        showAllMeals();
    }
}