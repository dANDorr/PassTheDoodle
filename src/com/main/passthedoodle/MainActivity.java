package com.main.passthedoodle;

import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends FragmentActivity {
    
	ViewPager mViewPager;
	MainPagerAdapter mMainPagerAdapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.main_pager);
        mViewPager.setAdapter(mMainPagerAdapter);
        
        // The default value 1 is the Home tab. Goes to games list if passed 2
        int tabInt = getIntent().getIntExtra("Tab", 1);
        mViewPager.setCurrentItem(tabInt);
        
        // so that games list doesn't refresh every time you swipe away and swipe back to it
        mViewPager.setOffscreenPageLimit(2);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public class MainPagerAdapter extends FragmentPagerAdapter {
    	public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    	@Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new CreateFragment();
                case 1:
                	return new HomeFragment();
                case 2:
                	return new BrowseFragment();
                default:
                    return new CreateFragment();
            }
        }
        @Override
        public int getCount() {
            return 3;
        }
        @Override
        public CharSequence getPageTitle(int position) {
        	if (position == 0)
        		return "Create";
        	else if (position == 1)
        		return "Home";
        	else if (position == 2)
        		return "Browse";
        	else
        		return "gtfo";
        }
    }

    public static class HomeFragment extends Fragment {
    	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    		View rootView = inflater.inflate(R.layout.fragment_home, container, false);
    		
    		rootView.findViewById(R.id.button_login)
            			.setOnClickListener(new View.OnClickListener() {
            				@Override
				    		public void onClick(View view) {
				                Intent intent = new Intent(getActivity(), LoginActivity.class);
				                startActivity(intent);
				            }
            			});
    		rootView.findViewById(R.id.button_register)
						.setOnClickListener(new View.OnClickListener() {
							@Override
				    		public void onClick(View view) {
				                Intent intent = new Intent(getActivity(), RegisterActivity.class);
				                startActivity(intent);
				            }
						});
    		rootView.findViewById(R.id.button_guest)
						.setOnClickListener(new View.OnClickListener() {
							@Override
				    		public void onClick(View view) {
				                Intent intent = new Intent(getActivity(), RegisterActivity.class);
				                startActivity(intent);
				            }
						});
    		return rootView;
    	}
    }
    
    public static class CreateFragment extends Fragment {
    	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    		View rootView = inflater.inflate(R.layout.fragment_create, container, false);
    		
    		rootView.findViewById(R.id.button_paint)
						.setOnClickListener(new View.OnClickListener() {
							@Override
				    		public void onClick(View view) {
				                Intent intent = new Intent(getActivity(), DrawingActivity.class);
				                startActivity(intent);
				            }
						});
			rootView.findViewById(R.id.button_guess)
						.setOnClickListener(new View.OnClickListener() {
							@Override
				    		public void onClick(View view) {
				                Intent intent = new Intent(getActivity(), TextActivity.class);
				                startActivity(intent);
				            }
						});
			rootView.findViewById(R.id.button_local_play)
						.setOnClickListener(new View.OnClickListener() {
							@Override
				    		public void onClick(View view) {
				                Intent intent = new Intent(getActivity(), DrawingActivity.class);
				                intent.putExtra("isLocal", true);
				                startActivity(intent);
				            }
						});
			rootView.findViewById(R.id.button_game)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), NewGameActivity.class);
                                startActivity(intent);
                            }
                        });
			return rootView;
    	}
    }
}