package br.leg.interlegis.saplmobile.sessao.fragment;
import org.w3c.dom.Document;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import br.leg.interlegis.saplmobile.R;
import br.leg.interlegis.saplmobile.SaplActivity;
import br.leg.interlegis.saplmobile.sessao.view.ListaDeMateriasEmTramitacao;


public class MateriasTramitacaoFragment  extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	Fragment fragment = this;

	Document dom = null;
 
	View rootView = null;

	Object syncToken = new Object();
 

	int sizeListDatas = 0;

	public ListaDeMateriasEmTramitacao lm = null;

	public static final String ARG_SECTION_NUMBER = "section_number";

	public MateriasTramitacaoFragment() {


	}
/*
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

							lm.removeAllViews();
							
							LinearLayout areaTop = (LinearLayout) rootView.findViewById(R.id.areaTop);
							for (int i = 0; i < areaTop.getChildCount()-1; i++) {

								View item = areaTop.getChildAt(i);
								if (item instanceof LinearLayout) {
									TextViewButtonSession itemText = (TextViewButtonSession) ((LinearLayout)item).getChildAt(0);
									itemText.selected = false;
									itemText.setBackgroundResource(R.drawable.back_button_top_select);
								}
							}

							((TextViewButtonSession)v).selected = true;

							Element el = ((TextViewButtonSession)v).el;
							TextView rodapeText = (TextView) rootView.findViewById(R.id.subTitleText);							
							rodapeText.setText(writeSubTitle(el));

							v.setBackgroundResource(R.drawable.back_button_top); 
							SaplSessaoPlenariaService.getInstance().reiniciarAtrasoTimeListaMaterias();
							lm.restartThMats(el); 
						}
					});

					((TextViewButtonSession)rowView.getChildAt(0)).setBackgroundResource(R.drawable.back_button_top_select);
					if (i == 0) {
						TextView rodapeText = (TextView) rootView.findViewById(R.id.subTitleText);							
						rodapeText.setText(writeSubTitle(el));
						lm.restartThMats(el); 
						((TextViewButtonSession)rowView.getChildAt(0)).setBackgroundResource(R.drawable.back_button_top);
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





			 String responseString = intent.getStringExtra(MyWebRequestService.RESPONSE_STRING);
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
			 

		}


	}

 */


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_sapl_materias_tramitacao,
				container, false);

		lm = (ListaDeMateriasEmTramitacao)rootView.findViewById(R.id.lm);
		lm.fragment = this;


		if (SaplActivity.urlBase.length() <= 1) {

			TextView rowView = (TextView) inflater.inflate(R.layout.spinner_item, null);
			rowView.setText("É necessário antes, que você configure qual SAPL você quer monitorar.\nAcesse, no canto direito superior as Configurações, e informe o endereço de internet do SAPL que será monitorado!");
			lm.addView(rowView); 
			rowView.setTextColor(Color.RED);
			return rootView;
		} 

		final ProgressDialog dialog = new ProgressDialog(SaplActivity.saplActivity); 
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage("Carregando Dados...");
		dialog.setCancelable(false);
		dialog.show();  		
		
		lm.requestMats(dialog);
		initView(rootView); 
		
		return rootView;
	}



	private void initView(final View rootView) {
 
		
		
		
	}
}