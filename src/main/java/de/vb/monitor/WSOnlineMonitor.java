package de.vb.monitor;

import java.io.*;
import java.net.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@SpringBootApplication
@EnableScheduling
public class WSOnlineMonitor {
	@ExceptionHandler(ConnectException.class)
	public String handleNoInternet() {
		return "err_io"; // the name of your error page
	}
	@ExceptionHandler(ParserConfigurationException.class)
	public String handleNotParsableResponse() {
		return "err_parsing"; // the name of your error page
	}
		private static final String[] ENDPOINTS = {
			"https://vbtest3.volkswohl-bund.de/vbl/ws/taa/2022-10/bipro/VBLService_2.4.6.1.12",
			"https://vbtest3.volkswohl-bund.de/vbl/ws/taa/2022-10/bipro/VBLService_2.4.6.1.12"
	};
	public static Properties appProps = new Properties();
	private static final Logger log = LoggerFactory.getLogger(WSOnlineMonitor.class);
	private static List<Long> responseTimes = new ArrayList<>();
	public static void main(String[] args) {
		init();
		SpringApplication.run(WSOnlineMonitor.class, args);
		log.info("Started...");
	}

	public static void init() {
		try (FileInputStream fileInputStream = new FileInputStream("app.properties")) {
			appProps.load(fileInputStream);
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		log.info("Properties read.");
	}




	@Scheduled(fixedRate = 10000) // trigger new run all 20 seconds
	private static void performTestRequests() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		log.info("\nStarting new run at " + dtf.format(now));
		new Thread(() -> {
				try {
					Path startPath = Paths.get("C:\\Projects\\moni1\\testdata");
					Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							if (file.toString().endsWith(".xml")) {
								String fileContent = new String(Files.readAllBytes(file));
								for (String endpoint : ENDPOINTS) {
									new Thread(() -> {
										ResponseDTO rdto = new ResponseDTO();
										try {
											rdto = sendPostRequest(endpoint, fileContent );

											SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											String timestamp = simpleDateFormat.format(new Date());

											InputSource src = new InputSource();
											src.setCharacterStream(new StringReader(rdto.getrBody()));
											String vbKennung = parseResponseBody(rdto.getrBody(), "pm:Kennung");
											String vbStatusID = parseResponseBody(rdto.getrBody(), "nachr:StatusID");

											appendToCSVFile(endpoint, rdto, timestamp, vbKennung, vbStatusID);

											log.info(String.valueOf(rdto.getrTime() + " - " + vbKennung + " : " + vbStatusID + " - " + endpoint.substring(0,20)));
										} catch (IOException | ParserConfigurationException e) {
											e.printStackTrace();
											log.error("No connection to endpoint or not parsable response");
										} catch (Exception e) {
											//System.err.println(rdto.getrBody());
											log.error(e.getMessage());
											throw new RuntimeException(e);
										}
									}).start();
								}
							}
							return FileVisitResult.CONTINUE;
						}
					});
					Thread.sleep(Long.parseLong(WSOnlineMonitor.appProps.getProperty("monitor.repeat.ms")));
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
		}).start();
	}
	private static ResponseDTO sendPostRequest(String urlString, String requestBody) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");

		OutputStream os = null;
		try {
			os = con.getOutputStream();
		} catch (Exception e) {
			log.error("no connection to endpoint");
		}
		os.write(requestBody.getBytes());
		os.flush();
		os.close();

		long startTime = System.currentTimeMillis();
		int responseCode = 0;
		try {
			responseCode = con.getResponseCode();
		} catch (SocketException e) {
			log.error("Now Response from server? " + e.getMessage());
			return null;
		}

		long endTime = System.currentTimeMillis();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder respBody = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			respBody.append(inputLine);
		}
		in.close();
		// System.out.println("Endpoint: " + urlString);
		// System.out.println(responseCode + ", Response time: " + (endTime - startTime) + " milliseconds");
		ResponseDTO rdto = new ResponseDTO(respBody.toString(), responseCode, (endTime-startTime));
		if ((endTime - startTime) > Long.parseLong(WSOnlineMonitor.appProps.getProperty("monitor.response.max"))) {
			//System.out.println("sending notification mail");
			SendEmail.sendNotificationMail(WSOnlineMonitor.appProps,"WS langsam: "+ (endTime - startTime) + " ms", "Response time: " + (endTime - startTime) + " milliseconds");
		}
		responseTimes.add(rdto.getrTime());
		return rdto;
	}

	private static String parseResponseBody(String response, String tagname) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(response)));

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		DOMSource source = new DOMSource(document);
		transformer.transform(source, result);


		NodeList nodes = document.getElementsByTagName(tagname);
		String wert = nodes.item(0).getTextContent();
		return wert;
	}
	private static void appendToCSVFile(String endpoint, ResponseDTO rdto, String timestamp, String vbKennung, String vbStatusID) throws IOException {
		FileWriter fileWriter = new FileWriter("response_data.csv", true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(timestamp + ";" + endpoint + ";" + vbStatusID + ";" + vbKennung + ";" + rdto.getrTime() + "\n");
		bufferedWriter.close();
	}


}
