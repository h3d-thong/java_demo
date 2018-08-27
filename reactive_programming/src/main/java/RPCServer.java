import com.rabbitmq.client.*;
import reactor.rabbitmq.Sender;

import java.io.IOException;


public class RPCServer {

    private static final String QUEUE =  "my-queue";

    private static Connection serverConnection;
    private static  Channel serverChannel;
    private static RpcServer rpcServer;
    Sender sender;

    public static void main(String[] args){
        try {
            requestReply();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void init() throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        serverConnection = connectionFactory.newConnection();
        serverChannel = serverConnection.createChannel();
        serverChannel.queueDeclare(QUEUE, false, false, false, null);
    }

    public static void requestReply() throws Exception {
        init();
        rpcServer = new TestRpcServer(serverChannel, QUEUE);
        new Thread(() -> {
            try {
                rpcServer.mainloop();
            } catch (Exception e) {
                // safe to ignore when loops ends/server is canceled
            }
        }).start();

    }

    private static class TestRpcServer extends RpcServer {

        public TestRpcServer(Channel channel, String queueName) throws IOException {
            super(channel, queueName);
        }

        @Override
        public byte[] handleCall(Delivery request, AMQP.BasicProperties replyProperties) {
            String input = new String(request.getBody());
            System.out.println("get message : "+input);
            return ("*** " + input + " ***").getBytes();
        }
    }
}
