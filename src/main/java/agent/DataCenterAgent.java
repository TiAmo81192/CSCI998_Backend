package agent;

import jade.core.Agent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class DataCenterAgent extends Agent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public void setup() {
        System.out.println("Create Data Center and Data Center Agent...");

        try {
            String dataCenterName = "cloud";
            Datacenter cloud = createDatacenter(dataCenterName, 44800, 40000);
            //cloud.setParentId(-1);
            MainClass.datacenters.add(cloud);
            System.out.println("The cloud id is "+cloud.getId());
            //System.out.println("The create fog device is "+ cloud.getName()+" and the agent Id is " + getAID().getLocalName());
        }catch(Exception e) {
            System.out.println("Data Center create error.");
        }
    }


    private static Datacenter createDatacenter(String dataCenterName, long mips, int ram) {
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        //    our machine
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        peList.add(new Pe(0, new PeProvisionerSimple(mips))); //create Processing Element and added these into a list
        //store the Pe id and MIPS rating

        int hostId = 0; //FogUtils.generateEntityId();//set host Id
        long storage = 1000000; //set host storage
        int bw = 10000; // set host bandwidth

        //set the power host
        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerSpaceShared(peList)
                )
        );


        String arch = "x86";//system architecture
        String os = "Linux";//operating system
        String vmm = "Xen";
        double time_zone = 10.0;//time zone this resource located
        double cost = 3.0; //the cost of using processing in this resource
        double costPerMem = 0.05; //the cost of using memory in this resource
        double costPerStorage = 0.001; //the cost of using storage in this resource
        double costPerBw = 0.0;//the cost of using bandwidth in this resource

        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        //set the fog device characteristics

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(dataCenterName, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        }catch(Exception e) {
            e.printStackTrace();
        }

        //datacenter.setLevel(level);
        return datacenter;
    }

}
