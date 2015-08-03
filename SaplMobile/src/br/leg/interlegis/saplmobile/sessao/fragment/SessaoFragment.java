package br.leg.interlegis.saplmobile.sessao.fragment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment; 
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import br.leg.interlegis.saplmobile.R;
import br.leg.interlegis.saplmobile.SaplActivity;
import br.leg.interlegis.saplmobile.services.SaplSessaoPlenariaService;
import br.leg.interlegis.saplmobile.sessao.view.ListaDeMateriasPorSessaoPlenaria;
import br.leg.interlegis.saplmobile.sessao.view.TextViewButtonSession;
import br.leg.interlegis.saplmobile.suport.Log;
import br.leg.interlegis.saplmobile.suport.NetworkUtil;


public class SessaoFragment  extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	Fragment fragment = this;

	Document dom = null;

	String proximaData = "";
	String dataSelecionada = "";

	View rootView = null;

	Object syncToken = new Object();

	boolean flagUpdate = true;
	boolean flagStart = true;



	int sizeListDatas = 0;

	public ListaDeMateriasPorSessaoPlenaria lm = null;

	public static final String ARG_SECTION_NUMBER = "section_number";


	private UpdateDatasSessoesReceiver receiver;



	public SessaoFragment() {


	}

	public class UpdateDatasSessoesReceiver extends BroadcastReceiver{

		public static final String PROCESS_UPDATE_DATAS = "br.leg.interlegis.saplmobile.intent.action.PROCESS_UPDATE_DATAS"; 

		@Override
		public void onReceive(Context context, Intent intent) {


			//TextView rodapeText = (TextView) getActivity().findViewById(R.id.subTitleText);							
			//rodapeText.setText();
			Log.d("CMJ",intent.getExtras().getString("value"));

			final Spinner dataSessao = (Spinner) rootView.findViewById(R.id.dataSessao);
			//dataSessao.setDropDownWidth(300);


			try {

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				dom = builder.parse(new File(SaplActivity.fileCacheListaSessoes));

			}
			catch (Exception ee) {
				Log.e("CMJ", "erro na importação do XML da lista de seções");
				return;
			}



			Element root = dom.getDocumentElement();
			Element e = null;

			NodeList items = root.getElementsByTagName("proxima-data");
			e = (Element) items.item(0);
			proximaData = e.getAttribute("value");

			items = root.getElementsByTagName("data");

			List<CharSequence> listaDatas = new ArrayList<CharSequence>();	

			for (int i=0 ; i < items.getLength(); i++) {
				e = (Element) items.item(i);
				listaDatas.add(e.getAttribute("value"));						
			} 

			if (flagUpdate) {
				flagUpdate = false;


				LayoutInflater inflater = (LayoutInflater) fragment.getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				root = dom.getDocumentElement();
				e = null;

				items = root.getElementsByTagName("sessao");


				LinearLayout areaTop = (LinearLayout) rootView.findViewById(R.id.areaTop);

				while (areaTop.getChildCount() > 1)
					areaTop.removeViewAt(0);

				for (int i = items.getLength() - 1; i >= 0 ; i--) {

					Element el = (Element) items.item(i);

					LinearLayout rowView = (LinearLayout) inflater.inflate(R.layout.sessao_do_dia_selecionado, null);
					((TextViewButtonSession)rowView.getChildAt(0)).setText(el.getAttribute("num_sessao_plen")+"ª Sessão "+
							el.getAttribute("nom_tip_sessao"));

					((TextViewButtonSession)rowView.getChildAt(0)).el = el;
					((TextViewButtonSession)rowView.getChildAt(0)).selected = false;

					((TextViewButtonSession)rowView.getChildAt(0)).setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {


							final ProgressDialog dialog = new ProgressDialog(SaplActivity.saplActivity); 
							dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							dialog.setMessage("Carregando Dados...");
							dialog.setCancelable(false);
							dialog.show();
							
							
							lm.removeAllViews();
							
							LinearLayout areaTop = (LinearLayout) rootView.findViewById(R.id.areaTop);
							for (int i = 0; i < areaTop.getChildCount()-1; i++) {

								View item = areaTop.getChildAt(i);
								if (item instanceof LinearLayout) {
									TextViewButtonSession itemText = (TextViewButtonSession) ((LinearLayout)item).getChildAt(0);
									itemText.selected = false;
									itemText.setBackgroundResource(R.drawable.back_button_top);
									itemText.setTextColor(Color.LTGRAY);
								}
							}

							((TextViewButtonSession)v).selected = true;
							((TextViewButtonSession)v).setTextColor(Color.DKGRAY);

							Element el = ((TextViewButtonSession)v).el;
							TextView rodapeText = (TextView) rootView.findViewById(R.id.subTitleText);							
							rodapeText.setText(writeSubTitle(el));

							v.setBackgroundResource(R.drawable.back_button_top_select); 
							SaplSessaoPlenariaService.getInstance().reiniciarAtrasoTimeListaMaterias();
							lm.restartThMats(el, dialog); 
							

							
						}
					});

					((TextViewButtonSession)rowView.getChildAt(0)).setBackgroundResource(R.drawable.back_button_top);

					((TextViewButtonSession)rowView.getChildAt(0)).setTextColor(Color.LTGRAY);
					if (i == 0) {
						TextView rodapeText = (TextView) rootView.findViewById(R.id.subTitleText);							
						rodapeText.setText(writeSubTitle(el));
						

						final ProgressDialog dialog = new ProgressDialog(SaplActivity.saplActivity); 
						dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						dialog.setMessage("Carregando Dados...");
						dialog.setCancelable(false);
						dialog.show();
						
						
						
						lm.restartThMats(el, dialog); 
						
						
						
						
						
						((TextViewButtonSession)rowView.getChildAt(0)).setBackgroundResource(R.drawable.back_button_top_select);
						((TextViewButtonSession)rowView.getChildAt(0)).setTextColor(Color.DKGRAY);
					}

			
					areaTop.addView(rowView, 0);
				}


			}



			if (listaDatas.size() == sizeListDatas) {
				return;
			}

			sizeListDatas = listaDatas.size();

			final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(fragment.getView().getContext(), R.layout.spinner_selected, listaDatas);
			adapter.setDropDownViewResource(R.layout.spinner_item);


			fragment.getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() { 
					dataSessao.setAdapter(adapter);
				}
			});


			if (dataSessao.getOnItemSelectedListener() == null)

				dataSessao.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) { 

						lm.removeAllViews();
						dataSelecionada = (String)(dataSessao.getItemAtPosition(arg2));						
						flagUpdate = true;
						SaplSessaoPlenariaService.getInstance().dataSelecionada = dataSelecionada;	
						SaplSessaoPlenariaService.getInstance().now();

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

						Log.d("CMJ", "nada");


					}

				}); 





			/* String responseString = intent.getStringExtra(MyWebRequestService.RESPONSE_STRING);
            String reponseMessage = intent.getStringExtra(MyWebRequestService.RESPONSE_MESSAGE);

            TextView myTextView = (TextView) getfindViewById(R.id.response);
            myTextView.setText(responseString);

            WebView myWebView = (WebView) findViewById(R.id.myWebView);
            myWebView.getSettings().setJavaScriptEnabled(true);
            try {
                myWebView.loadData(URLEncoder.encode(reponseMessage,"utf-8").replaceAll("\\+"," "), "text/html", "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
			 */

		}


	}


	@Override
	public void onDestroy() {
		Log.d("CMJ","desvinculou");

		if (receiver != null) {
			this.getActivity().unregisterReceiver(receiver);
			receiver = null;
		}
 
			SaplSessaoPlenariaService.fecharServico();
		
		 
		super.onDestroy();
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_sapl_sessao,
				container, false);

		lm = (ListaDeMateriasPorSessaoPlenaria)rootView.findViewById(R.id.lm);
		lm.fragment = this;


		if (SaplActivity.urlBase.length() <= 1) {

			TextView rowView = (TextView) inflater.inflate(R.layout.spinner_item, null);
			rowView.setText("É necessário antes, que você configure qual SAPL você quer monitorar.\nAcesse, no canto direito superior as Configurações, e informe o endereço de internet do SAPL que será monitorado!");
			lm.addView(rowView); 
			rowView.setTextColor(Color.RED);
			return rootView;
		}


		IntentFilter filter = new IntentFilter(UpdateDatasSessoesReceiver.PROCESS_UPDATE_DATAS);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new UpdateDatasSessoesReceiver();
		this.getActivity().registerReceiver(receiver, filter);

		if (SaplSessaoPlenariaService.getInstance() == null) {
			Intent intent = new Intent(this.getActivity(), SaplSessaoPlenariaService.class);
			this.getActivity().startService(intent);
		}

		return rootView;
	}


	public CharSequence writeSubTitle(Element el) {
		String str = 
				/*el.getAttribute("num_sessao_plen")+"ª Sessão"+
						(el.getAttribute("tip_sessao").equals("1")?" Ordinária":" Extraordinária")+

						" da "+*/el.getAttribute("num_sessao_leg")+"ª Sessão Legislativa"+
						" da "+el.getAttribute("num_legislatura")+"ª Legislatura"+
						" - Data: "+el.getAttribute("dat_inicio_sessao")+" ("+el.getAttribute("dia_sessao")+")"+
						" - Abertura: "+el.getAttribute("hr_inicio_sessao")+".";
		return str;
	}

	private void initView(final View rootView) {

		final Spinner dataSessao = (Spinner) rootView.findViewById(R.id.dataSessao);

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {

						if (!NetworkUtil.isConnected()) {

							synchronized (syncToken) {
								syncToken.notify();
							}


							synchronized (syncToken) {
								try {
									syncToken.wait(3000);
								} catch (InterruptedException ee) {
									ee.printStackTrace();
								}
							}
							continue;
						}

					} catch (Exception e) {
						synchronized (syncToken) {
							try {
								syncToken.wait(60000);
							} catch (InterruptedException ee) {
								ee.printStackTrace();
							}
						}
						continue;
					}
				}
			}
		}).start();
	}
}