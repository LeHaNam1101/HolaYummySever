package com.example.holayummyserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.holayummyserver.Common.Common;
import com.example.holayummyserver.Interface.ItemClickListener;
import com.example.holayummyserver.Model.Category;
import com.example.holayummyserver.Model.Food;
import com.example.holayummyserver.ViewHolder.FoodViewHolder;
import com.example.holayummyserver.databinding.ActivityFoodListBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import info.hoang8f.widget.FButton;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton fab;
    RelativeLayout rootLayout;

    //firebase
    FirebaseDatabase db;
    DatabaseReference foodList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    MaterialEditText edtName,edtDescription,edtPrice,edtDiscount;
    Button btnSelect,btnUpload;
    Food newFood;
    Uri saveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Firebase
        db = FirebaseDatabase.getInstance();
        foodList = db.getReference("Foods");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Init
        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);

        fab = (FloatingActionButton) findViewById(R.id.fab1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Code late
                ShowAddFoodDiaglog();
            }
        });

        if(getIntent() != null){
            categoryId = getIntent().getStringExtra("CategoryId");
            if(!categoryId.isEmpty()){
                loadListFood(categoryId);
            }
        }
    }

    private void ShowAddFoodDiaglog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Add new Food");
        alertDialog.setMessage("Please fill full infomation");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout, null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        edtDescription = add_menu_layout.findViewById(R.id.edtDescription);
        edtPrice = add_menu_layout.findViewById(R.id.edtPrice);
        edtDiscount = add_menu_layout.findViewById(R.id.edtDiscount);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.baseline_shopping_cart_24);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (newFood != null) {
                    foodList.push().setValue(newFood);
                    Snackbar.make(rootLayout, "New category" + newFood.getName() + " was added", Snackbar.LENGTH_SHORT)
                            .show();
                }

            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        alertDialog.show();
    }
    private void uploadImage() {
        if (saveUri != null) {
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodList.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    newFood = new Food();
                                    newFood.setName(edtName.getText().toString());
                                    newFood.setDescription(edtDescription.getText().toString());
                                    newFood.setPrice(edtPrice.getText().toString());
                                    newFood.setDiscount(edtDiscount.getText().toString());
                                    newFood.setMenuId(categoryId);
                                    newFood.setImage(uri.toString());



                                }
                            });
                        }
                    })
                    .addOnFailureListener((e) -> {
                        mDialog.dismiss();
                        Toast.makeText(FoodList.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded" + progress + "%");
                        }
                    });


        }
    }
    private void chooseImage() {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Create an intent to open the gallery app
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

// Start the intent to choose an image
        startActivityForResult(intent, Common.PICK_IMAGE_REQUEST);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("MenuId").equalTo(categoryId)

        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                                    //code late
                    }
                });

            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            saveUri = data.getData();
            btnSelect.setText("Image Selected !");
        }

    }
