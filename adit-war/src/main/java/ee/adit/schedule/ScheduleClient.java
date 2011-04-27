package ee.adit.schedule;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import ee.adit.dao.pojo.AditUser;
import ee.adit.dao.pojo.Notification;
import ee.adit.exception.AditInternalException;
import ee.adit.pojo.notification.LisaSyndmusRequest;
import ee.adit.pojo.notification.LisaSyndmusRequestKasutaja;
import ee.adit.pojo.notification.LisaSyndmusRequestLugejad;
import ee.adit.pojo.notification.LisaSyndmusResponse;
import ee.adit.service.UserService;
import ee.adit.util.Configuration;
import ee.adit.util.CustomXTeeResponseSanitizerInterceptor;
import ee.adit.util.SchedulerSoapArrayInterceptor;
import ee.adit.util.Util;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusResponseDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.OtsiKasutajadDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.OtsiKasutajadResponseDocument;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Lugejad;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.SyndmuseTyyp;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Tahtsus;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Lugejad.Kasutaja;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.LisaSyndmusDocument.LisaSyndmus.Keha.Lugejad.Kasutaja.KasutajaTyyp;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.OtsiKasutajadDocument.OtsiKasutajad;
import ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.OtsiKasutajadDocument.OtsiKasutajad.Keha.Kasutajad;
import ee.webmedia.xtee.client.service.SimpleXTeeServiceConfiguration;
import ee.webmedia.xtee.client.service.StandardXTeeConsumer;
import ee.webmedia.xtee.client.util.WSConsumptionLoggingInterceptor;
import ee.webmedia.xtee.client.util.XTeeResponseSanitizerInterceptor;

/**
 * Web service client class for notification calendar (teavituskalender) X-Road
 * database. Enables execution of notification calendar web service requests.
 * 
 * @author Jaak Lember, Interinx, jaak@interinx.com
 */
public class ScheduleClient {
    
    private static Logger logger = Logger.getLogger(ScheduleClient.class);
    
    private static final int RESULT_OK = 0;

    /**
     * Notification type SEND 
     */
    public static final String NOTIFICATION_TYPE_SEND = "send";
    
    /**
     * Notification type SHARE 
     */
    public static final String NOTIFICATION_TYPE_SHARE = "share";
    
    /**
     * Notification type VIEW 
     */
    public static final String NOTIFICATION_TYPE_VIEW = "view";
    
    /**
     * Notification type MODIFY 
     */
    public static final String NOTIFICATION_TYPE_MODIFY = "modify";
    
    /**
     * Notification type SIGN 
     */
    public static final String NOTIFICATION_TYPE_SIGN = "sign";

    /**
     * Notification priority LOW 
     */
    public static final String NOTIFICATION_PRIORITY_LOW = "madal";
    
    /**
     * Notification priority MEDIUM 
     */
    public static final String NOTIFICATION_PRIORITY_MEDIUM = "keskmine";
    
    /**
     * Notification priority HIGH 
     */
    public static final String NOTIFICATION_PRIORITY_HIGH = "korge";

    /**
     * Notification type type
     */
    public static final String NOTIFICATION_TYPE_TEAVITUSKALENDER_LIIGIS = "liigis";
    
    /**
     * Notification type OBLIGATORY
     */
    public static final String NOTIFICATION_TYPE_TEAVITUSKALENDER_KOHUSTUSLIK = "kohustuslik";

    /**
     * Notification user type PERSON
     */
    public static final String NOTIFICATION_USER_PERSON = "isik";
    
    /**
     * Notification user type OFFICIAL
     */
    public static final String NOTIFICATION_USER_OFFICIAL = "ametnik";
    
    /**
     * Notification user type INSTITUTION
     */
    public static final String NOTIFICATION_USER_INSTITUTION = "asutus";

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private Configuration configuration;

