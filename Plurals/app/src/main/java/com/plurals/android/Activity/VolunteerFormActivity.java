package com.plurals.android.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.plurals.android.Model.StateDistrictModel;
import com.plurals.android.R;
import com.plurals.android.Utility.CommonUtils;
import com.plurals.android.Utility.Constants;
import com.plurals.android.Utility.FileUtils;
import com.plurals.android.Utility.SendMail;
import com.plurals.android.Utility.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class VolunteerFormActivity extends AppCompatActivity {
    TextView textView;
    ArrayList<String> parliamentList, assemblyList, districtList;
    View snackView;
    ImageView back_button;
    Spinner sp_parliament, sp_assembly, sp_state, sp_district;
    ArrayAdapter parliamentAdapter, assemblyAdapter, stateAdapter, districtAdapter;
    EditText rv_name, rv_name_last, rv_mob, rv_email, rv_address_1, rv_pincode;
    TextView rv_save;
    Button rv_upload;
    String selectedState, selectedDistrict,selectedParliament,selectedAssembly,attachment;
    StateDistrictModel stateDistrictModel;
    ArrayList<String> stateList;
    Map<String, ArrayList<String>> pcMap = new TreeMap();

    private Boolean isValidName = false;
    private Boolean isValidNum = false;
    private Boolean isValidAddress1 = false;
    private Boolean isValidPincode = false;
    private Boolean isValidEmail = false;
    private Boolean isvalidState = false;
    private Boolean isValideDistrict = false;
    private Boolean isValideParliament = false;
    private Boolean isValideAssembly = false;
    private Boolean isAttached = false;
    private ProgressDialog progressDialog;
    static int PICK_FROM_GALLERY = 0;
    int columnIndex;
    static Uri URI = null;
    SharedPref sharedPref = SharedPref.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_form);
        snackView = findViewById(R.id.register_activity);
        back_button = findViewById(R.id.rv_back);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        findViews();
        fetchPCJson();
        fetchStateDistrictJson();
        // snackBar(snackView,"Welcome");
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
       // snackBar(snackView,"Welcome");
    }



    private void findViews() {
        String fullName = sharedPref.getUser_username(VolunteerFormActivity.this);
        String firstName,lastName;
        int idx = fullName.lastIndexOf(' ');
        Log.d("idx",""+idx);
        if (idx == -1) {
            firstName=fullName;
            lastName="";
        }
        else { firstName = fullName.substring(0, idx);
            lastName = fullName.substring(idx + 1);}
        sp_district = findViewById(R.id.sp_district);
        sp_state = findViewById(R.id.sp_state);
        rv_save = findViewById(R.id.rv_save);
        rv_pincode = findViewById(R.id.rv_pincode);
        rv_address_1 = findViewById(R.id.rv_address_1);
        rv_name = findViewById(R.id.rv_name);
        rv_name.setText(firstName);
        isValidName=true;
        rv_name_last = findViewById(R.id.rv_name_last);
        rv_name_last.setText(lastName);
        rv_mob = findViewById(R.id.rv_mob);
        rv_mob.setText(sharedPref.getMob(VolunteerFormActivity.this));
        isValidNum=true;
        rv_email = findViewById(R.id.rv_email);
        rv_email.setText(sharedPref.getUser_email(VolunteerFormActivity.this));
        isValidEmail=true;
        sp_assembly = findViewById(R.id.sp_assembly);
        sp_parliament = findViewById(R.id.sp_parliament);
        rv_save.setEnabled(false);

        rv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.hideForcefullyKeyboard(VolunteerFormActivity.this);
               // CommonUtils.showToastInDebug(VolunteerFormActivity.this, "saved");
               JSONObject dataJson= collectData();

               //TODO create your msg string below like -
                try {
                    progressDialog.show();
                    String msg=
                            "First Name = "+dataJson.getString("first_name")+
                               "\nLast Name =" +dataJson.getString("last_name")+
                                    "\nEmail =" +dataJson.getString("email")+
                                    "\nMobile =" +dataJson.getString("mobile")+
                                    "\nAdress =" +dataJson.getString("address")+
                                    "\nPincode =" +dataJson.getString("pincode")+
                                    "\nState =" +dataJson.getString("state")+
                                    "\nDistrict =" +dataJson.getString("district")+
                                    "\nParliament =" +dataJson.getString("parliament")+
                                    "\nAssembly =" +dataJson.getString("assembly");
                    SendMail sendMail = new SendMail(VolunteerFormActivity.this,"Volunteer Registered",msg,attachment){
                        @Override
                        protected void onPostExecute(Void aVoid) {
                            Log.d("mail","on post execute");
                           CommonUtils.toast_msg("Your Application submitted \n"+"Thanks",VolunteerFormActivity.this);
                            progressDialog.dismiss();
                            onBackPressed();
                            super.onPostExecute(aVoid);
                        }
                    };
                    sendMail.execute();

                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }

            }
        });

        setSpinnerListeners();
        setTextWatchers();
        rv_upload = findViewById(R.id.rv_upload);
        rv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMultiplePermissions();
               /* if (PICK_FROM_GALLERY==1)
                {
                  openFile();
                }*/
            }
        });
    }
    public void openImageGallery() {
        Log.e("attachment", "open gallery permission asked");
        if (PICK_FROM_GALLERY == 1) {
            Log.e("attachment", "open gallery after permission granted");
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, 1);

        }
    }
    public void openFile() {
        Log.e("attachment", "open gallery permission asked");
        if (PICK_FROM_GALLERY == 1) {
            Intent intent = new Intent();
            intent.setType("application/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra("return-data", true);
            startActivityForResult(
                    Intent.createChooser(intent, "Complete action using"),
                    PICK_FROM_GALLERY);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PICK_FROM_GALLERY==requestCode && data!=null) {

            Log.e("attachment", "request code = " + requestCode);
            Uri selectedImage = data.getData();
            Log.e("attachment", " selected image uri = " );
            String[] filePathColumn = {MediaStore.Files.FileColumns.MIME_TYPE};
            Log.e("attachment", "filePathColumn = " + filePathColumn);
            Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            Log.e("attachment", "cursor  = " + cursor);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                attachment = selectedImage.getPath();
            } else {
                cursor.moveToFirst();
                columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                Log.e("attachment", "coumn index  = " + columnIndex);
                attachment = cursor.getString(columnIndex);
                Log.e("attachment", "" + attachment);
                // attachment = Commons.getPath(selectedImage,context);
                Log.e("attachment", "" + attachment);
                URI = Uri.parse("content://" + attachment);

                attachment = FileUtils.getPath(this,selectedImage);
                Log.e("attachment", "" + attachment);
                Log.e("attachment", "uri = " + URI);
                cursor.close();
                isAttached=true;
                checkValidation();
            }

        }
    }

    private JSONObject collectData() {
        JSONObject data= new JSONObject();
        try {
            data.put("first_name",rv_name.getText().toString());
            data.put("last_name",rv_name_last.getText().toString());
            data.put("email",rv_email.getText().toString());
            data.put("mobile",rv_mob.getText().toString());
            data.put("address",rv_address_1.getText().toString());
            data.put("pincode",rv_pincode.getText().toString());
            data.put("state",selectedState);
            data.put("district",selectedDistrict);
            data.put("parliament",selectedParliament);
            data.put("assembly",selectedAssembly);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }



    private void setSpinnerListeners() {
        sp_parliament.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    isValideParliament = true;
                    selectedParliament=parliamentList.get(position);
                    assemblyList = new ArrayList();
                    assemblyList.add("Select An Assembly");
                    Collections.sort(pcMap.get(parliamentList.get(position)), String.CASE_INSENSITIVE_ORDER);

                    assemblyList.addAll(pcMap.get(parliamentList.get(position)));
                    assemblyAdapter = new ArrayAdapter(VolunteerFormActivity.this, android.R.layout.simple_spinner_item, assemblyList);
                    assemblyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_assembly.setAdapter(assemblyAdapter);

                } else {
                    selectedParliament="";
                    isValideParliament = false;
                    assemblyList = new ArrayList();
                    assemblyList.add("Select An Assembly");
                    assemblyAdapter = new ArrayAdapter(VolunteerFormActivity.this, android.R.layout.simple_spinner_item, assemblyList);
                    assemblyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_assembly.setAdapter(assemblyAdapter);

                }
                checkValidation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sp_assembly.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedAssembly=assemblyList.get(position);
                    isValideAssembly = true;
                } else {
                    selectedAssembly="";
                    isValideAssembly = false;
                }
                checkValidation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedState = stateList.get(position);
                    isvalidState = true;
                    districtList = new ArrayList();
                    districtList.add("District");
                    districtList.addAll(stateDistrictModel.getStates().get(position - 1).getDistricts());
                    districtAdapter = new ArrayAdapter(VolunteerFormActivity.this, android.R.layout.simple_spinner_item, districtList);
                    districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_district.setAdapter(districtAdapter);

                } else {
                    selectedState = "";
                    isvalidState = false;
                    districtList = new ArrayList();
                    districtList.add("District");
                    districtAdapter = new ArrayAdapter(VolunteerFormActivity.this, android.R.layout.simple_spinner_item, districtList);
                    districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_district.setAdapter(districtAdapter);

                }
                checkValidation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_district.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedDistrict = districtList.get(position);
                    isValideDistrict = true;
                } else {
                    selectedDistrict = "";
                    isValideDistrict = false;
                }
                checkValidation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    private void fetchPCJson() {
        String json = null;
        try {
            InputStream is = getAssets().open("bihar_pc_ac_list.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            JSONObject dataJson = new JSONObject(json);
            preparePCMap(dataJson);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    private void fetchStateDistrictJson() {
        String json = null;
        try {
            InputStream is = getAssets().open("india_state_capitals.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            stateDistrictModel = new Gson().fromJson(json, StateDistrictModel.class);
            if (stateDistrictModel != null) {
                initStateSpinenr();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    private void initStateSpinenr() {
        stateList = new ArrayList();
        stateList.add("State");

        for (int i = 0; i <= stateDistrictModel.getStates().size() - 1; i++) {
            stateList.add(stateDistrictModel.getStates().get(i).getState());
        }
        stateAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, stateList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_state.setAdapter(stateAdapter);


    }




    private void preparePCMap(JSONObject dataJson) {
        pcMap.clear();
        try {
            JSONArray array = dataJson.getJSONArray("bihar");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (pcMap.containsKey(obj.getString("PC_Name"))) {
                    pcMap.get(obj.getString("PC_Name")).add(obj.getString("AC_Name"));
                } else {
                    ArrayList<String> list = new ArrayList();
                    list.add(obj.getString("AC_Name"));
                    pcMap.put(obj.getString("PC_Name"), list);
                }
            }
            parliamentList = new ArrayList();
            parliamentList.add("Select A Parliament");
            parliamentList.addAll(pcMap.keySet());
            parliamentAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, parliamentList);
            parliamentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_parliament.setAdapter(parliamentAdapter);

            //add thumbnail text for assembly spinner
            assemblyList = new ArrayList();
            assemblyList.add("Select An Assembly");
            assemblyAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, assemblyList);
            assemblyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_assembly.setAdapter(assemblyAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void snackBar(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();

    }

    private void setTextWatchers() {
        rv_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    isValidName = true;
                } else {
                    isValidName = false;
                }
                checkValidation();
            }
        });
        rv_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && Pattern.matches(Constants.EMAIL_ID_REJEX, s.toString())) {
                    isValidEmail = true;
                } else {
                    isValidEmail = false;
                }
                checkValidation();

            }
        });
        rv_mob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10 && Pattern.matches(Constants.MOBILE_NUM_REGEX, s.toString())) {
                    isValidNum = true;
                } else {
                    isValidNum = false;
                }
                checkValidation();
            }
        });
        rv_address_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    isValidAddress1 = true;
                } else {
                    isValidAddress1 = false;
                }
                checkValidation();
            }
        });
        rv_pincode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 4 && Pattern.matches(Constants.PINCODE_REJEX, s.toString())) {
                    isValidPincode = true;
                } else {
                    isValidPincode = false;
                }
                checkValidation();
            }
        });
    }



    private void checkValidation() {
        if (isAttached&&isValidName && isValidEmail && isValidNum && isValidAddress1 && isValidPincode && isvalidState && isValideDistrict && isValideParliament && isValideAssembly) {
            rv_save.setAlpha(1f);
            rv_save.setEnabled(true);
        } else {
            rv_save.setAlpha(0.5f);
            rv_save.setEnabled(false);
        }
    }




    private void  requestMultiplePermissions(){
        Dexter.withActivity(VolunteerFormActivity.this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            PICK_FROM_GALLERY = 1;
                            openFile();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            Toast.makeText(VolunteerFormActivity.this, "permission Denied by user", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(VolunteerFormActivity.this, "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }
}
