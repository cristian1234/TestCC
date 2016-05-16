package com.test.cc;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentTransaction;

public class PostsActivity extends Activity {//FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.posts_activity);
        //if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();//getSupportFragmentManager().beginTransaction();
            PostListFragment fragment = new PostListFragment();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        //}

    }

}