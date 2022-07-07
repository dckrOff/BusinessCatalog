package com.a1tech.businesscatalog.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.a1tech.businesscatalog.Adapter.ItemsAdapter;
import com.a1tech.businesscatalog.Model.Item;
import com.a1tech.businesscatalog.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private RecyclerView rvMain;
    private ItemsAdapter itemsAdapter;
    private ArrayList<Item> itemList = new ArrayList<Item>();
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        getList();
        setAdapter();
    }

    private void init() {
        Objects.requireNonNull(getSupportActionBar()).setTitle("Каталог");  // provide compatibility to all the versions
        rvMain = findViewById(R.id.rv_main);
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("items_list");
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddItemActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // below line is to get our inflater
        MenuInflater inflater = getMenuInflater();

        // inside inflater we are inflating our menu file.
        inflater.inflate(R.menu.search_menu, menu);

        // below line is to get our menu item.
        MenuItem searchItem = menu.findItem(R.id.actionSearch);

        // getting search view of our item.
        SearchView searchView = (SearchView) searchItem.getActionView();

        // below line is to call set on query text listener method.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                filter(newText);
                return false;
            }
        });
        return true;
    }

    private void filter(String text) {
        // creating a new array list to filter our data.
        ArrayList<Item> filteredlist = new ArrayList<>();

        // running a for loop to compare elements.
        for (Item item : itemList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.getItemName().toLowerCase().contains(text.toLowerCase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
//            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
            itemsAdapter.filterList(filteredlist);
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            itemsAdapter.filterList(filteredlist);
        }
    }

    private void getList() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
//                    Log.e(TAG, "1) " + childDataSnapshot.getKey()); //displays the key for the node
//                    Log.e(TAG, "2) " + childDataSnapshot.child("img").getValue());   //gives the value for given keyname
                    itemList.add(new Item(childDataSnapshot.child("itemName").getValue().toString(), childDataSnapshot.child("itemPrice").getValue().toString(), childDataSnapshot.child("itemImg").getValue().toString(), childDataSnapshot.child("itemAmount").getValue().toString()));
                }
                setAdapter();
                Log.e(TAG, "list size=> " + itemList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void setAdapter() {
        rvMain.setLayoutManager(new GridLayoutManager(this, 2));
        itemsAdapter = new ItemsAdapter(getApplicationContext(), itemList);
        rvMain.setAdapter(itemsAdapter); // set the Adapter to RecyclerView
    }

    @Override
    protected void onResume() {
        super.onResume();
        getList();
    }
}