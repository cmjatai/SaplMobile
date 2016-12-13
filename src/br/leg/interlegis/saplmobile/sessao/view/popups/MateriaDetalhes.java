package br.leg.interlegis.saplmobile.sessao.view.popups;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.leg.interlegis.saplmobile.R;
import br.leg.interlegis.saplmobile.SaplActivity;
import br.leg.interlegis.saplmobile.sessao.view.SaplPopupView;
import br.leg.interlegis.saplmobile.suport.Utils;

public class MateriaDetalhes extends LinearLayout {

	LayoutInflater inflater = null;
	public String cod_materia = "";

	public MateriaDetalhes(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}	

	public void run(String _cod_materia, final SaplPopupView popup) {



		inflater = (LayoutInflater) SaplActivity.saplActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


		cod_materia = _cod_materia;

		final MateriaDetalhes itemMateria = this;


		new Thread(new Runnable() {

			@Override
			public void run() {

				String xmlMats = "";

				try { 
					xmlMats = Utils.executeHttpGet(SaplActivity.urlApiBase+"consultas/materia/materia_mostrar_proc?cod_materia="+cod_materia);

					Document xmlDom = null;
					try {
						xmlDom = Utils.loadXMLFromString(xmlMats);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 

					final	Element elRaiz = xmlDom.getDocumentElement();

					SaplActivity.saplActivity.runOnUiThread(new Runnable() {

						@Override
						public void run() { 
							Element el;

							// ***************************************************
							//TextView tvTitulo = ((TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_titulo));

							TextView tvTitulo = ((TextView) popup.findViewById(R.id.titulo));

							tvTitulo.setText(

									elRaiz.getAttribute("sgl_tipo_materia")+" "+
											elRaiz.getAttribute("num_ident_basica")+"/"+
											elRaiz.getAttribute("ano_ident_basica")+" - "+
											elRaiz.getAttribute("des_tipo_materia")  
									);
							//tvTituloPopup.setText(tvTitulo.getText());


							// ***************************************************
							TextView tvProtocolo = ((TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_protocolo));
							tvProtocolo.setText(elRaiz.getAttribute("num_protocolo"));


							// ***************************************************
							NodeList nlEmenta = elRaiz.getElementsByTagName("ementa");
							TextView tvEmenta = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_ementa);
							if (nlEmenta.getLength() > 0) {
								tvEmenta.setText(nlEmenta.item(0).getTextContent());			
							}
							else {
								tvEmenta.setText("");
							}
							// ***************************************************
							NodeList nlAutores = elRaiz.getElementsByTagName("autores");

							TextView tvAutor = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_autor);
							tvAutor.setText("");
							if (nlAutores.getLength() == 1) {
								el = (Element) nlAutores.item(0);
								nlAutores = el.getElementsByTagName("autor-materia"); 
								for (int i = 0; i < nlAutores.getLength(); i++) { 								
									tvAutor.setText(tvAutor.getText()+nlAutores.item(i).getTextContent() + (i < nlAutores.getLength()-1?";    ":""));		
								}
							}			
							// ***************************************************				  							
							Utils.openFileMateriasTextoIntegral(elRaiz, itemMateria, null);

							// ***************************************************
							TextView tvDatApr = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_dat_apresentacao);
							tvDatApr.setText(elRaiz.getAttribute("dat_apresentacao"));


							// Documentos Acessórios ***************************************************

							NodeList nlDocAcess = elRaiz.getElementsByTagName("documentos-acessorios");

							LinearLayout containerDocAcess =  (LinearLayout) itemMateria.findViewById(R.id.popup_titulo_materias_doc_acessorios);
							if (nlDocAcess.getLength() == 1) {
								el = (Element) nlDocAcess.item(0);
								nlDocAcess = el.getElementsByTagName("doc-acess");

								if (nlDocAcess.getLength() == 0) {
									containerDocAcess.setVisibility(View.GONE);
								}
								else {

									containerDocAcess.setVisibility(View.VISIBLE);
									containerDocAcess =  (LinearLayout) itemMateria.findViewById(R.id.popup_container_materias_doc_acessorios);
									containerDocAcess.removeAllViews();

									for (int i = 0; i < nlDocAcess.getLength(); i++) {

										el = (Element) nlDocAcess.item(i);	
										final String cod_doc_acess = el.getAttribute("cod_documento");

										LinearLayout item = (LinearLayout) inflater.inflate(R.layout.popup_materia_detalhes_item_doc_acessorios, null);
										Utils.openFileMateriasTextoIntegral(el, item, "");
 

										if (SaplActivity.files_pre_download) {

											final Element elRun = el;
											new Thread(new Runnable() {
												@Override
												public void run() { 
													try {
														Thread.sleep((long)(500+5000*Math.random()));
													} catch (InterruptedException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													Utils.downloadFileMaterias(elRun, "materia/", "", false); 
												}
											}).start();
										}
										
										tvTitulo = ((TextView) item.findViewById(R.id.materia_detalhes_titulo));
										tvTitulo.setText(
												el.getAttribute("nom_documento")
												);

										tvTitulo = ((TextView) item.findViewById(R.id.materia_detalhes_tipo_documento));
										tvTitulo.setText(
												el.getAttribute("des_tipo_documento")
												);

										tvTitulo = ((TextView) item.findViewById(R.id.materia_detalhes_autor));
										tvTitulo.setText(
												el.getAttribute("nom_autor_documento")
												);

										tvTitulo = ((TextView) item.findViewById(R.id.materia_detalhes_data_documento));
										tvTitulo.setText(
												el.getAttribute("dat_documento")
												);


										containerDocAcess.addView(item);
									}

								}
							}



							// Materia Anexada ***************************************************

							NodeList nlMatAnexadas = elRaiz.getElementsByTagName("materias-anexadas");

							LinearLayout containerMatAnexadas =  (LinearLayout) itemMateria.findViewById(R.id.popup_titulo_materias_anexadas);
							if (nlMatAnexadas.getLength() == 1) {
								el = (Element) nlMatAnexadas.item(0);
								nlMatAnexadas = el.getElementsByTagName("matanex");

								if (nlMatAnexadas.getLength() == 0) {
									containerMatAnexadas.setVisibility(View.GONE);
								}
								else {

									containerMatAnexadas.setVisibility(View.VISIBLE);
									containerMatAnexadas =  (LinearLayout) itemMateria.findViewById(R.id.popup_container_materias_anexadas);
									containerMatAnexadas.removeAllViews();

									for (int i = 0; i < nlMatAnexadas.getLength(); i++) {

										el = (Element) nlMatAnexadas.item(i);	
										final String cod_matanex = el.getAttribute("cod_materia");

										LinearLayout item = (LinearLayout) inflater.inflate(R.layout.popup_materia_detalhes_item_anexada, null);
										Utils.openFileMateriasTextoIntegral(el, item, null);



										if (SaplActivity.files_pre_download) {

											final Element elRun = el;
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
										}
										//Utils.downloadFileMaterias(el, "materia/", null, false);


										View.OnClickListener onclick =  new View.OnClickListener() {			
											@Override
											public void onClick(View arg0) {
												itemMateria.run(cod_matanex,popup);
											}
										};	


										tvTitulo = ((TextView) item.findViewById(R.id.sessao_plenaria_materia_titulo));
										tvTitulo.setText(

												el.getAttribute("sgl_tipo_materia")+" "+
														el.getAttribute("num_ident_basica")+"/"+
														el.getAttribute("ano_ident_basica")+" - "+
														el.getAttribute("des_tipo_materia")  
												);

										tvTitulo.setOnClickListener(onclick);

										tvProtocolo = ((TextView) item.findViewById(R.id.sessao_plenaria_materia_protocolo));
										tvProtocolo.setText(el.getAttribute("num_protocolo"));

										nlEmenta = el.getElementsByTagName("ementa-matanex");
										tvEmenta = (TextView) item.findViewById(R.id.sessao_plenaria_materia_ementa);
										if (nlEmenta.getLength() > 0) {
											tvEmenta.setText(nlEmenta.item(0).getTextContent());			
										}
										else {
											tvEmenta.setText("");
										}
										tvEmenta.setOnClickListener(onclick);

										nlAutores = el.getElementsByTagName("autores-matanex");

										tvAutor = (TextView) item.findViewById(R.id.sessao_plenaria_materia_autor);
										tvAutor.setText("");
										if (nlAutores.getLength() == 1) {
											el = (Element) nlAutores.item(0);
											nlAutores = el.getElementsByTagName("autor-matanex"); 
											for (int j = 0; j < nlAutores.getLength(); j++) { 								
												tvAutor.setText(tvAutor.getText()+nlAutores.item(j).getTextContent() + (j < nlAutores.getLength()-1?";    ":""));		
											}
										}			


										containerMatAnexadas.addView(item);
									}

								}
							}



							// Materia Anexadora ***************************************************

							NodeList nlMatAnexadora = elRaiz.getElementsByTagName("materia-anexadora");

							LinearLayout containerMatAnexadora =  (LinearLayout) itemMateria.findViewById(R.id.popup_titulo_materia_anexadora);
							if (nlMatAnexadora.getLength() == 1) {
								el = (Element) nlMatAnexadora.item(0);
								nlMatAnexadora = el.getElementsByTagName("matanexadora");

								if (nlMatAnexadora.getLength() == 0) {
									containerMatAnexadora.setVisibility(View.GONE);
								}
								else {

									containerMatAnexadora.setVisibility(View.VISIBLE);
									containerMatAnexadora =  (LinearLayout) itemMateria.findViewById(R.id.popup_container_materia_anexadora);
									containerMatAnexadora.removeAllViews();

									for (int i = 0; i < nlMatAnexadora.getLength(); i++) {

										el = (Element) nlMatAnexadora.item(i);	
										final String cod_matanex = el.getAttribute("cod_materia");

										LinearLayout item = (LinearLayout) inflater.inflate(R.layout.popup_materia_detalhes_item_anexada, null);

										Utils.openFileMateriasTextoIntegral(el, item, null);


										if (SaplActivity.files_pre_download) {

											final Element elRun = el;
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
										}
										
										
										View.OnClickListener onclick =  new View.OnClickListener() {			
											@Override
											public void onClick(View arg0) {
												itemMateria.run(cod_matanex,popup);
											}
										};	


										tvTitulo = ((TextView) item.findViewById(R.id.sessao_plenaria_materia_titulo));
										tvTitulo.setText(

												el.getAttribute("sgl_tipo_materia")+" "+
														el.getAttribute("num_ident_basica")+"/"+
														el.getAttribute("ano_ident_basica")+" - "+
														el.getAttribute("des_tipo_materia")  
												);

										tvTitulo.setOnClickListener(onclick);

										tvProtocolo = ((TextView) item.findViewById(R.id.sessao_plenaria_materia_protocolo));
										tvProtocolo.setText(el.getAttribute("num_protocolo"));

										nlEmenta = el.getElementsByTagName("ementa-matanexadora");
										tvEmenta = (TextView) item.findViewById(R.id.sessao_plenaria_materia_ementa);
										if (nlEmenta.getLength() > 0) {
											tvEmenta.setText(nlEmenta.item(0).getTextContent());			
										}
										else {
											tvEmenta.setText("");
										}
										tvEmenta.setOnClickListener(onclick);

										nlAutores = el.getElementsByTagName("autores-matanexadora");

										tvAutor = (TextView) item.findViewById(R.id.sessao_plenaria_materia_autor);
										tvAutor.setText("");
										if (nlAutores.getLength() == 1) {
											el = (Element) nlAutores.item(0);
											nlAutores = el.getElementsByTagName("autor-matanexadora"); 
											for (int j = 0; j < nlAutores.getLength(); j++) { 								
												tvAutor.setText(tvAutor.getText()+nlAutores.item(j).getTextContent() + (j < nlAutores.getLength()-1?";    ":""));		
											}
										}			
										containerMatAnexadora.addView(item);
									}

								}
							}

							// Tramitação ***************************************************

							NodeList nlTramitacao = elRaiz.getElementsByTagName("tramitacao");

							LinearLayout containerTramitacao =  (LinearLayout) itemMateria.findViewById(R.id.popup_titulo_tramitacao);
							if (nlTramitacao.getLength() == 1) {
								el = (Element) nlTramitacao.item(0);
								nlTramitacao = el.getElementsByTagName("item-tramitacao");

								if (nlTramitacao.getLength() == 0) {
									containerTramitacao.setVisibility(View.GONE);
								}
								else {

									containerTramitacao.setVisibility(View.VISIBLE);
									containerTramitacao =  (LinearLayout) itemMateria.findViewById(R.id.popup_container_tramitacao);
									containerTramitacao.removeAllViews();

									for (int i = 0; i < nlTramitacao.getLength(); i++) {

										LinearLayout item = (LinearLayout) inflater.inflate(R.layout.popup_materia_detalhes_item_tramitacao, null);


										Element elTr = (Element) nlTramitacao.item(i);

										TextView tvData = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_data);										
										tvData.setText(elTr.getAttribute("dat_tramitacao"));

										TextView tvOrigem = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_origem);

										String origem = ((Element)elTr.getElementsByTagName("origem").item(0)).getAttribute("nom_orgao");
										origem += ((Element)elTr.getElementsByTagName("origem").item(0)).getAttribute("nom_comissao");
										origem += ((Element)elTr.getElementsByTagName("origem").item(0)).getAttribute("nom_parlamentar");
										tvOrigem.setText(origem);

										TextView tvDestino = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_destino);		

										String destino = ((Element)elTr.getElementsByTagName("destino").item(0)).getAttribute("nom_orgao");
										destino += ((Element)elTr.getElementsByTagName("destino").item(0)).getAttribute("nom_comissao");
										destino += ((Element)elTr.getElementsByTagName("destino").item(0)).getAttribute("nom_parlamentar");
										tvDestino.setText(destino);


										TextView tvSit = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_situacao);		
										tvSit.setText(elTr.getAttribute("situacao"));

										TextView tvUltAc = (TextView) item.findViewById(R.id.materia_detalhes_tramitacao_ultima_acao);
										tvUltAc.setText(  elTr.getElementsByTagName("ultima-acao").item(0).getTextContent()  );



										containerTramitacao.addView(item);

									}


								}
							}






							// ***************************************************
							// ***************************************************
							// ***************************************************
							// ***************************************************


						}
					}); //final execução load



				} catch (Exception e1) {
					return;
				}




			}
		}).start();



	}
}