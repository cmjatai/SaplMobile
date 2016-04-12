package br.leg.interlegis.saplmobile.suport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri; 
import android.view.View;
import br.leg.interlegis.saplmobile.R;
import br.leg.interlegis.saplmobile.SaplActivity;

public class Utils {

	public static String dateTimeToStrBR(long longTime) {

		GregorianCalendar g = new GregorianCalendar();
		g.setTimeInMillis(longTime);

		String 	s = g.get(GregorianCalendar.DAY_OF_MONTH) + "/";
		s+= (g.get(GregorianCalendar.MONTH)+1) + "/";
		s+= g.get(GregorianCalendar.YEAR) + " ";	
		s+= g.get(GregorianCalendar.HOUR_OF_DAY) + ":";
		s+= (g.get(GregorianCalendar.MINUTE)+1) + ":";
		s+= g.get(GregorianCalendar.SECOND);
		return s;

	}

	public static String inToString(InputStream in) {

		ArrayList<byte[]> result = new ArrayList<byte[]>();

		byte[] buffer = new byte[1024];

		int countRead = 0;

		try {
			while ((countRead = in.read(buffer)) > 0) {

				if (countRead == buffer.length) {
					result.add(buffer);
					buffer = new byte[1024];
					continue;
				}

				byte[] aux = new byte[countRead];
				result.add(aux);

				for (int i = 0; i < aux.length; i++) {
					aux[i] = buffer[i];
				}

				buffer = new byte[1024];

			}

		} catch (IOException e) {
			return null;
		}

		if (result.size() == 0)
			return null;

		countRead = 0;
		for (byte[] buf: result)
			countRead += buf.length;

		buffer = new byte[countRead]; 

		int pos = 0;
		for (byte[] bs : result) {
			for (byte b : bs) {
				buffer[pos++] = b;
			}
		}
		try {
			return new String(buffer, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERRO";
		}		
	}

	public static String executeHttpGet(String URL)
	{
		InputStream in = null;
		try 
		{
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "android");
			HttpGet request = new HttpGet();
			request.setHeader("Content-Type", "text/html; charset=UTF-8");

			request.setURI(new URI(URL));
			HttpResponse response = client.execute(request);
			in = response.getEntity().getContent();

			return  Utils.inToString(in);

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			if (in != null) 
			{
				try 
				{
					in.close();
				} 
				catch (IOException e)    
				{
					Log.d("BBB", e.toString());
				}
			}
		}
		return "";
	}

