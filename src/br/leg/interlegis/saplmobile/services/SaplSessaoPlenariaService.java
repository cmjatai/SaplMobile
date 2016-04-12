package br.leg.interlegis.saplmobile.services;

import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder; 
import br.leg.interlegis.saplmobile.SaplActivity;
import br.leg.interlegis.saplmobile.sessao.fragment.SessaoFragment;
import br.leg.interlegis.saplmobile.sessao.view.ListaDeMateriasPorSessaoPlenaria;
import br.leg.interlegis.saplmobile.suport.Log;
import br.leg.interlegis.saplmobile.suport.NetworkUtil;
import br.leg.interlegis.saplmobile.suport.Utils;

public class SaplSessaoPlenariaService extends Service {


	private Timer timer = new Timer();

	public String dataSelecionada = "";

	public String codSessaoPlenaria = "";
	public String crc32 = "";



	private static SaplSessaoPlenariaService service = null;


	private boolean _servicoFechado = true;

	private boolean _servicoLivre = true;

	private int contadorProcessos = 606;
	private int contadortimers= 0;

	private float timeListaMaterias = 32;
	private float timeListaMaximo = 180;
	private int timeListaMateriasFixo = 32;
	private int timePontoDownloadFileMaterias = 10;
	private double timePontoDownloadFileMateriasBaseRandom = Math.random();

	private  NodeList nlMatExp = null; 
	private  NodeList nlMatOrdem = null;
	private Thread thLmats = null; 

	int downloadSimultaneos = 0;

	@Override
	public void onCreate() {
		super.onCreate();

		service = this;

		Log.d("CMJ", "Service onCreate");
	}

	@Override
	public void onDestroy() {
		service = null;
		super.onDestroy();
	}
	public static SaplSessaoPlenariaService getInstance() {
		return service;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		startservice();

		return startId;

	}

	public static void fecharServico() {


		if (service == null)
			return;

		service.timer.cancel(); 
		service.timer.purge(); 

		service._servicoFechado = true;
		service.stopSelf();
		service = null;
	}

	public void now() {
		timeListaMaterias = timeListaMateriasFixo;
		contadorProcessos = 606;
		updatePontoDownloadFileMaterias();

	}
	public void atrasarTimeListaMaterias() {

		Log.d("CMJ", "atraso: "+getInstance().timeListaMaterias);
		if (timeListaMaterias < timeListaMaximo)
			timeListaMaterias += 2;
		updatePontoDownloadFileMaterias();
	}

	public void reiniciarAtrasoTimeListaMaterias() {

		timeListaMaterias = timeListaMateriasFixo;
		contadorProcessos = 607;
		updatePontoDownloadFileMaterias(); 

	}

	private void updatePontoDownloadFileMaterias() {
		timePontoDownloadFileMaterias = 5+(int)(timePontoDownloadFileMateriasBaseRandom*(timeListaMaterias+5));
	}


	public void setNodeListDownloadFilesMaterias(final NodeList _nlMatExp, final NodeList _nlMatOrdem) {

		nlMatExp = _nlMatExp;
		nlMatOrdem = _nlMatOrdem; 
	}

