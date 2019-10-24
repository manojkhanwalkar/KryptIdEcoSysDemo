package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EncryptedMessage {

    String message;

    @JsonProperty
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

     SignatureContainer container;


    @JsonProperty
    public SignatureContainer getContainer() {
        return container;
    }

    public void setContainer(SignatureContainer container) {
        this.container = container;
    }
    
}
