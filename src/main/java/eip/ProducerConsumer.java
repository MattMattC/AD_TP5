package eip;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.log4j.BasicConfigurator;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import java.util.Scanner;

/**
 * Created by lock- on 25/03/2016.
 */
public class ProducerConsumer {

    public static void main(String[] args) throws Exception{
        //Config
        BasicConfigurator.configure();

        //Contexte Camel par déaut
        CamelContext context = new DefaultCamelContext();

        // Crée une route content le conso
        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // On définit un conso 'consumer-1'
                // qui va écrire le message
                from("direct:consumer-1").to("log:affiche-1-log");
                from("direct:consumer-2").to("file:messages");
                from("direct:consumer-all").choice()
                        .when(header("destinataire").isEqualTo("écrire"))
                        .to("direct:consumer-2")
                        .otherwise().to("direct:consumer-1");
                from("direct:Citymanager").setHeader(Exchange.HTTP_METHOD,constant("GET")).to("http://127.0.0.1:8084/all")
                        .log("reponse received : ${body}");
                from("direct:Citymanager-city").setHeader(Exchange.HTTP_METHOD,constant("GET"))
                        .recipientList(simple("http://127.0.0.1:8084/"+"${body}"))
                        .log("reponse received : ${body}");


                from("direct:Citymanager-geo").setHeader(Exchange.HTTP_METHOD,constant("GET"))
                        .recipientList(simple("http://api.geonames.org/search?q=${body}&username=m1gil"))
                        .log("reponse received : ${body}");

                from("direct:start").to("jgroups:m1gil");
                from("jgroups:m1gil").log("reponse received : ${body}");
//                    from("direct:Citymanager-city").
            }
        };

        // On ajoute la route au contexte
        routeBuilder.addRoutesToCamelContext(context);

        // On démare le contexte pour activer les routes
        context.start();

        // On crée un producteur
        ProducerTemplate pt = context.createProducerTemplate();

        // qui envoie un message au consommateur 'consumer-1'
        System.out.println("Envoyer un message au conso : ");
        Scanner sc = new Scanner(System.in);
        String msg = null;

        do{
            msg = sc.nextLine();
            if(!msg.equals("exit")){
                if(msg.charAt(0)=='w'){
                    pt.sendBodyAndHeader("direct:consumer-all", msg, "destinataire", "écrire");
                }else{

//                    pt.sendBody("direct:Citymanager", null);
//                    pt.sendBody("direct:Citymanager-city", msg);


                  //  pt.sendBody("direct:Citymanager-geo", msg);




                    pt.sendBody("direct:start", msg);
//                    pt.sendBody("direct:consumer-all", msg);

                }
            }
        }while(!msg.equals("exit"));


    }
}