//    private RecyclerView recyclerView;
//    private RecyclerView.LayoutManager layoutManager;
//    private FirebaseDatabase database;
//    private DatabaseReference foodList;
//    String categoryId="";
//    private FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
//    private FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
//    List<String> suggestList = new ArrayList<>();
//    private ActivityFoodListBinding binding;
//
//    MaterialEditText edtName;
//    Button btnUpload, btnSelect;
//
//    Food newFood;
//
//    DatabaseReference foods;
//    DrawerLayout drawer;
//    Uri saveUri;
//    StorageReference storageReference;
//    private final int PICK_IMAGE_REQUEST = 71;
//
//
//
//
//
//
//    private void bindingView() {
//        setContentView(R.layout.activity_food_list);
//        recyclerView = findViewById(R.id.recycler_food);
//        recyclerView.setHasFixedSize(true);
//        layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//    }
//
//    private void bindingAction() {
//        database = FirebaseDatabase.getInstance();
//        foodList = database.getReference("Foods");
//
//        if (getIntent() != null) {
//            categoryId = getIntent().getStringExtra("CategoryId");
//        }
//
//        if (!categoryId.isEmpty() && categoryId != null) {
//            loadListFood(categoryId);
//        }
//        loadSuggest();
//
//        binding = ActivityFoodListBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//        binding.fab.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                showDialog();
//            }
//
//
//        });
//    }
//
//
//    private void loadSuggest() {
//        foodList.orderByChild("MenuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot postSnapshot:snapshot.getChildren() ){
//                    Food item = postSnapshot.getValue(Food.class);
//                    suggestList.add(item.getName());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        bindingView();
//        bindingAction();
//    }
//
//    private void showDialog() {
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
//        alertDialog.setTitle("Add new food");
//        alertDialog.setMessage("Please fill full infomation");
//
//        LayoutInflater inflater = this.getLayoutInflater();
//        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout, null);
//
//        edtName = add_menu_layout.findViewById(R.id.edtName);
//        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
//        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);
//
//        btnSelect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                chooseImage();
//            }
//        });
//
//        btnUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                uploadImage();
//            }
//        });
//        alertDialog.setView(add_menu_layout);
//        alertDialog.setIcon(R.drawable.baseline_shopping_cart_24);
//
//        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//
//                if (newFood != null) {
//                    foods.push().setValue(newFood);
//                    Snackbar.make(drawer, "New food" + newFood.getName() + " was added", Snackbar.LENGTH_SHORT)
//                            .show();
//                }
//
//            }
//        });
//        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                dialog.dismiss();
//
//            }
//        });
//
//        alertDialog.show();
//    }
//
//    private void uploadImage() {
//        if (saveUri != null) {
//            ProgressDialog mDialog = new ProgressDialog(this);
//            mDialog.setMessage("Uploading...");
//            mDialog.show();
//
//            String imageName = UUID.randomUUID().toString();
//            StorageReference imageFolder = storageReference.child("images/" + imageName);
////            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
////                        @Override
////                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
////                            mDialog.dismiss();
////                            Toast.makeText(FoodList.this, "Uploaded", Toast.LENGTH_SHORT).show();
////                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
////                                @Override
////                                public void onSuccess(Uri uri) {
////                                    newFood = new Food(edtName.getText().toString(), uri.toString());
////
////
////                                }
////                            });
////                        }
////                    })
////                    .addOnFailureListener((e) -> {
////                        mDialog.dismiss();
////                        Toast.makeText(Home.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
////                    })
////                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
////                        @Override
////                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
////                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
////                            mDialog.setMessage("Uploaded" + progress + "%");
////                        }
////                    });
//
//
//        }
//    }
//
//    private void chooseImage() {
////        Intent intent = new Intent();
////        intent.setAction(Intent.ACTION_GET_CONTENT);
//        // Create an intent to open the gallery app
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//// Start the intent to choose an image
//        startActivityForResult(intent, PICK_IMAGE_REQUEST);
////        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//    }
//
//    private void loadListFood(String categoryId) {
//        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
//                Food.class,
//                R.layout.food_item,
//                FoodViewHolder.class,
//                foodList.orderByChild("MenuId").equalTo(categoryId)
//        ) {
//            @Override
//            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
//                viewHolder.txtMenuName.setText(model.getName());
//                Picasso.with(getBaseContext()).load(model.getImage())
//                        .into(viewHolder.ImageView);
//                final Food local = model;
//                viewHolder.setItemClickListener(new ItemClickListener() {
//                    @Override
//                    public void onClick(View view, int pos, boolean isLongClick) {
//                        Intent foodDetail = new Intent(FoodList.this, FoodList.class);
//                        foodDetail.putExtra("FoodId", adapter.getRef(pos).getKey());
//                        startActivity(foodDetail);
//                    }
//                });
//            }
//        };
//
//        recyclerView.setAdapter(adapter);
//    }



}