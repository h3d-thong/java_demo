import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Delivery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.*;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

public class RPCClient {

    private static final String QUEUE = "my-queue";
    public static void main(String[] args){
        Supplier<String> correlationIdSupplier = () -> UUID.randomUUID().toString();

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        SenderOptions senderOptions =  new SenderOptions()
                .connectionFactory(connectionFactory)
                .resourceCreationScheduler(Schedulers.elastic());

        Sender sender = ReactorRabbitMq.createSender(senderOptions);
        RpcClient rpcClient = sender.rpcClient(
                "", QUEUE, correlationIdSupplier
        );


        Flux.range(1,10).parallel().runOn(Schedulers.parallel()).doOnNext(i->{
            String reqs = i+"";
            String reqs2 = (i+20)+"";
            //System.out.println("Thread" + Thread.currentThread().getId());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Mono<Delivery> respons = rpcClient.rpc(Flux.just(
                    new RpcClient.RpcRequest(reqs.getBytes()),new RpcClient.RpcRequest(reqs2.getBytes())
            ));
            respons.doOnNext(res -> System.out.println("received :"+new String(res.getBody())+"done!")).subscribe();
        }).subscribe(m-> System.out.println("Sent: message "+m));

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void sleep(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
