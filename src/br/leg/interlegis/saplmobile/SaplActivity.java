package br.leg.interlegis.saplmobile;

import java.io.File;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout; 
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import br.leg.interlegis.saplmobile.services.SaplSessaoPlenariaService;
import br.leg.interlegis.saplmobile.sessao.fragment.MateriasTramitacaoFragment;
import br.leg.interlegis.saplmobile.sessao.fragment.MyDialogFragment.MyDialogFragmentListener;
import br.leg.interlegis.saplmobile.sessao.fragment.SessaoFragment;
import br.leg.interlegis.saplmobile.suport.Log;
/*
 * 
 * O Sistema de Apoio ao Processo Legislativo (SAPL) tem como finalidade apoiar as Casas Legislativas nas suas atividades relacionadas ao processo legislativo em geral, tais como: elaboração de proposições, recepção e tramitação das matérias legislativas, organização das sessões plenárias, manutenção atualizada da base de leis, entre outras. Ele também disponibiliza consultas às informações sobre mesa diretora, comissões, parlamentares, ordem do dia, proposições, matérias legislativas, normas jurídicas e outras. O SAPL facilita as atividades dos parlamentares, servidores da Casa e permite aos cidadãos acompanharem o andamento dos processos legislativos, além de pesquisar a legislação.

Versão Mobile: Oferecer consulta rápida, objetiva e, em tempo real, no caso de no momento da consulta, estar tendo uma sessão em plenário, para dispositivos moveis da plataforma android.
 * saplmobile1.23
 * 
 */
public class SaplActivity extends FragmentActivity implements
ActionBar.OnNavigationListener, MyDialogFragmentListener {

	public static SaplActivity saplActivity = null;

	//public static Properties properties = null; 
	public static SharedPreferences settings = null;

	public static String urlBase = "";


	public static String urlApiBase = "";
	public static String urlDocsBase = "";

	public static String uriFileBase = "";
	public static String uriCacheBase = "";

	public static boolean files_pre_download = false;

	public static boolean permitir_fechar_sistema = false;
	public static boolean mostrar_roteiro_sessao = false;


	public static String fileCacheListaSessoes = "";


	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mMenuTitles;

	MediaPlayer musica = null;

	private int menuItemSelect = 0;

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	public void onBackPressed() {

		if (musica != null)
			musica.stop();


		/*SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	      SharedPreferences.Editor editor = settings.edit();
	      editor.putBoolean("silentMode", mSilentMode);

	      // Commit the edits!
	      editor.commit();*/

		if (permitir_fechar_sistema) {
			SaplSessaoPlenariaService.fecharServico();
			super.onBackPressed();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		
		//SaplSessaoPlenariaService.fecharServico();
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("CMJ", "serviço inicial");



		saplActivity = this;
		setContentView(R.layout.activity_sapl);

		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.right_drawer);
		mMenuTitles = getResources().getStringArray(R.array.menu_array);

		createMenuActionBar();	

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new DrawerListAdapter(this, mMenuTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description for accessibility */
				R.string.drawer_close  /* "close drawer" description for accessibility */
				) {

			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(0);
		}

		setVariaveisEstaticas();  
	}


	public static void setVariaveisEstaticas() {

		settings = PreferenceManager.getDefaultSharedPreferences(saplActivity);
		uriCacheBase = saplActivity.getExternalCacheDir().getAbsolutePath();
		uriFileBase = saplActivity.getExternalFilesDir("").getAbsolutePath();  

		SaplActivity.resetCache();

		urlBase = settings.getString("url_base", "");
		files_pre_download = settings.getBoolean("files_pre_download", true);
		permitir_fechar_sistema = settings.getBoolean("permitir_fechar_sistema", true);
		mostrar_roteiro_sessao = settings.getBoolean("mostrar_roteiro_sessao", true);

		if (!urlBase.endsWith("/"))
			urlBase += "/";

		urlApiBase = urlBase+"api/";
		urlDocsBase = urlBase+"sapl_documentos/";		



		fileCacheListaSessoes = SaplActivity.uriCacheBase+"SaplListaSessoes.xml";



		//storeProperties();

	}
	/*
	public static void loadProperties() {

		properties = new Properties();		

		try {
			FileInputStream fIn = saplActivity.openFileInput("sapl.properties");
			properties.load(fIn);			
		} catch (Exception e) {
		}
	}

	public static void storeProperties() {

		properties = new Properties();		
		properties.setProperty("urlBase", "http://187.6.249.156:8080/");

		try {
			FileOutputStream fOut = saplActivity.openFileOutput("sapl.properties", MODE_PRIVATE);
			properties.store(fOut,null);			
		} catch (Exception e) {
		}
	}*/

	private static void resetCache() {

		File cache = new File(uriCacheBase);
		File fCaches[] = cache.listFiles();

		for (File f: fCaches)
			f.delete();


	}


	private void createMenuActionBar() {

		final ActionBar actionBar = getActionBar();
		//actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		String str[] =  null;

		if (menuItemSelect == 0)
			str =  getResources().getStringArray(R.array.menu_array_sapl);
		else if (menuItemSelect == 1)
			str = new String[] {
				"teste1",
				"teste2",
				"teste3",
		};

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
				// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, str), this);
	}



	private void selectItem(int position) {

		menuItemSelect = position;

		createMenuActionBar();

		View view = null;

		if (position == 1) {  
		} 

		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
		mTitle = mMenuTitles[position];
		getActionBar().setTitle(mTitle);
	} 

	public class DrawerListAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final String[] values;

		public DrawerListAdapter(Context context, String[] values) {
			super(context, R.layout.drawer_list_item, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View rowView = inflater.inflate(R.layout.drawer_list_item, parent, false);

			TextView textView = (TextView) rowView.findViewById(R.id.textItemMenu);

			textView.setText(values[position]);

			return rowView;
		}
	}
	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sapl, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch(item.getItemId()) {
		case R.id.action_settings: {
			Intent intent = new Intent(this, SaplSettingsActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.action_websearch: {
			// create intent to perform web search for this planet
			Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
			//intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
			intent.putExtra(SearchManager.QUERY, settings.getString("sapl_title", ""));
			// catch event that there's no activity to handle intent
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent);
			} else {
				Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
			}
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	} 

	@Override
	public boolean onNavigationItemSelected(int position, long id) {

		Fragment fragment = null;
		Bundle args = null;

		if (menuItemSelect == 0) {

			if (position == 0) {
				args = new Bundle();
				args.putInt(SessaoFragment.ARG_SECTION_NUMBER, position + 1);
				fragment = new SessaoFragment();
			}
			else if (position == 1) {
				args = new Bundle();
				args.putInt(MateriasTramitacaoFragment.ARG_SECTION_NUMBER, position + 1);
				fragment = new MateriasTramitacaoFragment();
			}

		}

		if (fragment == null)
			return true;



		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.container, fragment).commit();
		return true;
	}


	public void sound() {

		if (musica != null)
			musica.stop();

		musica = MediaPlayer.create(this, R.raw.message_update_lista);
		musica.setVolume(0, (float) 0.65); 
		musica.start();
	}


	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		Toast.makeText(saplActivity,
				"O botão OK foi clicado!", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		Toast.makeText(saplActivity,
				"O botão Cancelar foi clicado!", Toast.LENGTH_LONG).show();
	}
}
