package com;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Function {
	private static CloseableHttpClient client = HttpClients.custom().build();

	// public static Logger logger = Logger.getLogger(Function.class);

	public static void main(String[] args) {
		// List<Cookie> cookies = ((AbstractHttpClient)
		// client).getCookieStore().getCookies();
		Login();
	}

	public static void Login() {
		HttpResponse getMethodResponse = getResponse("http://www.zhihu.com/",
				null, null);
		DownloadVerifyCode();
		Scanner scanner = new Scanner(System.in);
		HttpResponse postMethodResponse = null;
		Document doc = null;
		String captcha;
		System.out.println("go to the dir to see the verify.jpg");
		captcha = scanner.next();
		List<NameValuePair> list = new ArrayList<>();
		try {
			doc = Jsoup.parse(EntityUtils.toString(getMethodResponse
					.getEntity()));
			String _xsrf = doc.select("input[name$=_xsrf]").first()
					.attr("value");
			list.add(new BasicNameValuePair("_xsrf", _xsrf));
			list.add(new BasicNameValuePair("email", "xxxxx"));
			list.add(new BasicNameValuePair("password", "xxxxxx"));
			list.add(new BasicNameValuePair("captcha", captcha));
			list.add(new BasicNameValuePair("remember_me", "true"));
			postMethodResponse = getResponse(
					"http://www.zhihu.com/login/email", list, null);
			System.out.println("post method statuscode is "
					+ postMethodResponse.getStatusLine().getStatusCode());
			System.out.println("response content is "
					+ EntityUtils.toString(postMethodResponse.getEntity()));
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		scanner.close();
	}

	static void DownloadVerifyCode() {
		// HttpGet getimg = new HttpGet(
		// "http://www.zhihu.com/captcha.gif?r=" + String.valueOf(new
		// Random().nextLong()).substring(0, 13));
		RequestConfig config = RequestConfig.custom().setConnectTimeout(6000)
				.setSocketTimeout(6000)
				.setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
		HttpGet getimg = new HttpGet("http://www.zhihu.com/captcha.gif?r="
				+ System.currentTimeMillis());
		getimg.setConfig(config);
		try {
			HttpResponse response = client.execute(getimg);
			System.out.println("verify.gif "
					+ response.getStatusLine().getStatusCode());
			InputStream in = response.getEntity().getContent();
			File verifyCode = new File("veryfileCode.gif");
			if (!verifyCode.exists()) {
				verifyCode.createNewFile();
				verifyCode.deleteOnExit();
			}
			FileOutputStream fo = new FileOutputStream(verifyCode);
			byte[] tmpBuf = new byte[1024];
			int bufLen = 0;
			// long downloadedSize = 0;
			while ((bufLen = in.read(tmpBuf)) > 0) {
				fo.write(tmpBuf, 0, bufLen);
				// downloadedSize += bufLen;
			}
			fo.close();
			// getimg.abort();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			getimg.abort();
		}

	}

	/**
	 * this is a method for execute a get or post;if you want to execute a get
	 * method ,make the param list to be null;default timeout period is 6000ms.
	 * if you want to add some yourself headers,the third param is for u;else
	 * make it be null;
	 * 
	 * @author Mercy
	 */
	public static HttpResponse getResponse(String url,
			List<NameValuePair> list, Header[] headers) {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(6000)
				.setSocketTimeout(6000)
				.setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
		HttpResponse response = null;
		HttpUriRequest request = null;
		if (list == null) {
			HttpGet get = new HttpGet(url);
			get.setConfig(config);
			request = get;
		} else {
			HttpPost post = new HttpPost(url);
			post.setConfig(config);
			HttpEntity entity = null;
			try {
				entity = new UrlEncodedFormEntity(list, "utf-8");
				post.setEntity(entity);
				request = post;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		request.setHeaders(headers);
		try {
			System.out.println("ready to link " + url);
			response = client.execute(request);
			System.out.println("status code is "
					+ response.getStatusLine().getStatusCode());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}
}
