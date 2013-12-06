package com.main.passthedoodle;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

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
        int tabInt = getIntent().getIntExtra("Tab", getResources().getInteger(R.integer.home_tab));
        mViewPager.setCurrentItem(tabInt);
        
        // so that games list doesn't refresh every time you swipe away and swipe back to it
        mViewPager.setOffscreenPageLimit(3);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_change_email).setIntent(new Intent(this, ChangeEmailActivity.class));
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
                    return new NewGameFragment();
                case 1:
                	return new HomeFragment();
                case 2:
                	return new BrowseFragment();
                case 3:
                    return new BrowseCompFragment();
//                case 4:
//                    return new TempFragment();
                default:
//                    return new TempFragment();
                	return new HomeFragment();
            }
        }
        @Override
        public int getCount() { // the number of tabs
            return 4;
        }
        @Override
        public CharSequence getPageTitle(int position) {
        	if (position == getResources().getInteger(R.integer.create_tab))
        		return "Create";
        	else if (position == getResources().getInteger(R.integer.home_tab))
        		return "Home";
        	else if (position == getResources().getInteger(R.integer.browse_tab))
        		return "Browse";
        	else if (position == 3)
        	    return "Browse Completed";
        	//else if (position == 4)
        	//    return "Temp";
        	else
        		return "Error";
        }
    }

    public static class HomeFragment extends Fragment {
    	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    		View rootView = inflater.inflate(R.layout.fragment_home, container, false);

    		rootView.findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            });
    		rootView.findViewById(R.id.button_logout).setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View view) {
    				getActivity().getSharedPreferences("ptd",0).edit().clear().commit();
    				view.findViewById(R.id.button_logout).setVisibility(View.GONE);
					View v = (View)view.getParent();
					v.findViewById(R.id.button_login).setVisibility(View.VISIBLE);
    			}
    		});
    		rootView.findViewById(R.id.button_guest).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                	final Dialog dialog = new Dialog(getActivity());
                	dialog.setContentView(R.layout.help_dialog);
                	dialog.setCancelable(true);
                	dialog.setTitle("How to play");
                	dialog.setCanceledOnTouchOutside(true);
                	ImageView image = (ImageView) dialog.findViewById(R.id.help_image);
                	image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                          dialog.dismiss();
                        }
                      });  
                	/*Button close = (Button) dialog.findViewById(R.id.close_help_button);
                	close.setOnClickListener(new OnClickListener() {
                		public void onClick(View view)
                		{
                			
                		}
                	});*/
                	dialog.show();
                }
            });
    		return rootView;
    	}
    	 @Override
    	 public void onResume() {
    		 SharedPreferences pref = getActivity().getSharedPreferences("ptd",0);
      		if(pref.contains("session"))
      		{
      			getActivity().findViewById(R.id.button_login).setVisibility(View.GONE);
      			getActivity().findViewById(R.id.button_logout).setVisibility(View.VISIBLE);
      		}
      		super.onResume();
    	 }
    }    
/*    
    public static class TempFragment extends Fragment {
    	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    		View rootView = inflater.inflate(R.layout.fragment_temp, container, false);
    		
    		rootView.findViewById(R.id.button_paint).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), DrawingActivity.class);
                    intent.putExtra("description", "anything");
                    startActivity(intent);
                }
            });
    		rootView.findViewById(R.id.button_guess).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), TextActivity.class);
                    intent.putExtra("filename", "l2ybsxynzj.png");
                    startActivity(intent);
                }
            });
			rootView.findViewById(R.id.button_local_play).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), DrawingActivity.class);
                    intent.putExtra("isLocal", true);
                    intent.putExtra("isInitialRound", true);
                    startActivity(intent);
                }
            }); 		
			rootView.findViewById(R.id.button_comp).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ViewCompletedActivity.class);
                    intent.putExtra("option", 0);
                    startActivity(intent);
                }
            });
			rootView.findViewById(R.id.button_comp_local).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ViewCompletedActivity.class);
                    intent.putExtra("isLocal", true);
                    startActivity(intent);
                }
            });

			return rootView;
    	}
    }
*/
}