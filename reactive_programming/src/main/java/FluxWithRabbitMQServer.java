import com.rabbitmq.client.*;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;
import reactor.rabbitmq.RpcClient;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class FluxWithRabbitMQServer {

    private static final String QUEUE = "data-queue";
    public static void main(String[] args){
        String queue = "rpcqueue2";
        Receiver receiver = ReactorRabbitMq.createReceiver();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection      = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.queueDeclare(queue, false, false, false, null);
            channel.queuePurge(queue);

            channel.basicQos(1);

            System.out.println("Awaiting RPC requests");

            Connection finalConnection = connection;
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    String response = "";
                    try {
                        String message = new String(body,"UTF-8");

                        System.out.println(" get rpc (" + message + ")");
                        response += "respon to rpc";
                    }
                    catch (RuntimeException e){
                        System.out.println(" [.] " + e.toString());
                    }
                    finally {
                        Sender sender = ReactorRabbitMq.createSender();
                        Mono<AMQP.Queue.DeclareOk> queueDeclaration = sender.declareQueue(QueueSpecification.queue(QUEUE));
                        Flux<Delivery> messages = receiver.consumeAutoAck(QUEUE);

                        queueDeclaration.thenMany(messages).subscribe(new FluxProcessor<Delivery, Object>() {
                            @Override
                            public void subscribe(CoreSubscriber<? super Object> actual) {
                                System.out.println("subscribe");
                            }

                            @Override
                            public void onSubscribe(Subscription s) {
                                System.out.println("on subscribe");
                            }

                            @Override
                            public void onNext(Delivery delivery) {
                                System.out.println("process "+new String(delivery.getBody())+" is running");
                            }

                            @Override
                            public void onError(Throwable t) {
                                System.out.println("on error");
                            }

                            @Override
                            public void onComplete() {
                                String result = "Message to client : done!";
                                try {
                                    channel.basicPublish( "", properties.getReplyTo(), replyProps, result.getBytes("UTF-8"));
                                    channel.basicAck(envelope.getDeliveryTag(), false);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });




                        // RabbitMq consumer worker thread notifies the RPC server owner thread
                        synchronized(this) {
                            this.notify();
                        }
                    }
                }
            };

            channel.basicConsume(queue, false, consumer);
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized(consumer) {
                    try {
                        consumer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null)
                try {
                    connection.close();
                } catch (IOException _ignore) {}
        }
    }
}
