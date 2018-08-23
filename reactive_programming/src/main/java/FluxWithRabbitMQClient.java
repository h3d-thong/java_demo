import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.ReactorRabbitMq;
import reactor.rabbitmq.RpcClient;
import reactor.rabbitmq.Sender;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

public class FluxWithRabbitMQClient {

    public static void main(String[] args){
        String queue = "rpc.server.queue";
        Supplier<String> correlationIdSupplier = () -> UUID.randomUUID().toString();
        Sender sender = ReactorRabbitMq.createSender();
        RpcClient rpcClient = sender.rpcClient(
                "", queue, correlationIdSupplier
        );

        Mono<Delivery> respons = rpcClient.rpc(Mono.just(
                new RpcClient.RpcRequest("12".getBytes())
        ));
        Mono<AMQP.Queue.DeclareOk> queueDeclaration = sender.declareQueue(QueueSpecification.queue(queue));
        queueDeclaration.thenMany(respons).subscribe(m->System.out.println("process done : "+new String(m.getBody())));
        rpcClient.close();
    }
}
