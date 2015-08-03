package br.leg.interlegis.saplmobile.sessao.view;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.leg.interlegis.saplmobile.R;
import br.leg.interlegis.saplmobile.SaplActivity;
import br.leg.interlegis.saplmobile.services.SaplSessaoPlenariaService;
import br.leg.interlegis.saplmobile.sessao.view.popups.MateriaDetalhes;
import br.leg.interlegis.saplmobile.suport.Utils;

public class ListaDeMateriasPorSessaoPlenaria extends LinearLayout implements View.OnTouchListener{

	Thread thLmats = null;
	public Fragment fragment = null;

	public String crc32 = "";

	public Element elSessao;

	LayoutInflater inflater = null;

	boolean initialStart = true;	

	boolean isRun = false;

	ArrayList<View> lViewsTodas = new ArrayList<View>();
	ArrayList<View> lViewsMatExpediente = new ArrayList<View>();

	Document xmlDom = null;
	Element elRaiz = null;
	ArrayList<ProgressDialog> dialog = new ArrayList<ProgressDialog>();
	private UpdateMateriasSessaoReceiver receiver;

	public ListaDeMateriasPorSessaoPlenaria(Context context, AttributeSet attrs) {
		super(context, attrs);
		/*
		 */
	}

	public class UpdateMateriasSessaoReceiver extends BroadcastReceiver{

		public static final String PROCESS_UPDATE_MATERIAS_SESSAO = "br.leg.interlegis.saplmobile.intent.action.PROCESS_UPDATE_MATERIAS_SESSAO"; 
		public static final String PROCESS_COUNT_DOWNLOADS_MATERIA_SESSAO = "br.leg.interlegis.saplmobile.intent.action.PROCESS_COUNT_DOWNLOADS_MATERIA_SESSAO"; 
		public static final String PROCESS_UPDATE_NEXT_MATERIAS_SESSAO = "br.leg.interlegis.saplmobile.intent.action.PROCESS_UPDATE_NEXT_MATERIAS_SESSAO"; 
			
