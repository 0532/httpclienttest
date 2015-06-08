package mock;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import model.T1001Request;
import model.T1001Response;
import model.TxnHead;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhanrui on 2015/3/31.
 */
public class HccbHttpServer {
    private void start() throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        camelContext.setTracing(true);


        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                final XStream xStream = new XStream(new DomDriver("GBK"));
                xStream.processAnnotations(T1001Response.class);
                XStreamDataFormat dataFormat = new XStreamDataFormat(xStream);
                dataFormat.setEncoding("GBK");


                from("netty4-http:http://0.0.0.0:8090/haier/batchsubs")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                String body = exchange.getIn().getBody(String.class);
                                System.out.println("request body:" + body);

                                //T1001 response
                                T1001Response resp = new T1001Response();
                                resp.setHead(new TxnHead().bizid("0101").txncode("1001"));

                                T1001Response.Record record = new T1001Response.Record();
                                record.setActno("actno001");
                                record.setActname("actname001");
                                resp.getBody().getRecords().add(record);
                                record = new T1001Response.Record();
                                record.setActno("actno002");
                                record.setActname("actname002");
                                resp.getBody().getRecords().add(record);

                                String xml = xStream.toXML(resp);

                                exchange.getOut().setHeader("Content-Type", "text/html; charset=gbk");
                                exchange.getOut().setBody(xml);
                            }
                        })
                .log("===Response:[${body}]");
            }
        });

        camelContext.start();
    }

    public static void main(String... argv) throws Exception {
        HccbHttpServer server = new HccbHttpServer();
        server.start();
    }
}
