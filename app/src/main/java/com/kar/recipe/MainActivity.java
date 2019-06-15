package com.kar.recipe;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.kar.recipe.DBHandle.Collection;
import com.kar.recipe.DBHandle.DBHandler;
import com.kar.recipe.DataClasses.Recipe;

import java.io.FileFilter;
import java.io.IOException;
import java.util.ServiceConfigurationError;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Filter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , SearchView.OnQueryTextListener {

    private String[] namesOfRecipes = {"Голубцы" , "Пельмени" , "Запеканка" , "Бутереброд", "Омлет" , "Картошка Фри" ,
            "Борщ", "Окрошка", "Крабовый салат", "Оливье", "Запеченный карп" , "Яблочный штрудель" , "Эклер" , "Салат Цезарь"};
    private int[] IMAGES = {R.drawable.golobci, R.drawable.pelmeni, R.drawable.zapekanka, R.drawable.sandwich, R.drawable.omlet,
            R.drawable.fri , R.drawable.borch, R.drawable.okroshka, R.drawable.krabpviy_salat, R.drawable.olive, R.drawable.karp,
            R.drawable.yablochniy_shtrudel, R.drawable.ekler, R.drawable.cezar_salat};

    private static Collection<Recipe> recipes;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CountDownLatch latch = new CountDownLatch(1);

        GetRecipesTask task = new GetRecipesTask(latch);
        task.execute();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        listView = (ListView) findViewById(R.id.listView);
        DishAdapter dishAdapter = new DishAdapter();
        listView.setAdapter(dishAdapter);
        listView.setTextFilterEnabled(true);

        SearchView mSearchView = (SearchView) findViewById(R.id.searchView_dish);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Search Here");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            listView.clearTextFilter();
        } else {
            listView.setFilterText(newText);
        }
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (GeneralData.user != null) {
            TextView textView_name = (TextView) findViewById(R.id.user_name);
            textView_name.setText(GeneralData.user.getName());
            ImageView imageView_avatar = (ImageView) findViewById(R.id.avatar_imageView);
            try {
                imageView_avatar.setImageBitmap(GeneralData.user.getAvatarImage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_recipes) {
            // Handle the camera action

        } else if (id == R.id.nav_favorite_recipes) {

        } else if (id == R.id.nav_search) {

        } else if (id == R.id.nav_sign_in) {

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        //@string/nav_header_title
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class DishAdapter extends BaseAdapter implements Filterable {

        private Collection<Recipe> current = recipes;

        @Override
        public android.widget.Filter getFilter() {
            return new android.widget.Filter() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    Collection<Recipe> recipesNew = recipes.find(recipe -> recipe.getName().toLowerCase().contains(constraint.toString().toLowerCase()));
                    filterResults.count = recipesNew.size();
                    filterResults.values = recipesNew;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    current = (Collection<Recipe>) results.values;
                    notifyDataSetChanged();
                }
            };
        }



        @Override
        public int getCount() {
            return current.size();
        }

        @Override
        public Object getItem(int position) {
            return current.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.dishlayout, null);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            TextView textView = (TextView) convertView.findViewById(R.id.textView_name);
            ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.imageButton_favorite);

            if(GeneralData.user != null){
                if (GeneralData.user.getSaves().findFirst(recipe -> recipe.getId() == current.get(position).getId()) != null){
                    imageButton.setImageResource(R.drawable.like);
                    imageButton.setSelected(true);
                }else{
                    imageButton.setImageResource(R.drawable.not_like);
                    imageButton.setSelected(false);
                }
            }else{
                imageButton.setImageResource(R.drawable.not_like);
                imageButton.setSelected(false);
            }

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Recipe recipe = current.get(position);
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("recipe", recipe);
                    intent.putExtra("recipe_bundle", bundle);
                    Log.d("intent", recipe.getIngredients().toString());
                    startActivity(intent);
                }
            });

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (GeneralData.user != null) {
                        imageButton.setSelected(!imageButton.isSelected());
                        if (imageButton.isSelected()) {
                            imageButton.setImageResource(R.drawable.like);
                            Snackbar.make(view, "Добавлено к помеченным", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            new AsyncTask<Void, Void, Void>() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    try {
                                        DBHandler.addSave(GeneralData.user.getId(), current.get(position).getId());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            }.execute();
                        } else {
                            imageButton.setImageResource(R.drawable.not_like);
                            Snackbar.make(view, "Удалено из помеченных", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            new AsyncTask<Void, Void, Void>() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    try {
                                        DBHandler.removeSave(GeneralData.user.getId(), current.get(position).getId());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            }.execute();
                        }
                    }else{
                        Snackbar.make(view, "Вы не вошли в аккаунт!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            });


            try {
                imageView.setImageBitmap(current.get(position).getImage());
            } catch (IOException e) {
                e.printStackTrace();
            }

            textView.setText(current.get(position).getName());

            return convertView;
        }
    }
    private static class GetRecipesTask extends AsyncTask<Void, Void, Collection<Recipe>> {
        private CountDownLatch latch;

        public GetRecipesTask(CountDownLatch latch) {
            this.latch = latch;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Collection<Recipe> doInBackground(Void... voids) {
            try {
                Collection<Recipe> recipes1 = DBHandler.getData().getRecipes();
                Log.d("hello", recipes1.toString());
                recipes = recipes1;
                latch.countDown();
                return recipes1;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
