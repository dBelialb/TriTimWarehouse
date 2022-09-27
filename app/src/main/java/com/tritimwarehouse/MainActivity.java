package com.tritimwarehouse;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.tritimwarehouse.models.User;


public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnRegister;
    FirebaseAuth auth;
    FirebaseDatabase base;
    DatabaseReference users;

    RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Открытие файла activity
        setContentView(R.layout.activity_main);
        //Прикрепление кнопок
        btnSignIn = findViewById(R.id.btnSingIn);
        btnRegister = findViewById(R.id.btnRegister);

        int orientation = Configuration.ORIENTATION_PORTRAIT;
        //Прикрепили к переменной файл activity
        root = findViewById(R.id.root_element);
        //Авторизация в базе данных
        auth = FirebaseAuth.getInstance();
        base = FirebaseDatabase.getInstance();
        users = base.getReference("Users");
        //Добавление кнопкам при нажатии вызов метода
        btnRegister.setOnClickListener(v -> showRegisterWindow());
        btnSignIn.setOnClickListener(v -> showSignWindow());
    }

    private void showSignWindow() {
        //Создания окна авторизации
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Авторизация");
        dialog.setMessage("Введите данные для входа");
        LayoutInflater inflater = LayoutInflater.from(this);
        View sign_in_window = inflater.inflate(R.layout.sign_in_window, null);
        dialog.setView(sign_in_window);
        //Создание переменных для получения данных из полей авторизации
        final MaterialEditText email = sign_in_window.findViewById(R.id.emailField);
        final MaterialEditText pass = sign_in_window.findViewById(R.id.passField);

        dialog.setNegativeButton("Отменить", (dialogInterface, which) -> dialogInterface.dismiss());
        dialog.setPositiveButton("Войти", (dialogInterface, which) -> {
            if (TextUtils.isEmpty(email.getText().toString())) {
                Snackbar.make(root, "Введите ваш email", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (pass.getText().toString().length() < 5) {
                Snackbar.make(root, "Введите пароль, который имеет длину более 5 символов", Snackbar.LENGTH_SHORT).show();
                return;
            }
            auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        startActivity(new Intent(MainActivity.this, MainStorage.class));
                        finish();
                    }).addOnFailureListener(e -> Snackbar.make(root, "Ошибка авторизации. " + e.getMessage(), Snackbar.LENGTH_SHORT).show());
        });
        dialog.show();
    }

    private void showRegisterWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Зарегистрироваться");
        dialog.setMessage("Введите данные для регистрации");
        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(register_window);

        final MaterialEditText email = register_window.findViewById(R.id.emailField);
        final MaterialEditText access = register_window.findViewById(R.id.nameField);
        final MaterialEditText pass = register_window.findViewById(R.id.passField);
        final MaterialEditText names = register_window.findViewById(R.id.namesField);

        dialog.setNegativeButton("Отменить", (dialogInterface, which) -> dialogInterface.dismiss());
        dialog.setPositiveButton("Добавить", (dialogInterface, which) -> {
            if (TextUtils.isEmpty(email.getText().toString())) {
                Snackbar.make(root, "Введите ваш email", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(names.getText().toString())) {
                Snackbar.make(root, "Введите ФИО", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(access.getText().toString())) {
                Snackbar.make(root, "Введите категорию доступа", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (access.getText().toString().equals("f1") || access.getText().toString().equals("b1")) {
                Snackbar.make(root, "Введите вашу категорию согласно подсказке", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (pass.getText().toString().length() < 5) {
                Snackbar.make(root, "Введите пароль, который имеет длину более 5 символов", Snackbar.LENGTH_SHORT).show();
                return;
            }
            auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        User user = new User();
                        user.setEmail(email.getText().toString());
                        user.setNames(names.getText().toString());
                        user.setAccess(access.getText().toString());
                        user.setPass(pass.getText().toString());

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(unused -> Snackbar.make(root, "Пользователь добавлен", Snackbar.LENGTH_SHORT).show());
                    }).addOnFailureListener(e -> Snackbar.make(root, "Ошибка регистрации. " + e.getMessage(),Snackbar.LENGTH_SHORT).show());
        });
        dialog.show();
    }
}