		@Override
		public void onReceive(Context context, Intent intent) {

			try {


				if (intent.getAction().equals(PROCESS_UPDATE_MATERIAS_SESSAO)) { 
					String xmlMats = intent.getStringExtra("xmlMats"); 
					requestMats(xmlMats); 
					while (!dialog.isEmpty()) {
						dialog.get(0).dismiss();
						dialog.remove(0);
					}

				}
				else if (intent.getAction().equals(PROCESS_COUNT_DOWNLOADS_MATERIA_SESSAO)) {

					int arquivosParaBaixar = intent.getIntExtra("arquivosParaBaixar", 0);
					int timePontoDownloadFileMaterias = intent.getIntExtra("timePontoDownloadFileMaterias", 0);
					TextView textDownload = (TextView) fragment.getActivity().findViewById(R.id.textDownloads);
					if (arquivosParaBaixar != 0)
						textDownload.setText(arquivosParaBaixar+" Arquivos a serem baixados...\nPróximo Download em alguns segundos...");
					else 
						textDownload.setText("");

				}
				else if (intent.getAction().equals(PROCESS_UPDATE_NEXT_MATERIAS_SESSAO)) {

					int proximaAtualizacao = intent.getIntExtra("proximaAtualizacao", 0);
					
					TextView textAtualiza = (TextView) fragment.getActivity().findViewById(R.id.textAtualiza);
					textAtualiza.setText("Proxima Atualização em aproximadamente "+proximaAtualizacao+"s");
					
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	public void restartThMats(Element el) {
		restartThMats(el,  null);
	}

	public void restartThMats(Element el, ProgressDialog _dialog) {
		if (_dialog != null)
			dialog.add(_dialog);
		this.elSessao = el; 

		crc32 = "";

		SaplSessaoPlenariaService.getInstance().codSessaoPlenaria = el.getAttribute("cod_sessao_plen");
		SaplSessaoPlenariaService.getInstance().crc32 = "";		

		if (receiver != null) {
			fragment.getActivity().unregisterReceiver(receiver);
			receiver = null;
		}		

		receiver = new UpdateMateriasSessaoReceiver();

		IntentFilter filter = new IntentFilter(UpdateMateriasSessaoReceiver.PROCESS_UPDATE_MATERIAS_SESSAO);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		fragment.getActivity().registerReceiver(receiver, filter);

		filter = new IntentFilter(UpdateMateriasSessaoReceiver.PROCESS_COUNT_DOWNLOADS_MATERIA_SESSAO);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		fragment.getActivity().registerReceiver(receiver, filter);
		
		filter = new IntentFilter(UpdateMateriasSessaoReceiver.PROCESS_UPDATE_NEXT_MATERIAS_SESSAO);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		fragment.getActivity().registerReceiver(receiver, filter);

		if (SaplSessaoPlenariaService.getInstance() == null) {
			Intent intent = new Intent(fragment.getActivity(), SaplSessaoPlenariaService.class);
			fragment.getActivity().startService(intent);
		}

	}

	public final void requestMats(String xmlMats) throws Exception {

		inflater = (LayoutInflater) fragment.getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


		/*		String md5Novo = Utils.md5(xmlMats);

		if (md5Novo.equals(md5Oficial)) 
			return;
		md5Oficial = md5Novo;
		 */

		final LinearLayout lm = this;

		Document _xmlDom = Utils.loadXMLFromString(xmlMats); 
		
		

		SaplActivity.saplActivity.runOnUiThread(new Runnable() { 
			@Override
			public void run() {  

				if (!dialog.isEmpty()) {
					dialog.get(dialog.size()-1).setMessage("Dados chegaram... construindo visualização...");
				}
			}
		});
		
		
		

		Element _elRaiz = _xmlDom.getDocumentElement();

		String newCrc32 = _elRaiz.getAttribute("crc32");

		if (newCrc32.equals(crc32)) {

			SaplSessaoPlenariaService.getInstance().atrasarTimeListaMaterias();

			if (SaplActivity.files_pre_download) { 


				final NodeList nlMatExp = xmlDom.getElementsByTagName("matexp");
				final NodeList nlMatOrdem = xmlDom.getElementsByTagName("matordem"); 

				SaplSessaoPlenariaService.getInstance().setNodeListDownloadFilesMaterias(nlMatExp, nlMatOrdem);



			}

			return; 
		}
	//
 
		xmlDom = _xmlDom;
		elRaiz = _elRaiz;		

		crc32 = newCrc32;

		SaplSessaoPlenariaService.getInstance().crc32 = crc32;

		//int ordem = 1;

		lViewsTodas.clear();
		lViewsMatExpediente.clear();
		



		NodeList nlMatExp = xmlDom.getElementsByTagName("matexp");
		for (int i = 0; i < nlMatExp.getLength(); i++) {
			Element el = (Element) nlMatExp.item(i);
			lViewsMatExpediente.add(fillTitle("Matérias do Expediente - "+elSessao.getAttribute("num_sessao_plen")+"ª Sessão "+
					elSessao.getAttribute("nom_tip_sessao"), true, !(i == 0)));
			lViewsMatExpediente.add(fillView(el, true));
		}

		if (lViewsMatExpediente.size() > 0) {
			View vAux = lViewsMatExpediente.get(lViewsMatExpediente.size()-1);
			vAux.setPadding(vAux.getPaddingLeft(), vAux.getPaddingTop(), vAux.getPaddingRight(), 20);
		}


		NodeList nlMatOrdem = xmlDom.getElementsByTagName("matordem"); 
		//if (nlMatOrdem.getLength() > 0)
		//	lViewsTodas.add(fillTitle("Ordem do Dia - "+elSessao.getAttribute("num_sessao_plen")+"ª Sessão "+
		//			elSessao.getAttribute("nom_tip_sessao"), false, false));


		for (int i = 0; i < nlMatOrdem.getLength(); i++) {
			Element el = (Element) nlMatOrdem.item(i);
			lViewsTodas.add(fillTitle("Ordem do Dia - "+elSessao.getAttribute("num_sessao_plen")+"ª Sessão "+
					elSessao.getAttribute("nom_tip_sessao"), false, !(i == 0)));
			lViewsTodas.add(fillView(el, false));
		}

		if (lViewsTodas.size() > 0) {
			View vAux = lViewsTodas.get(lViewsTodas.size()-1);
			vAux.setPadding(vAux.getPaddingLeft(), vAux.getPaddingTop(), vAux.getPaddingRight(), 20);
		}
		
		
		if (SaplActivity.mostrar_roteiro_sessao) {
			NodeList nlExpediente = xmlDom.getElementsByTagName("expediente"); 
			for (int i = 0; i < nlExpediente.getLength(); i++) {
				Element el = (Element) nlExpediente.item(i);

				LinearLayout ll =  (LinearLayout) inflater.inflate(R.layout.text_view_expediente, null);

				TextView textEx =  (TextView) ll.findViewById(R.id.texto_expediente);

				textEx.setText(Html.fromHtml(el.getTextContent()));

				if (i == 0)
					lViewsMatExpediente.add(0, ll);
				else if (i == 1)
					lViewsMatExpediente.add(ll);
				else 
					lViewsTodas.add(ll);
			} 
		} 
		

		lViewsTodas.addAll(0,lViewsMatExpediente); 
		
		
		/*		
		NodeList nlExpediente = xmlDom.getElementsByTagName("expediente"); 

		for (int i = 0; i < nlExpediente.getLength(); i++) {
			Element el = (Element) nlExpediente.item(i);

			TextView textEx =  (TextView) fragment.getActivity().findViewById(R.id.texto_expediente);

			textEx.setText(Html.fromHtml(el.getTextContent())); 

		}*/



		//	if (nlMatExp.getLength() > 0)
		//		lViewsTodas.add(0, fillTitle("Matérias do Expediente - "+elSessao.getAttribute("num_sessao_plen")+"ª Sessão "+
		//				elSessao.getAttribute("nom_tip_sessao"), true, false));

		if (lViewsTodas.size() > 0)
			fragment.getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {

					lm.removeAllViews();

					for (View v: lViewsTodas) {
						lm.addView(v); 
					}

				 android.util.Log.i("CMJ1","terminou: " +  new GregorianCalendar().toString());


					if (!initialStart)
						((SaplActivity)fragment.getActivity()).sound();
					else 
						initialStart = false;
				}
			});



		SaplSessaoPlenariaService.getInstance().reiniciarAtrasoTimeListaMaterias();
	}

	public LinearLayout fillTitle(final String title, boolean expediente, boolean transparent) {

		LinearLayout tituloLayout = (LinearLayout) inflater.inflate(R.layout.sessao_plenaria_titulo_partes, null);


		TextView tvTitulo = (TextView) tituloLayout.findViewById(R.id.textItemMenu);
		tvTitulo.setText(title);


		if (transparent) {
			tvTitulo.setBackgroundResource(android.R.color.transparent);
			tvTitulo.setTextColor(Color.rgb(190, 190, 190));

		} else {
			if (expediente)		
				tvTitulo.setBackgroundResource(R.drawable.container_corners_amarelo);
			else {
				tvTitulo.setBackgroundResource(R.drawable.container_corners_azul_escuro);
				tvTitulo.setTextColor(getResources().getColor(android.R.color.white));
			} 
		}


		return tituloLayout;

		/*
		if (expediente) {
			itemMateria.findViewById(R.id.sessao_plenaria_item_materia).setBackgroundResource(R.drawable.container_corners_amarelo);

		}*/

	}
	public LinearLayout fillView(final Element el, boolean expediente) {

		ItemListaMaterias itemMateria = (ItemListaMaterias) inflater.inflate(R.layout.sessao_plenaria_materia, null);

		itemMateria.cod_materia = el.getAttribute("cod_materia");


		if (expediente) {
			itemMateria.findViewById(R.id.sessao_plenaria_item_materia).setBackgroundResource(R.drawable.container_corners_amarelo);

		}

		TextView tvTitulo = ((TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_titulo));

		tvTitulo.setText(
 
						el.getAttribute("des_tipo_materia")+" nº "+
						el.getAttribute("num_ident_basica")+"/"+
						el.getAttribute("ano_ident_basica")
				);

		if (!expediente)
			tvTitulo.setBackgroundResource(R.drawable.container_corners_azul);

		View.OnClickListener onclick =  new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				LayoutInflater layoutInflater = (LayoutInflater)SaplActivity.saplActivity.getBaseContext().getSystemService(SaplActivity.LAYOUT_INFLATER_SERVICE);  
				SaplPopupView popup = (SaplPopupView) layoutInflater.inflate(R.layout.popup, null);  

				MateriaDetalhes materiaDetalhes = (MateriaDetalhes) layoutInflater.inflate(R.layout.popup_materia_detalhes, null);
				popup.show(materiaDetalhes);
				materiaDetalhes.run(el.getAttribute("cod_materia"),popup);
			}
		};