    /**
     * Adds a notification to "teavituskalender" X-Road database. <br>
     * <br>
     * This method intentionally throws no exception when failing. Periodic
     * attempts will be made to send notifications notifications that could not
     * be sent in previous attempts.
     * 
     * @param notification
     *            Local {@link Notification} object that needs to be sent to
     *            "teavituskalender" database.
     * @param eventType
     *            Name of notifications type in "teavituskalender" database
     * @param userService
     *            Current {@link UserService} instance
     * @return Notification ID in "teavituskalender" database
     */
    public long addEvent(Notification notification, final String eventType, final UserService userService) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(notification.getEventDate());
        AditUser eventOwner = userService.getUserByID(notification.getUserCode());
        return addEvent(notification.getId(), eventOwner, notification.getNotificationText(), eventType, cal,
                notification.getNotificationType(), notification.getDocumentId(), userService);
    }

    /**
     * Adds a notification to "teavituskalender" X-Road database. <br>
     * <br>
     * This method intentionally throws no exception when failing. Periodic
     * attempts will be made to send notifications notifications that could not
     * be sent in previous attempts.
     * 
     * @param eventOwner
     *            {@link AditUser} to whom this notification will be sent
     * @param eventText
     *            Notification text
     * @param eventType
     *            Name of notifications type in "teavituskalender" database
     * @param eventDate
     *            Date and time describing when the notified event occurred (for
     *            example, when a document was signed)
     * @param notificationType
     *            Code of notification type in local database. This type is more
     *            fine grained than the type in "teavituskalender" database
     * @param relatedDocumentId
     *            ID of the document this notification is about
     * @param userService
     *            Current {@link UserService} instance
     * @return Notification ID in "teavituskalender" database
     */
    public long addEvent(final AditUser eventOwner, final String eventText, final String eventType,
            final Calendar eventDate, final String notificationType, final long relatedDocumentId,
            final UserService userService) {

        return addEvent(0, eventOwner, eventText, eventType, eventDate, notificationType, relatedDocumentId,
                userService);
    }

    /**
     * Adds a notification to "teavituskalender" X-Road database. <br>
     * <br>
     * This method intentionally throws no exception when failing. Periodic
     * attempts will be made to send notifications notifications that could not
     * be sent in previous attempts.
     * 
     * @param notificationId
     *            Notification ID in local database
     * @param eventOwner
     *            {@link AditUser} to whom this notification will be sent
     * @param eventText
     *            Notification text
     * @param eventType
     *            Name of notifications type in "teavituskalender" database
     * @param eventDate
     *            Date and time describing when the notified event occurred (for
     *            example, when a document was signed)
     * @param notificationType
     *            Code of notification type in local database. This type is more
     *            fine grained than the type in "teavituskalender" database
     * @param relatedDocumentId
     *            ID of the document this notification is about
     * @param userService
     *            Current {@link UserService} instance
     * @return Notification ID in "teavituskalender" database
     */
    public static long addEvent(final long notificationId, final AditUser eventOwner, final String eventText,
            final String eventType, final Calendar eventDate, final String notificationType,
            final long relatedDocumentId, final UserService userService) {
        long eventId = 0;

        logger.debug("AddEvent with XteeBeans invoked...");

        try {

            logger.debug("Setting messageFactory for addEvent call.");
            System.setProperty("javax.xml.soap.MessageFactory", "weblogic.xml.saaj.MessageFactoryImpl");

            LisaSyndmusDocument doc = LisaSyndmusDocument.Factory.newInstance();
            LisaSyndmus req = doc.addNewLisaSyndmus();
            LisaSyndmusDocument.LisaSyndmus.Keha keha = req.addNewKeha();

            // Event is not visible to ADIT system user in portal.
            keha.setNahtavOmanikule(false);

            keha.setKirjeldus(eventText);
            keha.setTahtsus(Tahtsus.KESKMINE);
            keha.setSyndmuseTyyp(SyndmuseTyyp.LIIGIS);
            keha.setLiik(eventType);

            Lugejad recipients = keha.addNewLugejad();
            Kasutaja recipient = recipients.addNewKasutaja();
            // Remove country prefix because teavituskalender does not support
            // ID codes beginning with country prefix
            String userCode = eventOwner.getUserCode();
            recipient.setKood(Util.getPersonalIdCodeWithoutCountryPrefix(userCode));
            if (UserService.USERTYPE_PERSON.equalsIgnoreCase(eventOwner.getUsertype().getShortName())) {
                recipient.setKasutajaTyyp(KasutajaTyyp.ISIK);
            } else {
                recipient.setKasutajaTyyp(KasutajaTyyp.ASUTUS);
            }

            // Start and end times
            keha.setAlgus(eventDate);
            keha.setLopp(eventDate);
            
            // Start and end times of notification displaying
            Calendar displayEndDate = eventDate;
            displayEndDate.add(Calendar.DAY_OF_MONTH, 7);
            keha.setTeavitusAlgus(eventDate);
            keha.setTeavitusLopp(displayEndDate);

            ClassPathXmlApplicationContext ctx = null;
            try {
                ctx = startContext();
                StandardXTeeConsumer xteeService = (StandardXTeeConsumer) ctx.getBean("xteeConsumer");
                SimpleXTeeServiceConfiguration conf = (SimpleXTeeServiceConfiguration) xteeService
                        .getServiceConfiguration();
                conf.setDatabase("teavituskalender");
                conf.setMethod("lisaSyndmus");
                conf.setVersion("v1");

                SchedulerSoapArrayInterceptor ai = new SchedulerSoapArrayInterceptor();
                XTeeResponseSanitizerInterceptor ci = new XTeeResponseSanitizerInterceptor();
                WSConsumptionLoggingInterceptor li = new WSConsumptionLoggingInterceptor();

                xteeService.getWebServiceTemplate().setInterceptors(new ClientInterceptor[] {ai, ci, li });
                LisaSyndmusResponseDocument ret = (LisaSyndmusResponseDocument) xteeService.sendRequest(doc, conf);

                if (ret != null) {
                    if (ret.getLisaSyndmusResponse() != null) {
                        if (ret.getLisaSyndmusResponse().getKeha() != null) {
                            BigInteger resultEventId = ret.getLisaSyndmusResponse().getKeha().getSyndmusId();
                            logger.debug("LisaSyndmus result event ID: "
                                    + ((resultEventId == null) ? "NULL" : resultEventId.toString()));

                            if (ret.getLisaSyndmusResponse().getKeha().getTulemus() != null) {
                                BigInteger resultCode = ret.getLisaSyndmusResponse().getKeha().getTulemus().getTulemuseKood();
                                String resultMessage = ret.getLisaSyndmusResponse().getKeha().getTulemus().getTulemuseTekst();
                                logger.debug("LisaSyndmus result code: "
                                        + ((resultCode == null) ? "NULL" : resultCode.toString()));
                                logger.debug("LisaSyndmus result message: " + resultMessage);

                                if ((resultCode != null) && (resultCode.intValue() == RESULT_OK)) {
                                    eventId = resultEventId.longValue();
                                    logger.debug("Successfully added notification to 'teavituskalender' database. Related document ID: "
                                    	+ String.valueOf(relatedDocumentId));
                                } else {
                                	logger.error("Error adding notification to 'teavituskalender' database. Result code: "
                                		+ String.valueOf(resultCode) + ", result message: "+ resultMessage +", event ID: " + String.valueOf(resultEventId));
                                }
                            } else {
                                logger.error("Error adding notification to 'teavituskalender' database. Response's 'tulemus' part is NULL. Related document ID: "
                                	+ String.valueOf(relatedDocumentId));
                            }
                        } else {
                            logger.error("Error adding notification to 'teavituskalender' database. Response's 'keha' part is NULL. Related document ID: "
                            	+ String.valueOf(relatedDocumentId));
                        }
                    } else {
                        logger.error("Error adding notification to 'teavituskalender' database. Response's 'LisaSyndmusResponse' part is NULL. Related document ID: "
                                        + String.valueOf(relatedDocumentId));
                    }
                } else {
                    logger.error("Error adding notification to 'teavituskalender' database. Response document is NULL. Related document ID: "
                                    + String.valueOf(relatedDocumentId));
                }
            } finally {
                if (ctx != null) {
                    ctx.close();
                }
            }
        } catch (Exception ex) {
            logger.error("Error adding notification to 'teavituskalender' database. Related document ID: "
                    + String.valueOf(relatedDocumentId), ex);
        }

        // Save notification to database
        if (eventId > 0) {
            try {
                userService.addNotification(notificationId, relatedDocumentId, notificationType, eventOwner
                        .getUserCode(), eventDate.getTime(), eventText, eventId, Calendar.getInstance().getTime());
            } catch (Exception ex) {
                logger.error("Failed saving successfully sent notification.", ex);
            }
        } else {
            try {
                userService.addNotification(notificationId, relatedDocumentId, notificationType, eventOwner
                        .getUserCode(), eventDate.getTime(), eventText, null, null);
            } catch (Exception ex) {
                logger.error("Failed saving unsent notification.", ex);
            }
        }

        return eventId;
    }