	public static void downloadFileHttpGet(String URL, String fileDestination) throws Exception 
	{
		InputStream in = null;
		try 
		{
			System.gc();

			//	File f = new File(SaplActivity.uriFileBase);
			//	f.mkdirs();


			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "android");
			HttpGet request = new HttpGet();

			//request.setHeader("Content-Type", mimeType+"; charset=UTF-8");

			request.setURI(new URI(URL));
			HttpResponse response = client.execute(request);
			in = response.getEntity().getContent();


			//BufferedInputStream bis = new BufferedInputStream(in);

			FileOutputStream fos = new FileOutputStream(fileDestination);



			byte[] buffer = new byte[1048576];
			int len;
			while ((len = in.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			fos.close();


		} 
		finally 
		{
			if (in != null) 
			{
				try 
				{
					in.close();
				} 
				catch (IOException e)    
				{
					Log.d("BBB", e.toString());
				}
			}
		}
	}


	public static Document loadXMLFromString2(String xml) throws Exception
	{ 
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		InputSource is = new InputSource(new StringReader(xml)); 

		XmlPullParserFactory fact = XmlPullParserFactory.newInstance();
		fact.setNamespaceAware(true);
		XmlPullParser xpp = fact.newPullParser();
		xpp.setInput(is.getByteStream(), null);
		return null;
	}
	public static Document loadXMLFromString(String xml) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

	public static final String md5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	public static  String getType(File file) {
		String type = null;
		try {
			URL u = file.toURL();
			URLConnection uc = null;
			uc = u.openConnection();
			type = uc.getContentType();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return type;
	}

	public static GregorianCalendar strBRToDate(String s) {
		try {
			String dataHora[] = s.split(" ");

			String ss[] = dataHora[0].split("/");
			int a, m, d, h, mm, sss;
			a = Integer.parseInt(ss[2]);
			m = Integer.parseInt(ss[1])-1;
			d = Integer.parseInt(ss[0]);

			h = 0; mm = 0; sss = 0;
			if (dataHora.length > 1) { 
				ss = dataHora[1].split(":");
				h = Integer.parseInt(ss[0]);
				mm = Integer.parseInt(ss[1]);
				sss = Integer.parseInt(ss[2]);
			}

			GregorianCalendar g = new GregorianCalendar(a,m,d,h,mm,sss);	
			return g;
		} catch (Exception e) {
			return null;
		}
	}

	public static GregorianCalendar strToDate(String s) {

		try {
			String dataHora[] = s.split(" ");

			String ss[] = dataHora[0].split("/");
			int a, m, d, h, mm, sss;
			a = Integer.parseInt(ss[0]);
			m = Integer.parseInt(ss[1])-1;
			d = Integer.parseInt(ss[2]);

			h = 0; mm = 0; sss = 0;
			if (dataHora.length > 1) { 
				ss = dataHora[1].split(":");
				h = Integer.parseInt(ss[0]);
				mm = Integer.parseInt(ss[1]);
				sss = (int)Float.parseFloat(ss[2]);
			}

			GregorianCalendar g = new GregorianCalendar(a,m,d,h,mm,sss);	
			return g;
		} catch (Exception e) {
			return null;
		}
	}

	public static void openFileMateriasTextoIntegral(final Element el,
			View itemMateria) {
		openFileMateriasTextoIntegral(el, itemMateria, null);
	}


	public static void openFileMateriasTextoIntegral(final Element el,
			View itemMateria, String _sfxFile) {


		final String sfxFile = (_sfxFile == null ? "_texto_integral" : _sfxFile);			


		final View btnOpenFileImage = itemMateria.findViewById(R.id.sessao_plenaria_materia_btn_open_file_image);
		final View btnOpenFileText = itemMateria.findViewById(R.id.sessao_plenaria_materia_btn_open_file_text);

		if (el.getElementsByTagName("arquivo").getLength() == 0) {

			btnOpenFileImage.setVisibility(View.GONE);
			btnOpenFileText.setVisibility(View.GONE);
			/*
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			lp.setMargins(0, 1, 1, 0);

			viewTarge.setLayoutParams(lp);*/

		}
		else {

			View.OnClickListener onClick = new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					btnOpenFileImage.setBackgroundColor(Color.LTGRAY);
					btnOpenFileText.setBackgroundColor(Color.LTGRAY);


					final ProgressDialog dialog = new ProgressDialog(SaplActivity.saplActivity); 
					dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					dialog.setMessage("Abrindo Documento...");
					dialog.setCancelable(false);
					dialog.show();

					new Thread(new Runnable() {

						@Override
						public void run() {
							SaplActivity.saplActivity.runOnUiThread(new Runnable() {

								@Override
								public void run() {

									btnOpenFileImage.setBackgroundColor(Color.TRANSPARENT);
									btnOpenFileText.setBackgroundColor(Color.TRANSPARENT);
								}
							});

							downloadFileMaterias(el, "materia/", sfxFile, true);

							dialog.dismiss();

						}

					}).start();
				}

			};

			btnOpenFileImage.setOnClickListener(onClick);
			btnOpenFileText.setOnClickListener(onClick);

		}
	}

