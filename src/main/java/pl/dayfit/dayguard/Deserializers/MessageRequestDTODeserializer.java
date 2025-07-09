package pl.dayfit.dayguard.Deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import pl.dayfit.dayguard.DTOs.AttachmentMessageRequestDTO;
import pl.dayfit.dayguard.DTOs.MessageRequestDTO;
import pl.dayfit.dayguard.DTOs.TextMessageRequestDTO;

import java.io.IOException;

public class MessageRequestDTODeserializer extends StdDeserializer<MessageRequestDTO> {

    public MessageRequestDTODeserializer() {
        super(MessageRequestDTO.class);
    }

    @Override
    public MessageRequestDTO deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);

        Class<? extends MessageRequestDTO> targetType;

        if (node.has("message")) {
            targetType = TextMessageRequestDTO.class;
        } else if (node.has("attachments")) {
            targetType = AttachmentMessageRequestDTO.class;
        } else {
            throw new IllegalArgumentException("Invalid message body");
        }

        JsonParser treeParser = node.traverse(codec);
        treeParser.nextToken();

        JsonDeserializer<?> deserializer = deserializationContext.findContextualValueDeserializer(
                deserializationContext.constructType(targetType), null);

        return (MessageRequestDTO) deserializer.deserialize(treeParser, deserializationContext);
    }
}
