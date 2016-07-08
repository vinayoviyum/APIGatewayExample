package com.oviyum.apigatewayexample;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.amazonaws.auth.AWSBasicCognitoIdentityProvider;
import com.amazonaws.auth.AWSCognitoIdentityProvider;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.regions.Regions;
import com.oviyum.fleetfoot.FleetMobileClient;
import com.oviyum.fleetfoot.model.LoginRequest;
import com.oviyum.fleetfoot.model.LoginResponse;
import com.oviyum.fleetfoot.model.SetLocationRequest;
import com.oviyum.fleetfoot.model.SetLocationRequestLocationItem;
import com.oviyum.fleetfoot.model.SetLocationRequestOwnerItem;
import com.oviyum.fleetfoot.model.SetLocationResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    public LoginResponse loginResponse;
    public LoginRequest loginRequest;

    public SetLocationRequest locationRequest;
    public SetLocationResponse locationResponse;


    public ApiClientFactory apiClientFactory;
    public FleetMobileClient fleetMobileClient;
    public AWSCredentialsProvider awsCredentialsProvider;

    AWSCognitoIdentityProvider cognitoIdentityProvider;




    public Button login,location,logout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (Button)findViewById(R.id.login);
        location = (Button)findViewById(R.id.location);
        logout = (Button)findViewById(R.id.logout);
        loginRequest = new LoginRequest();

        apiClientFactory = new ApiClientFactory();
        fleetMobileClient = apiClientFactory.build(FleetMobileClient.class);

        loginRequest.setEmail("admin@shary.com");
        loginRequest.setPassword("spravce9631");
        loginRequest.setRememberMe(false);



        login.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login:
                new LoginProcess().execute();
                break;
            case R.id.location:
               // new LocationProcess().execute();
                break;
        }
    }


    private class LoginProcess extends AsyncTask<String,String,String>{

        public LoginProcess(){

        }


        @Override
        protected String doInBackground(String... strings) {
            loginResponse = fleetMobileClient.processLoginPost(loginRequest);
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            new LocationProcess().execute();
            super.onPostExecute(s);
        }
    }

    private class LocationProcess extends AsyncTask<String,String,String>{

        public LocationProcess(){

            try {
                cognitoIdentityProvider = new AWSBasicCognitoIdentityProvider("777031169818","us-east-1:a5cf40c8-c0a1-40ab-bd23-5d456c3fbc2e");

                Map<String, String> logins = new HashMap<String, String>();
                logins.put("dev2.octanesofttech.com", loginResponse.getCredentials().getSessionToken());
                cognitoIdentityProvider.setLogins(logins);


                awsCredentialsProvider = new CognitoCachingCredentialsProvider(
                        LoginActivity.this,          // activity context
                        cognitoIdentityProvider, // Cognito identity pool id
                        Regions.US_EAST_1 // region of Cognito identity pool
                );


                apiClientFactory.credentialsProvider(awsCredentialsProvider);
                apiClientFactory.apiKey(loginResponse.getCredentials().getAccessKeyId());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
                //apiClientFactory.credentialsProvider()'
//            try {
                locationResponse = fleetMobileClient.setSessionLocationPost(getLocationRequest(1));
//            }catch (Exception e){
//                System.out.print(e.toString());
//                e.printStackTrace();
//            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            locationResponse.getStatus();
            super.onPostExecute(s);
        }
    }




    public SetLocationRequest getLocationRequest(int locationID){
        LoginResponse loginResponse =  this.loginResponse;


        SetLocationRequest setLocationRequest = new SetLocationRequest();

        List<SetLocationRequestOwnerItem> setLocationRequestOwnerItems = getSetLocationRequestOwnerItemList(loginResponse);
        List<SetLocationRequestLocationItem> setLocationRequestLocationItems = getSetLocationRequestLocationItemList(loginResponse,locationID);

        setLocationRequest.setOwner(setLocationRequestOwnerItems);
        setLocationRequest.setLocation(setLocationRequestLocationItems);


        return setLocationRequest;
    }


    private List<SetLocationRequestOwnerItem> getSetLocationRequestOwnerItemList(LoginResponse loginResponse){

        List<SetLocationRequestOwnerItem> setLocationRequestOwnerItems = new ArrayList<>();
        SetLocationRequestOwnerItem locationRequestOwnerItem = new SetLocationRequestOwnerItem();


        locationRequestOwnerItem.setCompanyName(loginResponse.getOwners().get(0).getCompanyName());
        locationRequestOwnerItem.setId(loginResponse.getOwners().get(0).getId());
        locationRequestOwnerItem.setFleetUserId(loginResponse.getOwners().get(0).getFleetUserId());

        setLocationRequestOwnerItems.add(locationRequestOwnerItem);


        return setLocationRequestOwnerItems;

    }

    private List<SetLocationRequestLocationItem> getSetLocationRequestLocationItemList(LoginResponse loginResponse,int locationID){
        List<SetLocationRequestLocationItem> setLocationRequestLocationItems = new ArrayList<>();
        SetLocationRequestLocationItem locationRequestLocationItem = new SetLocationRequestLocationItem();

        locationRequestLocationItem.setId(loginResponse.getLocations().get(locationID).getId());
        locationRequestLocationItem.setFleetLocationId(loginResponse.getLocations().get(locationID).getFleetLocationId());
        locationRequestLocationItem.setCaption(loginResponse.getLocations().get(locationID).getCaption());

        setLocationRequestLocationItems.add(locationRequestLocationItem);

        return setLocationRequestLocationItems;
    }
}


