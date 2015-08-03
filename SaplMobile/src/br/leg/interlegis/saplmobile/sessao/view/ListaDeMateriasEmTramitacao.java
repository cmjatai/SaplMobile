package br.leg.interlegis.saplmobile.sessao.view;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.leg.interlegis.saplmobile.R;
import br.leg.interlegis.saplmobile.SaplActivity;
import br.leg.interlegis.saplmobile.SaplSettingsActivity;
import br.leg.interlegis.saplmobile.sessao.view.popups.MateriaDetalhes;
import br.leg.interlegis.saplmobile.suport.NetworkUtil;
import br.leg.interlegis.saplmobile.suport.Utils;

public class ListaDeMateriasEmTramitacao extends LinearLayout{

	public Fragment fragment = null; 

	public Element elMat;

	LayoutInflater inflater = null;
	ArrayList<ProgressDialog> dialog = new ArrayList<ProgressDialog>();

	public ListaDeMateriasEmTramitacao(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public LinearLayout fillTitle(final String title) {

		LinearLayout tituloLayout = (LinearLayout) inflater.inflate(R.layout.sessao_plenaria_titulo_partes, null);


		TextView tvTitulo = (TextView) tituloLayout.findViewById(R.id.textItemMenu);
		tvTitulo.setText(title);

		tvTitulo.setBackgroundResource(android.R.color.transparent);
		tvTitulo.setTextColor(Color.rgb(190, 190, 190));


		return tituloLayout;

		/*
		if (expediente) {
			itemMateria.findViewById(R.id.sessao_plenaria_item_materia).setBackgroundResource(R.drawable.container_corners_amarelo);

		}*/

	}

	public void requestMats(ProgressDialog _dialog) {
		if (_dialog != null)
			dialog.add(_dialog);

		inflater = (LayoutInflater) fragment.getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


		/*		String md5Novo = Utils.md5(xmlMats);

		if (md5Novo.equals(md5Oficial)) 
			return;
		md5Oficial = md5Novo;
		 */

		final LinearLayout lm = this;

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {



					Document xmlDom;
					try {

						String xmlMats = "";

						xmlMats = Utils.executeHttpGet(SaplActivity.urlXmlBase+"consultas/materia/materia_em_tramitacao_proc");
						xmlDom = Utils.loadXMLFromString(xmlMats);

						Element elRaiz = xmlDom.getDocumentElement();
						
						
						SaplActivity.saplActivity.runOnUiThread(new Runnable() { 
							@Override
							public void run() {  

								if (!dialog.isEmpty()) {
									dialog.get(0).setMessage("Dados chegaram... construindo visualização...");
								}
							}
						});
						
						NodeList nlMaterias = xmlDom.getElementsByTagName("materia");
						for (int i = 0; i < nlMaterias.getLength(); i++) {
							final Element el = (Element) nlMaterias.item(i);  
							SaplActivity.saplActivity.runOnUiThread(new Runnable() { 
								@Override
								public void run() {  
									lm.addView(fillView(el));
									//lm.addView(fillTitle("Matérias em tramitação"));
									NetworkUtil.isConnected();
								}
							});
						}
						
						while (!dialog.isEmpty()) {
							dialog.get(0).dismiss();
							dialog.remove(0);
						}

						if (SaplActivity.files_pre_download)
						for (int i = 0; i < nlMaterias.getLength(); i++) {
							final Element el = (Element) nlMaterias.item(i);

							if (Utils.downloadFileMaterias(el, "materia/", null, false))
								return;	 
						} 

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  

				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {

				}	 
			}
		}).start(); 

	}

	public LinearLayout fillView(final Element el) {

		ItemListaMaterias itemMateria = (ItemListaMaterias) inflater.inflate(R.layout.sessao_plenaria_materia, null);
		itemMateria.setPadding(itemMateria.getPaddingLeft(), itemMateria.getPaddingTop(), itemMateria.getPaddingRight(), 20);

		itemMateria.cod_materia = el.getAttribute("cod_materia");

		itemMateria.findViewById(R.id.sessao_plenaria_item_materia).setBackgroundResource(R.drawable.container_corners);

		TextView tvTitulo = ((TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_titulo));

		tvTitulo.setText(
				el.getAttribute("sgl_tipo_materia")+" "+
						el.getAttribute("num_ident_basica")+"/"+
						el.getAttribute("ano_ident_basica")+" - "+
						el.getAttribute("des_tipo_materia")  
				);
		tvTitulo.setBackgroundResource(R.drawable.container_corners_azul);

		View.OnClickListener onclick =  new View.OnClickListener() {	

			@Override
			public void onClick(View arg0) {

				LayoutInflater layoutInflater = (LayoutInflater)SaplActivity.saplActivity.getBaseContext().getSystemService(SaplActivity.LAYOUT_INFLATER_SERVICE);  
				SaplPopupView popup = (SaplPopupView) layoutInflater.inflate(R.layout.popup, null);  

				MateriaDetalhes materiaDetalhes = (MateriaDetalhes) layoutInflater.inflate(R.layout.popup_materia_detalhes, null);
				popup.show(materiaDetalhes);
				materiaDetalhes.run(el.getAttribute("cod_materia"), popup);

			}

		};
		tvTitulo.setOnClickListener(onclick);

		NodeList nlEmenta = el.getElementsByTagName("ementa");
		TextView tvEmenta = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_ementa);
		if (nlEmenta.getLength() > 0) {
			tvEmenta.setText(nlEmenta.item(0).getTextContent());			
		}
		else {
			tvEmenta.setText("");
		}
		tvEmenta.setOnClickListener(onclick);

		TextView tvProtocolo = ((TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_protocolo));
		tvProtocolo.setText(el.getAttribute("num_protocolo"));

		TextView tvVotacao = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_votacao);
		tvVotacao.setText("Tr");
		tvVotacao.setBackgroundResource(R.drawable.container_corners_amarelo);  


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
					tvAutor.setText(tvAutor.getText()+nlAutores.item(i).getTextContent() + (i < nlAutores.getLength()-1?";    ":""));		
				}
			}		 
		}

		TextView tvOrdem = (TextView) itemMateria.findViewById(R.id.sessao_plenaria_materia_ordem);
		tvOrdem.setVisibility(View.GONE);

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


					containerTramitacao.addView(item);
					break;
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
							Uri uri = Uri.parse(SaplActivity.urlBase+"consultas/norma_juridica/norma_juridica_mostrar_proc?cod_norma="+cod_norma+"&iframe=1#texto_integral");
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


}