	private void startservice() {


		timer.schedule( new TimerTask() {

			int contTimer = contadortimers++;

			@Override
			public void run() { 

				if (!_servicoLivre) {
					Log.d("CMJ", "retornou");
					return;
				}

				Log.d("CMJ", "GR: timer"+((int)getInstance().timeListaMaterias)+": "+contadorProcessos);

				 
				if (contadorProcessos % 59 == 0 || contadorProcessos == 606) { //Atualizar Lista de Datas 

					Log.i("CMJ", "LS: servico. "+contadorProcessos);

					Log.d("CMJ", "LS: timer"+((int)getInstance().timeListaMaterias)+": "+contTimer);


					NetworkUtil.isConnected();

					try {
						if (dataSelecionada == null || dataSelecionada.length() == 0) {
							_servicoLivre = false;
							Log.d("CMJ", "LS: inicio.");
							Utils.downloadFileHttpGet(SaplActivity.urlXmlBase+"consultas/sessao_plenaria/sessao_plenaria_lista",SaplActivity.fileCacheListaSessoes);
							Log.d("CMJ", "LS: fim.");
							_servicoLivre = true;
						}
						else {

							Log.d("CMJ",dataSelecionada);
							_servicoLivre = false;

							Log.d("CMJ", "LS: inicio.");
							Utils.downloadFileHttpGet(SaplActivity.urlXmlBase+"consultas/sessao_plenaria/sessao_plenaria_lista?data="+dataSelecionada,SaplActivity.fileCacheListaSessoes);
							Log.d("CMJ", "LS: fim.");
							_servicoLivre = true;

						}


					} catch (Exception e) { 
						_servicoLivre = true;
						Log.e("CMJ", "erro download lista de sessões!");
						return;						

					}

					Intent broadcastIntent = new Intent();
					broadcastIntent.setAction(SessaoFragment.UpdateDatasSessoesReceiver.PROCESS_UPDATE_DATAS); 
					broadcastIntent.putExtra("value", "serviço "+contadorProcessos+" - "+dataSelecionada);
					sendBroadcast(broadcastIntent);	

				} else {

					if ((contadorProcessos) % ((int)(timeListaMaterias)) == 0) { //Atualizar Lista de Matérias

						Log.i("CMJ", "LM: servico. "+contadorProcessos+"... "+codSessaoPlenaria);

						Log.i("CMJ", "LM: timer"+((int)getInstance().timeListaMaterias)+": "+contadorProcessos);
						NetworkUtil.isConnected();

						String xmlMats = "";

						try { 
							_servicoLivre = false;
							Log.d("CMJ", "LM: Início.");
							xmlMats = Utils.executeHttpGet(SaplActivity.urlXmlBase+"consultas/sessao_plenaria/sessao_plenaria_materias?cod_sessao_plen="+codSessaoPlenaria+"&crc32="+crc32);
							Log.d("CMJ", "LM: fim.");
							_servicoLivre = true;



						} catch (Exception e1) {
							Log.e("CMJ", "erro download lista de matérias!"+"?cod_sessao_plen="+codSessaoPlenaria);
							_servicoLivre = true;
							return;
						}

						Intent broadcastIntent = new Intent();
						broadcastIntent.setAction(ListaDeMateriasPorSessaoPlenaria.UpdateMateriasSessaoReceiver.PROCESS_UPDATE_MATERIAS_SESSAO); 
						broadcastIntent.putExtra("value", "serviço "+contadorProcessos+" - "+dataSelecionada);
						broadcastIntent.putExtra("xmlMats", xmlMats); 

						sendBroadcast(broadcastIntent);	
						
						

						int proximaAtualizacao = (int)(timeListaMaterias - (contadorProcessos) % ((int)(timeListaMaterias))) ;
						Log.d("CMJ", "proximaAtualizacao: "+proximaAtualizacao);

						broadcastIntent = new Intent();
						broadcastIntent.setAction(ListaDeMateriasPorSessaoPlenaria.UpdateMateriasSessaoReceiver.PROCESS_UPDATE_NEXT_MATERIAS_SESSAO);  
						broadcastIntent.putExtra("proximaAtualizacao", proximaAtualizacao);
						sendBroadcast(broadcastIntent);	 
					}

					if ((contadorProcessos) % 2 == 0) {

						int proximaAtualizacao = (int)(timeListaMaterias - (contadorProcessos) % ((int)(timeListaMaterias))) ;
						Log.d("CMJ", "proximaAtualizacao: "+proximaAtualizacao);

						Intent broadcastIntent = new Intent();
						broadcastIntent.setAction(ListaDeMateriasPorSessaoPlenaria.UpdateMateriasSessaoReceiver.PROCESS_UPDATE_NEXT_MATERIAS_SESSAO);  
						broadcastIntent.putExtra("proximaAtualizacao", proximaAtualizacao*2);
						sendBroadcast(broadcastIntent);	  
					}

					if (contadorProcessos % ((int)(timePontoDownloadFileMaterias)) == 0) { //Download Materias
						Log.d("CMJ1", "PD: "+timePontoDownloadFileMaterias);
						startDownloadFileMaterias();

						int arquivosParaBaixar = startCountArquivosParaBaixar(); 
						//Log.d("CMJ1", "arquivosParaBaixar: "+arquivosParaBaixar);
						Log.d("CMJ1", " ... downloadsSimultaneos:"+downloadSimultaneos);
						Intent broadcastIntent = new Intent();
						broadcastIntent.setAction(ListaDeMateriasPorSessaoPlenaria.UpdateMateriasSessaoReceiver.PROCESS_COUNT_DOWNLOADS_MATERIA_SESSAO); 
						broadcastIntent.putExtra("arquivosParaBaixar", arquivosParaBaixar);
						broadcastIntent.putExtra("timePontoDownloadFileMaterias", timePontoDownloadFileMaterias);

						sendBroadcast(broadcastIntent);	
					}
				}


				contadorProcessos++;
			}
		}, 0, 2000);
	}


