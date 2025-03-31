package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class activity_budget extends AppCompatActivity {

    private EditText edtBudget;
    private Button btnSetBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        edtBudget = findViewById(R.id.edt_budget);
        btnSetBudget = findViewById(R.id.btn_set_budget);

        btnSetBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String budget = edtBudget.getText().toString();
                Intent intent = new Intent(activity_budget.this, activity_hp.class);
                intent.putExtra("BUDGET_AMOUNT", budget);
                startActivity(intent);
            }
        });
    }
}
