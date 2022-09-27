package com.tritimwarehouse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.tritimwarehouse.models.Product;
import com.tritimwarehouse.models.User;


import java.util.ArrayList;
import java.util.List;

public class MainStorage extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> list;
    private String BASE_KEY = "Base";
    FirebaseDatabase baseProduct;
    private DatabaseReference main_base;
    private Button addBase;
    RelativeLayout boot;
    private String string;
    private List<Product> productList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Вывод activity
        setContentView(R.layout.activity_main_storage);
        boot = findViewById(R.id.storage);
        baseProduct = FirebaseDatabase.getInstance();
        main_base = baseProduct.getReference(BASE_KEY);
        //Получения у пользователя категории доступа
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference user1 = FirebaseDatabase.getInstance().getReference();
        DatabaseReference user2 = user1.child("Users").child(user);
        user2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user3 = snapshot.getValue(User.class);
                assert user3 != null;
                string = user3.getAccess();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        addBase = findViewById(R.id.btnSetProduct);
        addBase.setOnClickListener(v -> {
            if (string.equals("\"b1\""))
                showAddWindow();
            else {
                Snackbar.make(boot, "У вас недостаточно прав", Snackbar.LENGTH_SHORT).show();
            }
        });
        init();
        getDataFromDB();
        setOnClickItem();
    }

    private void showAddWindow() {
        AlertDialog.Builder dialogAdd = new AlertDialog.Builder(this);
        dialogAdd.setTitle("Добавить товар");
        dialogAdd.setMessage("Введите данные");
        LayoutInflater inflaterAdd = LayoutInflater.from(this);
        View add_window = inflaterAdd.inflate(R.layout.add_window, null);
        dialogAdd.setView(add_window);

        final MaterialEditText name = add_window.findViewById(R.id.name1Field);
        final MaterialEditText quantity = add_window.findViewById(R.id.int1Field);
        dialogAdd.setNegativeButton("Отменить", (dialogInterface1, which) -> dialogInterface1.dismiss());
        dialogAdd.setPositiveButton("Добавить", (dialogInterface1, which) -> {
            if (TextUtils.isEmpty(name.getText().toString())) {
                Snackbar.make(boot, "Введите название", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(quantity.getText().toString())) {
                Snackbar.make(boot, "Введите количество", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (Integer.parseInt(quantity.getText().toString()) <= 0) {
                Snackbar.make(boot, "Введите число больше 0", Snackbar.LENGTH_SHORT).show();
                return;
            }
            Product newProduct = new Product(name.getText().toString(), Integer.parseInt(quantity.getText().toString()));
            main_base.push().setValue(newProduct)
                    .addOnSuccessListener(unused -> Snackbar.make(boot, "Товар добавлен", Snackbar.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Snackbar.make(boot, "Ошибка. " + e.getMessage(), Snackbar.LENGTH_SHORT).show());
        });
        dialogAdd.show();
    }

    private void init() {
        listView = findViewById(R.id.listView);
        list = new ArrayList<>();
        productList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

    }

    private void getDataFromDB() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (list.size() > 0)
                    list.clear();
                if (productList.size() > 0)
                    productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product product = ds.getValue(Product.class);
                    assert product != null;
                    list.add(product.getName());
                    productList.add(product);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        main_base.addValueEventListener(valueEventListener);
    }

    private void setOnClickItem() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            AlertDialog.Builder dialogAdd = new AlertDialog.Builder(this);
            dialogAdd.setTitle("Действия");
            dialogAdd.setMessage("Выберите действие");
            LayoutInflater inflaterAdd = LayoutInflater.from(this);
            View change_window = inflaterAdd.inflate(R.layout.change_window, null);
            dialogAdd.setView(change_window);
            Product product = productList.get(position);
            dialogAdd.setNegativeButton("Количество", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Snackbar.make(boot, "Количество - " + product.getQuantity(), Snackbar.LENGTH_LONG).show();
                }
            });
            dialogAdd.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (string.equals("\"b1\""))
                        deleteData(position);
                    else {
                        dialog.dismiss();
                        Snackbar.make(boot, "У вас недостаточно прав", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
            dialogAdd.show();
        });
    }

    private void deleteData(int position) {
        Product product = productList.get(position);
        baseProduct = FirebaseDatabase.getInstance();
        main_base = baseProduct.getReference();
        main_base.child(BASE_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Product product1 = ds.getValue(Product.class);
                    String a = ds.getKey();
                    if (product1.getName().equals(product.getName())) {
                        main_base.child(BASE_KEY).child(a).removeValue().addOnSuccessListener(unused -> Snackbar.make(boot, "Товар удален", Snackbar.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Snackbar.make(boot, "Ошибка. " + e.getMessage(), Snackbar.LENGTH_SHORT).show());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("User", databaseError.getMessage());
            }
        });
    }

}