	public static boolean existFileMateria(final Element el, final String pathFile, 
			final String _sfxFile) {
		final String sfxFile = (_sfxFile == null ? "_texto_integral" : _sfxFile);			

		Element arquivo = (Element) el.getElementsByTagName("arquivo").item(0); 

		String url = SaplActivity.urlDocsBase+pathFile+el.getAttribute("cod_materia")+sfxFile;

		try {

			String lastModified = arquivo.getAttribute("last_modified");
			GregorianCalendar lastDate = strToDate(lastModified);

			String sufixo = sfxFile + String.valueOf(lastDate.getTimeInMillis()); 

			String ext = arquivo.getAttribute("content_type");
			String type = ext;
			if (ext == null || ext.length() == 0 || ext.equals("application/octet-stream")) {
				ext = ".pdf";
				type = "application/pdf";
			}
			else if (ext.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
				ext = ".docx";
			else if (ext.equals("application/vnd.oasis.opendocument.text"))
				ext = ".odt";
			else if (ext.equals("application/msword"))
				ext = ".doc";
			else {
				ext = ".pdf";
				type = "application/pdf";
			} 

			File file = new File(SaplActivity.uriFileBase+"/"+el.getAttribute("cod_materia")+sufixo+ext);


			return file.exists();



		} catch (Exception e) { 
		}

		return false;

	}

	public static boolean downloadFileMaterias(final Element el, final String pathFile, 
			final String _sfxFile, boolean openFile) {

		boolean result = true;

		final String sfxFile = (_sfxFile == null ? "_texto_integral" : _sfxFile);			

		
		Element arquivo = (Element) el.getElementsByTagName("arquivo").item(0); 

		String url = SaplActivity.urlDocsBase+pathFile+el.getAttribute("cod_materia")+sfxFile;

		try {

			String lastModified = arquivo.getAttribute("last_modified");
			GregorianCalendar lastDate = strToDate(lastModified);

			String sufixo = sfxFile + String.valueOf(lastDate.getTimeInMillis()); 

			String ext = arquivo.getAttribute("content_type");
			String type = ext;
			if (ext == null || ext.length() == 0 || ext.equals("application/octet-stream")) {
				ext = ".pdf";
				type = "application/pdf";
			}
			else if (ext.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
				ext = ".docx";
			else if (ext.equals("application/vnd.oasis.opendocument.text"))
				ext = ".odt";
			else if (ext.equals("application/msword"))
				ext = ".doc";
			else {
				ext = ".pdf";
				type = "application/pdf";
			} 

			File file = new File(SaplActivity.uriFileBase+"/"+el.getAttribute("cod_materia")+sufixo+ext);



			if (!file.exists()) {
				Utils.downloadFileHttpGet(url, SaplActivity.uriFileBase+"/"+el.getAttribute("cod_materia")+sufixo+ext);
				Log.i("CMJ", "downloadFileMaterias: "+el.getAttribute("cod_materia"));
			}
			else 
				result = false;

			file = new File(SaplActivity.uriFileBase+"/"+el.getAttribute("cod_materia")+sufixo+ext);

			//String type = Utils.getType(file);


			if (!openFile)
				return result;


			Intent it = new Intent();
			it.setAction(android.content.Intent.ACTION_VIEW);
			it.setDataAndType(Uri.fromFile(file), type);
			try {
				SaplActivity.saplActivity.startActivity(it);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}	
	
	
	public static int contarArquivosParaBaixar( NodeList nlMatExp, NodeList nlMatOrdem) {
		
		

		int arquivosParaBaixar = nlMatExp.getLength() + nlMatOrdem.getLength();
		for (int i = 0; i < nlMatExp.getLength(); i++) {
			Element el = (Element) nlMatExp.item(i);
			if (Utils.existFileMateria(el, "materia/", null))
				arquivosParaBaixar--;
			
			arquivosParaBaixar += contarArquivosParaBaixarParaMatAnexada(el);
		}

		for (int i = 0; i < nlMatOrdem.getLength(); i++) {
			Element el = (Element) nlMatOrdem.item(i);
			if (Utils.existFileMateria(el, "materia/", null))
				arquivosParaBaixar--;	
			arquivosParaBaixar += contarArquivosParaBaixarParaMatAnexada(el);			
		} 
		
		return arquivosParaBaixar;
	}

	private static int contarArquivosParaBaixarParaMatAnexada(Element el) {

		NodeList nlMatAnexadas = el.getElementsByTagName("materias-anexadas");
        int arquivosParaBaixar = 0;
		if (nlMatAnexadas.getLength() == 1) {
			Element elMatAnex = (Element) nlMatAnexadas.item(0);
			nlMatAnexadas = elMatAnex.getElementsByTagName("matanex");

			if (nlMatAnexadas.getLength() == 0) 
				return 0;


			for (int i = 0; i < nlMatAnexadas.getLength(); i++) {

				elMatAnex = (Element) nlMatAnexadas.item(i);
				if (!Utils.existFileMateria(el, "materia/", null))
					arquivosParaBaixar++;
			}
		}
		return arquivosParaBaixar;
	}
}
