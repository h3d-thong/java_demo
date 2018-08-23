import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.RpcServer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.*;
import reactor.rabbitmq.Receiver;

import java.util.UUID;
import java.util.function.Supplier;

/**
 *
 */
public class FluxWithRabbitMQDemo {

    private static final String QUEUE = "tttt";

    private final reactor.rabbitmq.Sender sender;
    private final Receiver receiver;
    private String requestQueueName = "rpc_queue";

    public FluxWithRabbitMQDemo() {
        this.sender = ReactorRabbitMq.createSender();
        this.receiver = ReactorRabbitMq.createReceiver();
    }

    public void run(int count) {

        //Create Sender (sent to RabbitMQ)
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        SenderOptions senderOptions =  new SenderOptions()
                .connectionFactory(connectionFactory)
                .resourceCreationScheduler(Schedulers.elastic());

        reactor.rabbitmq.Sender sender = ReactorRabbitMq.createSender(senderOptions);


        //Create Receiver (receive from RabbitMQ)
        Mono<AMQP.Queue.DeclareOk> queueDeclaration = sender.declareQueue(QueueSpecification.queue(QUEUE));
        Flux<Delivery> messages = receiver.consumeAutoAck(QUEUE);
        queueDeclaration.thenMany(messages).subscribe(m->
                System.out.println("Get message: "+ new String(m.getBody())));

        //Create data stream
        Flux<OutboundMessageResult> dataStream = sender.sendWithPublishConfirms(Flux.range(1, count)
                .filter(m -> m%2 == 0)//filter
                .parallel()//parallel process
                .runOn(Schedulers.parallel())
                .doOnNext(i->{
                    System.out.println("Message  " + i + " run on thread "+Thread.currentThread().getId());
                    sleep();  // sleep 1 seconds
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

        try {
            Thread.sleep(20000);
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

    public static void main(String[] args) throws Exception {
        int count = 20;
        FluxWithRabbitMQDemo sender = new FluxWithRabbitMQDemo();
        sender.run(count);
    }



}