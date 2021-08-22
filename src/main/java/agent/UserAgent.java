package agent;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

public class UserAgent extends Agent {

    public void setup() {
        addBehaviour(new CyclicBehaviour() {

            @Override
            public void action() {
                // TODO 自动生成的方法存根
                ACLMessage acl1=receive();
                if(acl1!=null) {
                    System.out.println("receiving");
                    doWait(2000);
                    System.out.println(getLocalName()+" receive a message");
                    System.out.println("the message is: "+acl1.getContent());
                    System.out.println("the message is: "+acl1.getSender().getLocalName());
                    doDelete();
                }
            }});
    }
}