package com.example.vojjalateja.symphonia;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    Context context;
    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private AutoCompleteTextView edtSeach;
    private BroadcastReceiver deletereceiver;
    public static int numberoffiles,firsttime,intenrnetConnection;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT = 100;
    private SearchSuggestions searchSuggestions;
    private ArrayAdapter<String> suggestionsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firsttime=0;
        viewPager = (ViewPager)findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        context=this;
        Typeface font = Typeface.createFromAsset(this.getAssets(), "fontawesome-webfont.ttf");
        TextView t0=new TextView(this);
        TextView t1=new TextView(this);
        t1.setText("\uf019");
        t0.setText("\uf001");
        t1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        t1.setTypeface(font);
        t1.setGravity(Gravity.CENTER);
        t0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        t0.setTypeface(font);
        t0.setGravity(Gravity.CENTER);
        t1.setTextColor(Color.parseColor("#9b59b6"));
        t0.setTextColor(Color.parseColor("#2196F3"));
        tabLayout.getTabAt(0).setCustomView(t0);
        tabLayout.getTabAt(1).setCustomView(t1);
        deletereceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                File file =new File(Environment.getExternalStorageDirectory()+"/symphonia_downloads");
                numberoffiles=file.listFiles().length;
                viewPager.getAdapter().notifyDataSetChanged();
            }
        };
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                File file =new File(Environment.getExternalStorageDirectory()+"/symphonia_downloads");
                int len=file.listFiles().length;
                if (position == 1&&len!=numberoffiles)
                {
                    numberoffiles=len;
                    viewPager.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) ) {
                    finish();
                }

            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.vojjalateja.deleteaction");
        registerReceiver(deletereceiver,filter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(deletereceiver);
    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new latestsongs());
        adapter.addFragment(new downloads());
        viewPager.setAdapter(adapter);
    }
    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        @Override
        public int getItemPosition(Object object) {
            if(object==getItem(1))
            return POSITION_NONE;
            else{
                if(firsttime==0){
                    firsttime=1;
                    return POSITION_NONE;
                }
                return POSITION_UNCHANGED;
            }
        }
        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                if(isSearchOpened)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtSeach.getWindowToken(), 0);
                    onBackPressed();
                }
                return true;
            case R.id.action_settings:
                Intent i=new Intent(context,prefsactivity.class);
                startActivity(i);
                return true;
            case R.id.action_search:
                handleMenuSearch();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    protected void handleMenuSearch(){
        ActionBar action = getSupportActionBar();
        if(isSearchOpened){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            action.setDisplayShowCustomEnabled(false);
            action.setDisplayShowTitleEnabled(true);
            mSearchAction.setVisible(true);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtSeach.getWindowToken(), 0);
            mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_open_search));
            isSearchOpened = false;
        } else {
            action.setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mSearchAction.setVisible(false);
            action.setCustomView(R.layout.search_bar);
            action.setDisplayShowTitleEnabled(false);
            edtSeach = (AutoCompleteTextView) action.getCustomView().findViewById(R.id.edtSearch);
            edtSeach.setHint("Enter a movie/song name");
            edtSeach.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        doSearch();
                        return true;
                    }
                    return false;
                }
            });
            edtSeach.requestFocus();
            suggestionsAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,new ArrayList<String>());
            edtSeach.setAdapter(suggestionsAdapter);
            edtSeach.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String query = s.toString().trim().replaceAll("\\s","+");
                    /*if(searchSuggestions!=null) {
                        searchSuggestions.cancel(true);
                        searchSuggestions = null;
                    }*/
                    searchSuggestions = new SearchSuggestions();
                    searchSuggestions.execute(query);
                }
            });
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSeach, InputMethodManager.SHOW_IMPLICIT);
            isSearchOpened = true;
        }
    }
    private void doSearch() {
        String searchedFor=edtSeach.getText().toString();
        Intent i=new Intent(this,searchactivity.class);
        i.putExtra("searchedfor",searchedFor);
        startActivity(i);
    }
    @Override
    public void onBackPressed() {
        if(isSearchOpened) {
            handleMenuSearch();
            return;
        }
        super.onBackPressed();
    }

    private class SearchSuggestions extends AsyncTask<String,Void,List<String> >{

        @Override
        protected List<String> doInBackground(String... params) {
            String query = params[0];
            Document document = null;
            List<String> suggestions = new ArrayList<>();
            try{
                document = Jsoup.connect("http://smp3dl.com/category/instant?q="+query).get();
                String[] result = document.select("body").get(0).text().split("name=");
                for(int i=1;i<result.length;i=i+2){
                    String st="";
                    for(int j=0;j<result[i].length();j++)
                    {
                        if(result[i].charAt(j)=='%'||result[i].charAt(j)=='&')
                            break;
                        if(result[i].charAt(j)=='+')
                            st=st+' ';
                        else
                            st=st+result[i].charAt(j);
                    }
                    suggestions.add(st);
                    Log.d("Pattern "+i,st);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return suggestions;
        }

        @Override
        protected void onPostExecute(List<String> suggestions) {
            super.onPostExecute(suggestions);
            if(edtSeach!=null && suggestionsAdapter!=null) {
                suggestionsAdapter.clear();
                suggestionsAdapter.addAll(suggestions);
                suggestionsAdapter.notifyDataSetChanged();
            }
        }
    }
}