//    /*
//     * public long addEvent( final long notificationId, final AditUser
//     * eventOwner, final String eventText, final String eventType, final
//     * Calendar eventDate, final String notificationType, final long
//     * relatedDocumentId, final UserService userService) { return addEvent1(
//     * notificationId, eventOwner, eventText, eventType, eventDate,
//     * notificationType, relatedDocumentId, userService ); }
//     */
//
//    /**
//     * Adds a notification to "teavituskalender" X-Road database. <br>
//     * <br>
//     * This method intentionally throws no exception when failing. Periodic
//     * attempts will be made to send notifications notifications that could not
//     * be sent in previous attempts.
//     * 
//     * @param notificationId
//     *            Notification ID in local database
//     * @param eventOwner
//     *            {@link AditUser} to whom this notification will be sent
//     * @param eventText
//     *            Notification text
//     * @param eventType
//     *            Name of notifications type in "teavituskalender" database
//     * @param eventDate
//     *            Date and time describing when the notified event occurred (for
//     *            example, when a document was signed)
//     * @param notificationType
//     *            Code of notification type in local database. This type is more
//     *            fine grained than the type in "teavituskalender" database
//     * @param relatedDocumentId
//     *            ID of the document this notification is about
//     * @param userService
//     *            Current {@link UserService} instance
//     * @return Notification ID in "teavituskalender" database
//     */
//    public long addEvent1(final long notificationId, final AditUser eventOwner, final String eventText,
//            final String eventType, final Calendar eventDate, final String notificationType,
//            final long relatedDocumentId, final UserService userService) {
//        long eventId = 0;
//
//        logger.debug("AddEvent with castor invoked...");
//
//        try {
//
//            logger.debug("Setting messageFactory for addEvent call.");
//            System.setProperty("javax.xml.soap.MessageFactory", "weblogic.xml.saaj.MessageFactoryImpl");
//
//            LisaSyndmusRequest request = new LisaSyndmusRequest();
//            request.setNahtavOmanikule(false);
//            request.setKirjeldus(eventText);
//            request.setTahtsus(NOTIFICATION_PRIORITY_MEDIUM);
//            request.setSyndmuseTyyp(NOTIFICATION_TYPE_TEAVITUSKALENDER_LIIGIS);
//            request.setLiik(eventType);
//
//            LisaSyndmusRequestLugejad lugejad = new LisaSyndmusRequestLugejad();
//
//            ArrayList<LisaSyndmusRequestKasutaja> kasutajad = new ArrayList<LisaSyndmusRequestKasutaja>();
//
//            LisaSyndmusRequestKasutaja kasutaja = new LisaSyndmusRequestKasutaja();
//
//            String userCode = eventOwner.getUserCode();
//            kasutaja.setKood(Util.getPersonalIdCodeWithoutCountryPrefix(userCode));
//
//            if (UserService.USERTYPE_PERSON.equalsIgnoreCase(eventOwner.getUsertype().getShortName())) {
//                kasutaja.setKasutajaTyyp(NOTIFICATION_USER_PERSON);
//            } else {
//                kasutaja.setKasutajaTyyp(NOTIFICATION_USER_INSTITUTION);
//            }
//
//            kasutajad.add(kasutaja);
//            lugejad.setKasutajad(kasutajad);
//            lugejad.setType("ns5:kasutaja" + '[' + kasutajad.size() + ']');
//            lugejad.setXsiType("SOAP-ENC:Array");
//
//            request.setLugejad(lugejad);
//
//            request.setAlgus(Util.dateToXMLDatePart(eventDate.getTime()));
//            request.setLopp(Util.dateToXMLDatePart(eventDate.getTime()));
//
//            try {
//
//                System.setProperty("weblogic.webservice.i18n.charset", "utf-8");
//
//                WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
//                webServiceTemplate.setMarshaller(getMarshaller());
//                webServiceTemplate.setUnmarshaller(getUnmarshaller());
//
//                // Interceptors
//                ClientInterceptor ci = new CustomXTeeResponseSanitizerInterceptor();
//                WSConsumptionLoggingInterceptor li = new WSConsumptionLoggingInterceptor();
//                webServiceTemplate.setInterceptors(new ClientInterceptor[] {li, ci });
//
//                // SaajSoapMessageFactory messageFactory = new
//                // SaajSoapMessageFactory(MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));
//                // SaajSoapMessage message = (SaajSoapMessage)
//                // messageFactory.createWebServiceMessage();
//
//                String xteeInstitution = getConfiguration().getXteeInstitution();
//                String xteeSecurityServer = getConfiguration().getXteeSecurityServer();
//                String xteeDatabase = "teavituskalender";
//                String xteeMethod = "lisaSyndmus";
//                String xteeVersion = "v1";
//                String xteeIdCode = getConfiguration().getXteeIdCode();
//
//                SimpleXTeeServiceConfiguration xTeeServiceConfiguration = new SimpleXTeeServiceConfiguration();
//                xTeeServiceConfiguration.setDatabase(xteeDatabase);
//                xTeeServiceConfiguration.setIdCode(xteeIdCode);
//                xTeeServiceConfiguration.setInstitution(xteeInstitution);
//                xTeeServiceConfiguration.setMethod(xteeMethod);
//                xTeeServiceConfiguration.setVersion(xteeVersion);
//                xTeeServiceConfiguration.setSecurityServer(xteeSecurityServer);
//
//                StandardXTeeConsumer customXTeeConsumer = new StandardXTeeConsumer();
//                customXTeeConsumer.setWebServiceTemplate(webServiceTemplate);
//                customXTeeConsumer.setServiceConfiguration(xTeeServiceConfiguration);
//
//                logger.error("Invoking notifications service...");
//                LisaSyndmusResponse response = (LisaSyndmusResponse) customXTeeConsumer.sendRequest(request);
//                logger.error("Notifications service returned response: " + response);
//
//                if (response != null) {
//                        Integer resultEventId = response.getSyndmusId();
//                        logger.debug("LisaSyndmus result event ID: "
//                                + ((resultEventId == null) ? "NULL" : resultEventId.toString()));
//
//                        if (response.getTulemus() != null) {
//                            Integer resultCode = response.getTulemus().getTulemuseKood();
//                            String resultMessage = response.getTulemus().getTulemuseTekst();
//                            logger.debug("LisaSyndmus result code: "
//                                    + ((resultCode == null) ? "NULL" : resultCode.toString()));
//                            logger.debug("LisaSyndmus result message: " + resultMessage);
//
//                            if ((resultCode != null) && (resultCode.intValue() == RESULT_OK)) {
//                                eventId = resultEventId.longValue();
//                                logger.debug("Successfully added notification to 'teavituskalender' database. Related document ID: "
//                                                + String.valueOf(relatedDocumentId));
//                            }
//                        } else {
//                            logger.error("Error adding notification to 'teavituskalender' database. Response's 'tulemus' part is NULL. Related document ID: "
//                                            + String.valueOf(relatedDocumentId));
//                        }
//                } else {
//                    throw new AditInternalException(
//                            "The 'getDocument' request was not successful: response could not be unmarshalled: unmarshalling returned null.");
//                }
//
//            } catch (Exception e) {
//                logger.error("Error while sending notifications: ", e);
//            }
//
//        } catch (Exception ex) {
//            logger.error("Error adding notification to 'teavituskalender' database. Related document ID: "
//                    + String.valueOf(relatedDocumentId), ex);
//        }
//
//        // Save notification to database
//        if (eventId > 0) {
//            try {
//                userService.addNotification(notificationId, relatedDocumentId, notificationType, eventOwner
//                        .getUserCode(), eventDate.getTime(), eventText, eventId, Calendar.getInstance().getTime());
//            } catch (Exception ex) {
//                logger.error("Failed saving successfully sent notification.", ex);
//            }
//        } else {
//            try {
//                userService.addNotification(notificationId, relatedDocumentId, notificationType, eventOwner
//                        .getUserCode(), eventDate.getTime(), eventText, null, null);
//            } catch (Exception ex) {
//                logger.error("Failed saving unsent notification.", ex);
//            }
//        }
//
//        return eventId;
//
//    }
//
//    /**
//     * Adds a notification to "teavituskalender" X-Road database. <br>
//     * <br>
//     * This method intentionally throws no exception when failing. Periodic
//     * attempts will be made to send notifications notifications that could not
//     * be sent in previous attempts.
//     * 
//     * @param notificationId
//     *            Notification ID in local database
//     * @param eventOwner
//     *            {@link AditUser} to whom this notification will be sent
//     * @param eventText
//     *            Notification text
//     * @param eventType
//     *            Name of notifications type in "teavituskalender" database
//     * @param eventDate
//     *            Date and time describibg when the notified event occured (for
//     *            example, when a document was signed)
//     * @param notificationType
//     *            Code of notification type in local database. This type is more
//     *            fine grained than the type in "teavituskalender" database
//     * @param relatedDocumentId
//     *            ID of the document this notification is about
//     * @param userService
//     *            Current {@link UserService} instance
//     * @return Notification ID in "teavituskalender" database
//     */
//    public long addEvent2(final long notificationId, final AditUser eventOwner, final String eventText,
//            final String eventType, final Calendar eventDate, final String notificationType,
//            final long relatedDocumentId, final UserService userService) {
//        long eventId = 0;
//
//        try {
//
//            logger.debug("Setting messageFactory for addEvent call.");
//            System.setProperty("javax.xml.soap.MessageFactory", "weblogic.xml.saaj.MessageFactoryImpl");
//
//            LisaSyndmusRequest request = new LisaSyndmusRequest();
//            request.setNahtavOmanikule(false);
//            request.setKirjeldus(eventText);
//            request.setTahtsus(NOTIFICATION_PRIORITY_MEDIUM);
//            request.setSyndmuseTyyp(NOTIFICATION_TYPE_TEAVITUSKALENDER_LIIGIS);
//            request.setLiik(eventType);
//
//            LisaSyndmusRequestLugejad lugejad = new LisaSyndmusRequestLugejad();
//
//            ArrayList<LisaSyndmusRequestKasutaja> kasutajad = new ArrayList<LisaSyndmusRequestKasutaja>();
//
//            LisaSyndmusRequestKasutaja kasutaja = new LisaSyndmusRequestKasutaja();
//
//            String userCode = eventOwner.getUserCode();
//            kasutaja.setKood(Util.getPersonalIdCodeWithoutCountryPrefix(userCode));
//
//            if (UserService.USERTYPE_PERSON.equalsIgnoreCase(eventOwner.getUsertype().getShortName())) {
//                kasutaja.setKasutajaTyyp(NOTIFICATION_USER_PERSON);
//            } else {
//                kasutaja.setKasutajaTyyp(NOTIFICATION_USER_INSTITUTION);
//            }
//
//            kasutajad.add(kasutaja);
//            lugejad.setKasutajad(kasutajad);
//            lugejad.setType("ns5:kasutaja + " + '[' + kasutajad.size() + ']');
//            lugejad.setXsiType("SOAP-ENC:Array");
//
//            request.setLugejad(lugejad);
//
//            request.setAlgus(Util.dateToXMLDate(eventDate.getTime()));
//            request.setLopp(Util.dateToXMLDate(eventDate.getTime()));
//
//            StringResult strr = new StringResult();
//            getMarshaller().marshal(request, strr);
//
//            try {
//
//                System.setProperty("weblogic.webservice.i18n.charset", "utf-8");
//
//                String xteeSecurityServer = getConfiguration().getXteeSecurityServer();
//
//                String xml = "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns4=\"http://x-tee.riik.ee/xsd/xtee.xsd\" xmlns:ns5=\"http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender\" env:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><env:Header><ns4:asutus xsi:type=\"xsd:string\">70006317</ns4:asutus><ns4:andmekogu xsi:type=\"xsd:string\">teavituskalender</ns4:andmekogu><ns4:isikukood xsi:type=\"xsd:string\">00000000000</ns4:isikukood><ns4:id xsi:type=\"xsd:string\">12be832471b70006317-841676050</ns4:id><ns4:nimi xsi:type=\"xsd:string\">teavituskalender.lisaSyndmus.v1</ns4:nimi><ns4:toimik></ns4:toimik></env:Header><env:Body>"
//                        + strr + "</env:Body></env:Envelope>";
//                // String xml =
//                // "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns4=\"http://x-tee.riik.ee/xsd/xtee.xsd\" xmlns:ns5=\"http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender\" env:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><env:Header><ns4:asutus xsi:type=\"xsd:string\">70006317</ns4:asutus><ns4:andmekogu xsi:type=\"xsd:string\">teavituskalender</ns4:andmekogu><ns4:isikukood xsi:type=\"xsd:string\">00000000000</ns4:isikukood><ns4:id xsi:type=\"xsd:string\">12be832471b70006317-841676050</ns4:id><ns4:nimi xsi:type=\"xsd:string\">teavituskalender.lisaSyndmus.v1</ns4:nimi><ns4:toimik></ns4:toimik></env:Header><env:Body><ns5:lisaSyndmus><ns5:keha><ns5:nahtavOmanikule>false</ns5:nahtavOmanikule><ns5:kirjeldus>Document Avaldus Jõgeva Linnavalitsusele was viewed by user EE70006317.</ns5:kirjeldus><ns5:tahtsus>keskmine</ns5:tahtsus><ns5:syndmuseTyyp>liigis</ns5:syndmuseTyyp><ns5:liik>Minu dokumentide teavitus</ns5:liik><ns5:lugejad xmlns:ns1=\"http://www.w3.org/2001/XMLSchema-instance\" ns1:type=\"SOAP-ENC:Array\" xmlns:ns2=\"http://schemas.xmlsoap.org/soap/encoding/\" ns2:arrayType=\"ns5:kasutaja[1]\"><ns5:kasutaja><ns5:kood>70006317</ns5:kood><ns5:kasutajaTyyp>asutus</ns5:kasutajaTyyp></ns5:kasutaja></ns5:lugejad><ns5:algus>2010-10-25T10:44:59.000+03:00</ns5:algus><ns5:lopp>2010-10-25T10:44:59.000+03:00</ns5:lopp></ns5:keha></ns5:lisaSyndmus></env:Body></env:Envelope>";
//                // String xml =
//                // "<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:SOAP-ENC="http://schemas.xmlsoap.org/soap/encoding/" xmlns:ns4="http://x-tee.riik.ee/xsd/xtee.xsd" xmlns:ns5="http://producers.teavituskalender.xtee.riik.ee/producer/teavituskalender" env:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><env:Header><ns4:asutus xsi:type="xsd:string">70006317</ns4:asutus><ns4:andmekogu xsi:type="xsd:string">teavituskalender</ns4:andmekogu><ns4:isikukood xsi:type="xsd:string">00000000000</ns4:isikukood><ns4:id xsi:type="xsd:string">12be832471b70006317-841676050</ns4:id><ns4:nimi xsi:type="xsd:string">teavituskalender.lisaSyndmus.v1</ns4:nimi><ns4:toimik></ns4:toimik></env:Header><env:Body><ns5:lisaSyndmus><ns5:keha><ns5:nahtavOmanikule>false</ns5:nahtavOmanikule><ns5:kirjeldus>Document Avaldus Jõgeva Linnavalitsusele was viewed by user EE70006317.</ns5:kirjeldus><ns5:tahtsus>keskmine</ns5:tahtsus><ns5:syndmuseTyyp>liigis</ns5:syndmuseTyyp><ns5:liik>Minu dokumentide teavitus</ns5:liik><ns5:lugejad xmlns:ns1="http://www.w3.org/2001/XMLSchema-instance" ns1:type="SOAP-ENC:Array" xmlns:ns2="http://schemas.xmlsoap.org/soap/encoding/" ns2:arrayType="ns5:kasutaja[1]"><ns5:kasutaja><ns5:kood>70006317</ns5:kood><ns5:kasutajaTyyp>asutus</ns5:kasutajaTyyp></ns5:kasutaja></ns5:lugejad><ns5:algus>2010-10-25T10:44:59.000+03:00</ns5:algus><ns5:lopp>2010-10-25T10:44:59.000+03:00</ns5:lopp></ns5:keha></ns5:lisaSyndmus></env:Body></env:Envelope>";
//
//                WebServiceTemplate webServiceTemplate2 = new WebServiceTemplate();
//                StringSource source = new StringSource(xml);
//                StreamResult result = new StreamResult();
//                StringWriter strWriter = new StringWriter();
//                result.setWriter(strWriter);
//
//                webServiceTemplate2.sendSourceAndReceiveToResult(xteeSecurityServer, source, result);
//
//                logger.debug("Notifications response message: " + strWriter.getBuffer().toString());
//
//                throw new Exception("Notifications response message: " + strWriter.getBuffer().toString());
//
//                /*
//                 * TransformerFactory transFactory =
//                 * TransformerFactory.newInstance(); Transformer transformer =
//                 * transFactory.newTransformer(); StringWriter buffer = new
//                 * StringWriter();
//                 * transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION
//                 * , "yes"); transformer.transform(new
//                 * DOMSource(result.getNode()), new StreamResult(buffer));
//                 * String str = buffer.toString();
//                 * 
//                 * LOG.debug("Notifications response message: " + buffer);
//                 */
//
//                /*
//                 * LisaSyndmusResponse response = (LisaSyndmusResponse)
//                 * customXTeeConsumer.sendRequest(request);
//                 * 
//                 * if(response != null) { if (response != null) { Integer
//                 * resultEventId = response.getSyndmusId();
//                 * LOG.debug("LisaSyndmus result event ID: " + ((resultEventId
//                 * == null) ? "NULL" : resultEventId.toString()));
//                 * 
//                 * if (response.getTulemus() != null) { Integer resultCode =
//                 * response.getTulemus().getTulemuseKood(); String resultMessage
//                 * = response.getTulemus().getTulemuseTekst();
//                 * LOG.debug("LisaSyndmus result code: " + ((resultCode == null)
//                 * ? "NULL" : resultCode.toString()));
//                 * LOG.debug("LisaSyndmus result message: " + resultMessage);
//                 * 
//                 * if ((resultCode != null) && (resultCode.intValue() ==
//                 * RESULT_OK)) { eventId = resultEventId.longValue();LOG.debug(
//                 * "Successfully added notification to 'teavituskalender' database. Related document ID: "
//                 * + String.valueOf(relatedDocumentId)); } } else {LOG.error(
//                 * "Error adding notification to 'teavituskalender' database. Response's 'tulemus' part is NULL. Related document ID: "
//                 * + String.valueOf(relatedDocumentId)); } } else {LOG.error(
//                 * "Error adding notification to 'teavituskalender' database. Response's 'LisaSyndmusResponse' part is NULL. Related document ID: "
//                 * + String.valueOf(relatedDocumentId)); } } else { throw new
//                 * AditInternalException(
//                 * "The 'getDocument' request was not successful: response could not be unmarshalled: unmarshalling returned null."
//                 * ); }
//                 */
//
//            } catch (Exception e) {
//                logger.error("Error while sending notifications: ", e);
//            }
//
//        } catch (Exception ex) {
//            logger.error("Error adding notification to 'teavituskalender' database. Related document ID: "
//                    + String.valueOf(relatedDocumentId), ex);
//        }
//
//        // Save notification to database
//        if (eventId > 0) {
//            try {
//                userService.addNotification(notificationId, relatedDocumentId, notificationType, eventOwner
//                        .getUserCode(), eventDate.getTime(), eventText, eventId, Calendar.getInstance().getTime());
//            } catch (Exception ex) {
//                logger.error("Failed saving successfully sent notification.", ex);
//            }
//        } else {
//            try {
//                userService.addNotification(notificationId, relatedDocumentId, notificationType, eventOwner
//                        .getUserCode(), eventDate.getTime(), eventText, null, null);
//            } catch (Exception ex) {
//                logger.error("Failed saving unsent notification.", ex);
//            }
//        }
//
//        return eventId;
//    }

    /**
     * Queries "teavituskalender" database to find out whether or not it
     * contains specified user in its user database.
     * 
     * @param userCode
     *            User (person or organization) code.
     * @return Whether or not 'teavituskalender' database contains a user
     *         matching the given code.
     */
    public static boolean userExists(String userCode) {
        boolean result = false;
        try {
            OtsiKasutajadDocument doc = OtsiKasutajadDocument.Factory.newInstance();
            OtsiKasutajad req = doc.addNewOtsiKasutajad();
            OtsiKasutajadDocument.OtsiKasutajad.Keha keha = req.addNewKeha();

            Kasutajad users = keha.addNewKasutajad();
            ee.riik.xtee.teavituskalender.producers.producer.teavituskalender.OtsiKasutajadDocument.OtsiKasutajad.Keha.Kasutajad.Kasutaja user = users
                    .addNewKasutaja();
            user.setKood(userCode);

            ClassPathXmlApplicationContext ctx = null;
            try {
                ctx = startContext();
                StandardXTeeConsumer xteeService = (StandardXTeeConsumer) ctx.getBean("xteeConsumer");
                SimpleXTeeServiceConfiguration conf = (SimpleXTeeServiceConfiguration) xteeService
                        .getServiceConfiguration();
                conf.setDatabase("teavituskalender");
                conf.setMethod("otsiKasutajad");
                conf.setVersion("v1");
                OtsiKasutajadResponseDocument ret = (OtsiKasutajadResponseDocument) xteeService.sendRequest(doc, conf);

                if (ret != null) {
                    if (ret.getOtsiKasutajadResponse() != null) {
                        if (ret.getOtsiKasutajadResponse().getKeha() != null) {
                            BigInteger foundUserCount = ret.getOtsiKasutajadResponse().getKeha().getLeitudArv();
                            logger.debug("OtsiKasutajad request found "
                                    + ((foundUserCount == null) ? "NULL" : foundUserCount.toString())
                                    + " mathing users.");

                            if (ret.getOtsiKasutajadResponse().getKeha().getTulemus() != null) {
                                BigInteger resultCode = ret.getOtsiKasutajadResponse().getKeha().getTulemus()
                                        .getTulemuseKood();
                                String resultMessage = ret.getOtsiKasutajadResponse().getKeha().getTulemus()
                                        .getTulemuseTekst();
                                logger.debug("OtsiKasutajad result code: "
                                        + ((resultCode == null) ? "NULL" : resultCode.toString()));
                                logger.debug("OtsiKasutajad result message: " + resultMessage);

                                if ((resultCode != null) && (resultCode.intValue() == RESULT_OK)) {
                                    result = (foundUserCount.intValue() > 0);
                                    logger.debug("Successfully found out whether or not user " + userCode
                                            + " exists in 'teavituskalender' database "
                                            + (result ? "(exists)." : "(does not exist)."));
                                }
                            } else {
                                logger
                                        .error("Error finding user in 'teavituskalender' database. Response's 'tulemus' part is NULL. Related user code: "
                                                + userCode);
                            }
                        } else {
                            logger
                                    .error("Error finding user in 'teavituskalender' database. Response's 'keha' part is NULL. Related user code: "
                                            + userCode);
                        }
                    } else {
                        logger
                                .error("Error finding user in 'teavituskalender' database. Response's 'LisaSyndmusResponse' part is NULL. Related user code: "
                                        + userCode);
                    }
                } else {
                    logger
                            .error("Error finding user in 'teavituskalender' database. Response document is NULL. Related user code: "
                                    + userCode);
                }
            } finally {
                if (ctx != null) {
                    ctx.close();
                }
            }
        } catch (Exception ex) {
            logger.error("Error finding user in 'teavituskalender' database. Related user code: " + userCode, ex);
        }
        return result;
    }

    /**
     * Helper method to start application context.
     * 
     * @return Application context as {@link ClassPathXmlApplicationContext}
     *         object
     */
    public static ClassPathXmlApplicationContext startContext() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("xtee.xml");
        ctx.start();
        return ctx;
    }

    /**
     * Get marshaller
     * 
     * @return marshaller
     */
    public Marshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Set marshaller
     * 
     * @param marshaller marshaller
     */
    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Get unmarshaller
     * 
     * @return unmarshaller
     */
    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    /**
     * Set unmarshaller
     * 
     * @param unmarshaller unmarshaller
     */
    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    /**
     * Get configuration
     * 
     * @return configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Set configuration
     * 
     * @param configuration configuration
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
