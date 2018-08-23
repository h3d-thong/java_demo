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

public class FluxWithRabbitMQClient {

    private static final String QUEUE = "data-queue";
    public static void main(String[] args){
        String queue = "rpcqueue2";
        Supplier<String> correlationIdSupplier = () -> UUID.randomUUID().toString();

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        SenderOptions senderOptions =  new SenderOptions()
                .connectionFactory(connectionFactory)
                .resourceCreationScheduler(Schedulers.elastic());

        Sender sender = ReactorRabbitMq.createSender(senderOptions);
        RpcClient rpcClient = sender.rpcClient(
                "", queue, correlationIdSupplier
        );

        Mono<Delivery> respons = rpcClient.rpc(Mono.just(
                new RpcClient.RpcRequest("12".getBytes())
        ));

        Mono<AMQP.Queue.DeclareOk> queueDeclaration = sender.declareQueue(QueueSpecification.queue(queue));
        queueDeclaration.thenMany(respons).subscribe(m->System.out.println(""+new String(m.getBody())));

        //Create data stream
        Flux<OutboundMessageResult> dataStream = sender.sendWithPublishConfirms(Flux.range(1, 20)
                .filter(m -> m%2 == 0)//filter
                .parallel()//parallel process
                .runOn(Schedulers.parallel())
                .doOnNext(i->{
                    //System.out.println("Message  " + i + " run on thread "+Thread.currentThread().getId());
                    sleep();

                })
                .map(i -> new OutboundMessage("", QUEUE, ("Message " + i).getBytes()))); // Map to message

        //Send data stream to RabbitMQ
        sender.declareQueue(QueueSpecification.queue(QUEUE))
                .thenMany(dataStream)
                .doOnError(e -> System.out.println("Send failed"+ e))
                .subscribe(m->{
                    if (m!= null){
                        System.out.println("Sent successfully message : "+new String(m.getOutboundMessage().getBody()));
                    }
                });

    }
    public static void sleep(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
