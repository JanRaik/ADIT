package ee.adit.ws.endpoint.document;

import org.springframework.stereotype.Component;

import ee.adit.ws.endpoint.AbstractAditBaseEndpoint;
import ee.webmedia.xtee.annotation.XTeeService;

@XTeeService(name = "confirmSignature", version = "v1")
@Component
public class ConfirmSignatureEndpoint extends AbstractAditBaseEndpoint {

	@Override
	protected Object invokeInternal(Object requestObject) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
