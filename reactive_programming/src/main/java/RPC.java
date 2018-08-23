import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.ReactorRabbitMq;
import reactor.rabbitmq.RpcClient;
import reactor.rabbitmq.Sender;

import java.util.UUID;
import java.util.function.Supplier;

public class RPC {

    public static void main(String[] args){
        String queue = "rpc.server.queue";
        Supplier<String> correlationIdSupplier = () -> UUID.randomUUID().toString();
        System.out.println(correlationIdSupplier.get());
        Sender sender = ReactorRabbitMq.createSender();
        RpcClient rpcClient = sender.rpcClient(
                "", queue, correlationIdSupplier
        );
        Mono<Delivery> reply = rpcClient.rpc(Mono.just(
                new RpcClient.RpcRequest("hello".getBytes())
        ));
        rpcClient.close();
    }
}
