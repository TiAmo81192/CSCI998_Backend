package org.fog.mas;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.distribution.DeterministicDistribution;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;

import jade.core.Agent;

public class FogAgent extends Agent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public void setup() {
        System.out.println("Create Fog and Fog Agent...");

        try {
            FogDevice proxy = createFogDevice("proxy-server-"+getAID().getLocalName(), 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);

            proxy.setParentId(2);
            System.out.println("The parent Id of proxy-server is "+MainClass.fogDevices.get(0).getId());
            proxy.setUplinkLatency(200);
            MainClass.fogDevices.add(proxy);
            System.out.println("The proxy-id is "+proxy.getId());

            for (int i=0; i<2; i++) {
                addGw(getAID().getLocalName()+"-"+i, proxy.getId());
            }
            //System.out.println("The create fog device is "+ cloud.getName()+" and the agent Id is " + getAID().getLocalName());
        }catch(Exception e) {
            System.out.println("Fog devices create error.");
        }
    }

    private static FogDevice addGw(String id, int parentId) {

        FogDevice gw = createFogDevice("gateway-"+id, 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
        MainClass.fogDevices.add(gw);
        gw.setParentId(parentId);
        gw.setUplinkLatency(4);
        return gw;
    }


    private static FogDevice createFogDevice(String nodeName, long mips, int ram, long upBw, long downBw,
                                             int level, double ratePerMips, double busyPower, double idlePower) {
        List<Pe> peList = new ArrayList<Pe>();

        peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); //create Processing Element and added these into a list
        //store the Pe id and MIPS rating
        int hostId = FogUtils.generateEntityId();//set host Id
        long storage = 500000; //set host storage
        int bw = 10000; // set host bandwidth

        //set the power host
        PowerHost host = new PowerHost(hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower)
        );

        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86";//system architecture
        String os = "Linux";//operating system
        String vmm = "Xen";
        double time_zone = 10.0;//time zone this resource located
        double cost = 3.0; //the cost of using processing in this resource
        double costPerMem = 0.05; //the cost of using memory in this resource
        double costPerStorage = 0.003; //the cost of using storage in this resource
        double costPerBw = 0.01;//the cost of using bandwidth in this resource

        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        //(storage area network) devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        //set the fog device characteristics

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(nodeName, characteristics, new AppModuleAllocationPolicy(hostList),
                    storageList, 10, upBw,downBw, 0, ratePerMips);

        }catch(Exception e) {
            e.printStackTrace();
        }
        fogdevice.setLevel(level);
        return fogdevice;
    }

}
