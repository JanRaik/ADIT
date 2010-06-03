package ee.adit.ws.endpoint;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ee.webmedia.xtee.XTeeHeader;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "getUserInfo", version = "v1")
@Component
public class GetUserInfoEndpoint extends XteeCustomEndpoint {

	private static Logger LOG = Logger.getLogger(GetUserInfoEndpoint.class);

	@Override
	protected void invokeInternal(Document requestKeha, Element responseKeha, XTeeHeader xteeHeader) throws Exception {
		LOG.debug("GetUserInfoEndpoint invoked.");
		
		// TODO: Kontrollime, kas päringu käivitanud infosüsteem on ADITis registreeritud
		
		// TODO: Kontrollime, kas päringu käivitanud infosüsteem tohib andmeid n�ha
		
		// TODO: Implement me!
	}
}