	protected int startCountArquivosParaBaixar() {

		if (nlMatExp == null || nlMatOrdem == null)
			return 0;

		if (nlMatExp.getLength() == 0 && nlMatOrdem.getLength() == 0)
			return 0;

		return Utils.contarArquivosParaBaixar(nlMatExp, nlMatOrdem);


	}

	protected void startDownloadFileMaterias() {

		if (nlMatExp == null || nlMatOrdem == null)
			return;

		if (nlMatExp.getLength() == 0 && nlMatOrdem.getLength() == 0)
			return;


		if ( (thLmats != null && thLmats.isAlive())) {
			return;
		}


		if (downloadSimultaneos > 5)
			return;



		/*
		if (timeListaMaterias < timePontoDownloadFileMaterias || (thLmats != null && thLmats.isAlive())) {
			return;
		}
		 */

		thLmats = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					downloadSimultaneos++;
					for (int i = 0; i < nlMatExp.getLength(); i++) {
						Element el = (Element) nlMatExp.item(i);
						if (Utils.downloadFileMaterias(el, "materia/", null, false)) { 	
							downloadSimultaneos--;
							return;
						}

					}

					for (int i = 0; i < nlMatOrdem.getLength(); i++) {
						Element el = (Element) nlMatOrdem.item(i);
						if (Utils.downloadFileMaterias(el, "materia/", null, false)) { 	
							downloadSimultaneos--;
							return;
						}

					} 					

					for (int i = 0; i < nlMatExp.getLength(); i++) {
						Element el = (Element) nlMatExp.item(i);
						if (executeParaMatAnexada(el)) { 	
							downloadSimultaneos--;
							return;
						}
					}

					for (int i = 0; i < nlMatOrdem.getLength(); i++) {
						Element el = (Element) nlMatOrdem.item(i);
						if (executeParaMatAnexada(el)) { 	
							downloadSimultaneos--;
							return;
						}
					} 
				}
				catch (Exception e) {
					Log.e("CMJ", "erro download de arquivo de matérias.");
					//e.printStackTrace();
				}
				finally {

				}	
				//	Log.d("CMJ", "saiu!");
			}

			private boolean executeParaMatAnexada(Element el) {

				NodeList nlMatAnexadas = el.getElementsByTagName("materias-anexadas");

				if (nlMatAnexadas.getLength() == 1) {
					Element elMatAnex = (Element) nlMatAnexadas.item(0);
					nlMatAnexadas = elMatAnex.getElementsByTagName("matanex");

					if (nlMatAnexadas.getLength() == 0) 
						return false;


					for (int i = 0; i < nlMatAnexadas.getLength(); i++) {

						elMatAnex = (Element) nlMatAnexadas.item(i);
						if (Utils.downloadFileMaterias(elMatAnex, "materia/", null, false))
							return true;
					}
				}
				return false;
			}

		});
		thLmats.start();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
