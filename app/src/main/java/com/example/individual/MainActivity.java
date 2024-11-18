package com.example.individual;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editTextValue, editTextUpdateId, editTextUpdateValue, editTextDeleteId;
    private TextView textViewData;

    private static final String BASE_URL = "http://192.168.56.1/php-android/";
    private static final String CREATE_URL = BASE_URL + "create.php";
    private static final String READ_URL = BASE_URL + "read.php";
    private static final String UPDATE_URL = BASE_URL + "update.php";
    private static final String DELETE_URL = BASE_URL + "delete.php";

    private RequestQueue requestQueue;

    private FirebaseFirestore firestore;
    private CollectionReference firestoreCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        editTextValue = findViewById(R.id.editTextValue);
        editTextUpdateId = findViewById(R.id.editTextUpdateId);
        editTextUpdateValue = findViewById(R.id.editTextUpdateValue);
        editTextDeleteId = findViewById(R.id.editTextDeleteId);
        textViewData = findViewById(R.id.textViewData);

        Button buttonCreateFirestore = findViewById(R.id.buttonCreateFirestore);
        Button buttonUpdateFirestore = findViewById(R.id.buttonUpdateFirestore);
        Button buttonDeleteFirestore = findViewById(R.id.buttonDeleteFirestore);
        Button buttonShowFirestore = findViewById(R.id.buttonShowFirestore);

        Button buttonCreateMySQL = findViewById(R.id.buttonCreateMySQL);
        Button buttonUpdateMySQL = findViewById(R.id.buttonUpdateMySQL);
        Button buttonDeleteMySQL = findViewById(R.id.buttonDeleteMySQL);
        Button buttonShowMySQL = findViewById(R.id.buttonShowMySQL);

        requestQueue = Volley.newRequestQueue(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        firestoreCollection = firestore.collection("crud_table");

        // Set Button Listeners for Firestore
        buttonCreateFirestore.setOnClickListener(v -> createDataFirestore());
        buttonUpdateFirestore.setOnClickListener(v -> updateDataFirestore());
        buttonDeleteFirestore.setOnClickListener(v -> deleteDataFirestore());
        buttonShowFirestore.setOnClickListener(v -> fetchDataFirestore());

        // Set Button Listeners for MySQL
        buttonCreateMySQL.setOnClickListener(v -> createDataMySQL());
        buttonUpdateMySQL.setOnClickListener(v -> updateDataMySQL());
        buttonDeleteMySQL.setOnClickListener(v -> deleteDataMySQL());
        buttonShowMySQL.setOnClickListener(v -> fetchDataMySQL());
    }

    // ---------- Firestore Methods ----------

    private void createDataFirestore() {
        String value = editTextValue.getText().toString().trim();
        if (value.isEmpty()) {
            Toast.makeText(this, "Enter a value", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("data", value);

        firestoreCollection.add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Added to Firestore", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding document", e);
                });
    }

    private void updateDataFirestore() {
        String id = editTextUpdateId.getText().toString().trim();
        String value = editTextUpdateValue.getText().toString().trim();

        if (id.isEmpty() || value.isEmpty()) {
            Toast.makeText(this, "Enter ID and Value", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreCollection.document(id).update("data", value)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Updated in Firestore", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "DocumentSnapshot updated with ID: " + id);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error updating document", e);
                });
    }

    private void deleteDataFirestore() {
        String id = editTextDeleteId.getText().toString().trim();
        if (id.isEmpty()) {
            Toast.makeText(this, "Enter ID", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreCollection.document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Deleted from Firestore", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "DocumentSnapshot deleted with ID: " + id);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error deleting document", e);
                });
    }

    private void fetchDataFirestore() {
        firestoreCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder data = new StringBuilder();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            data.append("ID: ").append(document.getId())
                                    .append(", Value: ").append(document.getString("data"))
                                    .append("\n");
                        }
                        textViewData.setText(data.toString());
                        Toast.makeText(this, "Fetched data from Firestore", Toast.LENGTH_SHORT).show();
                        Log.d("Firestore", "Fetched data: \n" + data.toString());
                    } else {
                        Toast.makeText(this, "Error fetching Firestore data", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error fetching documents", task.getException());
                    }
                });
    }

    // ---------- MySQL Methods ----------

    private void createDataMySQL() {
        final String value = editTextValue.getText().toString().trim();
        if (value.isEmpty()) {
            Toast.makeText(this, "Enter a value", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, CREATE_URL,
                response -> {
                    Toast.makeText(this, "Added to MySQL", Toast.LENGTH_SHORT).show();
                    Log.d("MySQL", "Successfully added data: " + response);
                },
                error -> {
                    Toast.makeText(this, "Error adding to MySQL: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("MySQL", "Error adding data", error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("data", value);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void updateDataMySQL() {
        final String id = editTextUpdateId.getText().toString().trim();
        final String value = editTextUpdateValue.getText().toString().trim();

        if (id.isEmpty() || value.isEmpty()) {
            Toast.makeText(this, "Enter ID and value", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_URL,
                response -> {
                    Toast.makeText(this, "Updated in MySQL", Toast.LENGTH_SHORT).show();
                    Log.d("MySQL", "Successfully updated data for ID: " + id + ", Response: " + response);
                },
                error -> {
                    Toast.makeText(this, "Error updating MySQL: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("MySQL", "Error updating data", error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("data", value);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void deleteDataMySQL() {
        final String id = editTextDeleteId.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(this, "Enter ID", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_URL,
                response -> {
                    Toast.makeText(this, "Deleted from MySQL", Toast.LENGTH_SHORT).show();
                    Log.d("MySQL", "Successfully deleted data with ID: " + id + ", Response: " + response);
                },
                error -> {
                    Toast.makeText(this, "Error deleting from MySQL: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("MySQL", "Error deleting data", error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void fetchDataMySQL() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, READ_URL, null,
                response -> {
                    StringBuilder data = new StringBuilder();

                    try {
                        // Extract the data array from the JSON object
                        JSONArray dataArray = response.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject jsonObject = dataArray.getJSONObject(i);
                            data.append("ID: ").append(jsonObject.getString("id"))
                                    .append(", Value: ").append(jsonObject.getString("data"))
                                    .append("\n");
                        }

                        textViewData.setText(data.toString());
                        Toast.makeText(this, "Fetched data from MySQL", Toast.LENGTH_SHORT).show();
                        Log.d("MySQL", "Fetched data: \n" + data.toString());
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing MySQL data", Toast.LENGTH_SHORT).show();
                        Log.e("MySQL", "JSON Parsing error", e);
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching MySQL data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("MySQL", "Error fetching data", error);
                });

        requestQueue.add(jsonObjectRequest);
    }

}
