package ee.adit.stateportal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ee.adit.pojo.EmailAddress;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusDocument;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusResponseDocument;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusDocument.TellimusteStaatus;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusResponseDocument.TellimusteStaatusResponse.Keha.Suunamised.Suunamine;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusResponseDocument.TellimusteStaatusResponse.Keha.TkalTeenused.TkalTeenus;
import ee.riik.xtee.riigiportaal.producers.producer.riigiportaal.TellimusteStaatusResponseDocument.TellimusteStaatusResponse.Keha.TkalTeenused.TkalTeenus.EpostStaatus;
import ee.webmedia.xtee.client.service.SimpleXTeeServiceConfiguration;
import ee.webmedia.xtee.client.service.StandardXTeeConsumer;

public class StatePortalClient {
	private static Logger LOG = Logger.getLogger(StatePortalClient.class);
	private static int RESULT_OK = 0;
	
	public static NotificationStatus getNotificationStatus(String userCode, String eventTypeName) {
		NotificationStatus result = null;
		try {
			TellimusteStaatusDocument doc = TellimusteStaatusDocument.Factory.newInstance(); 
			TellimusteStaatus req = doc.addNewTellimusteStaatus();
			TellimusteStaatusDocument.TellimusteStaatus.Keha keha = req.addNewKeha();
			
			// Remove country prefix because teavituskalender does not support
			// ID codes beginning with country prefix
			String fixedUserCode = "";
			if (userCode != null) {
				for (int i = 0; i < userCode.length(); i++) {
					if ("0123456789".contains(String.valueOf(userCode.charAt(i)))) {
						fixedUserCode = userCode.substring(i);
						break;
					}
				}
			}
			keha.setIsikukood(fixedUserCode);
			
			ClassPathXmlApplicationContext ctx = null;
			try {
				ctx = startContext();
				StandardXTeeConsumer xteeService = (StandardXTeeConsumer) ctx.getBean("xteeConsumer");
				SimpleXTeeServiceConfiguration conf = (SimpleXTeeServiceConfiguration) xteeService.getServiceConfiguration();
				conf.setDatabase("riigiportaal");
				conf.setMethod("tellimusteStaatus");
				conf.setVersion("v1");
				TellimusteStaatusResponseDocument ret = (TellimusteStaatusResponseDocument) xteeService.sendRequest(doc, conf);
				
				if (ret != null) {
					if (ret.getTellimusteStaatusResponse() != null) {
						if (ret.getTellimusteStaatusResponse().getKeha() != null) {
							if (ret.getTellimusteStaatusResponse().getKeha().getResult() != null) {
								BigInteger resultCode = ret.getTellimusteStaatusResponse().getKeha().getResult().getResultCode();
								String resultMessage = ret.getTellimusteStaatusResponse().getKeha().getResult().getResultText();
								LOG.debug("TellimusteStaatus result code: " + ((resultCode == null) ? "NULL" : resultCode.toString()));
								LOG.debug("TellimusteStaatus result message: " + resultMessage);
								
								if ((resultCode != null) && (resultCode.intValue() == RESULT_OK)) {
									result = new NotificationStatus();
									result.setNotificationTypeName(eventTypeName);
									if (ret.getTellimusteStaatusResponse().getKeha().getTkalTeenused() != null) {
										List<TkalTeenus> notifications = ret.getTellimusteStaatusResponse().getKeha().getTkalTeenused().getTkalTeenusList();
										for (TkalTeenus item : notifications) {
											if (eventTypeName.equalsIgnoreCase(item.getNimetus())) {
												result.setNotificationEmailStatus(item.getEpostStaatus() != EpostStaatus.EI);
												break;
											}
										}
									} else {
										LOG.debug("Notifications list is NULL!");
									}
									
									if (ret.getTellimusteStaatusResponse().getKeha().getSuunamised() != null) {
										result.setEmailList(new ArrayList<EmailAddress>());
										List<Suunamine> emails = ret.getTellimusteStaatusResponse().getKeha().getSuunamised().getSuunamineList();
										for (Suunamine item : emails) {
											EmailAddress address = new EmailAddress();
											address.setAddress(item.getEpost());
											address.setRedirectedTo(item.getAadress());
											result.getEmailList().add(address);
										}
									}
									
									LOG.debug("Successfully retreived notification status from 'riigiportaal' database. Related user: " + userCode);
								}
							} else {
								LOG.error("Error getting notification status from 'riigiportaal' database. Response's 'tulemus' part is NULL. Related user: " + userCode);
							}
						} else {
							LOG.error("Error getting notification status from 'riigiportaal' database. Response's 'keha' part is NULL. Related user: " + userCode);
						}
					} else {
						LOG.error("Error getting notification status from 'riigiportaal' database. Response's 'LisaSyndmusResponse' part is NULL. Related user: " + userCode);
					}
				} else {
					LOG.error("Error getting notification status from 'riigiportaal' database. Response document is NULL. Related user: " + userCode);
				}
			} finally {
				if (ctx != null) {
					ctx.close();
				}
			}
		} catch (Exception ex) {
			LOG.error("Error getting notification status from 'riigiportaal' database. Related user: " + userCode, ex);
		}
		return result;
	}
	
	public static ClassPathXmlApplicationContext startContext() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("xtee.xml");
		ctx.start();
		return ctx;
	}
}