		tvTitulo.setOnClickListener(onclick);

		//View viewTarge = itemMateria.findViewById(R.id.targe_type); 

		NodeList nlEmenta = el.getElementsByTagName("ementa");
		TextView tvEmenta = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_ementa);
		if (nlEmenta.getLength() > 0) {
			tvEmenta.setText(nlEmenta.item(0).getTextContent());			
		}
		else {
			tvEmenta.setText("");
		}


		NodeList nlTxtTramitacao = el.getElementsByTagName("txt-tramitacao");
		String txtTramitacao = "";

		if (nlTxtTramitacao.getLength() > 0) {
			txtTramitacao = nlTxtTramitacao.item(0).getTextContent();			
		}



		tvEmenta.setOnClickListener(onclick);

		TextView tvProtocolo = ((TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_protocolo));
		tvProtocolo.setText(el.getAttribute("num_protocolo"));

		NodeList nlVotacao = el.getElementsByTagName("votacao"); 

		if (nlVotacao.getLength() == 0) {
			TextView tvVotacao = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_votacao);
			tvVotacao.setText("Tr");
			tvVotacao.setBackgroundResource(R.drawable.container_corners_amarelo);
			//lViewsTramitacao.add(itemMateria);
			//lViewsTodas.add(itemMateria);

			//	viewTarge.setBackgroundResource(R.drawable.container_corners_amarelo);

		}
		else {

			Element elVot = (Element) nlVotacao.item(0);

			if (elVot.getAttribute("nom_resultado").equals("Aprovado")) {

				TextView tvVotacao = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_votacao);
				tvVotacao.setText("Ap");
				tvVotacao.setBackgroundResource(R.drawable.container_corners_verde);
			//	lViewsTodas.add(itemMateria);				
				//		viewTarge.setBackgroundResource(R.drawable.container_corners_verde);
			} 
			else if (elVot.getAttribute("nom_resultado").equals("Rejeitado")) {
				TextView tvVotacao = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_votacao);
				tvVotacao.setText("Rj");
				tvVotacao.setBackgroundResource(R.drawable.container_corners_vermelho);
			//	lViewsTodas.add(itemMateria);
				//		viewTarge.setBackgroundResource(R.drawable.container_corners_vermelho);
			}	
			else if (elVot.getAttribute("nom_resultado").equals("Pedido de Vista")) {
				TextView tvVotacao = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_votacao);
				tvVotacao.setText("Vst");
				tvVotacao.setBackgroundResource(R.drawable.container_corners_azul_escuro);
			//	lViewsTodas.add(itemMateria);
				//		viewTarge.setBackgroundResource(R.drawable.container_corners_vermelho);
			}
			else if (elVot.getAttribute("nom_resultado").equals("Prazo Regimental")) {
				TextView tvVotacao = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_votacao);
				tvVotacao.setText("PzRg");
				tvVotacao.setBackgroundResource(R.drawable.container_corners_azul_escuro);
			//	lViewsTodas.add(itemMateria);
				//		viewTarge.setBackgroundResource(R.drawable.container_corners_vermelho);
			}
		}
		//viewTarge.setBackgroundColor(Color.TRANSPARENT);

		int jj = 0;
		if (itemMateria.cod_materia.equals("3573")) {
			jj++;
		}


		NodeList nlAutor = el.getElementsByTagName("autor");
		TextView tvAutor = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_autor);
		if (nlAutor.getLength() > 0) {
			String txtAutor = "";
			for (int i = 0; i < nlAutor.getLength();i++) {
				if ( i!= 0 )
					txtAutor += ", ";
				txtAutor += nlAutor.item(i).getTextContent();
			}
			tvAutor.setText(txtAutor);
		}
		else {
			tvAutor.setText("");

			// ************************* **************************
			NodeList nlAutores = el.getElementsByTagName("autores");

			tvAutor.setText("");
			if (nlAutores.getLength() == 1) {
				Element ell = (Element) nlAutores.item(0);
				nlAutores = ell.getElementsByTagName("autor-materia"); 
				for (int i = 0; i < nlAutores.getLength(); i++) { 								
					tvAutor.setText(tvAutor.getText()+nlAutores.item(i).getTextContent() + (i < nlAutores.getLength()-1?";   ":""));		
				}
			}		 


		}

		TextView tvOrdem = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_ordem);
		tvOrdem.setText(String.valueOf(el.getAttribute("num_ordem")));

		TextView tvDataApres = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_dat_apresentacao);
		tvDataApres.setText(String.valueOf(el.getAttribute("dat_apresentacao")));


		// Tramitação ***************************************************

		NodeList nlTramitacao = el.getElementsByTagName("tramitacao");

		LinearLayout containerTramitacao =  (LinearLayout) itemMateria.findViewById(R.id.popup_titulo_tramitacao);
		if (nlTramitacao.getLength() == 1) {
			Element elTram = (Element) nlTramitacao.item(0);
			nlTramitacao = el.getElementsByTagName("item-tramitacao");

			if (nlTramitacao.getLength() == 0) {
				containerTramitacao.setVisibility(View.GONE);
			}
			else {

				containerTramitacao.setVisibility(View.VISIBLE);
				containerTramitacao =  (LinearLayout) itemMateria.findViewById(R.id.popup_container_tramitacao);
				containerTramitacao.removeAllViews();

				for (int i = 0; i < nlTramitacao.getLength(); i++) {

					LinearLayout item = (LinearLayout) inflater.inflate(R.layout.sessao_plenaria_item_tramitacao, null);


					Element elTr = (Element) nlTramitacao.item(i);
					/*
					TextView tvData = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_data);										
					tvData.setText(elTr.getAttribute("dat_tramitacao"));
					tvData.setVisibility(View.GONE);

					TextView tvOrigem = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_origem);

					String origem = ((Element)elTr.getElementsByTagName("origem").item(0)).getAttribute("nom_orgao");
					origem += ((Element)elTr.getElementsByTagName("origem").item(0)).getAttribute("nom_comissao");
					origem += ((Element)elTr.getElementsByTagName("origem").item(0)).getAttribute("nom_parlamentar");
					tvOrigem.setText(origem);

					tvOrigem.setVisibility(View.GONE);

					TextView tvDestino = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_destino);		

					String destino = ((Element)elTr.getElementsByTagName("destino").item(0)).getAttribute("nom_orgao");
					destino += ((Element)elTr.getElementsByTagName("destino").item(0)).getAttribute("nom_comissao");
					destino += ((Element)elTr.getElementsByTagName("destino").item(0)).getAttribute("nom_parlamentar");
					tvDestino.setText(destino);
					tvDestino.setVisibility(View.GONE);*/

					TextView tvSit = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_situacao);		
					tvSit.setText(elTr.getAttribute("situacao"));

					TextView tvUltAc = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_ultima_acao);
					tvUltAc.setText(  elTr.getElementsByTagName("ultima-acao").item(0).getTextContent()  );

					if (SaplActivity.mostrar_roteiro_sessao) {
						TextView tvTxtTramitacao = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_txt_sessao_plenaria);
						tvTxtTramitacao.setText(txtTramitacao);

					}

					containerTramitacao.addView(item);

				}


			}
		} 

		// Materia Anexada ***************************************************

		NodeList nlMatAnexadas = el.getElementsByTagName("materias-anexadas");

		LinearLayout containerMatAnexadas =  (LinearLayout) itemMateria.findViewById(R.id.popup_titulo_materias_anexadas);
		if (nlMatAnexadas.getLength() == 1) {
			Element elMatAnex = (Element) nlMatAnexadas.item(0);
			nlMatAnexadas = elMatAnex.getElementsByTagName("matanex");

			if (nlMatAnexadas.getLength() == 0) {
				containerMatAnexadas.setVisibility(View.GONE);
			}
			else {

				containerMatAnexadas.setVisibility(View.VISIBLE);
				containerMatAnexadas =  (LinearLayout) itemMateria.findViewById(R.id.popup_container_materias_anexadas);
				containerMatAnexadas.removeAllViews();

				for (int i = 0; i < nlMatAnexadas.getLength(); i++) {

					elMatAnex = (Element) nlMatAnexadas.item(i);	
					final String cod_matanex = elMatAnex.getAttribute("cod_materia");

					LinearLayout item = (LinearLayout) inflater.inflate(R.layout.popup_materia_detalhes_item_anexada, null);
					Utils.openFileMateriasTextoIntegral(elMatAnex, item, null);

					/*

					if (SaplActivity.files_pre_download) {

						final Element elRun = elMatAnex;
						new Thread(new Runnable() {
							@Override
							public void run() { 
								try {
									Thread.sleep((long)(500+5000*Math.random()));
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								Utils.downloadFileMaterias(elRun, "materia/", null, false); 
							}
						}).start();
					}*/
					//Utils.downloadFileMaterias(el, "materia/", null, false);
					/*

					View.OnClickListener onclick =  new View.OnClickListener() {			
						@Override
						public void onClick(View arg0) {
							itemMateria.run(cod_matanex,popup);
						}
					};	*/


					tvTitulo = ((TextView) item.findViewById(R.id.sessao_plenaria_materia_titulo));
					tvTitulo.setText(

							elMatAnex.getAttribute("sgl_tipo_materia")+" "+
									elMatAnex.getAttribute("num_ident_basica")+"/"+
									elMatAnex.getAttribute("ano_ident_basica")+" - "+
									elMatAnex.getAttribute("des_tipo_materia")  
							);

					tvTitulo.setOnClickListener(onclick);

					tvProtocolo = ((TextView) item.findViewById(R.id.sessao_plenaria_materia_protocolo));
					tvProtocolo.setText(elMatAnex.getAttribute("num_protocolo"));

					tvDataApres = (TextView) item.findViewById(R.id.sessao_plenaria_materia_dat_apresentacao);
					tvDataApres.setText(String.valueOf(elMatAnex.getAttribute("dat_apresentacao")));

					nlEmenta = elMatAnex.getElementsByTagName("ementa-matanex");
					tvEmenta = (TextView) item.findViewById(R.id.sessao_plenaria_materia_ementa);
					if (nlEmenta.getLength() > 0) {
						tvEmenta.setText(nlEmenta.item(0).getTextContent());			
					}
					else {
						tvEmenta.setText("");
					}
					tvEmenta.setOnClickListener(onclick);

					NodeList nlAutores = elMatAnex.getElementsByTagName("autores-matanex");

					tvAutor = (TextView) item.findViewById(R.id.sessao_plenaria_materia_autor);
					tvAutor.setText("");
					if (nlAutores.getLength() == 1) {
						elMatAnex = (Element) nlAutores.item(0);
						nlAutores = elMatAnex.getElementsByTagName("autor-matanex"); 
						for (int j = 0; j < nlAutores.getLength(); j++) { 								
							tvAutor.setText(tvAutor.getText()+nlAutores.item(j).getTextContent() + (j < nlAutores.getLength()-1?";    ":""));		
						}
					}			


					containerMatAnexadas.addView(item);
				}

			}
		}




		// Legislação Citada ***************************************************

		NodeList nlLegCitada = el.getElementsByTagName("legislacao-citada");

		LinearLayout containerLegCitada =  (LinearLayout) itemMateria.findViewById(R.id.popup_titulo_legislacao_citada);
		if (nlLegCitada.getLength() == 1) {
			Element elMatAnex = (Element) nlLegCitada.item(0);
			nlLegCitada = elMatAnex.getElementsByTagName("item");

			if (nlLegCitada.getLength() == 0) {
				containerLegCitada.setVisibility(View.GONE);
			}
			else {

				containerLegCitada.setVisibility(View.VISIBLE);
				containerLegCitada =  (LinearLayout) itemMateria.findViewById(R.id.popup_container_legislacao_citada);
				containerLegCitada.removeAllViews();

				for (int i = 0; i < nlLegCitada.getLength(); i++) {

					elMatAnex = (Element) nlLegCitada.item(i);	
					final String cod_norma = elMatAnex.getAttribute("cod_norma");

					LinearLayout item = (LinearLayout) inflater.inflate(R.layout.popup_norma_detalhes_legislacao_citada, null);

					/*
					Utils.openFileMateriasTextoIntegral(elMatAnex, item, null);
 
					View.OnClickListener onclick =  new View.OnClickListener() {			
						@Override
						public void onClick(View arg0) {
							itemMateria.run(cod_matanex,popup);
						}
					};	*/

					View.OnClickListener onclickNorma =  new View.OnClickListener() {	

						@Override
						public void onClick(View arg0) {

							// Endereço que será aberto
							Uri uri = Uri.parse(SaplActivity.urlBase+"consultas/norma_juridica/norma_juridica_mostrar_proc?cod_norma="+cod_norma+"#texto_integral");
							// Criar a Intent para aquele endereço
							Intent intent = new Intent(Intent.ACTION_VIEW, uri);
							// Enviar a Intent para o sistema operacional tratar
							SaplActivity.saplActivity.startActivity(intent); 
						} 
					};



					tvTitulo = ((TextView) item.findViewById(R.id.norma_titulo));
					tvTitulo.setText(

							elMatAnex.getAttribute("des_tipo_norma")+" "+
									elMatAnex.getAttribute("num_norma")+"/"+
									elMatAnex.getAttribute("ano_norma")  
							);
					tvTitulo.setOnClickListener(onclickNorma);



					tvTitulo = ((TextView) item.findViewById(R.id.norma_ementa));
					tvTitulo.setText(

							elMatAnex.getTextContent() 
							);

					tvTitulo.setOnClickListener(onclickNorma);

					//tvTitulo.setOnClickListener(onclick);


					containerLegCitada.addView(item);
				}

			}
		}








		Utils.openFileMateriasTextoIntegral(el, itemMateria, null);


		return itemMateria;

	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (!isRun) {
			isRun = true;
			restartThMats(this.elSessao);
		}

		return false;
	}


}
