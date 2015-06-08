package hccb;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import model.T1001Request;
import model.T1001Response;
import model.TxnHead;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhanrui on 2015/3/31.
 */

public class HccbClient {
    public void startTestT1001() throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        camelContext.setTracing(true);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                XStream xStream = new XStream(new DomDriver("GBK"));
                xStream.processAnnotations(T1001Request.class);
                XStreamDataFormat dataFormat = new XStreamDataFormat(xStream);
                dataFormat.setEncoding("GBK");

                XStream xStreamResp = new XStream(new DomDriver("GBK"));
                xStreamResp.processAnnotations(T1001Response.class);
                XStreamDataFormat dataFormatResp = new XStreamDataFormat(xStreamResp);
                dataFormatResp.setEncoding("GBK");
                from("direct:t1001request")
                        .marshal(dataFormat)
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                            }
                        })
                        .log("===t1001request BODY: ${body}")
                        .to("http4://" + "127.0.0.1" + ":" + "8090" + "/haier/batchsubs")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                            }
                        })
                        .unmarshal(dataFormatResp);
            }
        });


        //====
        T1001Request request = new T1001Request();
        request.setHead(new TxnHead().bizid("0101").txncode("1001"));
        request.getBody().setQrytype("01");  //01-正常代扣
        request.getBody().setPagenum("");    //为空时默认为第一页
        request.getBody().setPagesize("");  //每次请求记录数  可为空 为空时默认为1000笔记录


        camelContext.start();
        ProducerTemplate template = camelContext.createProducerTemplate();
        template.start();
        Exchange exchange = template.request("direct:t1001request", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {

            }
        });

        T1001Response response = (T1001Response)exchange.getOut().getBody();
        System.out.println("===" + response);
        System.out.println("===记录条数:" + response.getBody().getRecords().size());
    }

    public static void main(String... argv) throws Exception {
        HccbClient test = new HccbClient();
        test.startTestT1001();
    }

}
